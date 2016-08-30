package org.vitrivr.cineast.core.db;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ExecutionException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.common.util.concurrent.ListenableFuture;

import org.vitrivr.adam.grpc.AdamGrpc;
import org.vitrivr.adam.grpc.AdamGrpc.AckMessage;
import org.vitrivr.adam.grpc.AdamGrpc.AckMessage.Code;
import org.vitrivr.adam.grpc.AdamGrpc.BooleanQueryMessage;
import org.vitrivr.adam.grpc.AdamGrpc.BooleanQueryMessage.WhereMessage;
import org.vitrivr.adam.grpc.AdamGrpc.DataMessage;
import org.vitrivr.adam.grpc.AdamGrpc.DenseVectorMessage;
import org.vitrivr.adam.grpc.AdamGrpc.DistanceMessage;
import org.vitrivr.adam.grpc.AdamGrpc.DistanceMessage.DistanceType;
import org.vitrivr.cineast.core.config.QueryConfig;
import org.vitrivr.cineast.core.config.QueryConfig.Distance;
import org.vitrivr.cineast.core.data.StringDoublePair;
import org.vitrivr.cineast.core.data.providers.primitive.PrimitiveTypeProvider;
import org.vitrivr.cineast.core.util.LogHelper;
import org.vitrivr.adam.grpc.AdamGrpc.FeatureVectorMessage;
import org.vitrivr.adam.grpc.AdamGrpc.FromMessage;
import org.vitrivr.adam.grpc.AdamGrpc.NearestNeighbourQueryMessage;
import org.vitrivr.adam.grpc.AdamGrpc.ProjectionMessage;
import org.vitrivr.adam.grpc.AdamGrpc.QueryMessage;
import org.vitrivr.adam.grpc.AdamGrpc.QueryResultInfoMessage;
import org.vitrivr.adam.grpc.AdamGrpc.QueryResultTupleMessage;
import org.vitrivr.adam.grpc.AdamGrpc.QueryResultsMessage;

public class ADAMproSelector implements DBSelector {

	private ADAMproWrapper adampro = new ADAMproWrapper();
	
	private FromMessage.Builder fromBuilder = FromMessage.newBuilder();
	private QueryMessage.Builder qmBuilder = QueryMessage.newBuilder();
	private NearestNeighbourQueryMessage.Builder nnqmBuilder = NearestNeighbourQueryMessage.newBuilder();
	private BooleanQueryMessage.Builder bqmBuilder = BooleanQueryMessage.newBuilder();
	private WhereMessage.Builder wmBuilder = WhereMessage.newBuilder();
	private DistanceMessage.Builder dmBuilder = DistanceMessage.newBuilder();
	private static final Logger LOGGER = LogManager.getLogger();

	private static ArrayList<String> hints = new ArrayList<>(1);
	private static ProjectionMessage projectionMessage;
	
	private static final DistanceMessage chisquared, correlation, cosine, hamming, jaccard, kullbackleibler, chebyshev, euclidean, squaredeuclidean, manhattan, spannorm;
	
	static{
		
		hints.add("exact");
		
		projectionMessage = AdamGrpc.ProjectionMessage.newBuilder().setAttributes(
				AdamGrpc.ProjectionMessage.AttributeNameMessage.newBuilder().addAttribute("ap_distance").addAttribute("id")).build();
		
		DistanceMessage.Builder dmBuilder = DistanceMessage.newBuilder();
		
		chisquared = dmBuilder.clear().setDistancetype(DistanceType.chisquared).build();
		correlation = dmBuilder.clear().setDistancetype(DistanceType.correlation).build();
		cosine = dmBuilder.clear().setDistancetype(DistanceType.cosine).build();
		hamming = dmBuilder.clear().setDistancetype(DistanceType.hamming).build();
		jaccard = dmBuilder.clear().setDistancetype(DistanceType.jaccard).build();
		kullbackleibler = dmBuilder.clear().setDistancetype(DistanceType.kullbackleibler).build();
		chebyshev = dmBuilder.clear().setDistancetype(DistanceType.chebyshev).build();
		euclidean = dmBuilder.clear().setDistancetype(DistanceType.euclidean).build();
		squaredeuclidean = dmBuilder.clear().setDistancetype(DistanceType.squaredeuclidean).build();
		manhattan = dmBuilder.clear().setDistancetype(DistanceType.manhattan).build();
		spannorm = dmBuilder.clear().setDistancetype(DistanceType.spannorm).build();
		
	}
	
	@Override
	public boolean open(String name) {
		this.fromBuilder.setEntity(name);
		return true;
	}

	@Override
	public boolean close() {
		this.adampro.close();
		return false;
	}

	private QueryMessage buildQueryMessage(ArrayList<String> hints, BooleanQueryMessage bqMessage, ProjectionMessage pMessage, NearestNeighbourQueryMessage nnqMessage){
		synchronized (qmBuilder) {
			qmBuilder.clear();
			qmBuilder.setFrom(fromBuilder);
			if(hints != null && !hints.isEmpty()){
				qmBuilder.addAllHints(hints);
			}
			if(bqMessage != null){
				qmBuilder.setBq(bqMessage);
			}
			if(pMessage != null){
				qmBuilder.setProjection(pMessage);
			}
			if(nnqMessage != null){
				qmBuilder.setNnq(nnqMessage);
			}
			
			return qmBuilder.build();
		}
	}
	
