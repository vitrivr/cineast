package org.vitrivr.cineast.api.messages.result;

import java.util.List;

/**
 * A {@link FeaturesTextCategoryQueryResult} contains a list of Strings as content of the result message. All features for a given category and element (i.e. segment, object). Only used for String features (e.g. OCR, ASR) queries.
 *
 * @param queryId       String representing the ID of the query to which this part of the result message.
 * @param featureValues List of Strings containing the feature values for the given element and category.
 * @param category      Category for which the feature values were requested.
 * @param elementID     Element for which the feature values were requested.
 */
public record FeaturesTextCategoryQueryResult(String queryId, List<String> featureValues, String category, String elementID) {

}
