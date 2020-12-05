package org.vitrivr.cineast.core.features.abstracts;

import static org.vitrivr.cineast.core.util.CineastConstants.FEATURE_COLUMN_QUALIFIER;
import static org.vitrivr.cineast.core.util.CineastConstants.GENERIC_ID_COLUMN_QUALIFIER;

import org.apache.logging.log4j.LogManager;
import org.vitrivr.cineast.core.benchmark.BenchmarkManager;
import org.vitrivr.cineast.core.benchmark.engine.BenchmarkEngine;
import org.vitrivr.cineast.core.benchmark.model.Benchmark;
import org.vitrivr.cineast.core.config.QueryConfig;
import org.vitrivr.cineast.core.config.ReadableQueryConfig;
import org.vitrivr.cineast.core.data.distance.SegmentDistanceElement;
import org.vitrivr.cineast.core.data.providers.primitive.StringTypeProvider;
import org.vitrivr.cineast.core.data.score.ScoreElement;
import org.vitrivr.cineast.core.data.segments.SegmentContainer;

import java.util.ArrayList;
import java.util.List;

/**
 * This implementation of the AbstractFeatureModule executes every query, either based on a SegmentContainer
 * or on an existing segment, in three stages. This for in-depth analysis and benchmarking of the different
 * stages as well as a unified approach to similarity search.
 *
 * When implementing this class, you are expected to override the methods that represent the different stages
 * instead of implementing the getSimilar() methods.
 *
 * @author rgasser
 * @version 1.0
 * @created 25.04.17
 */
public abstract class StagedFeatureModule extends AbstractFeatureModule {

    protected static final org.apache.logging.log4j.Logger LOGGER = LogManager.getLogger();

    /**
     * Split markers used for benchmarking.
     */
    private final static String BENCHMARK_SPLITNAME_PREPROCESSING = "PREPROCESSING";
    private final static String BENCHMARK_SPLITNAME_LOOKUP = "LOOKUP";
    private final static String BENCHMARK_SPLITNAME_SIMILARITY = "SIMILARITY";
    private final static String BENCHMARK_SPLITNAME_POSTPROCESSING = "POSTPROCESSING";

    /** Instance of the BenchmarkEngine that is used to benchmark queries. */
    private final BenchmarkEngine benchmark_engine = BenchmarkManager.getInstance().getDefaultEngine();

    /**
     * Constructor
     *
     * @param tableName Name of the entity / table to persist data with and read data from.
     * @param maxDist Maximum distance value (for normalization).
     * @param vectorLength Dimensionality of the feature vector.
     */
    protected StagedFeatureModule(String tableName, float maxDist, int vectorLength) {
        super(tableName, maxDist, vectorLength);
    }

    /**
     * This method executes a regular similarity query based on a provided SegmentContainer. The query
     * is executed in three stages (hence the name of the class):
     *
     * <ol>
     *     <li>Pre-processing: Extracting features from the SegmentContainer.</li>
     *     <li>Similarity search: Performing the similarity query in the underlying storage engine.</li>
     *     <li>Post-processing: Aggregating the query results into the final Score elements.</li>
     * </ol>
     *
     * Every step is captured by a benchmarking object, allowing for in-depth analysis of the query process.
     *
     * Even though it is possible to re-implement this method, it is not recommended. Instead, try to override
     * the methods that represent the different stages.
     *
     * @param sc SegmentContainer to base the query on.
     * @param qc QueryConfiguration
     * @return List of results
     */
    @Override
    public List<ScoreElement> getSimilar(SegmentContainer sc, ReadableQueryConfig qc) {
        /* Initialize new Benchmark object. */
        Benchmark benchmark = benchmark_engine.startNew(this.getClass().getSimpleName() + " (" + qc.getQueryId().toString() + ")");

        /* Start query pre-processing phase. */
        benchmark.split(BENCHMARK_SPLITNAME_PREPROCESSING);

        /* Load default query-config. */
        QueryConfig qcc = this.defaultQueryConfig(qc);

        /* Extract features. */
        List<float[]> features = this.preprocessQuery(sc, qcc);

        if (features == null || features.isEmpty()) {
            LOGGER.warn("No features could be generated from the provided query. Aborting query execution...");
            benchmark.abort();
            return new ArrayList<>(0);
        }

        /* Generates a list of QueryConfigs for the feature. */
        List<ReadableQueryConfig> configs = this.generateQueryConfigsForFeatures(qcc, features);

        /* Start query lookup phase. */
        benchmark.split(BENCHMARK_SPLITNAME_SIMILARITY);
        List<SegmentDistanceElement> partialResults = this.lookup(features, configs);

        /* Start query-results post-processing phase. */
        benchmark.split(BENCHMARK_SPLITNAME_POSTPROCESSING);
        List<ScoreElement> results = this.postprocessQuery(partialResults, qcc);

        /* End the benchmark and return the results. */
        benchmark.end();
        return results;
    }

