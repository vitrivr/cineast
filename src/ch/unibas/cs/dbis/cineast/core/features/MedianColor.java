package ch.unibas.cs.dbis.cineast.core.features;

import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import ch.unibas.cs.dbis.cineast.core.color.ColorConverter;
import ch.unibas.cs.dbis.cineast.core.color.LabContainer;
import ch.unibas.cs.dbis.cineast.core.color.RGBContainer;
import ch.unibas.cs.dbis.cineast.core.color.ReadableRGBContainer;
import ch.unibas.cs.dbis.cineast.core.config.QueryConfig;
import ch.unibas.cs.dbis.cineast.core.data.MultiImage;
import ch.unibas.cs.dbis.cineast.core.data.SegmentContainer;
import ch.unibas.cs.dbis.cineast.core.data.StringDoublePair;
import ch.unibas.cs.dbis.cineast.core.data.providers.MedianImgProvider;
import ch.unibas.cs.dbis.cineast.core.features.abstracts.AbstractFeatureModule;
import ch.unibas.cs.dbis.cineast.core.util.TimeHelper;

public class MedianColor extends AbstractFeatureModule {

	private static final Logger LOGGER = LogManager.getLogger();
	
	public MedianColor(){
		super("features_MedianColor", 196f / 4f);
	}
	
	protected LabContainer getMedian(MedianImgProvider provider){
		return MedianColor.getMedian(provider.getMedianImg());
	}
	
	public static LabContainer getMedian(MultiImage img){
		int[] r = new int[256], g = new int[256], b = new int[256];
		int[] colors = img.getColors();
		
		for(int color : colors){
			if(ReadableRGBContainer.getAlpha(color) < 127){
				continue;
			}
			r[RGBContainer.getRed(color)]++;
			g[RGBContainer.getGreen(color)]++;
			b[RGBContainer.getBlue(color)]++;
		}
		
		return ColorConverter.RGBtoLab(medianFromHistogram(r), medianFromHistogram(g), medianFromHistogram(b));
	}
	
	private static int medianFromHistogram(int[] hist){
		int pos_l = 0, pos_r = hist.length - 1;
		int sum_l = hist[pos_l], sum_r = hist[pos_r];
		
		while(pos_l < pos_r){
			if(sum_l < sum_r){
				sum_l += hist[++pos_l];
			}else{
				sum_r += hist[--pos_r];
			}
		}
		return pos_l;
	}

	@Override
	public void processShot(SegmentContainer shot) {
		if(!phandler.idExists(shot.getId())){
			TimeHelper.tic();
			LOGGER.entry();
			LabContainer median = getMedian(shot);
	
			persist(shot.getId(), median);
			LOGGER.debug("MedianColor.processShot() done in {}", TimeHelper.toc());
			LOGGER.exit();
		}
	}

	@Override
	public List<StringDoublePair> getSimilar(SegmentContainer sc, QueryConfig qc) {
		LabContainer query = getMedian(sc.getMedianImg());
		return getSimilar(query.toArray(null), qc);
	}

}
