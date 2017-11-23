package org.vitrivr.cineast.core.features;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.vitrivr.cineast.core.data.CorrespondenceFunction;
import org.vitrivr.cineast.core.data.providers.primitive.PrimitiveTypeProvider;
import org.vitrivr.cineast.core.data.score.ObjectScoreElement;
import org.vitrivr.cineast.core.data.score.ScoreElement;
import org.vitrivr.cineast.core.features.abstracts.SolrTextRetriever;

public class VideoMetadata extends SolrTextRetriever {

  @Override
  protected String getEntityName() {
    return "features_meta";
  }


  @Override
  protected List<ScoreElement> processResults(String query,
      List<Map<String, PrimitiveTypeProvider>> resultList) {

    List<ScoreElement> scoreElements = new ArrayList<>(resultList.size());

    int words = query.split("\\s+").length;
    CorrespondenceFunction function = CorrespondenceFunction.fromFunction(score -> score / words / 10.0);

    for (Map<String, PrimitiveTypeProvider> result : resultList) {
      String id = result.get("id").getString();
      double score = function.applyAsDouble(result.get("ap_score").getFloat());

      scoreElements.add(new ObjectScoreElement(id, score));
      
    }

    return scoreElements;
  }

}
