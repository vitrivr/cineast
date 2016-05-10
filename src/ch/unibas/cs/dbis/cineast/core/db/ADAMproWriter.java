package ch.unibas.cs.dbis.cineast.core.db;

import java.util.ArrayList;
import java.util.concurrent.ExecutionException;

import com.google.common.util.concurrent.ListenableFuture;

import ch.unibas.dmi.dbis.adam.http.Grpc.AckMessage;
import ch.unibas.dmi.dbis.adam.http.Grpc.AckMessage.Code;
import ch.unibas.dmi.dbis.adam.http.Grpc.BooleanQueryMessage;
import ch.unibas.dmi.dbis.adam.http.Grpc.BooleanQueryMessage.WhereMessage;
import ch.unibas.dmi.dbis.adam.http.Grpc.InsertMessage;
import ch.unibas.dmi.dbis.adam.http.Grpc.InsertMessage.Builder;
import ch.unibas.dmi.dbis.adam.http.Grpc.InsertMessage.TupleInsertMessage;
import ch.unibas.dmi.dbis.adam.http.Grpc.QueryResponseInfoMessage;
import ch.unibas.dmi.dbis.adam.http.Grpc.QueryResultMessage;
import ch.unibas.dmi.dbis.adam.http.Grpc.SimpleBooleanQueryMessage;

public class ADAMproWriter extends ProtobufTupleGenerator {

	private String entityName;
	private Builder builder = InsertMessage.newBuilder();
	
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
	public boolean idExists(String id) {
		return exists("id", id);
	}

	@Override
	public boolean exists(String key, String value) { //TODO reduce the number of new objects created
		WhereMessage where = WhereMessage.newBuilder().setField(key).setValue(value).build();
		ArrayList<WhereMessage> tmp = new ArrayList<>(1);
		tmp.add(where);
		SimpleBooleanQueryMessage qbqm = SimpleBooleanQueryMessage.newBuilder().setEntity(this.entityName)
				.setBq(BooleanQueryMessage.newBuilder().addAllWhere(tmp)).build();
		ListenableFuture<QueryResponseInfoMessage> f = ADAMproWrapper.getInstance().booleanQuery(qbqm);
		QueryResponseInfoMessage responce;
		try {
			responce = f.get();
		} catch (InterruptedException | ExecutionException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return false;
		}
		
		QueryResultMessage result = responce.getResults(0);
		
		System.out.println(result);
		
		return false;
	}

	@Override
	public synchronized boolean persist(PersistentTuple<TupleInsertMessage> tuple) {
		this.builder.clear();
		this.builder.setEntity(this.entityName);
		ArrayList<TupleInsertMessage> tmp = new ArrayList<>(1);
		tmp.add(tuple.getPersistentRepresentation());
		this.builder.addAllTuples(tmp);
		AckMessage ack = ADAMproWrapper.getInstance().insertOneBlocking(this.builder.build());
		return ack.getCode() == Code.OK;
	}

}
