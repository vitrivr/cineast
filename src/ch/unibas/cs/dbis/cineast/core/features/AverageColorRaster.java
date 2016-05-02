package ch.unibas.cs.dbis.cineast.core.features;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import ch.unibas.cs.dbis.cineast.core.color.ColorConverter;
import ch.unibas.cs.dbis.cineast.core.color.FuzzyColorHistogramQuantizer;
import ch.unibas.cs.dbis.cineast.core.color.FuzzyColorHistogramQuantizer.Color;
import ch.unibas.cs.dbis.cineast.core.color.ReadableLabContainer;
import ch.unibas.cs.dbis.cineast.core.config.QueryConfig;
import ch.unibas.cs.dbis.cineast.core.data.FloatVector;
import ch.unibas.cs.dbis.cineast.core.data.FloatVectorImpl;
import ch.unibas.cs.dbis.cineast.core.data.MultiImage;
import ch.unibas.cs.dbis.cineast.core.data.ReadableFloatVector;
import ch.unibas.cs.dbis.cineast.core.data.SegmentContainer;
import ch.unibas.cs.dbis.cineast.core.data.StringDoublePair;
import ch.unibas.cs.dbis.cineast.core.db.PersistentTuple;
import ch.unibas.cs.dbis.cineast.core.features.abstracts.AbstractFeatureModule;
import ch.unibas.cs.dbis.cineast.core.util.ColorUtils;
import ch.unibas.cs.dbis.cineast.core.util.GridPartitioner;

public class AverageColorRaster extends AbstractFeatureModule {

	private static final Logger LOGGER = LogManager.getLogger();
	
	public AverageColorRaster(){
		super("features.AverageColorRaster", "raster", 1);
	}
	
	protected static int get(Color c){
		switch(c){
		case Black:		return 0;
		case Blue:		return 1;
		case Brown:		return 2;
		case Cyan:		return 3;
		case Green:		return 4;
		case Grey:		return 5;
		case Magenta:	return 6;
		case Navy:		return 7;
		case Orange:	return 8;
		case Pink:		return 9;
		case Red:		return 10;
		case Teal:		return 11;
		case Violet:	return 12;
		case White:		return 13;
		case Yellow:	return 14;
		default:		return -1;
		}
	}
	
	protected static Color get(int i){
		switch(i){
		case 0:			return Color.Black;
		case 1:			return Color.Blue;
		case 2:			return Color.Brown;
		case 3:			return Color.Cyan;
		case 4:			return Color.Green;
		case 5:			return Color.Grey;
		case 6:			return Color.Magenta;
		case 7:			return Color.Navy;
		case 8:			return Color.Orange;
		case 9:			return Color.Pink;
		case 10:		return Color.Red;
		case 11:		return Color.Teal;
		case 12:		return Color.Violet;
		case 13:		return Color.White;
		case 14:		return Color.Yellow;
		}
		return Color.Black;
	}
	
	protected static Color get(float f){
		return get(Math.round(f));
	}
	

	@Override
	public void processShot(SegmentContainer shot) {
		LOGGER.entry();
		if (!phandler.idExists(shot.getId())) {
			MultiImage avg = shot.getAvgImg();
			int[] colors = avg.getColors();
			ArrayList<Integer> ints = new ArrayList<>(colors.length);
			for(int i : colors){
				ints.add(i);
			}
			ArrayList<LinkedList<Integer>> partitions = GridPartitioner.partition(ints, avg.getWidth(), avg.getHeight(), 8, 8);
			
			float[] raster = new float[64];
			float[] hist = new float[15];
			
			for(int i = 0; i < 64; ++i){
				LinkedList<Integer> list = partitions.get(i);
				int col = ColorUtils.getAvg(list);
				ReadableLabContainer lab = ColorConverter.cachedRGBtoLab(col);
				raster[i] = get(FuzzyColorHistogramQuantizer.quantize(lab));
				hist[(int)raster[i]]++;
			}
			
			persist(shot.getId(), new FloatVectorImpl(hist), new FloatVectorImpl(raster));
			
		}
		LOGGER.exit();
	}
	
	protected void persist(String shotId, ReadableFloatVector fs1, ReadableFloatVector fs2) {//FIXME currently only one vector is supported
		PersistentTuple tuple = this.phandler.generateTuple(shotId, fs1, fs2);
		this.phandler.persist(tuple);
	}

	
	
//	@Override
//	public List<LongDoublePair> getSimilar(SegmentContainer qc) {
//		FloatVector query = buildQueryVector(qc);
//		
//		int limit = Config.getRetrieverConfig().getMaxResultsPerModule() * 5;
//
//		ResultSet rset = this.selector.select("SELECT * FROM features.AverageColorRaster USING DISTANCE MINKOWSKI(2)(\'" + query.toFeatureString() + "\', hist) ORDER USING DISTANCE LIMIT " + limit);
//		return manageResultSet(rset);
//
//	}
//
//	@Override
//	public List<LongDoublePair> getSimilar(SegmentContainer qc, String resultCacheName) {
//		FloatVector query = buildQueryVector(qc);
//		
//		int limit = Config.getRetrieverConfig().getMaxResultsPerModule() * 5;
//
//		ResultSet rset = this.selector.select(getResultCacheLimitSQL(resultCacheName) + " SELECT * FROM features.AverageColorRaster, c WHERE shotid = c.filter USING DISTANCE MINKOWSKI(2)(\'" + query.toFeatureString() + "\', hist) ORDER USING DISTANCE LIMIT " + limit);
//		return manageResultSet(rset);
//	}

