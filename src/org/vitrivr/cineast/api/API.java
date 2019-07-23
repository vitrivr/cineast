package org.vitrivr.cineast.api;

import com.google.common.collect.Ordering;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.imageio.ImageIO;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVRecord;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.joml.Vector3f;
import org.joml.Vector3i;
import org.vitrivr.cineast.api.session.CredentialManager;
import org.vitrivr.cineast.core.config.Config;
import org.vitrivr.cineast.core.config.IngestConfig;
import org.vitrivr.cineast.core.config.QueryConfig;
import org.vitrivr.cineast.core.data.entities.MediaObjectMetadataDescriptor;
import org.vitrivr.cineast.core.data.m3d.Mesh;
import org.vitrivr.cineast.core.data.score.SegmentScoreElement;
import org.vitrivr.cineast.core.data.tag.IncompleteTag;
import org.vitrivr.cineast.core.data.tag.Tag;
import org.vitrivr.cineast.core.db.dao.TagHandler;
import org.vitrivr.cineast.core.db.dao.reader.MediaObjectMetadataReader;
import org.vitrivr.cineast.core.evaluation.EvaluationConfig;
import org.vitrivr.cineast.core.evaluation.EvaluationException;
import org.vitrivr.cineast.core.evaluation.EvaluationRuntime;
import org.vitrivr.cineast.core.features.codebook.CodebookGenerator;
import org.vitrivr.cineast.core.features.listener.RetrievalResultCSVExporter;
import org.vitrivr.cineast.core.features.retriever.RetrieverInitializer;
import org.vitrivr.cineast.core.importer.handlers.AsrDataImportHandler;
import org.vitrivr.cineast.core.importer.handlers.DataImportHandler;
import org.vitrivr.cineast.core.importer.handlers.JsonDataImportHandler;
import org.vitrivr.cineast.core.importer.handlers.OcrDataImportHandler;
import org.vitrivr.cineast.core.importer.handlers.ProtoDataImportHandler;
import org.vitrivr.cineast.core.importer.vbs2019.AudioTranscriptImportHandler;
import org.vitrivr.cineast.core.importer.vbs2019.CaptionTextImportHandler;
import org.vitrivr.cineast.core.importer.vbs2019.GoogleVisionImportHandler;
import org.vitrivr.cineast.core.importer.vbs2019.TagImportHandler;
import org.vitrivr.cineast.core.importer.vbs2019.gvision.GoogleVisionCategory;
import org.vitrivr.cineast.core.render.JOGLOffscreenRenderer;
import org.vitrivr.cineast.core.run.ExtractionCompleteListener;
import org.vitrivr.cineast.core.run.ExtractionContainerProvider;
import org.vitrivr.cineast.core.run.ExtractionContextProvider;
import org.vitrivr.cineast.core.run.ExtractionDispatcher;
import org.vitrivr.cineast.core.run.path.ExtractionContainerProviderFactory;
import org.vitrivr.cineast.core.setup.EntityCreator;
import org.vitrivr.cineast.core.util.ContinuousRetrievalLogic;
import org.vitrivr.cineast.core.util.ReflectionHelper;
import org.vitrivr.cineast.core.util.json.JacksonJsonProvider;
import org.vitrivr.cineast.monitoring.PrometheusServer;

/**
 * Entry point. Has an executable main class which connects to the DB and opens a connection to the webserver Ports and additional settings can be specified at cineast.properties
 */
public class API {

  private static final Logger LOGGER = LogManager.getLogger();

  private static final RetrieverInitializer initializer = r -> r
      .init(Config.sharedConfig().getDatabase().getSelectorSupplier());

  private static final Pattern inputSplitPattern = Pattern.compile("([^\"]\\S*|\".+?\")\\s*");

  private static boolean running = true;

