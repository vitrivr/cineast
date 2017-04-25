package org.vitrivr.cineast.core.features.abstracts;

import org.vitrivr.cineast.core.benchmark.Benchmark;
import org.vitrivr.cineast.core.benchmark.BenchmarkEngine;
import org.vitrivr.cineast.core.benchmark.BenchmarkManager;
import org.vitrivr.cineast.core.config.Config;
import org.vitrivr.cineast.core.config.QueryConfig;
import org.vitrivr.cineast.core.config.ReadableQueryConfig;
import org.vitrivr.cineast.core.data.distance.DistanceElement;
import org.vitrivr.cineast.core.data.distance.SegmentDistanceElement;
import org.vitrivr.cineast.core.data.score.ScoreElement;
import org.vitrivr.cineast.core.data.segments.SegmentContainer;

import java.util.ArrayList;
import java.util.List;

/**
 * @author rgasser
 * @version 1.0
 * @created 25.04.17
 */
public abstract class StagedFeatureModule extends AbstractFeatureModule {


    private final static BenchmarkEngine BENCHMARK_ENGINE = BenchmarkManager.getDefaultEngine();


    private final static String BENCHMARK_SPLITNAME_PREPROCESSING = "PREPROCESSING";
    private final static String BENCHMARK_SPLITNAME_LOOKUP = "LOOKUP";
    private final static String BENCHMARK_SPLITNAME_POSTPROCESSING = "POSTPROCESSING";

    /** The maximum number of lookups that should be executed by the feature module. Is not bound by default (i.e. = -1)! */
    protected int maxLookups = -1;

    /**
     *
     * @param tableName
     * @param maxDist
     */
    protected StagedFeatureModule(String tableName, float maxDist) {
        super(tableName, maxDist);
    }

    /**
     *
     * @param sc
     * @param qc
     * @return
     */
    public List<ScoreElement> getSimilar(SegmentContainer sc, ReadableQueryConfig qc) {
        /* Initialize new Benchmark object. */
        Benchmark benchmark = BENCHMARK_ENGINE.startNew(this.getClass());

        /* Adjust query-config. */
        QueryConfig qcc = this.defaultQueryConfig(qc);

        /* Start query pre-processing phase. */
        benchmark.split(BENCHMARK_SPLITNAME_PREPROCESSING);
        List<float[]> features = this.preprocessQuery(sc, qcc);

        /* Start query lookup phase. */
        benchmark.split(BENCHMARK_SPLITNAME_LOOKUP);
        List<DistanceElement> partialResults = this.lookup(features, qcc);

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
     * @param qc A ReadableQueryConfig object that contains query-related configuration parameters.
     * @return Unfiltered list of partial results. May exceed the number of results a module is supposed to return and entries may occur multiple times.
     */
    protected List<DistanceElement> lookup(List<float[]> features, QueryConfig qc) {
        final int numberOfPartialResults = Config.sharedConfig().getRetriever().getMaxResultsPerModule();
        int counter = 1;
        List<DistanceElement> partialResults = new ArrayList<>();
        for (float[] feature : features) {
            if (this.maxLookups > 0 && counter > this.maxLookups) break;
            qc.setDistanceWeights(this.weightsForFeature(feature));
            partialResults.addAll(this.selector.getNearestNeighbours(numberOfPartialResults, feature, "feature", SegmentDistanceElement.class, qc));
            counter += 1;
        }
        return partialResults;
    }

    /**
     * This method represents the last step that's executed when processing a query. A list of partial-results (DistanceElements) returned by
     * the lookup stage is processed based on some internal method and finally converted to a list of ScoreElements. The filtered list of
     * ScoreElements is returned by the feature module during retrieval.
     *
     * @param partialResults List of partial results returned by the lookup stage.
     * @param qc A ReadableQueryConfig object that contains query-related configuration parameters.
     * @return List of final results. Is supposed to be de-duplicated and the number of items should not exceed the number of items per module.
     */
    protected abstract List<ScoreElement> postprocessQuery(List<DistanceElement> partialResults, ReadableQueryConfig qc);

    /**
     * Returns a weight-vector for the given feature. Defaults to null,
     * i.e. all components are weighted equally.
     *
     * @param feature Feature for which a weight-vector is required.
     * @return Weight vector for feature.
     */
    protected float[] weightsForFeature(float[] feature) {
        return null;
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
                .setCorrespondenceFunctionIfEmpty(this.linearCorrespondence)
                .setDistanceIfEmpty(QueryConfig.Distance.euclidean);
    }
}
