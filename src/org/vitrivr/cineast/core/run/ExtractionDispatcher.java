package org.vitrivr.cineast.core.run;

import java.io.File;
import java.io.IOException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.vitrivr.cineast.core.config.Config;
import org.vitrivr.cineast.core.config.IngestConfig;
import org.vitrivr.cineast.core.run.filehandler.GenericExtractionItemHandler;

/**
 * @author rgasser
 * @version 1.0
 * @created 13.01.17
 */
public class ExtractionDispatcher {

  private static final Logger LOGGER = LogManager.getLogger();

  /**
   * ExtractionContextProvider used to setup the extraction.
   */
  private IngestConfig context;

  /**
   * List of files due for extraction.
   */
  private ExtractionContainerProvider pathProvider;

  /**
   * Reference to the thread that is being used to run the ExtractionFileHandler.
   */
  private Thread fileHandlerThread;

  private ExtractionItemProcessor handler;

  private volatile boolean threadRunning = false;

  public boolean initialize(ExtractionContainerProvider pathProvider,
      IngestConfig context) throws IOException {
    File outputLocation = Config.sharedConfig().getExtractor().getOutputLocation();
    if (outputLocation == null) {
      LOGGER.error("invalid output location specified in config");
      return false;
    }
    outputLocation.mkdirs();
    if (!outputLocation.canWrite()) {
      LOGGER.error("cannot write to specified output location: '{}'",
          outputLocation.getAbsolutePath());
      return false;
    }

    this.pathProvider = pathProvider;
    this.context = context;

    if (this.fileHandlerThread == null) {
      this.handler = new GenericExtractionItemHandler(this.pathProvider, this.context, this.context.sourceType());
      this.fileHandlerThread = new Thread((GenericExtractionItemHandler) handler);
    } else {
      LOGGER.warn("You cannot initialize the current instance of ExtractionDispatcher again!");
    }

    return this.pathProvider.isOpen();
  }

  /**
   * Starts extraction by dispatching a new ExtractionFileHandler thread.
   *
   * @throws IOException If an error occurs during pre-processing of the files.
   */
  public synchronized void start() throws IOException {
    if (fileHandlerThread != null && !threadRunning) {
      this.fileHandlerThread.setName("extraction-file-handler-thread");
      this.fileHandlerThread.start();
      threadRunning = true;
    } else {
      LOGGER.warn("You cannot start the current instance of ExtractionDispatcher again!");
    }
  }

  public void registerListener(ExtractionCompleteListener listener) {
    if (this.fileHandlerThread == null) {
      LOGGER.error("Could not register listener, no thread available");
      throw new RuntimeException();
    }
    LOGGER.debug("Registering Listener {}", listener.getClass().getSimpleName());
    handler.addExtractionCompleteListener(listener);
  }
}
