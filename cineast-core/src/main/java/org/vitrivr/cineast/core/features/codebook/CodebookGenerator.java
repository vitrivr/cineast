package org.vitrivr.cineast.core.features.codebook;

import java.io.IOException;
import java.nio.file.Path;


public interface CodebookGenerator {
    /**
     * Generates a Codebook for files in the source folder and writes it to the destination folder.
     *
     * @param source Path pointing to a folder with file from which the codebook should be created.
     * @param destination Output file
     * @param words Number of words in the Codebook.
     *
     * @throws IOException If an error occurs while reading the files.
     */
    void generate(Path source, Path destination, int words) throws IOException;
}
