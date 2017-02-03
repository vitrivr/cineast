package org.vitrivr.cineast.core.run;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.vitrivr.cineast.core.config.IngestConfig;
import org.vitrivr.cineast.core.data.MediaType;
import org.vitrivr.cineast.core.run.filehandler.AudioExtractionFileHandler;
import org.vitrivr.cineast.core.run.filehandler.ImageExtractionFileHandler;
import org.vitrivr.cineast.core.run.filehandler.VideoExtractionFileHandler;
import org.vitrivr.cineast.core.util.LogHelper;
import org.vitrivr.cineast.core.util.json.JacksonJsonProvider;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author rgasser
 * @version 1.0
 * @created 13.01.17
 */
public class ExtractionDispatcher {

    private static final Logger LOGGER = LogManager.getLogger();

    /** ExtractionContextProvider used to setup the extraction. */
    private ExtractionContextProvider context;

    /** List of files due for extraction. */
    private List<Path> paths;

    /** Reference to the thread that is being used to run the ExtractionFileHandler. */
    private Thread fileHandlerThread;

    /**
     *
     * @param jobFile
     */
    public boolean initialize(File jobFile) throws IOException {
        JacksonJsonProvider reader = new JacksonJsonProvider();
        this.context = reader.toObject(jobFile, IngestConfig.class);

        /* Check if context could be read and an inputpath was specified. */
        if (context == null || this.context.inputPath() == null) return false;
        Path path = this.context.inputPath();

        /*
         * Recursively add all files under that path to the List of files that should be processed. Uses the context-provider
         * to determine the depth of recursion, skip files and limit the number of files.
         */
        if (!Files.exists(path)) return false;
        this.paths = Files.walk(path, this.context.depth())
                     .skip(this.context.skip())
                     .filter(p -> {
                         try {
                             return Files.exists(p) && Files.isRegularFile(p) && !Files.isHidden(p) && Files.isReadable(p);
                         } catch (IOException e) {
                             LOGGER.error("An IO exception occurred while testing the file under {}.", p.toString(), LogHelper.getStackTrace(e));
                             return false;
                         }
                     })
                     .limit(this.context.limit())
                     .collect(Collectors.toList());
        return true;
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
                    this.fileHandlerThread = new Thread(new ImageExtractionFileHandler(this.paths, this.context));
                    break;
                case VIDEO:
                    this.fileHandlerThread = new Thread(new VideoExtractionFileHandler(this.paths, this.context));
                    break;
                case AUDIO:
                    this.fileHandlerThread = new Thread(new AudioExtractionFileHandler(this.paths, this.context));
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
