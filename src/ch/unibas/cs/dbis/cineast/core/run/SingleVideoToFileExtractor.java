package ch.unibas.cs.dbis.cineast.core.run;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileFilter;
import java.io.FileReader;
import java.io.FilenameFilter;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import ch.unibas.cs.dbis.cineast.core.db.CSVWriter;
import ch.unibas.cs.dbis.cineast.core.db.ShotLookup.ShotDescriptor;
import ch.unibas.cs.dbis.cineast.core.decode.subtitle.SubTitle;
import ch.unibas.cs.dbis.cineast.core.decode.subtitle.cc.CCSubTitle;
import ch.unibas.cs.dbis.cineast.core.decode.subtitle.srt.SRTSubTitle;
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
import ch.unibas.cs.dbis.cineast.core.features.SaturationGrid8;
import ch.unibas.cs.dbis.cineast.core.features.SubDivAverageFuzzyColor;
import ch.unibas.cs.dbis.cineast.core.features.SubDivMedianFuzzyColor;
import ch.unibas.cs.dbis.cineast.core.features.SubDivMotionHistogram2;
import ch.unibas.cs.dbis.cineast.core.features.SubDivMotionHistogram3;
import ch.unibas.cs.dbis.cineast.core.features.SubDivMotionHistogram4;
import ch.unibas.cs.dbis.cineast.core.features.SubDivMotionHistogram5;
import ch.unibas.cs.dbis.cineast.core.features.SubtitleFulltextSearch;
import ch.unibas.cs.dbis.cineast.core.features.exporter.ShotThumbNails;
import ch.unibas.cs.dbis.cineast.core.features.extractor.Extractor;
import ch.unibas.cs.dbis.cineast.core.features.extractor.ExtractorInitializer;
import ch.unibas.cs.dbis.cineast.core.runtime.ShotDispatcher;
import ch.unibas.cs.dbis.cineast.core.segmenter.ShotSegmenter;

public class SingleVideoToFileExtractor {

	private static final Logger LOGGER = LogManager.getLogger();
	
	private static ExtractorInitializer initializer = new ExtractorInitializer() {

		@Override
		public void initialize(Extractor e) {
			e.init(new CSVWriter());
		}
		
	};
	
	public static void main(String[] args) {
		if(args.length < 3){
			printUseage();
		}
		
		final long videoId;
		long id = 0;
		try{
			id = Long.parseLong(args[2]);
		}catch(Exception e){
			printUseage();
		}
		videoId = id;
		
		File inputFolder = new File(args[0]);
		File outputFolder = new File(args[1] + "/" + videoId);
		
		CSVWriter.setFolder(outputFolder);
		
		String[] videoFiles = inputFolder.list(new VideoFileNameFilter());
		
		if(videoFiles == null || videoFiles.length == 0){
			LOGGER.error("no video found in {}", inputFolder.getAbsolutePath());
			System.exit(-1);
		}
		
		VideoDecoder vd = new JLibAVVideoDecoder(new File(inputFolder, videoFiles[0]));
		
		
		LOGGER.debug("Total frames: {}", vd.getTotalFrameCount());
		LOGGER.debug("frames per second: {}", vd.getFPS());
		
		List<ShotDescriptor> knownShots = readKnownShots(inputFolder, videoId);
		
		ShotSegmenter segmenter = new ShotSegmenter(vd, videoId, new CSVWriter(), knownShots);
		
		
		File[] subtitleFiles = inputFolder.listFiles(new FileFilter() {
			
			@Override
			public boolean accept(File pathname) {
				return pathname.getAbsolutePath().toLowerCase().endsWith(".srt");
			}
		});
		for(File f : subtitleFiles){
			SubTitle st = new SRTSubTitle(f, (float) vd.getFPS());
			segmenter.addSubTitle(st);
			LOGGER.info("added Subtitle " + f.getAbsolutePath() + " to segmenter");
		}
		
		subtitleFiles = inputFolder.listFiles(new FileFilter() {
			
			@Override
			public boolean accept(File pathname) {
				return pathname.getAbsolutePath().toLowerCase().endsWith(".txt");
			}
		});
		for(File f : subtitleFiles){
			SubTitle st = new CCSubTitle(f, (float) vd.getFPS());
			segmenter.addSubTitle(st);
			LOGGER.info("added Subtitle " + f.getAbsolutePath() + " to segmenter");
		}
		
		ArrayList<Extractor> featureList = getExtractors();
		
		ShotDispatcher dispatcher = new ShotDispatcher(featureList, initializer, segmenter);
		
		long startTime = System.currentTimeMillis();
		dispatcher.run();
		LOGGER.info("finished extraction for video {} in {}", videoId, formatTime(System.currentTimeMillis() - startTime));
	}
	
