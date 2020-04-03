package org.vitrivr.cineast.standalone.importer.lsc2020;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.List;

public class LSCUtilities {

    private LSCUtilities(){}

    /**
     * Writes the list as individual lines in a textfile.
     * The file must already be created
     * @param directory The directory the file resides in
     * @param file The file to write into
     * @param lines The lines to write
     * @throws IOException If something goes wrong. Possibly due to the file not being created beforehand
     */
    public static void writeLines(Path directory, String file, List<String> lines) throws IOException {
        Path p = directory.resolve(file);
        Files.write(p, lines, StandardOpenOption.TRUNCATE_EXISTING);
    }

    /**
     * Converts an imagepath to a corresponding segmentId.
     * <br>
     * In the LSC2020 dataset, image paths (from file lsc2020-visual-concepts.csv)
     * do contain more than the actual (image) file, hence some conversion is required
     * <br>
     * Prepends <code>is_</code>, removes anything before a slash ("<code>/</code>"), if present, and after a dot ("<code>.</code>") (i.e., file extension), if present
     * @param path
     * @return
     */
    public static String pathToSegmentId(String path) {
        int beginIdx = path.contains("/") ? path.indexOf("/")+1 : 0;
        int endIdx = path.contains(".") ? path.lastIndexOf(".") : path.length();
        return "is_" + path.substring(beginIdx, endIdx);
    }
}
