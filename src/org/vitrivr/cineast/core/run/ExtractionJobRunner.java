package org.vitrivr.cineast.core.run;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.vitrivr.cineast.core.config.Config;
import org.vitrivr.cineast.core.db.PersistencyWriter;
import org.vitrivr.cineast.core.db.PersistentTuple;
import org.vitrivr.cineast.core.db.SegmentLookup.SegmentDescriptor;
import org.vitrivr.cineast.core.decode.shotboundary.ShotBoundaryDecoder;
import org.vitrivr.cineast.core.decode.video.VideoDecoder;
import org.vitrivr.cineast.core.features.extractor.Extractor;
import org.vitrivr.cineast.core.features.extractor.ExtractorInitializer;
import org.vitrivr.cineast.core.runtime.ShotDispatcher;
import org.vitrivr.cineast.core.segmenter.ShotSegmenter;
import org.vitrivr.cineast.core.util.FileUtil;
import org.vitrivr.cineast.core.util.ReflectionHelper;

import com.eclipsesource.json.JsonArray;
import com.eclipsesource.json.JsonObject;
import com.eclipsesource.json.JsonValue;

public class ExtractionJobRunner implements Runnable{

	private static final Logger LOGGER = LogManager.getLogger();
	
	private File inputFile = null;
	private String inputName = null;
	private List<File> subtitleFiles = null;
	private String inputId = null;
	private List<Extractor> extractors = new ArrayList<>();
	private List<SegmentDescriptor> knownShots = null;
	
	public ExtractionJobRunner(File jobFile) {
		try {
			JsonObject obj = JsonValue.readFrom(new FileReader(jobFile)).asObject();
			parseJobConfig(obj);

		} catch (IOException e) {
			LOGGER.error("could not read input file '{}': {}", jobFile.getAbsolutePath(), e.getMessage());
		}
	}
	
	public ExtractionJobRunner(JsonObject jobConfig){
		parseJobConfig(jobConfig);
	}
	
	private boolean chechValidity() {
		if(inputFile == null){
			LOGGER.error("no input specified");
			return false;
		}
		if(!inputFile.isFile() || !inputFile.canRead()){
			LOGGER.error("Cannot access '{}'", inputFile.getAbsolutePath());
			return false;
		}
		if(inputId == null){ //TODO get id from external provider depending on database setting
			LOGGER.error("No id specified");
			return false;
		}
		if(inputName == null || inputName.isEmpty()){
			LOGGER.error("No input name specified");
			return false;
		}
		if(this.extractors.isEmpty()){
			LOGGER.error("No feature modules specified for extraction");
			return false;
		}
		
		return true;
		
	}

	private void parseJobConfig(JsonObject jobConfig){
		if(jobConfig == null){ //in this case we can assume that all the config is specified some other way and return.
			return;
		}
		List<String> entryNames = jobConfig.names();
		for(String entryName : entryNames){
			parseJobConfigEntry(entryName, jobConfig.get(entryName));
		}
	}

	private void parseJobConfigEntry(String entryName, JsonValue configEntry) {
		if(entryName == null){
			LOGGER.error("could not parse job config entry, name was null");
			return;
		}

		if(configEntry == null){
			LOGGER.error("could not parse job config entry, entry was null");
			return;
		}
		
		switch(entryName.toLowerCase()){
		
			case "input":{
				JsonObject inputObject;
				try{
					inputObject = configEntry.asObject();
				}catch(UnsupportedOperationException notAnObject){
					LOGGER.error("could not parse job config entry 'input': entry is not a valid json object");
					return;
				}
				parseInputEntry(inputObject);
				break;
			}
			case "features":{
				JsonArray inputArray;
				try{
					inputArray = configEntry.asArray();
				}catch(UnsupportedOperationException notAnArray){
					LOGGER.error("could not parse job config entry 'features': entry is not a valid json array");
					return;
				}
				parseFeaturesEntry(inputArray);
				break;
			}
			case "exporters":{
				JsonArray inputArray;
				try{
					inputArray = configEntry.asArray();
				}catch(UnsupportedOperationException notAnArray){
					LOGGER.error("could not parse job config entry 'exporters': entry is not a valid json array");
					return;
				}
				parseExportersEntry(inputArray);
				break;
			}
			case "database":{
				Config.setDatabaseConfig(configEntry.asObject());
				break;
			}
			case "retriever":{
				Config.setRetriverConfig(configEntry.asObject());
				break;
			}
			case "decoder":{
				Config.setDecoderConfig(configEntry.asObject());
				break;
			}
			case "extractor":{
				Config.setExtractorConfig(configEntry.asObject());
				break;
			}
			case "imagecache":{
				Config.setImagecacheConfig(configEntry.asObject());
				break;
			}
		
			default:{
				LOGGER.warn("unrecognized config entry {}, ignoring", entryName);
				break;
			}
		}
		
	}

