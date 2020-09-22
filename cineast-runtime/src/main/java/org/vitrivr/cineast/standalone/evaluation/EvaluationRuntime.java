package org.vitrivr.cineast.standalone.evaluation;

import org.apache.commons.lang3.tuple.ImmutableTriple;
import org.apache.commons.lang3.tuple.Triple;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.vitrivr.cineast.core.config.DatabaseConfig;
import org.vitrivr.cineast.core.config.ReadableQueryConfig;
import org.vitrivr.cineast.core.data.entities.MediaObjectDescriptor;
import org.vitrivr.cineast.core.data.entities.MediaSegmentDescriptor;
import org.vitrivr.cineast.core.data.query.containers.QueryContainer;
import org.vitrivr.cineast.core.data.score.SegmentScoreElement;
import org.vitrivr.cineast.core.db.dao.reader.MediaObjectReader;
import org.vitrivr.cineast.core.db.dao.reader.MediaSegmentReader;
import org.vitrivr.cineast.core.extraction.decode.general.Converter;
import org.vitrivr.cineast.core.util.LogHelper;
import org.vitrivr.cineast.core.util.json.JacksonJsonProvider;
import org.vitrivr.cineast.core.util.json.JsonReader;
import org.vitrivr.cineast.standalone.config.Config;
import org.vitrivr.cineast.standalone.util.ContinuousRetrievalLogic;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.*;
import java.util.concurrent.Callable;

/**
 * @author rgasser
 * @version 1.0
 * @created 05.05.17
 */
