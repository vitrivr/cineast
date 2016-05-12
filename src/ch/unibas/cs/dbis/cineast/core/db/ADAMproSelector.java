package ch.unibas.cs.dbis.cineast.core.db;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.ExecutionException;

import com.google.common.util.concurrent.ListenableFuture;

import ch.unibas.cs.dbis.cineast.core.config.QueryConfig;
import ch.unibas.cs.dbis.cineast.core.data.FloatArrayIterable;
import ch.unibas.cs.dbis.cineast.core.data.StringDoublePair;
import ch.unibas.dmi.dbis.adam.http.Grpc.DistanceMessage;
import ch.unibas.dmi.dbis.adam.http.Grpc.DistanceMessage.DistanceType;
import ch.unibas.dmi.dbis.adam.http.Grpc.FeatureVectorMessage;
import ch.unibas.dmi.dbis.adam.http.Grpc.NearestNeighbourQueryMessage;
import ch.unibas.dmi.dbis.adam.http.Grpc.QueryResponseInfoMessage;
import ch.unibas.dmi.dbis.adam.http.Grpc.QueryResultMessage;
import ch.unibas.dmi.dbis.adam.http.Grpc.SimpleQueryMessage;

public class ADAMproSelector implements DBSelector {

	private String entityName;
	private SimpleQueryMessage.Builder sqmBuilder = SimpleQueryMessage.newBuilder();
	private NearestNeighbourQueryMessage.Builder nnqmBuilder = NearestNeighbourQueryMessage.newBuilder();
	
	private static DistanceMessage minkowski_1;
	
	static{
		
		HashMap<String, String> tmp = new HashMap<>();
		tmp.put("norm", "1");
		
		minkowski_1 = DistanceMessage.newBuilder().setDistancetype(DistanceType.minkowski).putAllOptions(tmp).build();
		
	}
	
	@Override
	public boolean open(String name) {
		this.entityName = name;
		return true;
	}

	@Override
	public boolean close() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public List<StringDoublePair> getNearestNeighbours(int k, float[] vector, String column, QueryConfig config) {
		this.sqmBuilder.clear();
		this.nnqmBuilder.clear();
		
		FeatureVectorMessage fvqm = FeatureVectorMessage.newBuilder().addAllVector(new FloatArrayIterable(vector)).build();
		
		NearestNeighbourQueryMessage nnqMessage = nnqmBuilder.setColumn(column).setQuery(fvqm).setK(k).setDistance(minkowski_1).build();
		SimpleQueryMessage sqMessage = sqmBuilder.setEntity(entityName).setNnq(nnqMessage).build();
		
		ListenableFuture<QueryResponseInfoMessage> future = ADAMproWrapper.getInstance().standardQuery(sqMessage);
		
		QueryResponseInfoMessage response;
		try {
			response = future.get();
		} catch (InterruptedException | ExecutionException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		}
		
		ArrayList<StringDoublePair> _return = new ArrayList<>(k);
		
		for(QueryResultMessage msg : response.getResultsList()){
			String id = msg.getMetadata().get("id");
			if(id == null){
				continue;
			}
			_return.add(new StringDoublePair(id, msg.getDistance()));
		}
		
		return _return;
	}

}