	private FloatVector buildQueryVector(SegmentContainer qc) {
		MultiImage avg = qc.getAvgImg();
		int[] colors = avg.getColors();
		ArrayList<Integer> ints = new ArrayList<>(colors.length);
		for(int i : colors){
			ints.add(i);
		}
		ArrayList<LinkedList<Integer>> partitions = GridPartitioner.partition(ints, avg.getWidth(), avg.getHeight(), 8, 8);
		
		float[] queryraster = new float[64];
		float[] hist = new float[15];
		
		for(int i = 0; i < 64; ++i){
			LinkedList<Integer> list = partitions.get(i);
			int col = ColorUtils.getAvg(list);
			ReadableLabContainer lab = ColorConverter.cachedRGBtoLab(col);
			queryraster[i] = get(FuzzyColorHistogramQuantizer.quantize(lab));
			hist[(int)queryraster[i]]++;
		}

		return new FloatVectorImpl(hist);
	}
	
	protected static double register(float[] query, float[] db){
		double best = 0;
		if(query.length < 64 || db.length < 64){
			return 0;
		}
		for(int xoff = -4; xoff <= 4; ++xoff){
			for(int yoff = -4; yoff <= 4; ++yoff){
				double score = 0;
				for(int x = 0; x < 8; ++x){
					for(int y = 0; y < 8; ++y){
						int x1 = x + xoff, y1 = y + yoff;
						if(x1 >= 0 && x1 < 8 && y1 >= 0 && y1 < 8){
							int idx1 = 8 * x + y, idx2 = 8 * x1 + y1;
							score += score(query[idx1], db[idx2]);
						}
					}
				}
				best = Math.max(best, score);
			}
		}
		return best / 64d;
	}
	
	protected static double score(float f1, float f2){
		float fmin = Math.min(f1, f2), fmax = Math.max(f1, f2);
		Color c1 = get(fmin), c2 = get(fmax);
		if(c1 == c2){
			return 1d;
		}
		switch(c1){
		case Black: if(c2 == Color.Grey){
			return 0.25;
		}
			break;
		case Blue: if(c2 == Color.Navy || c2 == Color.Violet){
			return 0.5;
		}
		if(c2 == Color.Cyan){
			return 0.25;
		}
			break;
		case Brown: if(c2 == Color.Grey){
			return 0.5;
		}
			break;
		case Cyan: if(c2 == Color.White){
			return 0.25;
		}
			break;
		case Green: if(c2 == Color.Teal){
			return 0.5;
		}
			break;
		case Grey: if(c2 == Color.White || c2 == Color.Black){
			return 0.125;
		}
			break;
		case Magenta: if(c2 == Color.Violet || c2 == Color.Pink){
			return 0.5;
		}
			break;
		case Orange: if(c2 == Color.Red || c2 == Color.Yellow){
			return 0.5;
		}
			break;
		case Pink: if(c2 == Color.Red){
			return 0.5;
		}
			break;
		default:
			return 0;

		
		}
		return 0;
	}

//	@Override
//	public List<LongDoublePair> getSimilar(long shotId) {
//		int limit = Config.getRetrieverConfig().getMaxResultsPerModule() * 5;
//		
//		ResultSet rset = this.selector.select("WITH q AS (SELECT hist, raster FROM features.AverageColorRaster WHERE shotid = " + shotId + ") SELECT shotid, AverageColorRaster.raster, q.raster as queryraster FROM features.AverageColorRaster, q USING DISTANCE MINKOWSKI(2)(q.hist, AverageColorRaster.hist) ORDER USING DISTANCE LIMIT " + limit);
//		ArrayList<LongDoublePair> result = new ArrayList<>(limit);
//		if(rset != null){
//			try {
//				while(rset.next()){
//					long id = rset.getLong(2);
//					String rasterFromDB = rset.getString(3);
//					float[] raster = stringToFloatArray(rasterFromDB);
//					
//					rasterFromDB = rset.getString(4);
//					float[] queryraster = stringToFloatArray(rasterFromDB);
//					
//					result.add(new LongDoublePair(id, register(queryraster, raster)));
//				}
//			} catch (SQLException e) {
//				LOGGER.fatal(LogHelper.SQL_MARKER, LogHelper.getStackTrace(e));
//			}
//		}
//		return result;
//	}
	
	protected float[] stringToFloatArray(String feature){
		String[] rasterString = feature.split(",");
		float[] raster = new float[64];
		for(int i = 0; i < rasterString.length; ++i){
			raster[i] = Float.parseFloat(rasterString[i].replace('<', ' ').replace('>', ' ').trim());
		}
		return raster;
	}

	@Override
	public List<StringDoublePair> getSimilar(SegmentContainer sc, QueryConfig qc) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<StringDoublePair> getSimilar(long shotId, QueryConfig qc) {
		// TODO Auto-generated method stub
		return null;
	}

}
