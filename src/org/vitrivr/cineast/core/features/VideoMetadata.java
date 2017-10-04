package org.vitrivr.cineast.core.features;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.vitrivr.cineast.core.data.CorrespondenceFunction;
import org.vitrivr.cineast.core.data.entities.SegmentDescriptor;
import org.vitrivr.cineast.core.data.providers.primitive.PrimitiveTypeProvider;
import org.vitrivr.cineast.core.data.score.ScoreElement;
import org.vitrivr.cineast.core.data.score.SegmentScoreElement;
import org.vitrivr.cineast.core.db.dao.reader.SegmentLookup;
import org.vitrivr.cineast.core.features.abstracts.SolrTextRetriever;

public class VideoMetadata extends SolrTextRetriever {

  private static final int STEPSIZE = 10;

  @Override
  protected String getEntityName() {
    return "features_meta";
  }


  @Override
  protected List<ScoreElement> processResults(String query,
      List<Map<String, PrimitiveTypeProvider>> resultList) {
    SegmentLookup lookup = new SegmentLookup();

    List<ScoreElement> scoreElements = new ArrayList<>(resultList.size() * 50);

    int words = query.split("\\s+").length;
    CorrespondenceFunction function = CorrespondenceFunction.fromFunction(score -> score / words / 10.0);

    for (Map<String, PrimitiveTypeProvider> result : resultList) {
      String id = result.get("id").getString();
      double score = function.applyAsDouble(result.get("ap_score").getFloat());

      List<SegmentDescriptor> segments = lookup.lookUpSegmentsOfObject(id);
      for (int i = 0; i < segments.size(); i += STEPSIZE) {
        scoreElements.add(new SegmentScoreElement(segments.get(i).getSegmentId(), score));
      }
    }

    lookup.close();
    return scoreElements;
  }

}
