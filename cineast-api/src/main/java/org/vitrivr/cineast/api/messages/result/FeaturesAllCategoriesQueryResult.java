package org.vitrivr.cineast.api.messages.result;

import java.util.Map;


/**
 * A {@link FeaturesAllCategoriesQueryResult} contains a list of Strings as content of the result message. Map of all feature categories such as tags, captions, OCR, ASR etc. with their values. Used when all features are needed in a single representation.
 *
 * @param queryId    String representing the ID of the query to which this part of the result message.
 * @param featureMap Map that maps strings to all features for this category for a given element ID.
 * @param elementID  Element for which the feature values were requested.
 */
public record FeaturesAllCategoriesQueryResult(String queryId, Map<String, Object[]> featureMap, String elementID) {

}
