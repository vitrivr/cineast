package org.vitrivr.cineast.core.run.path;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.vitrivr.cineast.core.run.ExtractionContextProvider;
import org.vitrivr.cineast.core.util.LogHelper;

import java.io.IOException;
import java.nio.file.FileVisitOption;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.Iterator;

/*
 * Recursively add all files under that path to the List of files that should be processed. Uses
 * the context-provider to determine the depth of recursion, skip files and limit the number of
 * files.
 */
public class TreeWalkPathIteratorProvider implements PathIteratorProvider {

    private static final Logger LOGGER = LogManager.getLogger();

    private final Path basePath;
    private final ExtractionContextProvider context;

    public TreeWalkPathIteratorProvider(Path basePath, ExtractionContextProvider context){
        this.basePath = basePath;
        this.context = context;
    }

    @Override
    public Iterator<Path> getPaths() {
        try {
            return Files.walk(this.basePath, this.context.depth(), FileVisitOption.FOLLOW_LINKS).filter(p -> {
                try {
                    return Files.exists(p) && Files.isRegularFile(p) && !Files.isHidden(p)
                            && Files.isReadable(p);
                } catch (IOException e) {
                    LOGGER.error("An IO exception occurred while testing the media file at '{}': {}", p.toString(),
                            LogHelper.getStackTrace(e));
                    return false;
                }
            }).iterator();
        } catch (IOException e) {
            LOGGER.error("An IO exception occurred while scanning '{}': {}", basePath.toString(),
                    LogHelper.getStackTrace(e));
            return Collections.emptyIterator();
        }
    }
}
