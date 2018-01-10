package org.vitrivr.cineast.core.run;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.vitrivr.cineast.core.config.Config;
import org.vitrivr.cineast.core.config.IngestConfig;
import org.vitrivr.cineast.core.data.MediaType;
import org.vitrivr.cineast.core.run.filehandler.AudioExtractionFileHandler;
import org.vitrivr.cineast.core.run.filehandler.ImageExtractionFileHandler;
import org.vitrivr.cineast.core.run.filehandler.Model3DExtractionFileHandler;
import org.vitrivr.cineast.core.run.filehandler.VideoExtractionFileHandler;
import org.vitrivr.cineast.core.run.path.PathIteratorProvider;
import org.vitrivr.cineast.core.run.path.TreeWalkPathIteratorProvider;
import org.vitrivr.cineast.core.util.json.JacksonJsonProvider;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Iterator;

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
    private Iterator<Path> paths;

    /**
     * Reference to the thread that is being used to run the ExtractionFileHandler.
     */
    private Thread fileHandlerThread;

    /**
     * @param jobFile
     */
    public boolean initialize(File jobFile) throws IOException {
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

        JacksonJsonProvider reader = new JacksonJsonProvider();
        this.context = reader.toObject(jobFile, IngestConfig.class);

        /* Check if context could be read and an input path was specified. */
        if (context == null || this.context.inputPath() == null) {
            return false;
        }
        Path jobDirectory = jobFile.getAbsoluteFile().toPath().getParent();
        Path path = jobDirectory.resolve(this.context.inputPath()).normalize().toAbsolutePath();


        if (!Files.exists(path)) {
            LOGGER.warn("The path '{}' specified in the extraction configuration does not exist!",
                    path.toString());
            return false;
        }

        //TODO make configurable
        PathIteratorProvider pip = new TreeWalkPathIteratorProvider(path, context);
        this.paths = pip.getPaths();


        return this.paths.hasNext();
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
                            new ImageExtractionFileHandler(this.paths, this.context));
                    break;
                case VIDEO:
                    this.fileHandlerThread = new Thread(
                            new VideoExtractionFileHandler(this.paths, this.context));
                    break;
                case AUDIO:
                    this.fileHandlerThread = new Thread(
                            new AudioExtractionFileHandler(this.paths, this.context));
                    break;
                case MODEL3D:
                    this.fileHandlerThread = new Thread(
                            new Model3DExtractionFileHandler(this.paths, this.context));
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
