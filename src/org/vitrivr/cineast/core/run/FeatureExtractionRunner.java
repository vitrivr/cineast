package org.vitrivr.cineast.core.run;

import java.io.File;
import java.io.FileFilter;
import java.io.FilenameFilter;
import java.util.ArrayList;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.vitrivr.cineast.core.config.Config;
import org.vitrivr.cineast.core.db.PersistencyWriter;
import org.vitrivr.cineast.core.db.PersistentTuple;
import org.vitrivr.cineast.core.db.SegmentLookup;
import org.vitrivr.cineast.core.db.MultimediaObjectLookup;
import org.vitrivr.cineast.core.db.MultimediaObjectLookup.MultimediaObjectDescriptor;
import org.vitrivr.cineast.core.db.SegmentLookup.SegmentDescriptor;
import org.vitrivr.cineast.core.decode.subtitle.SubTitle;
import org.vitrivr.cineast.core.decode.subtitle.srt.SRTSubTitle;
import org.vitrivr.cineast.core.decode.video.VideoDecoder;
import org.vitrivr.cineast.core.features.AverageColor;
import org.vitrivr.cineast.core.features.AverageColorARP44;
import org.vitrivr.cineast.core.features.AverageColorARP44Normalized;
import org.vitrivr.cineast.core.features.AverageColorCLD;
import org.vitrivr.cineast.core.features.AverageColorCLDNormalized;
import org.vitrivr.cineast.core.features.AverageColorGrid8;
import org.vitrivr.cineast.core.features.AverageColorGrid8Normalized;
import org.vitrivr.cineast.core.features.AverageColorRaster;
import org.vitrivr.cineast.core.features.AverageFuzzyHist;
import org.vitrivr.cineast.core.features.AverageFuzzyHistNormalized;
import org.vitrivr.cineast.core.features.CLD;
import org.vitrivr.cineast.core.features.CLDNormalized;
import org.vitrivr.cineast.core.features.ChromaGrid8;
import org.vitrivr.cineast.core.features.DominantColors;
import org.vitrivr.cineast.core.features.DominantEdgeGrid16;
import org.vitrivr.cineast.core.features.DominantEdgeGrid8;
import org.vitrivr.cineast.core.features.EHD;
import org.vitrivr.cineast.core.features.EdgeARP88;
import org.vitrivr.cineast.core.features.EdgeARP88Full;
import org.vitrivr.cineast.core.features.EdgeGrid16;
import org.vitrivr.cineast.core.features.EdgeGrid16Full;
import org.vitrivr.cineast.core.features.HueValueVarianceGrid8;
import org.vitrivr.cineast.core.features.MedianColor;
import org.vitrivr.cineast.core.features.MedianColorARP44;
import org.vitrivr.cineast.core.features.MedianColorARP44Normalized;
import org.vitrivr.cineast.core.features.MedianColorGrid8;
import org.vitrivr.cineast.core.features.MedianColorGrid8Normalized;
import org.vitrivr.cineast.core.features.MedianColorRaster;
import org.vitrivr.cineast.core.features.MedianFuzzyHist;
import org.vitrivr.cineast.core.features.MedianFuzzyHistNormalized;
import org.vitrivr.cineast.core.features.MotionHistogram;
import org.vitrivr.cineast.core.features.STMP7EH;
import org.vitrivr.cineast.core.features.SaturationGrid8;
import org.vitrivr.cineast.core.features.SubDivAverageFuzzyColor;
import org.vitrivr.cineast.core.features.SubDivMedianFuzzyColor;
import org.vitrivr.cineast.core.features.SubDivMotionHistogram2;
import org.vitrivr.cineast.core.features.SubDivMotionHistogram3;
import org.vitrivr.cineast.core.features.SubDivMotionHistogram4;
import org.vitrivr.cineast.core.features.SubDivMotionHistogram5;
import org.vitrivr.cineast.core.features.exporter.RepresentativeFrameExporter;
import org.vitrivr.cineast.core.features.exporter.ShotThumbNails;
import org.vitrivr.cineast.core.features.extractor.Extractor;
import org.vitrivr.cineast.core.features.extractor.ExtractorInitializer;
import org.vitrivr.cineast.core.runtime.ShotDispatcher;
import org.vitrivr.cineast.core.segmenter.ShotSegmenter;
@Deprecated
public class FeatureExtractionRunner {

	private static final Logger LOGGER = LogManager.getLogger();
	
