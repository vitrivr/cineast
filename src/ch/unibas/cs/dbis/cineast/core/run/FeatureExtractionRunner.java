package ch.unibas.cs.dbis.cineast.core.run;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import ch.unibas.cs.dbis.cineast.core.config.Config;
import ch.unibas.cs.dbis.cineast.core.db.ADAMTuple;
import ch.unibas.cs.dbis.cineast.core.db.ADAMWriter;
import ch.unibas.cs.dbis.cineast.core.db.ReturningADAMTuple;
import ch.unibas.cs.dbis.cineast.core.db.ShotLookup;
import ch.unibas.cs.dbis.cineast.core.db.ShotLookup.ShotDescriptor;
import ch.unibas.cs.dbis.cineast.core.decode.video.JLibAVVideoDecoder;
import ch.unibas.cs.dbis.cineast.core.decode.video.VideoDecoder;
import ch.unibas.cs.dbis.cineast.core.features.AverageColor;
import ch.unibas.cs.dbis.cineast.core.features.AverageColorARP44;
import ch.unibas.cs.dbis.cineast.core.features.AverageColorCLD;
import ch.unibas.cs.dbis.cineast.core.features.AverageColorGrid8;
import ch.unibas.cs.dbis.cineast.core.features.AverageColorRaster;
import ch.unibas.cs.dbis.cineast.core.features.AverageFuzzyHist;
import ch.unibas.cs.dbis.cineast.core.features.CLD;
import ch.unibas.cs.dbis.cineast.core.features.ChromaGrid8;
import ch.unibas.cs.dbis.cineast.core.features.DominantColors;
import ch.unibas.cs.dbis.cineast.core.features.DominantEdgeGrid16;
import ch.unibas.cs.dbis.cineast.core.features.DominantEdgeGrid8;
import ch.unibas.cs.dbis.cineast.core.features.EHD;
import ch.unibas.cs.dbis.cineast.core.features.EdgeARP88;
import ch.unibas.cs.dbis.cineast.core.features.EdgeARP88Full;
import ch.unibas.cs.dbis.cineast.core.features.EdgeGrid16;
import ch.unibas.cs.dbis.cineast.core.features.EdgeGrid16Full;
import ch.unibas.cs.dbis.cineast.core.features.HueValueVarianceGrid8;
import ch.unibas.cs.dbis.cineast.core.features.MedianColor;
import ch.unibas.cs.dbis.cineast.core.features.MedianColorARP44;
import ch.unibas.cs.dbis.cineast.core.features.MedianColorGrid8;
import ch.unibas.cs.dbis.cineast.core.features.MedianColorRaster;
import ch.unibas.cs.dbis.cineast.core.features.MedianFuzzyHist;
import ch.unibas.cs.dbis.cineast.core.features.MotionHistogram;
import ch.unibas.cs.dbis.cineast.core.features.SaturationAndChroma;
import ch.unibas.cs.dbis.cineast.core.features.SaturationGrid8;
import ch.unibas.cs.dbis.cineast.core.features.SubDivAverageFuzzyColor;
import ch.unibas.cs.dbis.cineast.core.features.SubDivMedianFuzzyColor;
import ch.unibas.cs.dbis.cineast.core.features.SubDivMotionHistogram2;
import ch.unibas.cs.dbis.cineast.core.features.SubDivMotionHistogram3;
import ch.unibas.cs.dbis.cineast.core.features.SubDivMotionHistogram4;
import ch.unibas.cs.dbis.cineast.core.features.SubDivMotionHistogram5;
import ch.unibas.cs.dbis.cineast.core.features.exporter.ShotThumbNails;
import ch.unibas.cs.dbis.cineast.core.features.extractor.Extractor;
import ch.unibas.cs.dbis.cineast.core.features.extractor.ExtractorInitializer;
import ch.unibas.cs.dbis.cineast.core.runtime.ShotDispatcher;
import ch.unibas.cs.dbis.cineast.core.segmenter.ShotSegmenter;

public class FeatureExtractionRunner {

	private static final Logger LOGGER = LogManager.getLogger();
	private File collectionFolder;
	
	public FeatureExtractionRunner(File collectionFolder){
		this.collectionFolder = collectionFolder;
	}
	
