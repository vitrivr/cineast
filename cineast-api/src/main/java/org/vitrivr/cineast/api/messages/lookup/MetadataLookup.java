package org.vitrivr.cineast.api.messages.lookup;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Message of a metadata lookup query by a requester.
 *
 * @param ids     List of the ids to be looked up.
 * @param domains List of the metadata domains to be considered.
 */
public record MetadataLookup(List<String> ids, List<String> domains) {

}