	public void extract(File videoFile, String videoName){

		if(videoFile == null){
			throw new NullPointerException("videofile cannot be null");
		}
		
		if(videoName == null){
			throw new NullPointerException("videoName cannot be null");
		}
		
		if(!videoFile.exists() || !videoFile.isFile() || !videoFile.canRead()){
			LOGGER.error("cannot access video file {}", videoFile.getAbsolutePath());
			return;
		}
		
		PersistencyWriter<?> writer = Config.getDatabaseConfig().getWriterSupplier().get();
		writer.setFieldNames("id", "type", "name", "path", "width", "height", "framecount", "duration");

		VideoDecoder vd = Config.getDecoderConfig().newVideoDecoder(videoFile);

		LOGGER.debug("Total frames: {}", vd.getTotalFrameCount());
		LOGGER.debug("frames per second: {}", vd.getFPS());

		writer.open("cineast_multimediaobject");

		List<SegmentDescriptor> knownShots = null;
		String id = null;

		if (writer.exists("name", videoName)) {
			LOGGER.info("video '{}' is already in database", videoName);
//			ShotLookup lookup = new ShotLookup();
//			id = lookup.lookUpVideoid(videoName);
//			knownShots = lookup.lookUpVideo(id);
//			lookup.close();
			
			MultimediaObjectLookup lookup = new MultimediaObjectLookup();
			MultimediaObjectDescriptor descriptor = lookup.lookUpObjectByName(videoName);
			if(descriptor.exists()){
				id = descriptor.getId();
			}
			
			
		} else {
			
			
			
			id = "v_" + videoName.replace(' ', '-');//TODO
			
			PersistentTuple tuple = writer.generateTuple(id, 0, videoName, videoFile.getAbsolutePath(), vd.getWidth(), vd.getHeight(), vd.getTotalFrameCount(), vd.getTotalFrameCount() / vd.getFPS());
			writer.persist(tuple);
		
		}

		ShotSegmenter segmenter = new ShotSegmenter(vd, id, Config.getDatabaseConfig().getWriterSupplier().get(), knownShots);
		
		File parentFolder = videoFile.getParentFile();

		// search subtitles
		File[] subtitleFiles = parentFolder.listFiles(new FileFilter() {

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
		featureList.add(new AverageColorARP44());
		featureList.add(new AverageColorARP44Normalized());
		featureList.add(new AverageColorCLD());
		featureList.add(new AverageColorCLDNormalized());
		featureList.add(new AverageColorGrid8());
		featureList.add(new AverageColorGrid8Normalized());
		featureList.add(new AverageColorRaster());
		featureList.add(new AverageFuzzyHist());
		featureList.add(new AverageFuzzyHistNormalized());
		featureList.add(new ChromaGrid8());
		featureList.add(new CLD());
		featureList.add(new CLDNormalized());
		featureList.add(new DominantColors());
		featureList.add(new DominantEdgeGrid16());
		featureList.add(new DominantEdgeGrid8());
		featureList.add(new EdgeARP88());
		featureList.add(new EdgeARP88Full());
		featureList.add(new EdgeGrid16());
		featureList.add(new EdgeGrid16Full());
		featureList.add(new EHD());
		featureList.add(new HueValueVarianceGrid8());
		featureList.add(new MedianColor());
		featureList.add(new MedianColorARP44());
		featureList.add(new MedianColorARP44Normalized());
		featureList.add(new MedianColorGrid8());
		featureList.add(new MedianColorGrid8Normalized());
		featureList.add(new MedianColorRaster());
		featureList.add(new MedianFuzzyHist());
		featureList.add(new MedianFuzzyHistNormalized());
		featureList.add(new MotionHistogram());
		featureList.add(new SaturationGrid8());
		//featureList.add(new SimplePerceptualHash());
		featureList.add(new STMP7EH());
		featureList.add(new SubDivAverageFuzzyColor());
		featureList.add(new SubDivMedianFuzzyColor());
		featureList.add(new SubDivMotionHistogram2());
		featureList.add(new SubDivMotionHistogram3());
		featureList.add(new SubDivMotionHistogram4());
		featureList.add(new SubDivMotionHistogram5());
		featureList.add(new RepresentativeFrameExporter());
		featureList.add(new ShotThumbNails());

		ExtractorInitializer initializer = new ExtractorInitializer() {

			@Override
			public void initialize(Extractor e) {
				e.init(Config.getDatabaseConfig().getWriterSupplier());
			}
		};

		ShotDispatcher dispatcher = new ShotDispatcher(featureList, initializer, segmenter);

		dispatcher.run();

		vd.close();

		System.out.println("done");
	}
	
	public void extractFolder(File folder) {
		if(!folder.isDirectory()){
			LOGGER.error("{} is not a folder, abort extraction", folder.getAbsolutePath());
			return;
		}

		File[] videoFiles = folder.listFiles(new FilenameFilter() {

			@Override
			public boolean accept(File dir, String name) {
				String lower = name.toLowerCase();
				return lower.endsWith(".mp4") || lower.endsWith(".avi") || lower.endsWith(".mkv") || lower.endsWith(".mpg");
			}
		});
		if (videoFiles == null || videoFiles.length == 0) {
			LOGGER.error("no video found in {}", folder.getAbsolutePath());
			return;
		}


		extract(videoFiles[0], folder.getName());
		
	}
}
