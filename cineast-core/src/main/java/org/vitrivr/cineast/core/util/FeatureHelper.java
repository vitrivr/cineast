package org.vitrivr.cineast.core.util;

import static java.util.Arrays.asList;
import static org.vitrivr.cineast.core.util.CineastConstants.FEATURE_COLUMN_QUALIFIER;
import static org.vitrivr.cineast.core.util.CineastConstants.GENERIC_ID_COLUMN_QUALIFIER;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.vitrivr.cineast.core.data.providers.primitive.PrimitiveTypeProvider;
import org.vitrivr.cineast.core.db.DBSelector;
import org.vitrivr.cineast.core.features.DescriptionTextSearch;


public class FeatureHelper {


  public static Map<String, Set<String>> retrieveCaptionWithoutStopwordsBySegmentId(
      List<String> segmentIds,
      DBSelector selector) {
    Map<String, Set<String>> result = new HashMap<>();
    selector.open(DescriptionTextSearch.DESCRIPTION_TEXT_TABLE_NAME);
    List<Map<String, PrimitiveTypeProvider>> rows = selector.getRows(GENERIC_ID_COLUMN_QUALIFIER, segmentIds);
    rows.forEach(row -> {
      String id = row.get(GENERIC_ID_COLUMN_QUALIFIER).getString();
      Set<String> captionSet = result.get(id);
      if (captionSet == null) {
        captionSet = new HashSet<>();
      }
      captionSet.addAll(asList(filterStopWords(row.get(FEATURE_COLUMN_QUALIFIER).getString())));

      result.put(id, captionSet);
    });
    return result;
  }

  private static String[] filterStopWords(String caption) {
    // Source for stopwords array: https://gist.github.com/sebleier/554280
    Set<String> stopWords = new HashSet<>(Arrays
        .asList("i", "me", "my", "myself", "we", "our", "ours", "ourselves", "you", "your", "yours",
            "yourself", "yourselves", "he", "him", "his", "himself", "she", "her", "hers",
            "herself", "it", "its", "itself", "they", "them", "their", "theirs", "themselves",
            "what", "which", "who", "whom", "this", "that", "these", "those", "am", "is", "are",
            "was", "were", "be", "been", "being", "have", "has", "had", "having", "do", "does",
            "did", "doing", "a", "an", "the", "and", "but", "if", "or",
            "because", "as", "until", "while", "of", "at", "by", "for", "with", "about", "against",
            "between", "into", "through", "during", "before", "after", "above", "below", "to",
            "from", "up", "down", "in", "out", "on", "off", "over", "under", "again", "further",
            "then", "once", "here", "there", "when", "where", "why", "how", "all", "any", "both",
            "each", "few", "more", "most", "other", "some", "such", "no", "nor", "not", "only",
            "own", "same", "so", "than", "too", "very", "s", "t",
            "can", "will", "just", "don", "should", "now"));

    return Arrays.stream(caption.replaceAll("[^a-zA-Z ]", "").toLowerCase().split("\\s+"))
        .filter(i -> !stopWords.contains(i)).toArray(String[]::new);
  }

}
