package org.vitrivr.cineast.core.run;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.vitrivr.cineast.core.config.Config;
import org.vitrivr.cineast.core.data.MediaType;
import org.vitrivr.cineast.core.run.filehandler.AudioExtractionFileHandler;
import org.vitrivr.cineast.core.run.filehandler.ImageExtractionFileHandler;
import org.vitrivr.cineast.core.run.filehandler.Model3DExtractionFileHandler;
import org.vitrivr.cineast.core.run.filehandler.VideoExtractionFileHandler;

import java.io.File;
import java.io.IOException;

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
    private ExtractionContextProvider context;

    /**
     * List of files due for extraction.
     */
    private ExtractionPathProvider pathProvider;

    /**
     * Reference to the thread that is being used to run the ExtractionFileHandler.
     */
    private Thread fileHandlerThread;

    public boolean initialize(ExtractionPathProvider pathProvider, ExtractionContextProvider context) throws IOException {
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

        return this.pathProvider.isOpen();
    }

    /**
     * Starts extraction by dispatching a new ExtractionFileHandler thread.
     *
     * @throws IOException If an error occurs during pre-processing of the files.
     */
    public void start() throws IOException {
        if (this.fileHandlerThread == null) {
            MediaType sourceType = this.context.sourceType();
            switch (sourceType) {
                case IMAGE:
                    this.fileHandlerThread = new Thread(
                        new ImageExtractionFileHandler(this.pathProvider, this.context));
                    break;
                case VIDEO:
                    this.fileHandlerThread = new Thread(
                        new VideoExtractionFileHandler(this.pathProvider, this.context));
                    break;
                case AUDIO:
                    this.fileHandlerThread = new Thread(
                        new AudioExtractionFileHandler(this.pathProvider, this.context));
                    break;
                case MODEL3D:
                    this.fileHandlerThread = new Thread(
                        new Model3DExtractionFileHandler(this.pathProvider, this.context));
                    break;
                default:
                    break;
            }
            if (fileHandlerThread != null) {
                this.fileHandlerThread.setName("extraction-file-handler-thread");
                this.fileHandlerThread.start();
            }
        } else {
            LOGGER.warn("You cannot start the current instance of ExtractionDispatcher again!");
        }
    }
}
