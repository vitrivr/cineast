package org.vitrivr.cineast.api.messages.result;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Contains a list of all features and the IDs of the objects they belong to for a given table/entity name.
 *
 * @param queryId    Query ID as a string to which this result belongs to.
 * @param featureMap Map containing a list of IDs/feature array for every object ID for every feature in the category.
 * @param entityName Entity name for which the features of all objects were requested.
 */
public record FeaturesByEntityQueryResult(String queryId, ArrayList<HashMap<String, Object>> featureMap, String entityName) {

}
