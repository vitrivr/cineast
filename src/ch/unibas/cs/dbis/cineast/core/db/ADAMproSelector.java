package ch.unibas.cs.dbis.cineast.core.db;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutionException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.common.util.concurrent.ListenableFuture;

import ch.unibas.cs.dbis.cineast.core.config.QueryConfig;
import ch.unibas.cs.dbis.cineast.core.data.FloatArrayIterable;
import ch.unibas.cs.dbis.cineast.core.data.StringDoublePair;
import ch.unibas.cs.dbis.cineast.core.data.providers.primitive.PrimitiveTypeProvider;
import ch.unibas.dmi.dbis.adam.http.Adam;
import ch.unibas.dmi.dbis.adam.http.Adam.AckMessage;
import ch.unibas.dmi.dbis.adam.http.Adam.AckMessage.Code;
import ch.unibas.dmi.dbis.adam.http.Adam.BooleanQueryMessage;
import ch.unibas.dmi.dbis.adam.http.Adam.BooleanQueryMessage.WhereMessage;
import ch.unibas.dmi.dbis.adam.http.Adam.DataMessage;
import ch.unibas.dmi.dbis.adam.http.Adam.DenseVectorMessage;
import ch.unibas.dmi.dbis.adam.http.Adam.DistanceMessage;
import ch.unibas.dmi.dbis.adam.http.Adam.DistanceMessage.DistanceType;
import ch.unibas.dmi.dbis.adam.http.Adam.FeatureVectorMessage;
import ch.unibas.dmi.dbis.adam.http.Adam.FromMessage;
import ch.unibas.dmi.dbis.adam.http.Adam.NearestNeighbourQueryMessage;
import ch.unibas.dmi.dbis.adam.http.Adam.ProjectionMessage;
import ch.unibas.dmi.dbis.adam.http.Adam.QueryMessage;
import ch.unibas.dmi.dbis.adam.http.Adam.QueryResultInfoMessage;
import ch.unibas.dmi.dbis.adam.http.Adam.QueryResultTupleMessage;
import ch.unibas.dmi.dbis.adam.http.Adam.QueryResultsMessage;

public class ADAMproSelector implements DBSelector {

	private ADAMproWrapper adampro = new ADAMproWrapper();
	
	private FromMessage.Builder fromBuilder = FromMessage.newBuilder();
	private FromMessage fromMessage;
	private QueryMessage.Builder sqmBuilder = QueryMessage.newBuilder();
	private NearestNeighbourQueryMessage.Builder nnqmBuilder = NearestNeighbourQueryMessage.newBuilder();
	private BooleanQueryMessage.Builder bqmBuilder = BooleanQueryMessage.newBuilder();
	private static final Logger LOGGER = LogManager.getLogger();

	private static DistanceMessage minkowski_1;
	private static ArrayList<String> hints = new ArrayList<>(1);
	private static ProjectionMessage projectionMessage;
	
	static{
		
		HashMap<String, String> tmp = new HashMap<>();
		tmp.put("norm", "1");
		
		minkowski_1 = DistanceMessage.newBuilder().setDistancetype(DistanceType.minkowski).putAllOptions(tmp).build();
		
		hints.add("exact");
		
		projectionMessage = Adam.ProjectionMessage.newBuilder().setField(
				Adam.ProjectionMessage.FieldnameMessage.newBuilder().addField("adamprodistance").addField("id")).build();
		
	}
	
	@Override
	public boolean open(String name) {
		this.fromBuilder.setEntity(name);
		this.fromMessage = this.fromBuilder.build();
		return true;
	}

	@Override
	public boolean close() {
		this.adampro.close();
		return false;
	}

	public List<float[]> getFeatureVectors(String fieldName, String value, String vectorName){
		WhereMessage where = WhereMessage.newBuilder().setField(fieldName).setValue(value).build();
		ArrayList<WhereMessage> tmp = new ArrayList<>(1);
		tmp.add(where);
		QueryMessage qbqm = QueryMessage.newBuilder().setFrom(fromBuilder.build())
				.setBq(BooleanQueryMessage.newBuilder().addAllWhere(tmp)).build();
		ListenableFuture<QueryResultsMessage> f = this.adampro.booleanQuery(qbqm);
		QueryResultInfoMessage responses;
		ArrayList<float[]> _return = new ArrayList<>();
		try {
			responses = f.get().getResponses(0); //only head (end-result) is important
		} catch (InterruptedException | ExecutionException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return _return;
		}
		
		AckMessage ack = responses.getAck();
		if(ack.getCode() != Code.OK){
			LOGGER.error("error in getFeatureVectors ({}) : {}", ack.getCode(), ack.getMessage());
			return _return;
		}
		
		for(QueryResultTupleMessage result : responses.getResultsList()){
			
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
		FeatureVectorMessage fvqm = FeatureVectorMessage.newBuilder().setDenseVector(DenseVectorMessage.newBuilder().addAllVector(new FloatArrayIterable(vector))).build();
		
		NearestNeighbourQueryMessage nnqMessage;
		synchronized (nnqmBuilder) {
			this.nnqmBuilder.clear();
			nnqMessage = nnqmBuilder.setColumn(column).setQuery(fvqm).setK(k).setDistance(minkowski_1).build();
		}
		QueryMessage sqMessage;
		synchronized (sqmBuilder) {
			this.sqmBuilder.clear();
			sqMessage = sqmBuilder.setProjection(projectionMessage)
					.setFrom(fromMessage).setNnq(nnqMessage).addAllHints(hints).build();
		}
		
		ListenableFuture<QueryResultsMessage> future = this.adampro.standardQuery(sqMessage);

		QueryResultInfoMessage response;
		try {
			response = future.get().getResponses(0);  //only head (end-result) is important
		} catch (InterruptedException | ExecutionException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return new ArrayList<>(1);
		}
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
		WhereMessage where = WhereMessage.newBuilder().setField(fieldName).setValue(value).build();
		ArrayList<WhereMessage> tmp = new ArrayList<>(1);
		tmp.add(where);
		
		BooleanQueryMessage bqMessage;
		synchronized (bqmBuilder) {
			bqmBuilder.clear();
			bqMessage = bqmBuilder.addAllWhere(tmp).build();
		}
		
		QueryMessage qbqm;
		synchronized (sqmBuilder) {
			this.sqmBuilder.clear();
			qbqm = this.sqmBuilder.setFrom(fromMessage).setBq(bqMessage).build();
		}
		ListenableFuture<QueryResultsMessage> f = this.adampro.booleanQuery(qbqm);
		QueryResultInfoMessage responce;
		
		
		try {
			responce = f.get().getResponses(0);
		} catch (InterruptedException | ExecutionException e) {
			e.printStackTrace();
			return new ArrayList<>(1);
			
		}
		
		List<QueryResultTupleMessage> resultList = responce.getResultsList();
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

}
