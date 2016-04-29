package ch.unibas.cs.dbis.cineast.core.features.abstracts;

import ch.unibas.cs.dbis.cineast.core.data.ReadableFloatVector;
import ch.unibas.cs.dbis.cineast.core.db.PersistencyWriter;
import ch.unibas.cs.dbis.cineast.core.db.PersistentTuple;
import ch.unibas.cs.dbis.cineast.core.features.extractor.Extractor;

public abstract class SubDivMotionHistogram extends MotionHistogramCalculator implements Extractor {

protected PersistencyWriter phandler;
	
	protected SubDivMotionHistogram(String tableName, String colName, double maxDist){
		super(tableName, (float)maxDist);
	}

	@Override
	public void init(PersistencyWriter<?> phandler) {
		this.phandler = phandler;
		this.phandler.open(this.tableName);

	}
	
	protected void persist(String shotId, ReadableFloatVector fs1, ReadableFloatVector fs2) {
		PersistentTuple tuple = this.phandler.generateTuple(shotId, fs1, fs2); //FIXME currently only one vector is supported
		this.phandler.persist(tuple);
	}
	
	@Override
	public void finish() {
		if(this.phandler != null){
			this.phandler.close();
		}
		super.finish();
	}
}