	private static List<ShotDescriptor> readKnownShots(File inputFolder, long videoId) {
		File input = new File(inputFolder, "shotEndFrames.csv");
		if(!(input.exists() && input.isFile() && input.canRead())){
			return null;
		}
		try {
			BufferedReader inReader = new BufferedReader(new FileReader(input));
			String line = inReader.readLine();
			inReader.close();
			
			String[] split = line.split(",");
			ArrayList<ShotDescriptor> shots = new ArrayList<>(split.length);
			
			int startFrame = 1;
			int endFrame = Integer.parseInt(split[0].trim());
			ShotDescriptor descriptor = new ShotDescriptor(videoId, 1, startFrame, endFrame);
			shots.add(descriptor);
			
			for(int i = 0; i < split.length - 1; ++i){
				startFrame = Integer.parseInt(split[i].trim()) + 1;
				endFrame = Integer.parseInt(split[i + 1].trim());
				descriptor = new ShotDescriptor(videoId, i + 2, startFrame, endFrame);
				shots.add(descriptor);
			}
			LOGGER.debug("successfully read {} shot boundaries", shots.size());
			return shots;
			
		} catch (Exception e) {
			LOGGER.error("error while reading shot boundaries");
		}
		
		
		return null;
	}

	private static void printUseage(){
		System.out.println();
		System.out.println("expected parameters: <input folder> <output folder> <video id>");
		System.out.println();
		System.out.println("input folder:  folder containing one video and possibly additional files,");
		System.out.println("               such as .srt subtitles");
		System.out.println("output folder: folder to which the extracted feature data should be written.");
		System.out.println("               a subfolder will be created according to the video id");
		System.out.println("video id:      a non-negative globally unique integer used to refer to the video.");
		System.out.println();
		System.exit(-1);
	}
	
	private static ArrayList<Extractor> getExtractors(){
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
		featureList.add(new SubtitleFulltextSearch());
		featureList.add(new AverageColorRaster());
		featureList.add(new MedianColorRaster());
		featureList.add(new MotionHistogram());
		featureList.add(new SubDivMotionHistogram2());
		featureList.add(new SubDivMotionHistogram3());
		featureList.add(new SubDivMotionHistogram4());
		featureList.add(new SubDivMotionHistogram5());
		featureList.add(new DominantEdgeGrid16());
		featureList.add(new DominantEdgeGrid8());
		
		return featureList;
	}
	
	private static String formatTime(long ms){
		return String.format("%02d:%02d:%02d", TimeUnit.MILLISECONDS.toHours(ms),
	            TimeUnit.MILLISECONDS.toMinutes(ms) - TimeUnit.HOURS.toMinutes(TimeUnit.MILLISECONDS.toHours(ms)),
	            TimeUnit.MILLISECONDS.toSeconds(ms) - TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(ms)));
	}

}
class VideoFileNameFilter implements FilenameFilter{

	private static boolean isVideoFile(String fileName){
		String lowerCase = fileName.toLowerCase();
		return  lowerCase.endsWith(".avi") ||
				lowerCase.endsWith(".mkv") ||
				lowerCase.endsWith(".mov") ||
				lowerCase.endsWith(".mp4") ||
				lowerCase.endsWith(".mpg") ||
				lowerCase.endsWith(".ogv") ||
				lowerCase.endsWith(".webm");
	}
	
	@Override
	public boolean accept(File dir, String name) {
		return isVideoFile(name);
	}
	
}