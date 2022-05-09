package org.vitrivr.cineast.api.messages.result;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * Contains lists of all features and the IDs of the objects they belong to, mapped by table/entity name.
 *
 * @param queryId    Query ID as a string to which this result belongs to.
 * @param featureMap Map containing a list of IDs/feature array for every object ID for every feature in the category.
 * @param category   Category for which the features of all objects were requested.
 */
public record FeaturesByCategoryQueryResult(String queryId, Map<String, ArrayList<HashMap<String, Object>>> featureMap, String category) {

}
