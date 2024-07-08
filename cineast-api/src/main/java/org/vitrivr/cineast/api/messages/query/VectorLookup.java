package org.vitrivr.cineast.api.messages.query;

import java.util.Map;
import org.vitrivr.cineast.api.messages.lookup.IdList;

public record VectorLookup(IdList ids, String feature, String projection, Map<String, String> properties) {

}
