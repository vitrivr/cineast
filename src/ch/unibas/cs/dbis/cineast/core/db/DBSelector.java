package ch.unibas.cs.dbis.cineast.core.db;

public interface DBSelector {

	boolean open(String name);
	
	boolean close();
	
}
