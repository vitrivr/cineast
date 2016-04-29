package ch.unibas.cs.dbis.cineast.core.features.abstracts;

import ch.unibas.cs.dbis.cineast.core.data.ReadableFloatVector;
import ch.unibas.cs.dbis.cineast.core.db.DBSelector;
import ch.unibas.cs.dbis.cineast.core.db.PersistencyWriter;
import ch.unibas.cs.dbis.cineast.core.db.PersistentTuple;
import ch.unibas.cs.dbis.cineast.core.features.extractor.Extractor;
import ch.unibas.cs.dbis.cineast.core.features.retriever.Retriever;

public abstract class AbstractFeatureModule implements Extractor, Retriever {

//	private static Logger LOGGER = LogManager.getLogger();
	
	protected PersistencyWriter<?> phandler;
	protected DBSelector selector;
	protected final float maxDist;
	protected final String colName, tableName;

	protected AbstractFeatureModule(String tableName, String colName, float maxDist){
		this.colName = colName;
		this.tableName = tableName;
		this.maxDist = maxDist;
	}
	
	@Override
	public void init(PersistencyWriter<?> phandler) {
		this.phandler = phandler;
		this.phandler.open(this.tableName);
	}
	
	@Override
	public void init(DBSelector selector) {
		this.selector = selector;
	}

	float[] arrayCache = null; //avoiding the creation of new arrays on every call
	@SuppressWarnings({ "rawtypes", "unchecked" })
	protected void persist(String shotId, ReadableFloatVector fv) {
		PersistentTuple tuple = this.phandler.generateTuple(shotId, arrayCache = fv.toArray(arrayCache));
		this.phandler.persist(tuple);
	}


	@Override
	public void finish() {
		if (this.phandler != null) {
			this.phandler.close();
		}
		if (this.selector != null) {
			this.selector.close();
		}
	}

}