	/**
	 * Parses the features config entry which is expected to have the structure specified in {@link ReflectionHelper#instanciateFromJson(JsonObject, Class, String)}}
	 * @param inputArray
	 */
	private void parseFeaturesEntry(JsonArray inputArray) {
		for(JsonValue jval : inputArray){
			try{
				JsonObject jobj = jval.asObject();
				Extractor e = ReflectionHelper.newExtractor(jobj);
				if(e != null){
					this.extractors.add(e);
				}
			}catch(UnsupportedOperationException notAnObject){
				LOGGER.warn("Could not parse job config entry in 'input.features[]': entry is not a valid json object, skipping");
			}
		}
	}
	
	/**
	 * Parses the exporters config entry which is expected to have the structure specified in {@link ReflectionHelper#instanciateFromJson(JsonObject, Class, String)}}
	 * @param inputArray
	 */
	private void parseExportersEntry(JsonArray inputArray) {
		for(JsonValue jval : inputArray){
			try{
				JsonObject jobj = jval.asObject();
				Extractor exporter = ReflectionHelper.newExporter(jobj); 
				if(exporter != null){
					this.extractors.add(exporter);
				}
			}catch(UnsupportedOperationException notAnObject){
				LOGGER.warn("Could not parse job config entry in 'input.exporters[]': entry is not a valid json object, skipping");
			}
		}
	}

