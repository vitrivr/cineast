package org.vitrivr.cineast.core.metadata;

import java.io.IOException;
import java.nio.file.Path;

import java.util.Map;
import java.util.Set;

/**
 * @author rgasser
 * @version 1.0
 * @created 19.01.17
 */
public interface MetadataExtractor {
    /**
     * Extracts the metadata from the specified path and returns a HashMap that maps the extracted
     * fieldnames to the respective value.
     *
     * @param path Path to the file for which metadata should be extracted.
     * @return HashMap with metadata. May be empty.
     * @throws IOException If an IO error occurs during meta-data extraction.
     */
    Map<String,String> extract(Path path) throws IOException;

    /**
     * Returns a set of the mime/types of supported files.
     *
     * @return Set of the mime-type of file formats that are supported by the current Decoder instance.
     */
    Set<String> supportedFiles();
}
