package org.vitrivr.cineast.api;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;

import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.cli.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import org.joml.Vector3f;
import org.joml.Vector3i;
import org.vitrivr.cineast.api.rest.RestfulAPI;
import org.vitrivr.cineast.api.websocket.WebsocketAPI;
import org.vitrivr.cineast.core.config.Config;
import org.vitrivr.cineast.core.config.IngestConfig;
import org.vitrivr.cineast.core.data.m3d.Mesh;
import org.vitrivr.cineast.core.features.codebook.CodebookGenerator;
import org.vitrivr.cineast.core.features.retriever.RetrieverInitializer;
import org.vitrivr.cineast.core.render.JOGLOffscreenRenderer;
import org.vitrivr.cineast.core.run.ExtractionDispatcher;
import org.vitrivr.cineast.core.setup.EntityCreator;
import org.vitrivr.cineast.core.util.LogHelper;
import org.vitrivr.cineast.core.util.ReflectionHelper;

import javax.imageio.ImageIO;

/**
 * Entry point. 
 * Has an executable main class which connects to the DB and opens a connection to the webserver
 * Ports and additional settings can be specified at cineast.properties
 */
public class API {

    private static final Logger LOGGER = LogManager.getLogger();

    private static final RetrieverInitializer initializer = r -> r.init(Config.getDatabaseConfig().getSelectorSupplier());

	private static final Pattern inputSplitPattern = Pattern.compile("([^\"]\\S*|\".+?\")\\s*");

    private static boolean running = true;

    /**
	 *
	 * @param args
	 */
	public static void main(String[] args) {
		CommandLine commandline = handleCommandLine(args);
		if (commandline != null) {
			if (commandline.hasOption("config")) {
				Config.loadConfig(commandline.getOptionValue("config"));
			}

			/* Handle -job; start handleExtraction. */
			if (commandline.hasOption("job")) {
				handleExtraction(new File(commandline.getOptionValue("job")));
				return;
			}

			/* Handle -job; start handleExtraction. */
            if (commandline.hasOption("3d")) {
                handle3Dtest();
                return;
            }

			/* Handle -cli; start CLI. */
			if (Config.sharedConfig().getApi().getEnableCli() || commandline.hasOption('i')) {
				CineastCLI cli = new CineastCLI();
				cli.start();
				return;
			}

			/* Handle -setup; start database setup. */
			if (commandline.hasOption("setup")) {
				HashMap<String, String> options = new HashMap<>();
				String[] flags = commandline.getOptionValue("setup").split(";");
				for (String flag : flags) {
					String[] pair = flag.split("=");
					if (pair.length == 2) {
						options.put(pair[0], pair[1]);
					}
				}

				handleSetup(options);
				return;
			}

			/* Start the WebSocket API if it was configured. */
			if (Config.sharedConfig().getApi().getEnableWebsocket()) {
                handleWebsocketStart();
            }

			/* Start the RESTful API if it was configured. */
			if (Config.sharedConfig().getApi().getEnableRest()) {
                handleRestful();
            }

            /* Start the Legacy API if it was configured. */
            if (Config.sharedConfig().getApi().getEnableLegacy()) {
                handleLegacy();
            }
		} else {
			LOGGER.fatal("Could not parse commandline arguments.");
		}
	}


	/**
	 * Handles the database setup option (CLI and program-argument)
	 */
	private static void handleSetup(HashMap<String,String> options) {
		EntityCreator ec = Config.sharedConfig().getDatabase().getEntityCreatorSupplier().get();
		if (ec != null) ec.setup(options);
	}

	/**
	 * Starts the WebSocket interface (CLI and program-argument)
	 */
	private static void handleWebsocketStart() {
		if (!WebsocketAPI.isRunning()) {
			System.out.print("Starting WebSocket API...");
			int port = Config.sharedConfig().getApi().getHttpPort();
			int threadPoolSize = Config.sharedConfig().getApi().getThreadPoolSize();
			WebsocketAPI.start(port, threadPoolSize);
			System.out.print("WebSocket API started!");
		} else {
			System.err.print("WebSocket API is already running...");
		}
	}

