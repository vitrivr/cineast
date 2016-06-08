package ch.unibas.cs.dbis.cineast.core.db;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.common.util.concurrent.ListenableFuture;

import ch.unibas.cs.dbis.cineast.core.config.QueryConfig;
import ch.unibas.cs.dbis.cineast.core.data.FloatArrayIterable;
import ch.unibas.cs.dbis.cineast.core.data.StringDoublePair;
import ch.unibas.dmi.dbis.adam.http.Grpc;
import ch.unibas.dmi.dbis.adam.http.Grpc.AckMessage;
import ch.unibas.dmi.dbis.adam.http.Grpc.AckMessage.Code;
import ch.unibas.dmi.dbis.adam.http.Grpc.BooleanQueryMessage;
import ch.unibas.dmi.dbis.adam.http.Grpc.BooleanQueryMessage.WhereMessage;
import ch.unibas.dmi.dbis.adam.http.Grpc.DataMessage;
import ch.unibas.dmi.dbis.adam.http.Grpc.DenseVectorMessage;
import ch.unibas.dmi.dbis.adam.http.Grpc.DistanceMessage;
import ch.unibas.dmi.dbis.adam.http.Grpc.DistanceMessage.DistanceType;
import ch.unibas.dmi.dbis.adam.http.Grpc.FeatureVectorMessage;
import ch.unibas.dmi.dbis.adam.http.Grpc.FromMessage;
import ch.unibas.dmi.dbis.adam.http.Grpc.NearestNeighbourQueryMessage;
import ch.unibas.dmi.dbis.adam.http.Grpc.QueryMessage;
import ch.unibas.dmi.dbis.adam.http.Grpc.QueryResultInfoMessage;
import ch.unibas.dmi.dbis.adam.http.Grpc.QueryResultTupleMessage;
import ch.unibas.dmi.dbis.adam.http.Grpc.QueryResultsMessage;

public class ADAMproSelector implements DBSelector {

	private ADAMproWrapper adampro = new ADAMproWrapper();
	
	private FromMessage.Builder fromBuilder = FromMessage.newBuilder();
	private QueryMessage.Builder sqmBuilder = QueryMessage.newBuilder();
	private NearestNeighbourQueryMessage.Builder nnqmBuilder = NearestNeighbourQueryMessage.newBuilder();
	private static final Logger LOGGER = LogManager.getLogger();

	private static DistanceMessage minkowski_1;
	
	static{
		
		HashMap<String, String> tmp = new HashMap<>();
		tmp.put("norm", "1");
		
		minkowski_1 = DistanceMessage.newBuilder().setDistancetype(DistanceType.minkowski).putAllOptions(tmp).build();
		
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

	public List<Map<String, String>> getFeatureVectors(String fieldName, String value){
		WhereMessage where = WhereMessage.newBuilder().setField(fieldName).setValue(value).build();
		ArrayList<WhereMessage> tmp = new ArrayList<>(1);
		tmp.add(where);
		QueryMessage qbqm = QueryMessage.newBuilder().setFrom(fromBuilder.build())
				.setBq(BooleanQueryMessage.newBuilder().addAllWhere(tmp)).build();
		ListenableFuture<QueryResultsMessage> f = this.adampro.booleanQuery(qbqm);
		QueryResultInfoMessage responses;
		ArrayList<Map<String, String>> _return = new ArrayList<>();
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
			Map<String, String> metadata = new HashMap<String, String>();

			for (Map.Entry<String, DataMessage> entry : result.getData().entrySet()) {
				metadata.put(entry.getKey(), ""); //TODO: adjust data to use
			}

			_return.add(metadata);
		}
		
		return _return;
		
	}
	
	@Override
	public List<StringDoublePair> getNearestNeighbours(int k, float[] vector, String column, QueryConfig config) {
		this.sqmBuilder.clear();
		this.nnqmBuilder.clear();
		
		ArrayList<String> hints = new ArrayList<>(1);
		hints.add("exact");
		
		
		FeatureVectorMessage fvqm = FeatureVectorMessage.newBuilder().setDenseVector(DenseVectorMessage.newBuilder().addAllVector(new FloatArrayIterable(vector))).build();
		
		NearestNeighbourQueryMessage nnqMessage = nnqmBuilder.setColumn(column).setQuery(fvqm).setK(k).setDistance(minkowski_1).build();
		QueryMessage sqMessage = sqmBuilder.setProjection(Grpc.ProjectionMessage.newBuilder().setField(Grpc.ProjectionMessage.FieldnameMessage.newBuilder().addField("adamprodistance").addField("id"))).setFrom(fromBuilder.build()).setNnq(nnqMessage).addAllHints(hints).build();
		
		LOGGER.debug(sqMessage);
		
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
	protected void finalize() throws Throwable {
		this.close();
		super.finalize();
	}

}
