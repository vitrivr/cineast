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
     * Determines the path of the input-file or folder.
     *
     * @return Path to the input-file or folder.
     */
    public Path inputPath();

    /**
     * Determines the MediaType of the source material. Only one media-type
     * can be specified per ExtractionContextProvider.
     *
     * @return Media-type of the source material.
     */
    public MediaType sourceType();

    /**
     * Returns a list of named categories for which features should be extracted. These
     * categories must exist and be configured in the config.json of Cineast!
     *
     * @return List of named categories.
     */
    public List<String> getCategories();

    /**
     * Limits the number of files that should be extracted. This a predicate is applied
     * before extraction starts. If extraction fails for some fails the effective number
     * of extracted files may be lower.
     *
     * @return A number greater than zero.
     */
    int limit();

    /**
     *  Limits the depth of recursion when extraction folders of files. Has no
     *  effect if the inputPath points to a file.
     *
     * @return A number greater than zero.
     */
    int depth();
}
