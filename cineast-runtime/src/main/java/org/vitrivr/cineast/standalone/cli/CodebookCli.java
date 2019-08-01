package org.vitrivr.cineast.standalone.cli;

import com.github.rvesse.airline.annotations.Command;
import com.github.rvesse.airline.annotations.Option;
import org.vitrivr.cineast.core.features.codebook.CodebookGenerator;
import org.vitrivr.cineast.core.util.ReflectionHelper;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * A CLI command that can be used to generate SURF or HOG codebooks based on a set of images.
 *
 * @author Ralph Gasser
 * @version 1.0
 */
@Command(name = "codebook", description = "Generates a codebook of defined size based on a set of images using some specified features.")
public class CodebookCli extends CineastCli {

    @Option(name = { "-n", "--name" }, description = "The fully qualified name of the codebook generator. Supported values are HOGCodebookGenerator and SURFCodebookGenerator.")
    private String name;

    @Option(name = { "-i", "--input" }, description = "The folder containing the source images for codebook generation.")
    private String input;

    @Option(name = { "-o", "--output" }, description = "The generated output file.")
    private String output;

    @Option(name = { "-w", "--words" }, description = "The size of the vocabulary (i.e. the size of the codebook).")
    private int words;

    @Override
    public void run() {
        super.loadConfig();
        final CodebookGenerator generator = ReflectionHelper.newCodebookGenerator(name);
        final Path input = Paths.get(this.input);
        final Path output = Paths.get(this.output);
        if (generator != null) {
            try {
                generator.generate(input, output, this.words);
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            System.err.println(String.format("The specified codebook generator '%s' does not exist.", name));
        }
    }
}
