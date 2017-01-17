package org.vitrivr.cineast.core.run;

import org.vitrivr.cineast.core.data.MediaType;

import java.io.File;
import java.nio.file.Path;
import java.util.List;

/**
 * @author rgasser
 * @version 1.0
 * @created 13.01.17
 */
public interface ExtractionContextProvider {

    /**
     *
     * @return
     */
    public Path inputPath();

    /**
     *
     */
    public MediaType sourceType();

    /**
     *
     */
    public List<String> getCategories();
}
