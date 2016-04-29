package ch.unibas.cs.dbis.cineast.core.features;

import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import boofcv.io.image.ConvertBufferedImage;
import boofcv.struct.image.GrayU8;
import ch.unibas.cs.dbis.cineast.core.config.QueryConfig;
import ch.unibas.cs.dbis.cineast.core.data.FloatVectorImpl;
import ch.unibas.cs.dbis.cineast.core.data.Frame;
import ch.unibas.cs.dbis.cineast.core.data.LongDoublePair;
import ch.unibas.cs.dbis.cineast.core.data.MultiImage;
import ch.unibas.cs.dbis.cineast.core.data.SegmentContainer;
import ch.unibas.cs.dbis.cineast.core.features.abstracts.AbstractFeatureModule;
import ch.unibas.cs.dbis.cineast.core.util.MathHelper;
/**
 * see Efficient Use of MPEG-7 Edge Histogram Descriptor by Won '02
 * see http://stackoverflow.com/questions/909542/opencv-edge-extraction
 * @author rosluc00
 *
 */
public class EHD extends AbstractFeatureModule {

	private static final Logger LOGGER = LogManager.getLogger();

	public EHD(){
		super("features.EHD", "hist", 16f / 4f);
	}
	
	private static final float[]
			mv = new float[]{1f, -1f, 1f, -1f},
			mh = new float[]{1f, 1f, -1f, -1f},
			m45 = new float[]{MathHelper.SQRT2_f, 0, 0, -MathHelper.SQRT2_f},
			m135 = new float[]{0, MathHelper.SQRT2_f, -MathHelper.SQRT2_f, 0},
			mn = new float[]{2f, -2f, -2f, 2};
			
	

	@Override
	public void processShot(SegmentContainer shot) {
		LOGGER.entry();
		if (!phandler.idExists(shot.getId())) {
			List<Frame> frames = shot.getFrames();
			float[] hist = new float[80];
			for(Frame f : frames){
				MultiImage img = f.getImage();
				hist = process(img, hist);
			}
			float count = frames.size();
			for(int i = 0; i < 80; ++i){
				hist[i] /= count;
			}
			persist(shot.getId(), new FloatVectorImpl(hist));
		}
		LOGGER.exit();
	}

//	@Override
//	public List<LongDoublePair> getSimilar(SegmentContainer qc) {
//		FloatVector query = new FloatVectorImpl(process(qc.getMostRepresentativeFrame().getImage(), new float[80]));
//		int limit = Config.getRetrieverConfig().getMaxResultsPerModule();
//		
//		ResultSet rset = this.selector.select("SELECT * FROM features.EHD USING DISTANCE MINKOWSKI(1)(\'" + query.toFeatureString() + "\', hist) ORDER USING DISTANCE LIMIT " + limit);
//		return manageResultSet(rset);
//	}
//	
//	@Override
//	public List<LongDoublePair> getSimilar(SegmentContainer qc, String resultCacheName) {
//		FloatVector query = new FloatVectorImpl(process(qc.getMostRepresentativeFrame().getImage(), new float[80]));
//		int limit = Config.getRetrieverConfig().getMaxResultsPerModule();
//		
//		ResultSet rset = this.selector.select(getResultCacheLimitSQL(resultCacheName) + " SELECT * FROM features.EHD, c WHERE shotid = c.filter USING DISTANCE MINKOWSKI(1)(\'" + query.toFeatureString() + "\', hist) ORDER USING DISTANCE LIMIT " + limit);
//		return manageResultSet(rset);
//	}

	protected static float[] process(MultiImage img, float[] hist){
		GrayU8 gray = ConvertBufferedImage.convertFrom(img.getBufferedImage(), (GrayU8) null);
		int width = img.getWidth(), height = img.getHeight();
		for(int x = 0; x < 4; ++x){
			for(int y = 0; y < 4; ++y){
				GrayU8 subImage = gray.subimage(width * x / 4, height * y / 4,width * (x + 1) / 4, height * (y + 1) / 4, null);
				int count = 0;
				int[] tmp = new int[5];
				for(int xx = 0; xx < subImage.getWidth() - 1; xx += 2){
					for(int yy = 0; yy < subImage.getHeight() - 1; yy += 2){
						count++;
						int index = edgeType(
								subImage.unsafe_get(xx, yy),
								subImage.unsafe_get(xx + 1, yy),
								subImage.unsafe_get(xx, yy + 1),
								subImage.unsafe_get(xx + 1, yy + 1)
								);
						if(index > -1){
							tmp[index]++;
						}
					}
				}
				int offset = (4 * x + y) * 5;
				for(int i = 0; i < 5; ++i){
					hist[offset + i] += ((float)tmp[i]) / (float)count;
				}				
			}
		}
		return hist;
	}
	
	private static int edgeType(int i1, int i2, int i3, int i4){
		float[] coeffs = new float[]{
				mv[0]	* i1 + mv[1]	* i2 + mv[2]	* i3 + mv[3]	* i4,
				mh[0]	* i1 + mh[1]	* i2 + mh[2]	* i3 + mh[3]	* i4,
				m45[0]	* i1 + m45[1]	* i2 + m45[2]	* i3 + m45[3]	* i4,
				m135[0] * i1 + m135[1]	* i2 + m135[2]	* i3 + m135[3]	* i4,
				mn[0]	* i1 + mn[1]	* i2 + mn[2]	* i3 + mn[3]	* i4,
		};
		
		int maxid = 0;
		for(int i = 1; i < 5; ++i){
			if(coeffs[maxid] < coeffs[i]){
				maxid = i;
			}
		}
		
		if(coeffs[maxid] >= 14){
			return maxid;
		}
		
		return -1;
	}

	@Override
	public List<LongDoublePair> getSimilar(SegmentContainer sc, QueryConfig qc) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<LongDoublePair> getSimilar(long shotId, QueryConfig qc) {
		// TODO Auto-generated method stub
		return null;
	}

}
