package ch.unibas.cs.dbis.cineast.core.features.abstracts;

import ch.unibas.cs.dbis.cineast.core.data.FeatureString;
import ch.unibas.cs.dbis.cineast.core.db.PersistencyWriter;
import ch.unibas.cs.dbis.cineast.core.db.PersistentTuple;
import ch.unibas.cs.dbis.cineast.core.features.extractor.Extractor;

public abstract class SubDivMotionHistogram extends MotionHistogramCalculator implements Extractor {

protected PersistencyWriter phandler;
	
	protected SubDivMotionHistogram(String tableName, String colName, double maxDist){
		super(tableName, colName, (float)maxDist);
	}

	@Override
	public void init(PersistencyWriter<?> phandler) {
		this.phandler = phandler;
		this.phandler.open(this.tableName);

	}
	
	protected void addToDB(long shotId, FeatureString fs1, FeatureString fs2) {
		PersistentTuple tuple = this.phandler.makeTuple(shotId, fs1, fs2);
		this.phandler.write(tuple);
	}
	
	@Override
	public void finish() {
		if(this.phandler != null){
			this.phandler.close();
		}
		super.finish();
	}
}
