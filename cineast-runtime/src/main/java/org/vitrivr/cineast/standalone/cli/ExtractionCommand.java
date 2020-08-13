package org.vitrivr.cineast.standalone.cli;

import com.github.rvesse.airline.annotations.Command;
import com.github.rvesse.airline.annotations.Option;
import java.io.File;
import java.io.IOException;

import org.vitrivr.cineast.core.config.DatabaseConfig;
import org.vitrivr.cineast.core.util.json.JacksonJsonProvider;
import org.vitrivr.cineast.standalone.config.IngestConfig;
import org.vitrivr.cineast.standalone.run.ExtractionCompleteListener;
import org.vitrivr.cineast.standalone.run.ExtractionContainerProvider;
import org.vitrivr.cineast.standalone.run.ExtractionDispatcher;
import org.vitrivr.cineast.standalone.run.path.ExtractionContainerProviderFactory;

/**
 * A CLI command that can be used to start a media extraction based on an extraction definition file.
 *
 * @author Ralph Gasser
 * @version 1.0
 */
@Command(name = "extract", description = "Starts a media extracting using the specified settings.")
public class ExtractionCommand implements Runnable {

  @Option(name = {"-e", "--extraction"}, title = "Extraction config", description = "Path that points to a valid Cineast extraction config file.")
  private String extractionConfig;

  @Option(name = {"--no-finalize"}, title = "Do Not Finalize", description = "If this flag is not set, automatically rebuilds indices & optimizes all entities when writing to cottontail after the extraction. Set this flag when you want more performance with external parallelism.")
  private boolean doNotFinalize = false;


  @Override
  public void run() {
    final ExtractionDispatcher dispatcher = new ExtractionDispatcher();
    /* Prevent NullPointerException crash when extraction config isn't provided */
    if (this.extractionConfig == null) {
      System.err.println("No extraction config argument provided!");
      return;
    }
    final File file = new File(this.extractionConfig);
    if (file.exists()) {
      try {
        final JacksonJsonProvider reader = new JacksonJsonProvider();
        final IngestConfig context = reader.toObject(file, IngestConfig.class);
        final ExtractionContainerProvider provider = ExtractionContainerProviderFactory.tryCreatingTreeWalkPathProvider(file, context);
        if (dispatcher.initialize(provider, context)) {
          /* Only attempt to optimize Cottontail entities if we were extracting into Cottontail, otherwise an unavoidable error message would be displayed when extracting elsewhere. */
          if (!doNotFinalize && context != null && context.getDatabase().getSelector() == DatabaseConfig.Selector.COTTONTAIL && context.getDatabase().getWriter() == DatabaseConfig.Writer.COTTONTAIL) {
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
          System.err.printf("Could not start handleExtraction with configuration file '%s'. Does the file exist?%n", file.toString());
        }
      } catch (IOException e) {
        System.err.printf("Could not start handleExtraction with configuration file '%s' due to a IO error.%n", file.toString());
        e.printStackTrace();
      } catch (ClassCastException e) {
        System.err.println("Could not register completion listener for extraction.");
      }
    } else {
      System.err.printf("Could not start handleExtraction with configuration file '%s'; the file does not exist!%n", file.toString());
    }
  }
}
