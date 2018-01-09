package org.vitrivr.cineast.core.features;

import org.vitrivr.cineast.core.config.ReadableQueryConfig;
import org.vitrivr.cineast.core.data.CorrespondenceFunction;
import org.vitrivr.cineast.core.data.entities.SimpleFulltextFeatureDescriptor;
import org.vitrivr.cineast.core.data.providers.primitive.PrimitiveTypeProvider;
import org.vitrivr.cineast.core.data.score.ObjectScoreElement;
import org.vitrivr.cineast.core.data.score.ScoreElement;
import org.vitrivr.cineast.core.data.score.SegmentScoreElement;
import org.vitrivr.cineast.core.data.segments.SegmentContainer;
import org.vitrivr.cineast.core.features.abstracts.SolrTextRetriever;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class VideoMetadata extends SolrTextRetriever {

    /** Name of the entity associated wiht {@link VideoMetadata}. */
    public static final String ENTITY_NAME = "features_meta";


    /**
     * Default constructor for {@link VideoMetadata}.
     */
    public VideoMetadata() {
        super(ENTITY_NAME);
    }


    /**
     * Performs a fulltext search using the text specified in {@link SegmentContainer#getText()}. In contrast to convention used in most
     * feature modules, the data used during ingest and retrieval is usually different for {@link SolrTextRetriever} subclasses.
     *
     * <strong>Important:</strong> This implementation is tailored to the Apache Solr storage engine used by ADAMpro. It uses Lucene's
     * fuzzy search functionality.
     *
     * @param sc The {@link SegmentContainer} used for lookup.
     * @param qc The {@link ReadableQueryConfig} used to configure the query.
     * @return List of {@link ScoreElement}s.
     */
    @Override
    public List<ScoreElement> getSimilar(SegmentContainer sc, ReadableQueryConfig qc) {
        final String[] terms = Arrays.stream(sc.getText().split("\\s")).map(s -> s + "~0.5").toArray(String[]::new);
        final List<Map<String, PrimitiveTypeProvider>> resultList = this.selector.getFulltextRows(500, SimpleFulltextFeatureDescriptor.FIELDNAMES[1], terms);
        final CorrespondenceFunction f = CorrespondenceFunction.fromFunction(score -> score / terms.length / 10f);
        final List<ScoreElement> scoreElements = new ArrayList<>(resultList.size());
        for (Map<String, PrimitiveTypeProvider> result : resultList) {
            String id = result.get("id").getString();
            double score = f.applyAsDouble(result.get("ap_score").getFloat());
            scoreElements.add(new ObjectScoreElement(id, score));
        }
        return scoreElements;
    }

    /**
     *
     * @param shot
     */
    @Override
    public void processSegment(SegmentContainer shot) {
        /* TODO: Not implemented because video metadata extraction is not integrated into pipeline yet. */
    }
}
