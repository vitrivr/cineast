package org.vitrivr.cineast.core.features;

import static org.vitrivr.cineast.core.util.CineastConstants.GENERIC_ID_COLUMN_QUALIFIER;
import static org.vitrivr.cineast.core.util.CineastConstants.FEATURE_COLUMN_QUALIFIER;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.stream.Collectors;
import org.apache.commons.text.similarity.LevenshteinDistance;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.vitrivr.cineast.core.config.ReadableQueryConfig;
import org.vitrivr.cineast.core.data.entities.SimpleFulltextFeatureDescriptor;
import org.vitrivr.cineast.core.data.providers.primitive.PrimitiveTypeProvider;
import org.vitrivr.cineast.core.data.score.ScoreElement;
import org.vitrivr.cineast.core.data.score.SegmentScoreElement;
import org.vitrivr.cineast.core.data.segments.SegmentContainer;
import org.vitrivr.cineast.core.features.abstracts.AbstractTextRetriever;

public class LevenshteinScoringTextRetriever extends AbstractTextRetriever {

  private static final Logger LOGGER = LogManager.getLogger();

  public LevenshteinScoringTextRetriever(Map<String, String> properties) {
    super(ProvidedOcrSearch.PROVIDED_OCR_SEARCH_TABLE_NAME, properties);
  }

  private static final LevenshteinDistance distance = new LevenshteinDistance();

  public static float similarity(String query, String candidate) {

    if (query.isBlank() || candidate.isBlank()) {
      return 0f;
    }

    int levDist = distance.apply(query, candidate);

    if (query.length() < candidate.length()) {
      levDist -= (candidate.length() - query.length()); //don't penalize matching substrings
    }

    return 1f - ((float)levDist / (float)query.length());

  }

  public List<ScoreElement> getSimilar(SegmentContainer sc, ReadableQueryConfig qc) {

    String text = sc.getText();
    if (text == null || text.isBlank()) {
      return Collections.emptyList();
    }
    return this.getSimilar(qc, text);
  }

  protected List<ScoreElement> getSimilar(ReadableQueryConfig qc, String... terms) {

    if (terms.length == 0) {
      return Collections.emptyList();
    }

    final Map<String, Float> scoreMap = new HashMap<>();

    for (String term : terms) {

      if (term.isBlank()) {
        continue;
      }

      String stripped = term.strip();

      final List<Map<String, PrimitiveTypeProvider>> resultList = this.selector.getFulltextRows(qc.getResultsPerModule(), SimpleFulltextFeatureDescriptor.FIELDNAMES[1], qc, generateQuery(stripped));
      LOGGER.trace("Retrieved {} results for term '{}'", resultList.size(), term);

      for (Map<String, PrimitiveTypeProvider> result : resultList) {
        String id = result.get(GENERIC_ID_COLUMN_QUALIFIER).getString();
        String text = result.get(FEATURE_COLUMN_QUALIFIER).getString();

        float score = similarity(stripped, text);

        float bestScore = scoreMap.getOrDefault(id, 0f);

        if (score > bestScore) {
          scoreMap.put(id, score);
        }
      }

    }

    return scoreMap.entrySet().stream().map(entry -> new SegmentScoreElement(entry.getKey(), entry.getValue())).sorted(SegmentScoreElement.SCORE_COMPARATOR).collect(Collectors.toList());

  }

}
