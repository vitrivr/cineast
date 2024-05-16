package org.vitrivr.cineast.api.messages.result;

import java.util.List;
import org.vitrivr.cineast.api.rest.handlers.actions.bool.FindDistinctElementsByColumnsPostHandler;

/**
 * A {@link DistinctElementsResult} contains the response to a {@link FindDistinctElementsByColumnsPostHandler} request. It contains a list of elements which can be considered as a set.
 *
 * @param queryId          String representing the ID of the query to which this part of the result message.
 * @param distinctElements List of Strings containing distinct elements.
 */
public record DistinctElementsMultipleColumnsResult(String queryId, List<List<String>> distinctElements) {

}
