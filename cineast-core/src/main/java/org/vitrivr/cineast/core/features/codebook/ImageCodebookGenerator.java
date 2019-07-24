package org.vitrivr.cineast.core.features.codebook;

import boofcv.alg.bow.ClusterVisualWords;
import boofcv.io.UtilIO;
import org.ddogleg.clustering.ComputeClusters;
import org.vitrivr.cineast.core.extraction.decode.general.Decoder;
import org.vitrivr.cineast.core.extraction.decode.image.DefaultImageDecoder;

import javax.activation.MimetypesFileTypeMap;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayDeque;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * Default implementation of a Codebook generator for images. Extend and add the details like
 * the images to use.
 *
 * @author rgasser
 * @version 1.0
 * @created 19.01.17
 */
public abstract class ImageCodebookGenerator implements CodebookGenerator {
    /** K-Means clusterer used for the clustering step in the Codebook generation. */
    protected ComputeClusters<double[]> clusterer;

    /** The visual words cluster. */
    protected ClusterVisualWords cluster;

    /**
     * Default constructor.
     *
     * @param vectorsize Size of the input vectors (that are getting clustered).
     * @param verbose true if the clusterer should print output about its progress.
     */
    public ImageCodebookGenerator(int vectorsize, boolean verbose) {
        this.init();
        this.clusterer.setVerbose(verbose);
        this.cluster = new ClusterVisualWords(clusterer, vectorsize,0xA1CF3B12);
    }

    /**
     * @param source
     * @param destination
     * @param words
     */
    @Override
    public void generate(Path source, Path destination, int words) throws IOException {
        long start = System.currentTimeMillis();
        final Decoder<BufferedImage> decoder = new DefaultImageDecoder();
        final MimetypesFileTypeMap filetypemap = new MimetypesFileTypeMap("mime.types");

        /* Filter the list of files and aggregate it. */
        List<Path> paths = Files.walk(source)
            .filter(path -> {
                if (decoder.supportedFiles() != null) {
                    String type = filetypemap.getContentType(path.toString());
                    return decoder.supportedFiles().contains(type);
                } else {
                    return true;
                }
            }).collect(Collectors.toList());


        /* Prepare array dequeue. */
        ArrayDeque<Path> files = new ArrayDeque<>(paths);

        /* Prepare data-structures to track progress. */
        int max = files.size();
        int counter = 0;
        int skipped = 0;
        char[] progressBar = new char[15];
        int update = max/progressBar.length;

        /* */
        System.out.println(String.format("Creating codebook of %d words from %d files.", words, files.size()));

        /*
         * Iterates over the files Dequeue. Every element that has been processed in removed from
         * that Dequeue.
         */
        Path path = null;
        while ((path = files.poll()) != null) {
            if (decoder.init(path, null)) {
                BufferedImage image = decoder.getNext();
                if (image != null) {
                    this.process(image);
                } else {
                    skipped++;
                }
            } else {
                skipped++;
            }
            if (counter % update == 0) {
              this.updateProgressBar(progressBar, max, counter);
            }
            System.out.print(String.format("\rAdding vectors to codebook: %d/%d files processed (%d skipped) |%s| (Memory left: %.2f/%.2f GB)", counter,max,skipped, String.valueOf(progressBar), Runtime.getRuntime().freeMemory()/1000000.0f, Runtime.getRuntime().totalMemory()/1000000.0f));
            counter++;
        }

        /* Dispose of unnecessary elements. */
        files = null;
        progressBar = null;

        /* Start clustering.*/
        System.out.println(String.format("\nClustering... this could take a while."));
        this.cluster.process(words);

        /* Save file...*/
        System.out.println(String.format("Saving vocabulary with %d entries.", words));
        UtilIO.save(this.cluster.getAssignment(), destination.toString());

        long duration = System.currentTimeMillis()-start;
        System.out.println(String.format("Done! Took me %dhours %dmin %dsec", TimeUnit.MILLISECONDS.toHours(duration), TimeUnit.MILLISECONDS.toMinutes(duration), TimeUnit.MILLISECONDS.toSeconds(duration)));

    }

    /**
     * Updates the char-array of the progress-bar.
     */
    private void updateProgressBar(char[] progressBar, int max, int counter) {
        int progress = (int)(((float)counter/(float)max)*progressBar.length - 1);
        for (int i=0;i<progressBar.length;i++) {
            if (i < progress) {
                progressBar[i] = '=';
            } else if (i == progress) {
                progressBar[i] = '>';
            } else {
                progressBar[i] = ' ';
            }
        }
    }

    /**
     * Processes the content (i.e. creates descriptors) and add the generated
     * descriptors to the cluster.
     *
     * @param content The image to process.
     */
    protected abstract void process(BufferedImage content);

    /**
     * Initializes the codebook generator (i.e. setup the clusterer etc.)
     */
    protected abstract void init();
}
