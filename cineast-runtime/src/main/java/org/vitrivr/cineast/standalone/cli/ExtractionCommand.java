package org.vitrivr.cineast.standalone.cli;

import com.github.rvesse.airline.annotations.Command;
import com.github.rvesse.airline.annotations.Option;
import com.github.rvesse.airline.annotations.restrictions.Required;
import java.io.File;
import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.List;
import java.util.UUID;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.vitrivr.cineast.core.db.DataSource;
import org.vitrivr.cineast.core.iiif.IIIFConfig;
import org.vitrivr.cineast.core.iiif.UnsupportedIIIFAPIException;
import org.vitrivr.cineast.core.iiif.discoveryapi.v1.OrderedCollectionFactory;
import org.vitrivr.cineast.core.iiif.imageapi.ImageFetcher;
import org.vitrivr.cineast.core.iiif.presentationapi.ManifestFactory;
import org.vitrivr.cineast.core.util.json.JacksonJsonProvider;
import org.vitrivr.cineast.standalone.config.IngestConfig;
import org.vitrivr.cineast.standalone.config.InputConfig;
import org.vitrivr.cineast.standalone.run.ExtractionCompleteListener;
import org.vitrivr.cineast.standalone.run.ExtractionContainerProvider;
import org.vitrivr.cineast.standalone.run.ExtractionDispatcher;
import org.vitrivr.cineast.standalone.run.path.ExtractionContainerProviderFactory;

/**
 * A CLI command that can be used to start a media extraction based on an extraction definition file.
 */
@Command(name = "extract", description = "Starts a media extracting using the specified settings.")
public class ExtractionCommand extends AbstractCineastCommand {

  private static final Logger LOGGER = LogManager.getLogger();

  @Option(name = {"--no-finalize"}, title = "Do Not Finalize", description = "If this flag is not set, automatically rebuilds indices & optimizes all entities when writing to cottontail after the extraction. Set this flag when you want more performance with external parallelism.")
  private final boolean doNotFinalize = false;

  @Required
  @Option(name = {"-e", "--extraction"}, title = "Extraction config", description = "Path that points to a valid Cineast extraction config file.")
  private String extractionConfig;

  /**
   * Deletes all the IIIF resource and metadata files downloaded by {@link ExtractionCommand#prepareIIIFExtractionJob(IIIFConfig, String)} once extraction has been performed on them.
   */
  private Runnable postExtractionIIIFCleanup;

  @Override
  public void execute() {
    final ExtractionDispatcher dispatcher = new ExtractionDispatcher();
    final File file = new File(this.extractionConfig);
    if (!file.exists()) {
      System.err.printf("Could not start handleExtraction with configuration file '%s'; the file does not exist!%n", file);
      return;
    }

    try {
      final JacksonJsonProvider reader = new JacksonJsonProvider();
      final IngestConfig context = reader.toObject(file, IngestConfig.class);
      // Check if the config specifies a IIIF job
      if (context != null) {
        InputConfig inputConfig = context.getInput();
        IIIFConfig iiifConfig = inputConfig.getIiif();
        if (iiifConfig != null) {
          String directoryPath;
          String iiifConfigPath = inputConfig.getPath();
            /* If the user hasn't asked to save the images once extraction is complete or if the user has not specified a download directory for the IIIF images, then save the images in a new "iiif-media-{@link System#currentTimeMillis}" subfolder.
            This folder can act as the temp directory during extraction and can either be deleted or retained post extraction based on the value set in {@link IIIFConfig#keepImagesPostExtraction} */
          if (!iiifConfig.isKeepImagesPostExtraction() || iiifConfigPath == null || iiifConfigPath.isEmpty()) {
            directoryPath = file.getAbsoluteFile().toPath().getParent() + "/iiif-media-" + System.currentTimeMillis();
            inputConfig.setPath(directoryPath);
          } else {
            directoryPath = iiifConfigPath;
          }
          prepareIIIFExtractionJob(iiifConfig, directoryPath);
        }
      }
      final ExtractionContainerProvider provider = ExtractionContainerProviderFactory.tryCreatingTreeWalkPathProvider(file, context);
      if (dispatcher.initialize(provider, context)) {
        /* Only attempt to optimize Cottontail entities if we were extracting into Cottontail, otherwise an unavoidable error message would be displayed when extracting elsewhere. */
        if (!doNotFinalize && context != null && context.getDatabase().getSelector() == DataSource.COTTONTAIL && context.getDatabase().getWriter() == DataSource.COTTONTAIL) {
          dispatcher.registerListener(new ExtractionCompleteListener() {
            @Override
            public void extractionComplete() {
              OptimizeEntitiesCommand.optimizeAllCottontailEntities();
            }
          });
        }
        dispatcher.registerListener((ExtractionCompleteListener) provider);
        dispatcher.start();
        dispatcher.block();
      } else {
        System.err.printf("Could not start handleExtraction with configuration file '%s'. Does the file exist?%n", file);
      }
    } catch (IOException e) {
      System.err.printf("Could not start handleExtraction with configuration file '%s' due to a IO error.%n", file);
      e.printStackTrace();
    } catch (ClassCastException e) {
      System.err.println("Could not register completion listener for extraction.");
    } finally {
      if (postExtractionIIIFCleanup != null) {
        postExtractionIIIFCleanup.run();
      }
    }
  }

