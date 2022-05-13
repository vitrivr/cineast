package org.vitrivr.cineast.api.messages.result;

import java.util.List;
import java.util.Map;
import org.vitrivr.cineast.api.rest.handlers.actions.bool.SelectFromTablePostHandler;

/**
 * Returned as part of a {@link SelectFromTablePostHandler}. Each element of the result list is a row
 *
 * @param columns
 */
public record SelectResult(List<Map<String, String>> columns) {
}