	/**
	 * Parses the input config entry which is expected to have the following structure:<pre>
	 * {
	 * 	"folder" : "...",
	 * 	"file" : "...",
	 * 	"name" : "...",
	 * 	"subtitles" : ["", "", ""],
	 * 	"id" : string,
	 *  "shotboundaries" : "..."
	 * }
	 * 'folder' designates the base folder in which the input files are to be found. If 'file' or 'subtitles' is not specified, the folder is scanned for files.
	 * 'file' designates the input (video) file to be processed. If a 'folder' is specified, the path is expected to be relative to it and absolute otherwise.
	 * 'name' designates the name of the entry to add. If not specified, the name of the 'folder' or the name of the 'file' is used.
	 * 'subtitles' holds the filenames of the subtitle files to use during extractions. If a 'folder' is specified, the paths are expected to be relative to it and absolute otherwise.
	 * 'id' designates the base id to use for this entry. This entry is required in case of extraction without connection to a database.
	 * 'shotboundaries' a file containing pre-computed shot boundaries. This is needed in case an extraction is to be resumed without a database connection to ensure consistent ids.
	 * 
	 * </pre>
	 */
	private void parseInputEntry(JsonObject inputObject) {
		File baseFolder = null;
		if(inputObject.get("folder") != null){
			try{
				String folderName = inputObject.get("folder").asString();
				File folder = new File(folderName);
				if(!folder.exists()){
					LOGGER.warn("Error while parsing 'input.folder': folder {} does not exist, ignoring 'folder'", folder.getAbsolutePath());
				}else if(!folder.isDirectory()){
					LOGGER.warn("Error while parsing 'input.folder': {} is not a directory, ignoring 'folder'", folder.getAbsolutePath());
				}else if(!folder.canRead()){
					LOGGER.warn("Error while parsing 'input.folder': {} is not readable, ignoring 'folder'", folder.getAbsolutePath());
				}else{
					baseFolder = folder;
				}
			}catch(UnsupportedOperationException nameNotAString){
				LOGGER.warn("Could not parse job config entry 'input.folder': entry is not a valid string, ignoring 'folder'");
			}catch(SecurityException canNotRead){
				LOGGER.warn("Error while parsing 'input.folder': security settings do not permitt access, ignoring 'folder'");
			}
		} //baseFolder can be null at this point which is a valid state. It will cause all other paths to be considered absolute.
		
		if(inputObject.get("file") != null){
			try{
				String fileName = inputObject.get("file").asString();
				File inputFileCandidate = new File(baseFolder, fileName);
				if(!inputFileCandidate.exists()){
					LOGGER.warn("Error while parsing 'input.file': file {} does not exist, ignoring 'file'", inputFileCandidate.getAbsolutePath());
				}else if(!inputFileCandidate.isFile()){
					LOGGER.warn("Error while parsing 'input.file': {} is not a file, ignoring 'file'", inputFileCandidate.getAbsolutePath());
				}else if(!inputFileCandidate.canRead()){
					LOGGER.warn("Error while parsing 'input.file': {} is not readable, ignoring 'file'", inputFileCandidate.getAbsolutePath());
				}else if(!FileUtil.isVideoFileName(inputFileCandidate.getAbsolutePath())){
					LOGGER.warn("Error while parsing 'input.file': {} does not have a known video file extension, ignoring 'file'", inputFileCandidate.getAbsolutePath());
				}else{
					this.inputFile = inputFileCandidate;
				}
			}catch(UnsupportedOperationException nameNotAString){
				LOGGER.warn("Could not parse job config entry 'input.file': entry is not a valid string, ignoring 'file'");
			}catch(SecurityException canNotRead){
				LOGGER.warn("Error while parsing 'input.file': security settings do not permitt access, ignoring 'file'");
			}
		}
		
		if(this.inputFile == null){ //no valid input file specified...
			if(baseFolder == null){
				LOGGER.error("Error while parsing 'input', neither a valid 'folder' nor a valid 'file' was specified. Aborting parsing of 'input'");
				return; //not all is lost at this point, a valid input could still come from a command line argument, etc.
			}
			//... scanning for one
			LOGGER.info("No valid input file specified, start scanning {}", baseFolder.getAbsolutePath());
			File[] fileCandidates = baseFolder.listFiles(FileUtil.VIDEO_FILE_FILTER);
			for(File inputFileCandidate : fileCandidates){
				try{
					if(inputFileCandidate.canRead()){
						this.inputFile = inputFileCandidate;
						LOGGER.info("Found input file {}", this.inputFile.getAbsolutePath());
						break;
					}
				}catch(SecurityException e){
					//ignore at this point
				}
			}
		}
		
		if(this.inputFile == null){
			LOGGER.error("Error while parsing 'input', neither a valid 'folder' nor a valid 'file' was specified. Aborting parsing of 'input'");
			return; //not all is lost at this point, a valid input could still come from a command line argument, etc.
		}
		
		if(inputObject.get("name") != null){
			try{
				String name = inputObject.get("name").asString();
				if(name.isEmpty()){
					LOGGER.warn("Could not parse job config entry 'input.name': 'name' was empty, ignoring 'name'");
				}else{
					this.inputName = name;
				}
			}catch(UnsupportedOperationException nameNotAString){
				LOGGER.warn("Could not parse job config entry 'input.name': entry is not a valid string, ignoring 'name'");
			}
		}
		
		if(this.inputName == null){
			if(baseFolder != null){
				this.inputName = baseFolder.getName();
			}else{ //removing file ending to get name
				String name = this.inputFile.getName();
				int stopIndex = name.lastIndexOf('.');
				if(stopIndex > 0){
					this.inputName = name.substring(0, stopIndex);
				}else{//this should never happen unless the check for valid file names is changed to accept something else than <something>.<ending>
					this.inputName = name;
				}
				
			}
		}
		
		if(inputObject.get("subtitles") != null){
			try{
				JsonArray subtitleArray = inputObject.get("subtitles").asArray();
				this.subtitleFiles = new ArrayList<>(Math.max(1, subtitleArray.size())); //list is generated here so an empty array or an array full of unreadable files can still be considered a valid state
				for(JsonValue subtitleValue : subtitleArray){
					try{
						String subtitleFileName = subtitleValue.asString();
						File subtitleFile = new File(baseFolder, subtitleFileName);
						if(!subtitleFile.exists()){
							LOGGER.warn("Error while parsing entry in 'input.subtitles[]': file {} does not exist, skipping", subtitleFile.getAbsolutePath());
						}else if(!subtitleFile.isFile()){
							LOGGER.warn("Error while parsing entry in 'input.subtitles[]': {} is not a file, skipping'", subtitleFile.getAbsolutePath());
						}else if(!subtitleFile.canRead()){
							LOGGER.warn("Error while parsing entry in 'input.subtitles[]': {} is not readable, skipping", subtitleFile.getAbsolutePath());
						}else if(!FileUtil.isSubtitleFileName(subtitleFile.getAbsolutePath())){
							LOGGER.warn("Error while parsing 'entry in 'input.subtitles[]': {} does not have a known video file extension, skipping", subtitleFile.getAbsolutePath());
						}else{
							this.subtitleFiles.add(subtitleFile);
							LOGGER.info("Adding subtitle file {}", subtitleFile.getAbsolutePath());
						}
						
					}catch(UnsupportedOperationException nameNotAString){
						LOGGER.warn("Could not parse job config entry in 'input.subtitles[]': entry is not a valid string, skipping");
					}catch(SecurityException canNotRead){
						LOGGER.warn("Error while parsing entry in 'input.subtitles[]': security settings do not permitt access, skipping");
					}
				}
			}catch(UnsupportedOperationException notAnArray){
				LOGGER.warn("Could not parse job config entry 'input.subtitles': entry is not a valid array, ignoring 'subtitles'");
			}
		}
		
		if(this.subtitleFiles == null && baseFolder != null){//start scanning for subtitles
			LOGGER.info("No subtitles specified, start scanning {} for suitable files", baseFolder.getAbsolutePath());
			File[] fileCandidates = baseFolder.listFiles(FileUtil.SUBTITLE_FILE_FILTER);
			this.subtitleFiles = new ArrayList<>(Math.max(1, fileCandidates.length));
			for(File subtitleFileCandidate : fileCandidates){
				try{
					if(subtitleFileCandidate.canRead()){
						this.subtitleFiles.add(subtitleFileCandidate);
						LOGGER.info("Found subtitle file {}",subtitleFileCandidate.getAbsolutePath());
					}
				}catch(SecurityException e){
					//ignore at this point
				}
			}
		}
		
		if(this.subtitleFiles == null){
			this.subtitleFiles = new ArrayList<>(1);
		}
		
		LOGGER.info("Found {} valid subtitle files", this.subtitleFiles.size());
		
		
		if(inputObject.get("id") != null){
			try{
				String id = inputObject.get("id").asString();
				if(id != null && !id.isEmpty()){
					this.inputId = id;
				}else{
					LOGGER.warn("Could not parse job config entry 'input.id': entry cannot be empty, ignoring 'id'");
				}
			}catch(UnsupportedOperationException e){
				LOGGER.warn("Could not parse job config entry 'input.id': entry is not a string, ignoring 'id'");
			}
		}
		
		if(inputObject.get("shotboundaries") != null){
			try{
				String fileName = inputObject.get("shotboundaries").asString();
				try{
					this.knownShots = ShotBoundaryDecoder.decode(new File(fileName), this.inputId);
				}catch(SecurityException e){
					LOGGER.warn("Could not read shotboundaries: {}", e.getMessage());
				} catch (NullPointerException e) {
					LOGGER.warn("Could not read shotboundaries: {}", e.getMessage());
				} catch (FileNotFoundException e) {
					LOGGER.warn("Could not read shotboundaries: {}", e.getMessage());
				}
				
			}catch(UnsupportedOperationException notAString){
				LOGGER.warn("Could not parse job config entry 'input.shotboundaries': entry is not a valid string, ignoring 'shotboundaries'");
			}
		}
		
	}