  /**
   * Configures an IIIF extraction job by downloading all specified images from the server onto to the filesystem and pointing the {@link ExtractionContainerProvider} to that directory.
   *
   * @param iiifConfig    The IIIF config parsed as an {@link IIIFConfig}
   * @param directoryPath The path where the downloaded IIIF content should be stored
   * @throws IOException Thrown if downloading or writing an image, or its associated information encounters an IOException
   */
  private void prepareIIIFExtractionJob(IIIFConfig iiifConfig, String directoryPath) throws IOException {
    LOGGER.info("Starting IIIF resource download and extraction job");
    final Path jobDirectory = Paths.get(directoryPath);
    if (!Files.exists(jobDirectory)) {
      Files.createDirectories(jobDirectory);
    }
    final String jobDirectoryString = jobDirectory.toString();
    // Process Image API job
    processIIIFImageAPIJob(iiifConfig, jobDirectoryString);
    // Process Presentation API job
    processIIIFPresentationAPIJob(iiifConfig, jobDirectoryString);
    // Process Change Discovery API job
    processIIIFChangeDiscoveryAPIJob(iiifConfig.getOrderedCollectionUrl(), jobDirectoryString);
    if (!iiifConfig.isKeepImagesPostExtraction()) {
      postExtractionIIIFCleanup = () -> {
        try {
          Files.walkFileTree(jobDirectory, new SimpleFileVisitor<>() {
            @Override
            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
              Files.delete(file);
              return FileVisitResult.CONTINUE;
            }

            @Override
            public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
              Files.delete(dir);
              return FileVisitResult.CONTINUE;
            }
          });
          LOGGER.debug("All temporarily downloaded IIIF resources deleted from the filesystem along with their parent directory " + jobDirectoryString);
        } catch (IOException e) {
          LOGGER.error("Could not delete temp IIIF resource directory post extraction.\t" + e.getMessage());
          e.printStackTrace();
        }
      };
    }
  }

  private static void processIIIFChangeDiscoveryAPIJob(String orderedCollectionUrl, String jobDirectoryString) throws IOException {
    if (orderedCollectionUrl != null && !orderedCollectionUrl.isEmpty()) {
      OrderedCollectionFactory collectionFactory = null;
      try {
        collectionFactory = new OrderedCollectionFactory(orderedCollectionUrl);
      } catch (Exception e) {
        LOGGER.error(e.getMessage());
        e.printStackTrace();
      }
      if (collectionFactory != null) {
        String jobIdentifier = UUID.randomUUID().toString();
        String collectionJobDirectoryString = jobDirectoryString + "/ordered_collection_job_" + jobIdentifier;
        Path collectionJobDirectory = Paths.get(collectionJobDirectoryString);
        if (!Files.exists(collectionJobDirectory)) {
          Files.createDirectories(collectionJobDirectory);
        }
        try {
          LOGGER.info("Starting downloading of all manifest images specified in the OrderedCollection at url: " + orderedCollectionUrl);
          collectionFactory.saveAllCreatedImages(collectionJobDirectoryString, "image_" + jobIdentifier + "_");
        } catch (Exception e) {
          LOGGER.error("Error occurred while downloading manifest images specified in the OrderedCollection at url: " + orderedCollectionUrl);
          e.printStackTrace();
        }
      }
    }
  }

  private static void processIIIFPresentationAPIJob(IIIFConfig iiifConfig, String jobDirectoryString) {
    List<String> manifestUrls = iiifConfig.getManifestUrls();
    if (manifestUrls == null) {
      return;
    }
    for (var manifestUrl : manifestUrls) {
      if (manifestUrl != null && !manifestUrl.isEmpty()) {
        try {
          var manifestFactory = new ManifestFactory(manifestUrl);
          var pathSafeManifestUrl = manifestUrl.replaceAll("[^a-zA-Z0-9.\\-]", "_");
          String jobIdentifier = UUID.randomUUID().toString();
          Path manifestJobDirectory = Paths.get(jobDirectoryString, pathSafeManifestUrl + "_" + jobIdentifier);
          if (!Files.exists(manifestJobDirectory)) {
            Files.createDirectories(manifestJobDirectory);
          }
          manifestFactory.saveMetadataJson(manifestJobDirectory.toString(), "metadata_" + jobIdentifier);
          manifestFactory.saveAllCanvasImages(manifestJobDirectory.toString(), "image_" + jobIdentifier);
        } catch (Exception e) {
          LOGGER.error(e.getMessage());
          e.printStackTrace();
        }
      }
    }
  }

  private static void processIIIFImageAPIJob(IIIFConfig iiifConfig, String jobDirectoryString) {
    var items = iiifConfig.getIiifItems();
    if (items == null) {
      return;
    }
    for (var item : items) {
      try {
        ImageFetcher.fetch(item, jobDirectoryString);
      } catch (IOException | UnsupportedIIIFAPIException e) {
        LOGGER.error(e.getMessage());
        e.printStackTrace();
      }
    }
  }
}
