package org.vitrivr.cineast.core.db;

import com.google.common.util.concurrent.ListenableFuture;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.vitrivr.adam.grpc.AdamGrpc;
import org.vitrivr.adam.grpc.AdamGrpc.*;
import org.vitrivr.adam.grpc.AdamGrpc.AckMessage.Code;
import org.vitrivr.adam.grpc.AdamGrpc.BooleanQueryMessage;
import org.vitrivr.adam.grpc.AdamGrpc.BooleanQueryMessage.WhereMessage;
import org.vitrivr.adam.grpc.AdamGrpc.DataMessage;
import org.vitrivr.adam.grpc.AdamGrpc.DenseVectorMessage;
import org.vitrivr.adam.grpc.AdamGrpc.DistanceMessage;
import org.vitrivr.adam.grpc.AdamGrpc.DistanceMessage.DistanceType;
import org.vitrivr.adam.grpc.AdamGrpc.FeatureVectorMessage;
import org.vitrivr.adam.grpc.AdamGrpc.FromMessage;
import org.vitrivr.adam.grpc.AdamGrpc.NearestNeighbourQueryMessage;
import org.vitrivr.adam.grpc.AdamGrpc.ProjectionMessage;
import org.vitrivr.adam.grpc.AdamGrpc.QueryMessage;
import org.vitrivr.adam.grpc.AdamGrpc.QueryResultInfoMessage;
import org.vitrivr.adam.grpc.AdamGrpc.QueryResultTupleMessage;
import org.vitrivr.adam.grpc.AdamGrpc.QueryResultsMessage;
import org.vitrivr.cineast.core.config.QueryConfig;
import org.vitrivr.cineast.core.config.QueryConfig.Distance;
import org.vitrivr.cineast.core.data.StringDoublePair;
import org.vitrivr.cineast.core.data.providers.primitive.PrimitiveTypeProvider;
import org.vitrivr.cineast.core.util.LogHelper;

import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

public class ADAMproSelector implements DBSelector {

	private ADAMproWrapper adampro = new ADAMproWrapper();
	
