package ch.unibas.cs.dbis.cineast.core.db;

public interface PersistencyWriter<T extends PersistentTuple<?>> {

	boolean open(String name);
	
	boolean check(String condition);
	
	T makeTuple(Object...objects);
	
	void write(T tuple);
	
	boolean close();
	
}