	/**
	 * Stops the WebSocket interface (CLI)
	 */
	private static void handleWebsocketStop() {
		if (WebsocketAPI.isRunning()) {
			System.out.print("Stopping WebSocket API...");
			WebsocketAPI.stop();
			System.out.print("WebSocket API stopped!");
		} else {
			System.err.print("WebSocket API has not been started yet...");
		}
	}

	/**
	 * Starts the RESTful interface (CLI and program-argument)
	 */
	private static void handleRestful() {
		System.out.print("Starting RESTful API...");
        int port = Config.sharedConfig().getApi().getHttpPort();
		int threadPoolSize = Config.sharedConfig().getApi().getThreadPoolSize();
		RestfulAPI.start(port, threadPoolSize);
		System.out.print("RESTful API started!");
	}

    /**
     * Starts the Legacy JSON interface (program-argument)
     */
    private static void handleLegacy() {
        try {
            System.out.print("Starting Legacy API...");
            ServerSocket ssocket  = new ServerSocket(Config.sharedConfig().getApi().getLegacyPort());
            while (running) {
                JSONAPIThread thread = new JSONAPIThread(ssocket.accept());
                thread.start();
            }
            ssocket.close();
        } catch (IOException e) {
			System.err.println("Error occurred while listening on ServerSocket.");
        }
    }

	/**
	 * Starts the codebook generation process (CLI only). A valid classname name, input/output path and the
	 * number of words must be specified.
	 *
	 * @param name Name of the codebook generator class. Either a FQN oder the classes simple name.
	 * @param input Path to the input folder containing the data to derive a codebook from (e.g. images).
	 * @param output Path to the output file for the codebook.
	 * @param words The number of words in the codebook.
	 */
    private static void handleCodebook(String name, Path input, Path output, int words) {
		CodebookGenerator generator = ReflectionHelper.newCodebookGenerator(name);
		if (generator != null) {
			try {
				generator.generate(input, output, words);
			} catch (IOException e) {
				e.printStackTrace();
			}
		} else {
			System.err.println(String.format("The specified codebook generator '{}' does not exist.", name));
		}
	}

	/**
	 * Starts the extraction process (CLI and program-argument). A valid configuration file (JSON) must be provided in order
	 * to configure that extraction run. Refer to ExtractionConfig class for structural information.
	 *
	 * @see ExtractionDispatcher
	 * @see IngestConfig
	 *
	 * @param file Configuration file for the extraction.
	 */
	private static void handleExtraction(File file) {
		ExtractionDispatcher dispatcher = new ExtractionDispatcher();
		try {
            if (dispatcher.initialize(file)) {
                dispatcher.start();
            } else {
                System.err.println(String.format("Could not start handleExtraction with configuration file '%s'. Does the file exist?", file.toString()));
            }
		} catch (IOException e) {
			System.err.println(String.format("Could not start handleExtraction with configuration file '{}' due to a serious IO error.", file.toString()));
		}
	}

