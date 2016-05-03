package ch.unibas.cs.dbis.cineast.core.db;

import java.util.ArrayList;

import ch.unibas.dmi.dbis.adam.http.Grpc.AckMessage;
import ch.unibas.dmi.dbis.adam.http.Grpc.AckMessage.Code;
import ch.unibas.dmi.dbis.adam.http.Grpc.InsertMessage;
import ch.unibas.dmi.dbis.adam.http.Grpc.InsertMessage.Builder;
import ch.unibas.dmi.dbis.adam.http.Grpc.InsertMessage.TupleInsertMessage;

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
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean exists(String key, String value) {
		// TODO Auto-generated method stub
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