	private BooleanQueryMessage buildBooleanQueryMessage(WhereMessage where, WhereMessage...whereMessages){
		ArrayList<WhereMessage> tmp = new ArrayList<>(1 + (whereMessages == null ? 0 : whereMessages.length));
		tmp.add(where);
		if(whereMessages != null){
			for(WhereMessage w : whereMessages){
				tmp.add(w);
			}
		}
		synchronized (bqmBuilder) {
			bqmBuilder.clear();
			return bqmBuilder.addAllWhere(tmp).build();
		}
	}
	
	private WhereMessage buildWhereMessage(String key, String value){
		synchronized (wmBuilder) {
			wmBuilder.clear();
			return wmBuilder.setAttribute(key).setValue(value).build();
		}
	}
	
	private NearestNeighbourQueryMessage buildNearestNeighbourQueryMessage(String column, FeatureVectorMessage fvm, int k, QueryConfig qc){
		synchronized (nnqmBuilder) {
			this.nnqmBuilder.clear();
			nnqmBuilder.setAttribute(column).setQuery(fvm).setK(k);
			nnqmBuilder.setDistance(buildDistanceMessage(qc));
			if(qc != null){
				Optional<float[]> weights = qc.getDistanceWeights();
				if(weights.isPresent()){
					nnqmBuilder.setWeights(DataMessageConverter.convertFeatureVectorMessage(weights.get()));
				}
			}
			return nnqmBuilder.build();
		}
	}
	
	private DistanceMessage buildDistanceMessage(QueryConfig qc){
		if(qc == null){
			return manhattan;
		}
		Optional<Distance> distance = qc.getDistance();
		if(!distance.isPresent()){
			return manhattan;
		}
		switch(distance.get()){
		case chebyshev:
			return chebyshev;
		case chisquared:
			return chisquared;
		case correlation:
			return correlation;
		case cosine:
			return cosine;
		case euclidean:
			return euclidean;
		case hamming:
			return hamming;
		case jaccard:
			return jaccard;
		case kullbackleibler:
			return kullbackleibler;
		case manhattan:
			return manhattan;
		case minkowski:{
			
			float norm = qc.getNorm().orElse(1f);
			
			if(Math.abs(norm - 1f) < 1e6f){
				return manhattan;
			}
			
			if(Math.abs(norm - 2f) < 1e6f){
				return euclidean;
			}
			
			HashMap<String, String> tmp = new HashMap<>();
			tmp.put("norm", Float.toString(norm));
			
			synchronized (dmBuilder) {
				return dmBuilder.clear().setDistancetype(DistanceType.minkowski).putAllOptions(tmp).build();
			}
			
			}
		case spannorm:
			return spannorm;
		case squaredeuclidean:
			return squaredeuclidean;
		default:
			return manhattan;		
		}
	}
	
	public List<float[]> getFeatureVectors(String fieldName, String value, String vectorName){
		QueryMessage qbqm = buildQueryMessage(hints, buildBooleanQueryMessage(buildWhereMessage(fieldName, value)), null, null);
				
		ListenableFuture<QueryResultsMessage> f = this.adampro.booleanQuery(qbqm);
		ArrayList<float[]> _return = new ArrayList<>();
		QueryResultsMessage r;
		try {
			r = f.get();
		} catch (InterruptedException | ExecutionException e) {
			LOGGER.error(LogHelper.getStackTrace(e));
			return new ArrayList<>(1);
		}
		
		if(r.getResponsesCount() == 0){
			return new ArrayList<>(1);
		}
		
		QueryResultInfoMessage response = r.getResponses(0);  //only head (end-result) is important
		
		AckMessage ack = response.getAck();
		if(ack.getCode() != Code.OK){
			LOGGER.error("error in getFeatureVectors ({}) : {}", ack.getCode(), ack.getMessage());
			return _return;
		}
		
		for(QueryResultTupleMessage result : response.getResultsList()){
			
			Map<String, DataMessage> data = result.getData();
			
			if(!data.containsKey(vectorName)){
				continue;
			}
			
			DataMessage dm = data.get(vectorName);
			
			if(dm.getDatatypeCase() != DataMessage.DatatypeCase.FEATUREDATA){
				continue;
			}
			
			FeatureVectorMessage featureData = dm.getFeatureData();
			
			
			if(featureData.getFeatureCase() != FeatureVectorMessage.FeatureCase.DENSEVECTOR){
				continue; //TODO add correct handling for sparse and int vectors
			}
			
			DenseVectorMessage dense = featureData.getDenseVector(); 
			
			
			List<Float> list = dense.getVectorList();
			if(list.isEmpty()){
				continue;
			}
			
			float[] vector = new float[list.size()];
			int i = 0;
			for(float x : list){
				vector[i++] = x;
			}
			
			_return.add(vector);
			
		}
		
		return _return;
		
	}
	
