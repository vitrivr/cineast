package org.vitrivr.cineast.api.rest.handlers.actions;

import java.util.List;
import java.util.Map;

import org.vitrivr.cineast.api.rest.handlers.abstracts.ParsingActionHandler;
import org.vitrivr.cineast.core.data.entities.MultimediaObjectDescriptor;
import org.vitrivr.cineast.core.data.entities.SegmentDescriptor;
import org.vitrivr.cineast.core.data.messages.general.AnyMessage;
import org.vitrivr.cineast.core.db.dao.reader.SegmentLookup;

public class FindSegmentsByObjectIdActionHandler extends ParsingActionHandler<AnyMessage> {

    private final static String ID_NAME = ":id";

    /**
     * Processes a HTTP GET request. Searches all {@link SegmentDescriptor}s that belong to the
     * {@link MultimediaObjectDescriptor} with the given ID.
     *
     * @param parameters Map containing named parameters in the URL.
     * @return List of {@link SegmentDescriptor}
     */
    @Override
    public List<SegmentDescriptor> doGet(Map<String, String> parameters) {
        final String objectId = parameters.get(ID_NAME);
        final SegmentLookup sl = new SegmentLookup();
        final List<SegmentDescriptor> list = sl.lookUpSegmentsOfObject(objectId);
        sl.close();
        return list;
    }

    @Override
    public Class<AnyMessage> inClass() {
        return AnyMessage.class;
    }

}
