package ch.unibas.cs.dbis.cineast.core.db;

import ch.unibas.dmi.dbis.adam.http.Grpc.InsertMessage.TupleInsertMessage;

public class ADAMproWriter extends ProtobufTupleGenerator {

	@Override
	public boolean open(String name) {
		// TODO Auto-generated method stub
		return false;
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
	public boolean persist(PersistentTuple<TupleInsertMessage> tuple) {
		// TODO Auto-generated method stub
		return false;
	}

}