	@Override
	public List<StringDoublePair> getNearestNeighbours(int k, float[] vector, String column, QueryConfig config) {
		NearestNeighbourQueryMessage nnqMessage = buildNearestNeighbourQueryMessage(column, DataMessageConverter.convertFeatureVectorMessage(vector), k, config);
		QueryMessage sqMessage = buildQueryMessage(hints, null, projectionMessage, nnqMessage);
		ListenableFuture<QueryResultsMessage> future = this.adampro.standardQuery(sqMessage);
		
		QueryResultsMessage result;
		try {
			result = future.get();
		} catch (InterruptedException | ExecutionException e) {
			LOGGER.error(LogHelper.getStackTrace(e));
			return new ArrayList<>(1);
		}
		
		if(result.getResponsesCount() == 0){
			return new ArrayList<>(1);
		}
		
		QueryResultInfoMessage response = result.getResponses(0);  //only head (end-result) is important
		
		ArrayList<StringDoublePair> _return = new ArrayList<>(k);

		AckMessage ack = response.getAck();
		if(ack.getCode() != Code.OK){
			LOGGER.error("error in getNearestNeighbours ({}) : {}", ack.getCode(), ack.getMessage());
			return _return;
		}
		
		for(QueryResultTupleMessage msg : response.getResultsList()){
			String id = msg.getData().get("id").getStringData();
			if(id == null){
				continue;
			}
			_return.add(new StringDoublePair(id, msg.getData().get("adamprodistance").getFloatData()));
		}
		
		return _return;
	}

	@Override
	public List<Map<String, PrimitiveTypeProvider>> getRows(String fieldName, String value) {
		WhereMessage where = buildWhereMessage(fieldName, value);		
		BooleanQueryMessage bqMessage = buildBooleanQueryMessage(where);
		QueryMessage qbqm = buildQueryMessage(hints, bqMessage, null, null);
		ListenableFuture<QueryResultsMessage> f = this.adampro.booleanQuery(qbqm);
		QueryResultsMessage result;
		try {
			result = f.get();
		} catch (InterruptedException | ExecutionException e) {
			LOGGER.error(LogHelper.getStackTrace(e));
			return new ArrayList<>(1);
		}
		
		if(result.getResponsesCount() == 0){
			return new ArrayList<>(1);
		}
		
		QueryResultInfoMessage response = result.getResponses(0);  //only head (end-result) is important
		
		List<QueryResultTupleMessage> resultList = response.getResultsList();
		if(resultList.isEmpty()){
			return new ArrayList<>(1);
		}
		ArrayList<Map<String, PrimitiveTypeProvider>> _return = new ArrayList<>(resultList.size());
		for(QueryResultTupleMessage resultMessage : resultList){
			Map<String, DataMessage> data = resultMessage.getData();
			Set<String> keys = data.keySet();
			HashMap<String, PrimitiveTypeProvider> map = new HashMap<>();
			for(String key : keys){
				map.put(key, DataMessageConverter.convert(data.get(key)));
			}
			_return.add(map);
		}
		
		return _return;
	}

	@Override
	protected void finalize() throws Throwable {
		this.close();
		super.finalize();
	}

	@Override
	public List<Map<String, PrimitiveTypeProvider>> getNearestNeighbourRows(int k, float[] vector, String column, QueryConfig config) {
		NearestNeighbourQueryMessage nnqMessage = buildNearestNeighbourQueryMessage(column, DataMessageConverter.convertFeatureVectorMessage(vector), k, config);

		QueryMessage sqMessage = buildQueryMessage(hints, null, null, nnqMessage);
		
		ListenableFuture<QueryResultsMessage> future = this.adampro.standardQuery(sqMessage);

		QueryResultsMessage result;
		try {
			result = future.get();
		} catch (InterruptedException | ExecutionException e) {
			LOGGER.error(LogHelper.getStackTrace(e));
			return new ArrayList<>(1);
		}
		
		if(result.getResponsesCount() == 0){
			return new ArrayList<>(1);
		}
		
		QueryResultInfoMessage response = result.getResponses(0);  //only head (end-result) is important
		
		ArrayList<Map<String, PrimitiveTypeProvider>> _return = new ArrayList<>(k);

		AckMessage ack = response.getAck();
		if(ack.getCode() != Code.OK){
			LOGGER.error("error in getNearestNeighbourRows ({}) : {}", ack.getCode(), ack.getMessage());
			return _return;
		}
		
		for(QueryResultTupleMessage resultMessage : response.getResultsList()){
			Map<String, DataMessage> data = resultMessage.getData();
			Set<String> keys = data.keySet();
			HashMap<String, PrimitiveTypeProvider> map = new HashMap<>();
			for(String key : keys){
				map.put(key, DataMessageConverter.convert(data.get(key)));
			}
			_return.add(map);
		}
		
		return _return;
	}

}
