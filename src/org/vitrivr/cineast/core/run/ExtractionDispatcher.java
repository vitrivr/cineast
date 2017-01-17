package org.vitrivr.cineast.core.run;

import org.vitrivr.cineast.core.config.ImportConfig;
import org.vitrivr.cineast.core.data.MediaType;
import org.vitrivr.cineast.core.run.filehandler.ImageExtractionFileHandler;
import org.vitrivr.cineast.core.run.filehandler.VideoExtractionFileHandler;
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
    /**
     *
     */
    private ImportConfig context;

    /**
     *
     */
    private List<Path> paths;


    /**
     *
     * @param jobFile
     */
    public boolean initialize(File jobFile) throws IOException {
        JacksonJsonProvider reader = new JacksonJsonProvider();
        this.context = reader.toObject(jobFile, ImportConfig.class);

        /* Check if context could be read and an inputpath was specified. */
        if (context == null || this.context.inputPath() == null) return false;
        Path path = this.context.inputPath();

        /* Recursively add all files under that path to the List of files that should be processed. */
        if (!Files.exists(path)) return false;
        this.paths = Files.walk(path, Integer.MAX_VALUE) /* TODO: Make configurable. */
                     .filter(Files::isRegularFile)
                     .filter(p -> {
                         try {
                             return !Files.isHidden(p);
                         } catch (IOException e) {
                             e.printStackTrace();
                             return false;
                         }
                     })
                     .collect(Collectors.toList());
        return true;
    }

    /**
     *
     */
    public void start() {
        MediaType sourceType = this.context.sourceType();

        switch (sourceType) {
            case IMAGE:
                new Thread(new ImageExtractionFileHandler(this.paths, this.context)).start();
                break;
            case VIDEO:
                new Thread(new VideoExtractionFileHandler(this.paths, this.context)).start();
                break;
            default:
                break;
        }
    }
}
