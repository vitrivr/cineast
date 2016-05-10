package ch.unibas.cs.dbis.cineast.core.run;

import java.io.File;
import java.io.FileFilter;
import java.io.FilenameFilter;
import java.util.ArrayList;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import ch.unibas.cs.dbis.cineast.core.config.Config;
import ch.unibas.cs.dbis.cineast.core.config.DatabaseConfig;
import ch.unibas.cs.dbis.cineast.core.db.ADAMproWriter;
import ch.unibas.cs.dbis.cineast.core.db.PersistencyWriter;
import ch.unibas.cs.dbis.cineast.core.db.PersistentTuple;
import ch.unibas.cs.dbis.cineast.core.db.ShotLookup;
import ch.unibas.cs.dbis.cineast.core.db.ShotLookup.ShotDescriptor;
import ch.unibas.cs.dbis.cineast.core.decode.subtitle.SubTitle;
import ch.unibas.cs.dbis.cineast.core.decode.subtitle.srt.SRTSubTitle;
import ch.unibas.cs.dbis.cineast.core.decode.video.JLibAVVideoDecoder;
import ch.unibas.cs.dbis.cineast.core.decode.video.VideoDecoder;
import ch.unibas.cs.dbis.cineast.core.features.AverageColor;
import ch.unibas.cs.dbis.cineast.core.features.AverageColorARP44;
import ch.unibas.cs.dbis.cineast.core.features.AverageColorARP44Normalized;
import ch.unibas.cs.dbis.cineast.core.features.AverageColorCLD;
import ch.unibas.cs.dbis.cineast.core.features.AverageColorCLDNormalized;
import ch.unibas.cs.dbis.cineast.core.features.AverageColorGrid8;
import ch.unibas.cs.dbis.cineast.core.features.AverageColorGrid8Normalized;
import ch.unibas.cs.dbis.cineast.core.features.AverageColorRaster;
import ch.unibas.cs.dbis.cineast.core.features.AverageFuzzyHist;
import ch.unibas.cs.dbis.cineast.core.features.AverageFuzzyHistNormalized;
import ch.unibas.cs.dbis.cineast.core.features.CLD;
import ch.unibas.cs.dbis.cineast.core.features.CLDNormalized;
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
import ch.unibas.cs.dbis.cineast.core.features.MedianColorARP44Normalized;
import ch.unibas.cs.dbis.cineast.core.features.MedianColorGrid8;
import ch.unibas.cs.dbis.cineast.core.features.MedianColorGrid8Normalized;
import ch.unibas.cs.dbis.cineast.core.features.MedianColorRaster;
import ch.unibas.cs.dbis.cineast.core.features.MedianFuzzyHist;
import ch.unibas.cs.dbis.cineast.core.features.MedianFuzzyHistNormalized;
import ch.unibas.cs.dbis.cineast.core.features.MotionHistogram;
import ch.unibas.cs.dbis.cineast.core.features.STMP7EH;
import ch.unibas.cs.dbis.cineast.core.features.SaturationGrid8;
import ch.unibas.cs.dbis.cineast.core.features.SimplePerceptualHash;
import ch.unibas.cs.dbis.cineast.core.features.SubDivAverageFuzzyColor;
import ch.unibas.cs.dbis.cineast.core.features.SubDivMedianFuzzyColor;
import ch.unibas.cs.dbis.cineast.core.features.SubDivMotionHistogram2;
import ch.unibas.cs.dbis.cineast.core.features.SubDivMotionHistogram3;
import ch.unibas.cs.dbis.cineast.core.features.SubDivMotionHistogram4;
import ch.unibas.cs.dbis.cineast.core.features.SubDivMotionHistogram5;
import ch.unibas.cs.dbis.cineast.core.features.exporter.RepresentativeFrameExporter;
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
		
		PersistencyWriter writer = new ADAMproWriter();
		writer.setFieldNames("id", "type", "name", "path", "width", "height", "framecount", "duration");
		
		VideoDecoder vd = new JLibAVVideoDecoder(new File(collectionFolder, fileName));
		
		
		LOGGER.debug("Total frames: {}", vd.getTotalFrameCount());
		LOGGER.debug("frames per second: {}", vd.getFPS());
		
		writer.open("cineast.videos");
		
		List<ShotDescriptor> knownShots = null;
		String id = "";
		
		if(writer.exists("name", fileName)){
			System.err.println(fileName + " allready in database");
			ShotLookup lookup = new ShotLookup();
			id = lookup.lookUpVideoid(fileName);
			knownShots = lookup.lookUpVideo(id);
			lookup.close();
		}else{//TODO
//			ReturningADAMTuple tuple = (ReturningADAMTuple) writer.makeTuple(fileName, fileName, vd.getWidth(), vd.getHeight(), vd.getTotalFrameCount(), vd.getTotalFrameCount() / vd.getFPS());
//			writer.write(tuple);
//			
//			id = (int) tuple.getReturnValue();
		}
		
		
		ShotSegmenter segmenter = new ShotSegmenter(vd, id, new ADAMproWriter(), knownShots);
		
		ArrayList<Extractor> featureList = new ArrayList<>();
		featureList.add(new AverageColor());
		featureList.add(new DominantColors());
		featureList.add(new MedianColor());
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
				e.init(new ADAMproWriter());				
			}
		};
		
		ShotDispatcher dispatcher = new ShotDispatcher(featureList, initializer, segmenter);
		
		dispatcher.run();		
		
		System.out.println("done");
	}
	
	
	public void extractFolder(File folder) {
		if(!folder.isDirectory()){
			LOGGER.error("{} is not a folder, abort extraction", folder.getAbsolutePath());
			return;
		}
		extractFolder(folder.getParentFile(), folder.getName());
	}

	public void extractFolder(String folderName){
		extractFolder(this.collectionFolder, folderName);
	}
	
	public void extractFolder(File baseFolder, String folderName) {

		File inputfolder = new File(baseFolder, folderName);
		String[] videoFiles = inputfolder.list(new FilenameFilter() {

			@Override
			public boolean accept(File dir, String name) {
				String lower = name.toLowerCase();
				return lower.endsWith(".mp4") || lower.endsWith(".avi") || lower.endsWith(".mkv") || lower.endsWith(".mpg");
			}
		});
		if (videoFiles == null || videoFiles.length == 0) {
			LOGGER.error("no video found in {}", inputfolder.getAbsolutePath());
			return;
		}

		String path = folderName + "/" + videoFiles[0];

		
		
		PersistencyWriter<?> writer = new ADAMproWriter();
		writer.setFieldNames("id", "type", "name", "path", "width", "height", "framecount", "duration");

		VideoDecoder vd = new JLibAVVideoDecoder(new File(baseFolder, path));

		LOGGER.debug("Total frames: {}", vd.getTotalFrameCount());
		LOGGER.debug("frames per second: {}", vd.getFPS());

		writer.open("cineast.videos");

		List<ShotDescriptor> knownShots = null;
		String id = null;

		if (writer.exists("name", folderName)) {
//			System.err.println(folderName + " allready in database");
//			ShotLookup lookup = new ShotLookup();
//			id = lookup.lookUpVideoid(folderName);
//			knownShots = lookup.lookUpVideo(id);
//			lookup.close();
		} else {
			
			
			
			id = folderName.replace(' ', '-');//TODO
			
			PersistentTuple tuple = writer.generateTuple(id, 0, folderName, path, vd.getWidth(), vd.getHeight(), vd.getTotalFrameCount(), vd.getTotalFrameCount() / vd.getFPS());
			writer.persist(tuple);
		
		}

		ShotSegmenter segmenter = new ShotSegmenter(vd, id, new ADAMproWriter(), knownShots);

		// search subtitles
		File[] subtitleFiles = inputfolder.listFiles(new FileFilter() {

			@Override
			public boolean accept(File pathname) {
				return pathname.getAbsolutePath().toLowerCase().endsWith(".srt");
			}
		});
		for (File f : subtitleFiles) {
			SubTitle st = new SRTSubTitle(f, (float) vd.getFPS());
			segmenter.addSubTitle(st);
			LOGGER.info("added Subtitle " + f.getAbsolutePath() + " to segmenter");
		}

		ArrayList<Extractor> featureList = new ArrayList<>();
		featureList.add(new AverageColor());
//		featureList.add(new AverageColorARP44());
//		featureList.add(new AverageColorARP44Normalized());
//		featureList.add(new AverageColorCLD());
//		featureList.add(new AverageColorCLDNormalized());
//		featureList.add(new AverageColorGrid8());
//		featureList.add(new AverageColorGrid8Normalized());
//		featureList.add(new AverageColorRaster());
//		featureList.add(new AverageFuzzyHist());
//		featureList.add(new AverageFuzzyHistNormalized());
//		featureList.add(new ChromaGrid8());
//		featureList.add(new CLD());
//		featureList.add(new CLDNormalized());
//		featureList.add(new DominantColors());
//		featureList.add(new DominantEdgeGrid16());
//		featureList.add(new DominantEdgeGrid8());
//		featureList.add(new EdgeARP88());
//		featureList.add(new EdgeARP88Full());
//		featureList.add(new EdgeGrid16());
//		featureList.add(new EdgeGrid16Full());
//		featureList.add(new EHD());
//		featureList.add(new HueValueVarianceGrid8());
//		featureList.add(new MedianColor());
//		featureList.add(new MedianColorARP44());
//		featureList.add(new MedianColorARP44Normalized());
//		featureList.add(new MedianColorGrid8());
//		featureList.add(new MedianColorGrid8Normalized());
//		featureList.add(new MedianColorRaster());
//		featureList.add(new MedianFuzzyHist());
//		featureList.add(new MedianFuzzyHistNormalized());
//		featureList.add(new MotionHistogram());
//		featureList.add(new SaturationGrid8());
//		featureList.add(new SimplePerceptualHash());
//		featureList.add(new STMP7EH());
//		featureList.add(new SubDivAverageFuzzyColor());
//		featureList.add(new SubDivMedianFuzzyColor());
//		featureList.add(new SubDivMotionHistogram2());
//		featureList.add(new SubDivMotionHistogram3());
//		featureList.add(new SubDivMotionHistogram4());
//		featureList.add(new SubDivMotionHistogram5());
		featureList.add(new RepresentativeFrameExporter());
		featureList.add(new ShotThumbNails());

		ExtractorInitializer initializer = new ExtractorInitializer() {

			@Override
			public void initialize(Extractor e) {
				e.init(new ADAMproWriter());
			}
		};

		ShotDispatcher dispatcher = new ShotDispatcher(featureList, initializer, segmenter);

		dispatcher.run();

		vd.close();

		System.out.println("done");
	}
}
