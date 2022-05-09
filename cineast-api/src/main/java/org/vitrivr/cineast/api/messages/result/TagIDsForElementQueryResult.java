package org.vitrivr.cineast.api.messages.result;

import java.util.List;

/**
 * General-purpose result for any query that expects a list of tag IDs for an element ID as a result.
 *
 * @param queryId   String representing the ID of the query to which this part of the result message.
 * @param tags      List of Strings containing the tag IDs that belong to the element ID and represent the result of the query.
 * @param elementID String representing the element ID of the element of which the tag IDs were looked up for.
 */
public record TagIDsForElementQueryResult(String queryId, List<String> tags, String elementID) {

}
