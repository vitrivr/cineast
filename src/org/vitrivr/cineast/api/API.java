package org.vitrivr.cineast.api;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.function.Supplier;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.vitrivr.cineast.core.config.Config;
import org.vitrivr.cineast.core.features.neuralnet.NeuralNetFeature;
import org.vitrivr.cineast.core.features.neuralnet.classification.tf.NeuralNetVGG16Feature;
import org.vitrivr.cineast.core.features.retriever.Retriever;
import org.vitrivr.cineast.core.features.retriever.RetrieverInitializer;
import org.vitrivr.cineast.core.run.ExtractionJobRunner;
import org.vitrivr.cineast.core.setup.EntityCreator;
import org.vitrivr.cineast.core.util.LogHelper;

/**
 * Entry point. 
 * Has an executable main class which connects to the DB and opens a connection to the webserver
 * Ports and additional settings can be specified at cineast.properties
 */
public class API {

	private static RetrieverInitializer initializer = new RetrieverInitializer() {

		@Override
		public void initialize(Retriever r) {
			r.init(Config.getDatabaseConfig().getSelectorSupplier());

		}
	};

	private static final Pattern inputSplitPattern = Pattern.compile("([^\"]\\S*|\".+?\")\\s*");
	
	private static Logger LOGGER = LogManager.getLogger();
	
	private static boolean running = true;

	public static void main(String[] args) {
		// TODO parse command line arguments

		CommandLine commandline = handleCommandLine(args);
		
		if(commandline.hasOption("config")){
			Config.parse(new File(commandline.getOptionValue("config")));
		}
		
		boolean disableAllAPI = false;

		if(commandline.getArgList().contains("playground")){
			/**DBSelector selector = Config.getDatabaseConfig().getSelectorSupplier().get();
			selector.open("cineast_segment");
			for (Map<String, PrimitiveTypeProvider> map : selector.preview()) {
				for(Map.Entry<String, PrimitiveTypeProvider> entry : map.entrySet()){
					LOGGER.info(entry.getKey()+" "+entry.getValue());
				}
				System.out.println("-------------\n");
			}*/
		}


		if(commandline.getArgList().contains("neuralnet")){
			LOGGER.info("Initializing nn persistent layer");
			NeuralNetFeature feature = new NeuralNetVGG16Feature(Config.getNeuralNetConfig());

			feature.initalizePersistentLayer(() -> new EntityCreator());
			LOGGER.info("Initalizing writers");
			feature.init(Config.getDatabaseConfig().getWriterSupplier());
			feature.init(Config.getDatabaseConfig().getSelectorSupplier());
			LOGGER.info("Filling labels");
			feature.fillConcepts(Config.getNeuralNetConfig().getConceptsPath());
			feature.fillLabels(new HashMap<>());

			disableAllAPI = true;
			LOGGER.info("done");

		}

		if(commandline.hasOption("job")){
			ExtractionJobRunner ejr = new ExtractionJobRunner(new File(commandline.getOptionValue("job")));
			Thread thread = new Thread(ejr);
			thread.start();
			disableAllAPI = true;
		}
		
		if(!disableAllAPI && Config.getApiConfig().getEnableCli() || commandline.hasOption('i')){
			APICLIThread cli = new APICLIThread();
			cli.start();
		}
		
		if(!disableAllAPI && Config.getApiConfig().getEnableJsonAPI()){
			try {
				ServerSocket ssocket = new ServerSocket(Config.getApiConfig().getJsonApiPort());
				/*
				 * Wait for a connection, Open a new Thread for each connection.
				 */
				while (running) {
					JSONAPIThread thread = new JSONAPIThread(ssocket.accept());
					thread.start();
				}
				ssocket.close();
			} catch (IOException e) {
				LOGGER.fatal(LogHelper.getStackTrace(e));
			}
			LOGGER.info("Exiting...");
		}
	}

	private static CommandLine handleCommandLine(String[] args) {
		Options options = new Options();
		
		options.addOption("h", "help", false, "print this message");
		options.addOption("i", "interactive", false, "enables the CLI independently of what is specified in the config");
		
		Option configLocation = new Option(null, "config", true, "alternative config file, by default 'cineast.json' is used");
		configLocation.setArgName("CONFIG_FILE");
		options.addOption(configLocation);
		
		Option extractionJob = new Option(null, "job", true, "job file containing settings for extraction");
		configLocation.setArgName("JOB_FILE");
		options.addOption(extractionJob);
		
		CommandLineParser parser = new DefaultParser();
		CommandLine line;
	    try {
	         line = parser.parse( options, args );
	    }catch(ParseException e) {
	    	LOGGER.error("Error parsing command line arguments: {}", e.getMessage());
	    	return null;
	    }

	    if(line.hasOption("help")){
	    	HelpFormatter formatter = new HelpFormatter();
	    	formatter.printHelp("cineast", options);
	    }
	    
		return line;
	}

	public static RetrieverInitializer getInitializer() {
		return initializer;
	}
	
	private static final class APICLIThread extends Thread{
		
		@Override
		public void run(){
			BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
			String line = null;
			try {
				while ((line = reader.readLine()) != null) {
					line = line.trim();
					if (line.isEmpty()) {
						continue;
					}
					Matcher matcher = inputSplitPattern.matcher(line);
					List<String> commands = new ArrayList<String>();
					while (matcher.find()) {
						commands.add(matcher.group(1).replace("\"", ""));
					}
	
					if (commands.isEmpty()) {
						continue;
					}
					switch (commands.get(0).toLowerCase()) {
					case "extract": {
						if (commands.size() < 2) {
							System.out.println("expected base folder of video to extract");
							break;
						}
						File videoFolder = new File(commands.get(1));
						if (!videoFolder.exists() || !videoFolder.isDirectory()) {
							System.out.println("expected base folder of video to extract: "
									+ videoFolder.getAbsolutePath() + " is not a folder");
							break;
						}
//						FeatureExtractionRunner runner = new FeatureExtractionRunner();
//						runner.extractFolder(videoFolder);

						ExtractionJobRunner runner = new ExtractionJobRunner(videoFolder, "test");
						runner.run();
						break;
					}
					case "setup": {
						
						EntityCreator ec = new EntityCreator();
						
						System.out.print("setting up basic entities...");
						
						ec.createMultiMediaObjectsEntity();
						ec.createSegmentEntity();
						
						System.out.println("done");
						
						
						System.out.print("collecting retriever classes...");
						
						HashSet<Retriever> retrievers = new HashSet<>();
						
						for(String category : Config.getRetrieverConfig().getRetrieverCategories()){
							retrievers.addAll(Config.getRetrieverConfig().getRetrieversByCategory(category).keySet());
						}
						
						System.out.println("done");
						
						Supplier<EntityCreator> supply = new Supplier<EntityCreator>() {
							
							@Override
							public EntityCreator get() {
								return ec;
							}
						};
						
						for(Retriever r : retrievers){
							System.out.println("setting up " + r.getClass().getSimpleName());
							r.initalizePersistentLayer(supply);
						}
						
						System.out.println("setup done.");
						
						break;
					}
					case "exit":
					case "quit": {
						running = false;
						System.exit(0);
						break;
					}
					default:
						System.err.println("unrecognized command: " + line);
					}
				}
			} catch (IOException e) {
				//ignore
			}
		}
		
	}
	
}