	@Override
	public void run() {
		
		if(!chechValidity()){
			LOGGER.error("incomplete parameters for extraction");
			return;
		}
		
		PersistencyWriter<?> writer = Config.getDatabaseConfig().getWriterSupplier().get();
		writer.setFieldNames("id", "type", "name", "path", "width", "height", "framecount", "duration");
		writer.open("cineast_multimediaobject");

		VideoDecoder vd = Config.getDecoderConfig().newVideoDecoder(this.inputFile);
			
		PersistentTuple tuple = writer.generateTuple(inputId, 0, inputName, inputFile.getAbsolutePath(), vd.getWidth(), vd.getHeight(), vd.getTotalFrameCount(), vd.getTotalFrameCount() / vd.getFPS());
		writer.persist(tuple);
		
		
		ShotSegmenter segmenter = new ShotSegmenter(vd, inputId, Config.getDatabaseConfig().getWriterSupplier().get(), knownShots);
		
		ExtractorInitializer initializer = new ExtractorInitializer() {

			@Override
			public void initialize(Extractor e) {
				e.init(Config.getDatabaseConfig().getWriterSupplier());
			}
		};

		ShotDispatcher dispatcher = new ShotDispatcher(this.extractors, initializer, segmenter);

		dispatcher.run();

		vd.close();
		
	}


}