    /**
     * Performs a test of the JOGLOffscreenRenderer class. If the environment supports OpenGL rendering, an image
     * should be generated depicting two colored triangles on black background. If OpenGL rendering is not supported,
     * an exception will be thrown.
     */
	private static void handle3Dtest() {

		System.out.println("Performing 3D test...");

        Mesh mesh = new Mesh();
        mesh.addVertex(new Vector3f(1.0f,0.0f,0.0f), new Vector3f(1.0f, 0.0f, 0.0f));
        mesh.addVertex(new Vector3f(0.0f,1.0f,0.0f), new Vector3f(0.0f, 1.0f, 0.0f));
        mesh.addVertex(new Vector3f(0.0f,0.0f,1.0f), new Vector3f(0.0f, 0.0f, 1.0f));

        mesh.addVertex(new Vector3f(-1.0f,0.0f,0.0f), new Vector3f(1.0f, 1.0f, 0.0f));
        mesh.addVertex(new Vector3f(0.0f,-1.0f,0.0f), new Vector3f(0.0f, 1.0f, 1.0f));
        mesh.addVertex(new Vector3f(0.0f,0.0f,1.0f), new Vector3f(1.0f, 0.0f, 1.0f));

        mesh.addFace(new Vector3i(1,2,3), null);
        mesh.addFace(new Vector3i(4,5,6), null);

        JOGLOffscreenRenderer renderer = new JOGLOffscreenRenderer(250, 250);
        renderer.render(mesh, 2.0f,0.0f,0.0f);
        BufferedImage image = renderer.obtain();
        try {
            ImageIO.write(image, "PNG", new File("cineast-3dtest.png"));
            System.out.println("3D test complete. Check for cineast-3dtest.png");
        } catch (IOException e) {
			System.err.println("Could not save rendered image due to an IO error.");
        }
    }


	private static CommandLine handleCommandLine(String[] args) {
		Options options = new Options();
		
		options.addOption("h", "help", false, "print this message");
		options.addOption("i", "interactive", false, "enables the CLI independently of what is specified in the config");
        options.addOption("3d", "test3d", false, "tests Cineast's off-screen 3D renderer. If test succeeds, an image should be exported");

        Option configLocation = new Option(null, "config", true, "alternative config file, by default 'cineast.json' is used");
		configLocation.setArgName("CONFIG_FILE");
		options.addOption(configLocation);
		
		Option extractionJob = new Option(null, "job", true, "job file containing settings for handleExtraction");
		extractionJob.setArgName("JOB_FILE");
		options.addOption(extractionJob);
		
		Option setup = new Option(null, "setup", false, "initialize the underlying storage layer");
		setup.setArgName("FLAGS");
		options.addOption(setup);


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


	/**
	 * @author rgasser
	 * @version 1.0
	 * @created 22.01.17
	 */
	private static class CineastCLI extends Thread {
		@Override
		public void run() {
			BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
			System.out.println("Cineast CLI started. I'm awaiting your commands...");
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
								System.err.println("You must specify the path to the extraction configuration file (1 argument).");
								break;
							}
							File file = new File(commands.get(1));
							API.handleExtraction(file);
							break;
						}
						case "codebook": {
							if (commands.size() < 5) {
								System.err.println("You must specify the name of the codebook generator, the source and destination path and the number of words for the codebook (4 arguments).");
								break;
							}

                            /* Parse information from input. */
							String codebookGenerator = commands.get(1);
							Path src = Paths.get(commands.get(2));
							Path dst = Paths.get(commands.get(3));
							Integer words = Integer.parseInt(commands.get(4));

                            /* Start codebook generation. */
							API.handleCodebook(codebookGenerator, src, dst, words);
							break;
						}
                        case "3d":
                        case "test3d": {
                            handle3Dtest();
                            break;
                        }
						case "setup": {
							HashMap<String, String> options = new HashMap<>();
							if (commands.size() > 1) {
								String[] flags = commands.get(1).split(";");
								for (String flag : flags) {
									String[] pair = flag.split("=");
									if (pair.length == 2) {
										options.put(pair[0], pair[1]);
									}
								}
							}
							handleSetup(options);
							break;
						}
                        case "ws":
						case "websocket": {
							if (commands.size() < 2) {
								System.err.println("You must specify whether you want to start or stop the websocket (1 argument).");
								break;
							}
							if (commands.get(1).toLowerCase().equals("start")) {
								handleWebsocketStart();
							} else {
								handleWebsocketStop();
							}
							break;
						}
						case "exit":
						case "quit": {
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

	public static RetrieverInitializer getInitializer() {
		return initializer;
	}
}
