package org.vitrivr.cineast.api.messages.result;

import java.util.List;
import org.vitrivr.cineast.core.data.tag.Tag;

/**
 * General-purpose result for any query that expects a list of tags as a result.
 *
 * @param queryId String representing the ID of the query to which this part of the result message.
 * @param tags    List of Strings containing the tags that represent the result of the query.
 */
public record TagsQueryResult(String queryId, List<Tag> tags) {

}