    /**
     * This method executes a similarity query based on an existing segment. The query is executed in three stages
     * (hence the name of the class):
     *
     * <ol>
     *     <li>Lookup: Retrieving the features associated with the provided segment ID.</li>
     *     <li>Similarity search: Performing the similarity query in the underlying storage engine.</li>
     *     <li>Post-processing: Aggregating the query results into the final Score elements.</li>
     * </ol>
     *
     * Every step is captured by a benchmarking object, allowing for in-depth analysis of the query process.
     *
     * Even though it is possible to re-implement this method, it is not recommended. Instead, try to override
     * the methods that represent the different stages.
     *
     * @param segmentId ID of the segment that is used as example.
     * @param qc QueryConfiguration
     *
     * @return List of results
     */
    @Override
    public List<ScoreElement> getSimilar(String segmentId, ReadableQueryConfig qc) {
        /* Initialize new Benchmark object. */
        Benchmark benchmark = benchmark_engine.startNew(this.getClass().getSimpleName() + " (" + qc.getQueryId().toString() + ")");

        /* Start query pre-processing phase. */
        benchmark.split(BENCHMARK_SPLITNAME_LOOKUP);

        /* Load default query-config. */
        QueryConfig qcc = this.defaultQueryConfig(qc);

        /* Lookup features. */
        List<float[]> features = this.selector.getFeatureVectors(GENERIC_ID_COLUMN_QUALIFIER,  new StringTypeProvider(segmentId), FEATURE_COLUMN_QUALIFIER);
        if (features.isEmpty()) {
            LOGGER.warn("No features could be fetched for the provided segmentId '{}'. Aborting query execution...", segmentId);
            benchmark.end();
            return new ArrayList<>(0);
        }

        /* Generate a list of QueryConfigs for the feature. */
        List<ReadableQueryConfig> configs = this.generateQueryConfigsForFeatures(qcc, features);

        /* Start query lookup phase. */
        benchmark.split(BENCHMARK_SPLITNAME_SIMILARITY);
        List<SegmentDistanceElement> partialResults = this.lookup(features, configs);

        /* Start query-results post-processing phase. */
        benchmark.split(BENCHMARK_SPLITNAME_POSTPROCESSING);
        List<ScoreElement> results = this.postprocessQuery(partialResults, qcc);

        /* End the benchmark and return the results. */
        benchmark.end();
        return results;
    }

    /**
     * This method represents the first step that's executed when processing query. The associated SegmentContainer is
     * examined and feature-vectors are being generated. The generated vectors are returned by this method together with an
     * optional weight-vector.
     *
     * <strong>Important: </strong> The weight-vector must have the same size as the feature-vectors returned by the method.
     *
     * @param sc SegmentContainer that was submitted to the feature module
     * @param qc A QueryConfig object that contains query-related configuration parameters. Can still be edited.
     * @return A pair containing a List of features and an optional weight vector.
     */
    protected abstract List<float[]> preprocessQuery(SegmentContainer sc, ReadableQueryConfig qc);

    /**
     * This method represents the lookup step that's executed when processing a query. Lookup is based on the feature vectors
     * returned by the first stage and a lookup is executed for each. Partial-results are accumulated in a list, which is returned
     * by the method at the end.
     *
     * <strong>Important: </strong> The weight-vector must have the same size as the feature-vectors returned by the method.
     *
     * @param features A list of feature-vectors (usually generated in the first stage). For each feature, a lookup is executed. May be empty!
     * @param configs A ReadableQueryConfig object that contains query-related configuration parameters.
     * @return Unfiltered list of partial results. May exceed the number of results a module is supposed to return and entries may occur multiple times.
     */
    protected List<SegmentDistanceElement> lookup(List<float[]> features, List<ReadableQueryConfig> configs) {
        List<SegmentDistanceElement> partialResults;
        if (features.size() == 1) {
            partialResults = this.selector.getNearestNeighboursGeneric(configs.get(0).getResultsPerModule(), features.get(0), FEATURE_COLUMN_QUALIFIER, SegmentDistanceElement.class, configs.get(0));
        } else if (features.size() > 1) {
            partialResults = this.selector.getBatchedNearestNeighbours(configs.get(0).getResultsPerModule(), features, FEATURE_COLUMN_QUALIFIER, SegmentDistanceElement.class, configs);
        } else {
            partialResults = new ArrayList<>(0);
        }
        return partialResults;
    }

    /**
     * This method represents the last step that's executed when processing a query. A list of partial-results (DistanceElements) returned by
     * the lookup stage is processed based on some internal method and finally converted to a list of ScoreElements. The filtered list of
     * ScoreElements is returned by the feature module during retrieval.
     *
     * @param partialResults List of partial results returned by the lookup stage.
     * @return List of final results. Is supposed to be de-duplicated and the number of items should not exceed the number of items per module.
     */
    protected abstract List<ScoreElement> postprocessQuery(List<SegmentDistanceElement> partialResults, ReadableQueryConfig qcc);

    /**
     * Returns a list of QueryConfigs for the given list of features. By default, this method simply returns a list of the
     * same the provided config. However, this method can be re-implemented to e.g. add a static or dynamic weight vectors.
     *
     * @param qc Original query config
     * @param features List of features for which a QueryConfig is required.
     * @return New query config (may be identical to the original one).
     */
    protected List<ReadableQueryConfig> generateQueryConfigsForFeatures(ReadableQueryConfig qc, List<float[]> features) {
        List<ReadableQueryConfig> configs = new ArrayList<>(features.size());
        for (int i=0;i<features.size();i++) {
            configs.add(qc);
        }
        return configs;
    }

    /**
     * Merges the provided QueryConfig with the default QueryConfig enforced by the
     * feature module.
     *
     * @param qc QueryConfig provided by the caller of the feature module.
     * @return Modified QueryConfig.
     */
    protected QueryConfig defaultQueryConfig(ReadableQueryConfig qc) {
        return new QueryConfig(qc)
                .setCorrespondenceFunctionIfEmpty(this.correspondence)
                .setDistanceIfEmpty(QueryConfig.Distance.euclidean);
    }
}