	private FromMessage.Builder fromBuilder = FromMessage.newBuilder();
	private final QueryMessage.Builder qmBuilder = QueryMessage.newBuilder();
	private final NearestNeighbourQueryMessage.Builder nnqmBuilder = NearestNeighbourQueryMessage.newBuilder();
	private final BooleanQueryMessage.Builder bqmBuilder = BooleanQueryMessage.newBuilder();
	private final WhereMessage.Builder wmBuilder = WhereMessage.newBuilder();
	private final DistanceMessage.Builder dmBuilder = DistanceMessage.newBuilder();
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
			Collections.addAll(tmp, whereMessages);
		}
		synchronized (bqmBuilder) {
			bqmBuilder.clear();
			return bqmBuilder.addAllWhere(tmp).build();
		}
	}
	
	private WhereMessage buildWhereMessage(String key, String value){
		synchronized (wmBuilder) {
			wmBuilder.clear();
			return wmBuilder.setAttribute(key).addValues(DataMessage.newBuilder().setStringData(value)).build();
		}
	}
	
	private WhereMessage buildWhereMessage(String key, String... values){
        synchronized (wmBuilder) {
            wmBuilder.clear();
			DataMessage.Builder damBuilder = DataMessage.newBuilder();

			wmBuilder.setAttribute(key);

			for(String value : values){
				wmBuilder.addValues(damBuilder.setStringData(value).build());
			}

            return wmBuilder.build();
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
			return new ArrayList<>(0);
		}
		
		if(r.getResponsesCount() == 0){
			return new ArrayList<>(0);
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
			return new ArrayList<>(0);
		}
		
		if(result.getResponsesCount() == 0){
			return new ArrayList<>(0);
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
			_return.add(new StringDoublePair(id, msg.getData().get("ap_distance").getFloatData()));
		}
		
		return _return;
	}

	@Override
	public List<Map<String, PrimitiveTypeProvider>> getRows(String fieldName, String... values) {
		if(values == null || values.length == 0){
			LOGGER.error("Cannot query empty value list in ADAMproSelector.getRows()");
			return new ArrayList<>(0);
		}
		
		if(values.length == 1){
			return getRows(fieldName, values[0]);
		}
		
        WhereMessage where = buildWhereMessage(fieldName, values);
        BooleanQueryMessage bqMessage = buildBooleanQueryMessage(where);
        return executeBooleanQuery(bqMessage);
    }
	
	@Override
	public List<Map<String, PrimitiveTypeProvider>> getRows(String fieldName, String value) {
		WhereMessage where = buildWhereMessage(fieldName, value);		
		BooleanQueryMessage bqMessage = buildBooleanQueryMessage(where);
		return executeBooleanQuery(bqMessage);
	}

	/**
	 * SELECT label FROM ...
	 * Be careful with the size of the resulting List :)
	 */
	@Override
	public List<PrimitiveTypeProvider> getAll(String label) {
		List<Map<String, PrimitiveTypeProvider>> resultList = getAll();
		return resultList.stream().map(row -> row.get(label)).collect(Collectors.toList());
	}

	/**
	 * TODO This is currently an ugly hack where we abuse the preview-function with LIMIT = COUNT() using the getProperties ADAMpro-method.
	 * Once ADAMpro supports SELECT * FROM $ENTITY, this method should be rewritten
	 */
	@Override
	public List<Map<String, PrimitiveTypeProvider>> getAll() {
		EntityPropertiesMessage propertiesMessage = this.adampro.getProperties(EntityNameMessage.newBuilder().setEntity(fromBuilder.getEntity()).build());
		int count;
		try{
			count = Integer.parseInt(propertiesMessage.getProperties().get("count"));
		}catch(Exception e){
			count = 1000000;	//You should not get more than 1M tuples into the system anyway. Again, this method is temporary
								//This comment was written in October '16.
		}
		return preview(count);
	}

	@Override
	public boolean existsEntity(String eName) {
		return this.adampro.existsEntity(eName);
	}

	@Override
	public List<Map<String, PrimitiveTypeProvider>> preview(int k) {
        PreviewMessage msg = PreviewMessage.newBuilder().setEntity(this.fromBuilder.getEntity()).setN(k).build();
		ListenableFuture<QueryResultsMessage> f = this.adampro.previewEntity(msg);
		QueryResultsMessage result;
		try {
			result = f.get();
		} catch (InterruptedException | ExecutionException e) {
			LOGGER.error(LogHelper.getStackTrace(e));
			return new ArrayList<>(0);
		}

		if(result.getResponsesCount() == 0){
			return new ArrayList<>(0);
		}

		QueryResultInfoMessage response = result.getResponses(0);  //only head (end-result) is important

		List<QueryResultTupleMessage> resultList = response.getResultsList();
		return resultsToMap(resultList);
	}

	/**
	 * @param resultList can be empty
	 * @return an ArrayList of length one if the resultList is empty, else the  transformed QueryResultTupleMessage
	 */
	private List<Map<String, PrimitiveTypeProvider>> resultsToMap(List<QueryResultTupleMessage> resultList) {
		if(resultList.isEmpty()){
			return new ArrayList<>(0);
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

	/**
	 * Executes a QueryMessage and returns the resulting tuples
	 * @return an empty ArrayList if an error happens. Else just the list of rows
	 */
	private List<Map<String, PrimitiveTypeProvider>> executeQuery(QueryMessage qm){
		ListenableFuture<QueryResultsMessage> f = this.adampro.standardQuery(qm);
		QueryResultsMessage result;
		try {
			result = f.get();
		} catch (InterruptedException | ExecutionException e) {
			LOGGER.error(LogHelper.getStackTrace(e));
			return new ArrayList<>(0);
		}

		if(result.getResponsesCount() == 0){
			return new ArrayList<>(0);
		}

		QueryResultInfoMessage response = result.getResponses(0);  //only head (end-result) is important

		List<QueryResultTupleMessage> resultList = response.getResultsList();
		return resultsToMap(resultList);
	}

	private List<Map<String, PrimitiveTypeProvider>> executeBooleanQuery(BooleanQueryMessage bqm) {
		QueryMessage qbqm = buildQueryMessage(hints, bqm, null, null);
		return executeQuery(qbqm);
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
			return new ArrayList<>(0);
		}
		
		if(result.getResponsesCount() == 0){
			return new ArrayList<>(0);
		}
		
		QueryResultInfoMessage response = result.getResponses(0);  //only head (end-result) is important
		
		ArrayList<Map<String, PrimitiveTypeProvider>> _return = new ArrayList<>(k);

		AckMessage ack = response.getAck();
		if(ack.getCode() != Code.OK){
			LOGGER.error("error in getNearestNeighbourRows ({}) : {}", ack.getCode(), ack.getMessage());
			return _return;
		}
		return resultsToMap(response.getResultsList());
	}

}
