package org.vitrivr.cineast.core.extraction.decode.general;


import org.vitrivr.cineast.core.data.query.containers.QueryContainer;

import java.nio.file.Path;
import java.util.Set;


public interface Converter {
    /**
     * Converts a single file to a QueryContainer.
     *
     * @param path Path the file that should be converted.
     * @return QueryContainer for the specified file.
     */
    QueryContainer convert(Path path);

    /**
     * Returns a set of the mime/types of supported files.
     *
     * @return Set of the mime-type of file formats that are supported by the current Decoder instance.
     */
    Set<String> supportedFiles();
}
