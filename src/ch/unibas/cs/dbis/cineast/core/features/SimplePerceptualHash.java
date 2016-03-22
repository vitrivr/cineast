package ch.unibas.cs.dbis.cineast.core.features;

import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import ch.unibas.cs.dbis.cineast.core.color.RGBContainer;
import ch.unibas.cs.dbis.cineast.core.color.ReadableRGBContainer;
import ch.unibas.cs.dbis.cineast.core.config.Config;
import ch.unibas.cs.dbis.cineast.core.data.FrameContainer;
import ch.unibas.cs.dbis.cineast.core.data.LongDoublePair;
import ch.unibas.cs.dbis.cineast.core.data.MultiImage;
import ch.unibas.cs.dbis.cineast.core.db.PersistentTuple;
import ch.unibas.cs.dbis.cineast.core.features.abstracts.AbstractFeatureModule;
import ch.unibas.cs.dbis.cineast.core.util.ColorUtils;
import ch.unibas.cs.dbis.cineast.core.util.GridPartitioner;

public class SimplePerceptualHash extends AbstractFeatureModule {

	public SimplePerceptualHash() {
		super("features.SimplePerceptualHash", "hash", 1);
	}

	@Override
	public void processShot(FrameContainer shot) {
		if(!phandler.check("SELECT * FROM features.SimplePerceptualHash WHERE shotid = " + shot.getId())){
			addToDB(shot.getId(), hash(shot.getMostRepresentativeFrame().getImage()));
		}
	}

	@Override
	public List<LongDoublePair> getSimilar(FrameContainer qc) {
		int limit = Config.getRetrieverConfig().getMaxResultsPerModule();
		ResultSet rset = this.selector.select("SELECT 0 as dist, shotId FROM features.SimplePerceptualHash, q WHERE SimplePerceptualHash.hash = " + hash(qc.getMostRepresentativeFrame().getImage()) + " LIMIT " + limit);
		return manageResultSet(rset);
	}

	@Override
	public List<LongDoublePair> getSimilar(long shotId) {
		int limit = Config.getRetrieverConfig().getMaxResultsPerModule();
		
		ResultSet rset = this.selector.select("WITH q AS (SELECT hash FROM features.SimplePerceptualHash WHERE shotid = " + shotId + ") SELECT 0 as dist, shotId FROM features.SimplePerceptualHash, q WHERE SimplePerceptualHash.hash = q.hash LIMIT " + limit);
		return manageResultSet(rset);
	}
	
	@Override
	public List<LongDoublePair> getSimilar(FrameContainer qc, String resultCacheName) {
		int limit = Config.getRetrieverConfig().getMaxResultsPerModule();
		ResultSet rset = this.selector.select(getResultCacheLimitSQL(resultCacheName) + " SELECT 0 as dist, shotId FROM features.SimplePerceptualHash, q, c WHERE shotid = c.filter AND SimplePerceptualHash.hash = " + hash(qc.getMostRepresentativeFrame().getImage()) + " LIMIT " + limit);
		return manageResultSet(rset);
	}
	
	@Override
	public List<LongDoublePair> getSimilar(long shotId, String resultCacheName) {
		int limit = Config.getRetrieverConfig().getMaxResultsPerModule();
		
		ResultSet rset = this.selector.select(getResultCacheLimitSQL(resultCacheName) + ", q AS (SELECT hash FROM features.SimplePerceptualHash WHERE shotid = " + shotId + ") SELECT 0 as dist, shotId FROM features.SimplePerceptualHash, q, c WHERE shotid = c.filter AND SimplePerceptualHash.hash = q.hash LIMIT " + limit);
		return manageResultSet(rset);
	}

	private void addToDB(long shotId, long hash) {
		PersistentTuple tuple = phandler.makeTuple(shotId, hash);
		this.phandler.write(tuple);
	}
	
	public static long hash(MultiImage img){
		ArrayList<ReadableRGBContainer> rgbs = new ArrayList<>();
		int[] colors = img.getColors();
		for(int c : colors){
			rgbs.add(new ReadableRGBContainer(c));
		}
		float avg = new ReadableRGBContainer(ColorUtils.getAvg(colors)).getLuminance();
		
		ArrayList<LinkedList<ReadableRGBContainer>> partitions = GridPartitioner.partition(rgbs, img.getWidth(), img.getHeight(), 8, 8);
		
		long hash = 0l;
		
		RGBContainer container = new RGBContainer(0);
		
		for(int i = 0; i < 64; ++i){
			ColorUtils.getAvg(partitions.get(i), container);
			float lum = container.getLuminance();
			
			if(lum > avg){
				hash |= (1l << i);
			}
		}
		return hash;
	}
	
}