	public void extract(String fileName){
		
		ADAMWriter writer = new ADAMWriter(Config.getDBLocation(), Config.getDBUser(), Config.getDBPassword(), "id"){

			@Override
			public int getParameterCount() {
				return 6;
			}

			@Override
			public String[] getParameterNames() {
				return new String[]{"name", "path", "width", "height", "frames", "seconds"};
			}
			
		};
		
		VideoDecoder vd = new JLibAVVideoDecoder(new File(collectionFolder, fileName));
		
		
		LOGGER.debug("Total frames: {}", vd.getTotalFrameCount());
		LOGGER.debug("frames per second: {}", vd.getFPS());
		
		writer.open("cineast.videos");
		
		List<ShotDescriptor> knownShots = null;
		int id;
		
		if(writer.check("select * from cineast.videos where name = \'" + ADAMTuple.escape(fileName) + "\'")){
			System.err.println(fileName + " allready in database");
			ShotLookup lookup = new ShotLookup(Config.getDBLocation(), Config.getDBUser(), Config.getDBPassword());
			id = lookup.lookUpVideoid(ADAMTuple.escape(fileName));
			knownShots = lookup.lookUpVideo(id);
			lookup.close();
		}else{
			ReturningADAMTuple tuple = (ReturningADAMTuple) writer.makeTuple(fileName, fileName, vd.getWidth(), vd.getHeight(), vd.getTotalFrameCount(), vd.getTotalFrameCount() / vd.getFPS());
			writer.write(tuple);
			
			id = (int) tuple.getReturnValue();
		}
		
		
		ShotSegmenter segmenter = new ShotSegmenter(vd, id, new ADAMWriter(Config.getDBLocation(), Config.getDBUser(), Config.getDBPassword(), "id") {
			
			@Override
			public String[] getParameterNames() {
				return new String[]{"number", "video", "startFrame", "endFrame"};
			}
			
			@Override
			public int getParameterCount() {
				return 4;
			}
		}, knownShots);
		
		ArrayList<Extractor> featureList = new ArrayList<>();
		featureList.add(new AverageColor());
		featureList.add(new DominantColors());
		featureList.add(new MedianColor());
		featureList.add(new SaturationAndChroma());
		featureList.add(new AverageFuzzyHist());
		featureList.add(new MedianFuzzyHist());
		featureList.add(new AverageColorARP44());
		featureList.add(new MedianColorARP44());
		featureList.add(new SubDivAverageFuzzyColor());
		featureList.add(new SubDivMedianFuzzyColor());
		featureList.add(new AverageColorGrid8());
		featureList.add(new ChromaGrid8());
		featureList.add(new SaturationGrid8());
		featureList.add(new AverageColorCLD());
		featureList.add(new CLD());
		featureList.add(new HueValueVarianceGrid8());
		featureList.add(new MedianColorGrid8());
		featureList.add(new EdgeARP88());
		featureList.add(new EdgeGrid16());
		featureList.add(new ShotThumbNails());
		featureList.add(new EdgeARP88Full());
		featureList.add(new EdgeGrid16Full());
		featureList.add(new EHD());
		featureList.add(new AverageColorRaster());
		featureList.add(new MedianColorRaster());
		featureList.add(new MotionHistogram());
		featureList.add(new SubDivMotionHistogram2());
		featureList.add(new SubDivMotionHistogram3());
		featureList.add(new SubDivMotionHistogram4());
		featureList.add(new SubDivMotionHistogram5());
		featureList.add(new DominantEdgeGrid16());
		featureList.add(new DominantEdgeGrid8());

		
		ExtractorInitializer initializer = new ExtractorInitializer() {
			
			@Override
			public void initialize(Extractor e) {
				e.init(new ADAMWriter(Config.getDBLocation(), Config.getDBUser(), Config.getDBPassword()){

					@Override
					public int getParameterCount() {
						return 0;
					}

					@Override
					public String[] getParameterNames() {
						return null;
					}
					
				});				
			}
		};
		
		ShotDispatcher dispatcher = new ShotDispatcher(featureList, initializer, segmenter);
		
		dispatcher.run();		
		
		System.out.println("done");
	}
	
}
