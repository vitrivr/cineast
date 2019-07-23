package org.vitrivr.cineast.api.rest.handlers.actions;

import java.util.List;
import java.util.Map;

import org.vitrivr.cineast.api.rest.handlers.abstracts.ParsingActionHandler;
import org.vitrivr.cineast.core.data.entities.MediaObjectDescriptor;
import org.vitrivr.cineast.core.data.entities.MediaSegmentDescriptor;
import org.vitrivr.cineast.core.data.messages.general.AnyMessage;
import org.vitrivr.cineast.core.db.dao.reader.MediaSegmentReader;

public class FindSegmentsByObjectIdActionHandler extends ParsingActionHandler<AnyMessage> {

    private final static String ID_NAME = ":id";

    /**
     * Processes a HTTP GET request. Searches all {@link MediaSegmentDescriptor}s that belong to the
     * {@link MediaObjectDescriptor} with the given ID.
     *
     * @param parameters Map containing named parameters in the URL.
     * @return List of {@link MediaSegmentDescriptor}
     */
    @Override
    public List<MediaSegmentDescriptor> doGet(Map<String, String> parameters) {
        final String objectId = parameters.get(ID_NAME);
        final MediaSegmentReader sl = new MediaSegmentReader();
        final List<MediaSegmentDescriptor> list = sl.lookUpSegmentsOfObject(objectId);
        sl.close();
        return list;
    }

    @Override
    public Class<AnyMessage> inClass() {
        return AnyMessage.class;
    }

}