public class
EvaluationRuntime implements Callable {

    private static final Logger LOGGER = LogManager.getLogger();

    /** MediaSegmentReader instance used to read segments from the storage layer. */
    private final MediaSegmentReader mediaSegmentReader;

    /** MediaObjectReader instance used to read multimedia objects from the storage layer. */
    private final MediaObjectReader mediaObjectReader;

    /** Instance of EvaluationConfig that is used with this runtime. */
    private final EvaluationConfig config;

    /** Caching structure used to cache MultimediaObjectDescriptors. */
    private final HashMap<String, MediaObjectDescriptor> cache;

    /** Number of files that were processed successfully. */
    private int processed = 0;

    /** Number of files that were skipped deliberately. */
    private int skipped = 0;

    /** Number of files that were skipped due to processing errors. */
    private int error = 0;

    private final ContinuousRetrievalLogic retrievalLogic;


    public EvaluationRuntime(Path configPath, DatabaseConfig dbConfig) {
        JsonReader reader = new JacksonJsonProvider();
        this.config = reader.toObject(configPath.toFile(), EvaluationConfig.class);
        this.cache = new HashMap<>(this.config.getSize());
        this.mediaSegmentReader = new MediaSegmentReader(dbConfig.getSelectorSupplier().get());
        this.mediaObjectReader = new MediaObjectReader(dbConfig.getSelectorSupplier().get());
        this.retrievalLogic = new ContinuousRetrievalLogic(dbConfig);
    }

    /**
     * Executes the evaluation and returns a Triple that contains the number of files that were processed,
     * skipped due to errors and skipped deliberately. The actual evaluation results are written to files.
     *
     * @see EvaluationConfig
     *
     * @return computed result
     * @throws EvaluationException if unable to compute a result
     */
    @Override
    public Triple<Integer,Integer,Integer> call() throws EvaluationException, IOException {
        /* Tries to instantiate the converter. */
        final Converter converter = this.config.getConverter();
        if (converter == null) {
            throw new EvaluationException("Failed to instantiate the converter class.");
        }

        /* Instantiates the groundtruth and checks if it contains classes. */
        final Groundtruth gt = this.config.getGroundtruth();
        if (gt.numberOfClasses() == 0) {
            throw new EvaluationException(String.format("The specified ground truth '%s' does not contain any classes.", this.config.getClassfile()));
        }

        /* Updates the retrieval configuration. */
        Config.sharedConfig().getRetriever().setMaxResults(this.config.getSize());
        Config.sharedConfig().getRetriever().setResultsPerModule(this.config.getSize());

        /* Prepares the iterator for the test files. */
        final Iterator<Path> testfilesIterator;
        try {
            testfilesIterator = Files.walk(this.config.getTestfiles()).filter(p -> {
                try {
                    return Files.exists(p) && Files.isRegularFile(p) && !Files.isHidden(p) && Files.isReadable(p);
                } catch (IOException e) {
                    LOGGER.error("An IO exception occurred while testing the media file at '{}'.", p.toString(), LogHelper.getStackTrace(e));
                    return false;
                }
            }).iterator();
        } catch (IOException exception) {
            throw new EvaluationException(String.format("Could not obtain test files under the specified path '%s'.", this.config.getTestfiles()));
        }

        /* Prepare folder structure per category. */
        for (String category : this.config.getCategories()) {
            Files.createDirectories(this.config.getResults().resolve(category));
        }

        /* Prepare a placeholder query-config. */
        final ReadableQueryConfig queryConfig = new ReadableQueryConfig(null);

        /* Prepare a random number generator that decides if a file should be used for evaluation or not. */
        final Random random = new Random();

        /* Perform evaluation for every file. */
        Path path;
        while(testfilesIterator.hasNext()) {
            path = testfilesIterator.next();
            if (random.nextBoolean() && config.getMode() == EvaluationConfig.EvaluationMode.RANDOM) {
                LOGGER.info("Randomly skipping file {}.", path);
                this.skipped += 1;
                continue;
            }

            /* Try to create a QueryContainer. If this fails, the file is skipped. */
            final QueryContainer container = converter.convert(path);
            if (container == null) {
                LOGGER.warn("Failed to convert the file {}. File is being skipped...", path.getFileName());
                this.error += 1;
                continue;
            }

            LOGGER.info("Starting evaluation for {}", path);
            for (String category : this.config.getCategories()) {
                List<SegmentScoreElement> scores = this.retrievalLogic.retrieve(container, category, queryConfig);
                EvaluationResult result = this.performEvaluation(scores, path, gt);
                this.writeToFile(category, result);
            }

            this.processed += 1;
        }

        return new ImmutableTriple<>(this.processed, this.error, this.skipped);
    }


    /**
     * Writes the results for a feature category to a CSV file. If a file with that name already exists, the
     * existing file is overwritten!
     *
     * @param category The feature category of the file.
     * @param result The evaluation results.
     * @throws IOException If writing of file fails.
     */
    private void writeToFile(String category, EvaluationResult result) throws IOException {
        BufferedWriter writer = Files.newBufferedWriter(this.config.getResults().resolve(category).resolve(result.getDocId() + "-" + result.getCl() + "-results.csv"), StandardOpenOption.CREATE, StandardOpenOption.WRITE, StandardOpenOption.TRUNCATE_EXISTING);
        writer.append(result.toString(this.config.getDelimiter()));
        writer.newLine();
        writer.close();
    }

    /**
     * Performs the actual evaluation for a result set and reference document. Now iterate through the list of retrieved documents and
     * decides for each entry if its class is equal to the class of the reference document (i.e. if it is relevant or not). Based on
     * the outcome, the EvaluationResult is updated.
     *
     * @param scores The result set - list of SegmentScoreElements.
     * @param path Path to the reference document file. Its filename is used as docID
     * @param gt Ground truth object used for evaluation.
     * @return EvaluationResult
     *
     * @throws EvaluationException If something goes wrong during evaluation, e.g. reference document has no class or a item in the result set has no class
     */
    private EvaluationResult performEvaluation(List<SegmentScoreElement> scores, Path path, Groundtruth gt) throws EvaluationException {
        /* Constructs a document ID from the filename and fetches the file's class from the ground truth. */
        String docID = path.getFileName().toString();
        String fileClass = gt.classForDocId(docID).orElseThrow(() -> new EvaluationException(String.format("The provided test file %s does not have a class associated with it.",path.getFileName())));

        /* Prepare empty evaluation results. */
        EvaluationResult result = new EvaluationResult(docID, gt);

        /*
         * Now iterate through the list of retrieved documents and decide for each entry if it relevant according to its class.
         */
        for (int k=1; k<=scores.size(); k++) {
            SegmentScoreElement score = scores.get(k-1);
            MediaObjectDescriptor object = this.objectDescriptorForId(score.getSegmentId());
            if (object != null) {
                if (gt.classForDocId(object.getName()).orElse("<none>").equals(fileClass)) {
                    result.documentAvailable(object.getName(), k, true);
                } else {
                    result.documentAvailable(object.getName(), k, false);
                }
                if (result.done()) {
                    LOGGER.info("All relevant objects were retrieved. Starting next round...", score.getSegmentId());
                    break;
                }
            } else {
                throw new EvaluationException(String.format("The provided test file %s does not have a class associated with it.", score.getSegmentId()));
            }
        }

        return result;
    }

    /**
     * Returns a MediaObjectDescriptor for the provided docID. This method uses a cache to speedup
     * lookup of objects.
     *
     * @param docID ID of the segment for which the MediaObjectDescriptor is required.
     * @return MediaObjectDescriptor
     */
    private MediaObjectDescriptor objectDescriptorForId(String docID) {
        if (this.cache.containsKey(docID)) {
          return this.cache.get(docID);
        }
        Optional<MediaSegmentDescriptor> descriptor = this.mediaSegmentReader.lookUpSegment(docID);
        if (descriptor.isPresent()) {
            MediaObjectDescriptor object = this.mediaObjectReader.lookUpObjectById(descriptor.get().getObjectId());
            this.cache.put(docID, object);
            return object;
        } else {
            return null;
        }
    }
}