  public static void main(String[] args) {
    CommandLine commandline = handleCommandLine(args);
    if (commandline != null) {
      if (commandline.hasOption("config")) {
        Config.loadConfig(commandline.getOptionValue("config"));
      }
      PrometheusServer.initialize();

      /* Handle --setup; start database setup. */
      if (commandline.hasOption("setup")) {
        HashMap<String, String> options = new HashMap<>();
        String optionValue = commandline.getOptionValue("setup");
        String[] flags = optionValue != null ? optionValue.split(";") : new String[0];
        for (String flag : flags) {
          String[] pair = flag.split("=");
          if (pair.length == 2) {
            options.put(pair[0], pair[1]);
          }
        }
        handleSetup(options);
        PrometheusServer.stopServer();
        return;
      }


      /* Handle --job; start handleExtraction. */
      if (commandline.hasOption("job")) {
        handleExtraction(new File(commandline.getOptionValue("job")));
        return;
      }

      if (commandline.hasOption("server")) {
        SessionExtractionContainer.open(new File(commandline.getOptionValue("server")));
      }

      /* Handle --3d; start handleExtraction. */
      if (commandline.hasOption("3d")) {
        handle3Dtest();
        return;
      }


      /* Handle -i; start CLI. */
      if (Config.sharedConfig().getApi().getEnableCli() || commandline.hasOption('i')) {
        CineastCLI cli = new CineastCLI();
        cli.start();
      }

      /* Start the RESTful API if it was configured. */
      if (Config.sharedConfig().getApi().getEnableRest() || Config.sharedConfig().getApi()
          .getEnableRestSecure() || Config.sharedConfig().getApi().getEnableWebsocket() || Config
          .sharedConfig().getApi().getEnableWebsocketSecure()) {
        handleHTTP();
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
  private static void handleSetup(HashMap<String, String> options) {
    EntityCreator ec = Config.sharedConfig().getDatabase().getEntityCreatorSupplier().get();
    if (ec != null) {
      ec.setup(options);
      ec.close();
    }
  }


  /**
   * Starts the HTTP (RESTful / WebSocket) interface (CLI and program-argument)
   */
  private static void handleHTTP() {
    System.out.println("Starting HTTP API...");
    APIEndpoint.start();
    System.out.println("HTTP API started!");
  }

  /**
   * Starts the Legacy JSON interface (program-argument)
   */
  private static void handleLegacy() {
//    try {
//      System.out.println("Starting Legacy API...");
//      ServerSocket ssocket = new ServerSocket(Config.sharedConfig().getApi().getLegacyPort());
//      while (running) {
//        JSONAPIThread thread = new JSONAPIThread(ssocket.accept());
//        thread.start();
//      }
//      ssocket.close();
//    } catch (IOException e) {
//      System.err.println("Error occurred while listening on ServerSocket.");
//    }

    LOGGER.error("Legacy API no longer supported.");

  }

  /**
   * Starts the codebook generation process (CLI only). A valid classname name, input/output path and the number of words must be specified.
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
      System.err
          .println(String.format("The specified codebook generator '%s' does not exist.", name));
    }
  }

  /**
   * Starts the extraction process (CLI and program-argument). A valid configuration file (JSON) must be provided in order to configure that extraction run. Refer to {@link IngestConfig} class for structural information.
   *
   * @param file Configuration file for the extraction.
   * @see ExtractionDispatcher
   * @see IngestConfig
   */
  private static void handleExtraction(File file) {
    ExtractionDispatcher dispatcher = new ExtractionDispatcher();
    try {
      JacksonJsonProvider reader = new JacksonJsonProvider();
      IngestConfig context = reader.toObject(file, IngestConfig.class);
      ExtractionContainerProvider provider = ExtractionContainerProviderFactory
          .tryCreatingTreeWalkPathProvider(file, context);
      if (dispatcher.initialize(provider, context)) {
        dispatcher.start();
        try {
          dispatcher.registerListener((ExtractionCompleteListener) provider);
        } catch (ClassCastException e) {
          LOGGER.debug("Could not register listener");
        }
      } else {
        System.err.println(String.format(
            "Could not start handleExtraction with configuration file '%s'. Does the file exist?",
            file.toString()));
      }
    } catch (IOException e) {
      System.err.println(String.format(
          "Could not start handleExtraction with configuration file '%s' due to a IO error.",
          file.toString()));
      e.printStackTrace();
    }
  }

  /**
   * Starts an evaluation process. A valid configuration file (JSON) must be provided in order to configure that evaluation run. Refer to {@link EvaluationConfig} class for structural information.
   *
   * @param path Path to the configuration file for the extraction.
   * @see EvaluationConfig
   */
  private static void handleEvaluation(Path path) {
    try {
      EvaluationRuntime runtime = new EvaluationRuntime(path);
      runtime.call(); /* TODO: This is a quick & dirty solution. On the long run, API should probably have a dedicated ExecutorService for the different kinds of tasks it can dispatch. */
    } catch (IOException e) {
      System.err.println(String
          .format("Could not start evaluation with configuration file '%s' due to a IO error.",
              path.toString()));
      e.printStackTrace();
    } catch (EvaluationException e) {
      System.err.println(
          String.format("Something went wrong during the evaluation wiht '%s'.", path.toString()));
      e.printStackTrace();
    }
  }

  /**
   * Starts the DataImportHandler for PROTO files.
   *
   * @param path Path to the file or folder that should be imported.
   * @param batchsize Batch size to use with the DataImportHandler
   */
  private static void handleImport(Path path, String option, int batchsize) {
    DataImportHandler handler;
    switch (option) {
      case "proto":
        handler = new ProtoDataImportHandler(2, batchsize);
        handler.doImport(path);
        break;
      case "json":
        handler = new JsonDataImportHandler(2, batchsize);
        handler.doImport(path);
        break;
      case "asr":
        handler = new AsrDataImportHandler(1, batchsize);
        handler.doImport(path);
        break;
      case "ocr":
        handler = new OcrDataImportHandler(1, batchsize);
        handler.doImport(path);
        break;
      case "caption":
        handler = new CaptionTextImportHandler(1, batchsize);
        handler.doImport(path);
        break;
      case "audio":
        handler = new AudioTranscriptImportHandler(1, batchsize);
        handler.doImport(path);
        break;
      case "tags":
        handler = new TagImportHandler(1, batchsize);
        handler.doImport(path);
        break;
      case "vision":
        LOGGER.info("Starting import for Google Vision files at {}", path);
        List<GoogleVisionImportHandler> handlers = new ArrayList<>();
        for (GoogleVisionCategory category : GoogleVisionCategory.values()) {
          GoogleVisionImportHandler _handler = new GoogleVisionImportHandler(1, batchsize, category, false);
          _handler.doImport(path);
          handlers.add(_handler);
          if (category == GoogleVisionCategory.LABELS || category == GoogleVisionCategory.WEB) {
            GoogleVisionImportHandler _handlerTrue = new GoogleVisionImportHandler(1, batchsize, category, true);
            _handlerTrue.doImport(path);
            handlers.add(_handlerTrue);
          }
        }
        handlers.forEach(GoogleVisionImportHandler::waitForCompletion);
        LOGGER.info("Submitted all Google Vision imports for {}", path);
        break;
    }
  }

  /**
   * Performs a test of the JOGLOffscreenRenderer class. If the environment supports OpenGL rendering, an image should be generated depicting two colored triangles on black background. If OpenGL rendering is not supported, an exception will be thrown.
   */
  private static void handle3Dtest() {

    System.out.println("Performing 3D test...");

    Mesh mesh = new Mesh(2, 6);
    mesh.addVertex(new Vector3f(1.0f, 0.0f, 0.0f), new Vector3f(1.0f, 0.0f, 0.0f));
    mesh.addVertex(new Vector3f(0.0f, 1.0f, 0.0f), new Vector3f(0.0f, 1.0f, 0.0f));
    mesh.addVertex(new Vector3f(0.0f, 0.0f, 1.0f), new Vector3f(0.0f, 0.0f, 1.0f));

    mesh.addVertex(new Vector3f(-1.0f, 0.0f, 0.0f), new Vector3f(1.0f, 1.0f, 0.0f));
    mesh.addVertex(new Vector3f(0.0f, -1.0f, 0.0f), new Vector3f(0.0f, 1.0f, 1.0f));
    mesh.addVertex(new Vector3f(0.0f, 0.0f, 1.0f), new Vector3f(1.0f, 0.0f, 1.0f));

    mesh.addFace(new Vector3i(1, 2, 3));
    mesh.addFace(new Vector3i(4, 5, 6));

    JOGLOffscreenRenderer renderer = new JOGLOffscreenRenderer(250, 250);
    renderer.retain();
    renderer.positionCameraPolar(2.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f);
    renderer.assemble(mesh);
    renderer.render();
    BufferedImage image = renderer.obtain();
    renderer.clear();
    renderer.release();

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
    options.addOption("i", "interactive", false,
        "enables the CLI independently of what is specified in the config");
    options.addOption("3d", "test3d", false,
        "tests Cineast's off-screen 3D renderer. If test succeeds, an image should be exported");

    Option configLocation = new Option(null, "config", true,
        "alternative config file, by default 'cineast.json' is used");
    configLocation.setArgName("CONFIG_FILE");
    options.addOption(configLocation);

    Option server = new Option(null, "server", true,
        "config file for extraction server mode");
    server.setArgName("SERVER_FILE");
    options.addOption(server);

    Option extractionJob = new Option(null, "job", true,
        "job file containing settings for handleExtraction");
    extractionJob.setArgName("JOB_FILE");
    options.addOption(extractionJob);

    options.addOption(Option.builder().longOpt("setup")
        .optionalArg(true)
        .numberOfArgs(1)
        .argName("FLAGS")
        .desc("initialize the underlying storage layer with optional <key=value> parameters")
        .build());

    CommandLineParser parser = new DefaultParser();
    CommandLine line;
    try {
      line = parser.parse(options, args);
    } catch (ParseException e) {
      LOGGER.error("Error parsing command line arguments: {}", e.getMessage());
      return null;
    }

    if (line.hasOption("help")) {
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
      System.out.println("Cineast CLI started.");
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
                System.err.println(
                    "You must specify the path to the extraction configuration file (1 argument).");
                break;
              }
              File file = new File(commands.get(1));
              API.handleExtraction(file);
              break;
            }
            case "codebook": {
              if (commands.size() < 5) {
                System.err.println(
                    "You must specify the name of the codebook generator, the source and destination path and the number of words for the codebook (4 arguments).");
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
            case "import": {
              if (commands.size() < 3) {
                System.err.println("You must specify the mode and the path to data file/folder.");
                break;
              }
              final String mode = commands.get(1);
              Path path = Paths.get(commands.get(2));
              int batchsize = 100;
              if (commands.size() == 4) {
                batchsize = Integer.parseInt(commands.get(3));
              }
              handleImport(path, mode, batchsize);
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
//            case "ws":
//            case "websocket": {
//              if (commands.size() < 2) {
//                System.err.println(
//                    "You must specify whether you want to start or stop the websocket (1 argument).");
//                break;
//              }
//              if (commands.get(1).toLowerCase().equals("start")) {
//                handleWebsocketStart();
//              } else {
//                //handleWebsocketStop();
//              }
//              break;
//            }
            case "retrieve": {
              if (commands.size() < 3) {
                System.err.println(
                    "You must specify the segment id to be used as a query and the category of retrievers.");
                break;
              }

              String segmentId = commands.get(1);
              String category = commands.get(2);

              List<SegmentScoreElement> results = ContinuousRetrievalLogic
                  .retrieve(segmentId, category,
                      QueryConfig.newQueryConfigFromOther(Config.sharedConfig().getQuery()));

              System.out.println("results:");
              for (SegmentScoreElement e : results) {
                System.out.print(e.getSegmentId());
                System.out.print(": ");
                System.out.println(e.getScore());
              }
              System.out.println();

              break;
            }
            case "evaluation":
            case "evaluate": {
              if (commands.size() < 2) {
                System.err.println(
                    "You must specify the path to the evaluation configuration file (1 argument).");
                break;
              }
              Path path = Paths.get(commands.get(1));
              API.handleEvaluation(path);
              break;
            }
            case "exportresults": {
              ContinuousRetrievalLogic.addRetrievalResultListener(
                  new RetrievalResultCSVExporter()
              );
              System.out
                  .println("added RetrievalResultCSVExporter to ContinuousRetrievalLogic");
              break;
            }

            case "metadata": {
              if (commands.size() < 2) {
                System.err.println("You must specify at least one object id to lookup.");
                break;
              }
              List<String> ids = commands.subList(1, commands.size());
              Ordering<MediaObjectMetadataDescriptor> ordering =
                  Ordering.explicit(ids).onResultOf(d -> d.getObjectId());
              try (MediaObjectMetadataReader r = new MediaObjectMetadataReader()) {
                List<MediaObjectMetadataDescriptor> descriptors = r.lookupMultimediaMetadata(ids);
                descriptors.sort(ordering);
                descriptors.forEach(System.out::println);
              }
              break;
            }
            case "adduser": {
              if (commands.size() < 3) {
                System.err.println("You must specify username and password of the user to add");
                break;
              }

              String username = commands.get(1);
              String password = commands.get(2);

              boolean admin = commands.size() >= 4 && commands.get(3).equalsIgnoreCase("admin");

              CredentialManager.createUser(username, password, admin);

              break;
            }
            case "exit":
            case "quit": {
              System.exit(0);
              break;
            }
            case "help": {
              System.out.println("3d\t\t\ttests the 3d rendering capabilities");
              System.out.println(
                  "codebook\t\tgenerates a visual codebook from a folder containing images");
              System.out.println("\t\t\t\t<generator> <source> <destination> <number of words>");
              System.out.println("exit\t\t\texit cineast");
              System.out.println(
                  "exportresults\t\tenables RetrievalResultCSVExporter for retrieval results");
              System.out.println("help\t\t\tprints this message");
              System.out.println(
                  "import\t\t\timports data from specified path into currently configured database");
              System.out.println("\t\t\t\t<path>");
              System.out.println("metadata\t\tshows all avalilable metadata for specified segment");
              System.out.println("\t\t\t\t<segment id>");
              System.out.println("quit\t\t\tsee 'exit'");
              System.out.println(
                  "retrieve\t\tshows segments simiar to specified segment given the specified category");
              System.out.println("\t\t\t\t<segment id> <category>");
              System.out.println("setup\t\t\tinitializes database");
              System.out.println("test3d\t\t\tsee '3d'");
              System.out.println();
              break;
            }
            case "load_tags": {

              if (commands.size() < 2) {
                System.err.println("You must specify the path of the csv file to load");
                break;
              }

              File inputFile = new File(commands.get(1));
              if (!inputFile.exists() || !inputFile.isFile() || !inputFile.canRead()) {
                System.err.println("cannot read '" + inputFile.getAbsolutePath() + "'");
                break;
              }

              TagHandler tagHandler = new TagHandler();

              try {
                FileReader in = new FileReader(inputFile);
                Iterable<CSVRecord> records = CSVFormat.DEFAULT.parse(in);

                for (CSVRecord record : records) {
                  if (record.size() < 2) {
                    continue;
                  }
                  Tag toInsert;
                  if (record.size() >= 3) {
                    toInsert = new IncompleteTag(record.get(0), record.get(1), record.get(2));
                  } else {
                    toInsert = new IncompleteTag(record.get(0), record.get(1), "");
                  }

                  if (!toInsert.hasId() || !toInsert.hasName()) {
                    continue;
                  }

                  Tag previous = tagHandler.getTagById(toInsert.getId());
                  if (previous != null) {
                    System.out.println(
                        "Tag with id '" + previous.getId() + "' already exists: " + previous);
                    continue;
                  }

                  if (tagHandler.addTag(toInsert)) {
                    System.out.println("added tag " + toInsert);
                  } else {
                    System.out.println("could not add tag " + toInsert);
                  }

                }
              } catch (IOException e) {
                System.err.println("Error while reading '" + inputFile.getAbsolutePath() + "'");
                e.printStackTrace();
              }

              tagHandler.close();
              break;
            }
            default:
              System.err.println("unrecognized command: " + line);
          }
        }
      } catch (Throwable t) {
        t.printStackTrace();
      }
    }
  }

  public static RetrieverInitializer getInitializer() {
    return initializer;
  }
}
