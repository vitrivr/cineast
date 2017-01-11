package org.vitrivr.cineast.api.rest.handlers;

import org.vitrivr.cineast.core.data.api.Empty;
import org.vitrivr.cineast.api.rest.handlers.basic.ParsingActionHandler;
import org.vitrivr.cineast.core.data.entities.MultimediaObjectDescriptor;
import org.vitrivr.cineast.core.db.MultimediaObjectLookup;

import java.util.List;
import java.util.Map;

/**
 * @author rgasser
 * @version 1.0
 * @created 10.01.17
 */
public class FindObjectAllActionHandler extends ParsingActionHandler<Empty> {
    @Override
    public Object invoke(Empty type, Map<String, String> parameters) {
        List<MultimediaObjectDescriptor> multimediaobjectIds = new MultimediaObjectLookup().getAllVideos();
        return multimediaobjectIds.toArray(new MultimediaObjectDescriptor[multimediaobjectIds.size()]);
    }

    @Override
    public Class<Empty> inClass() {
        return Empty.class;
    }
}
