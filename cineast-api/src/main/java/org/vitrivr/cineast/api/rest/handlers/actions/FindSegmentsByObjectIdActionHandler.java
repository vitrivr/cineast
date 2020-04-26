package org.vitrivr.cineast.api.rest.handlers.actions;

import java.util.List;
import java.util.Map;

import org.vitrivr.cineast.api.messages.general.AnyMessage;
import org.vitrivr.cineast.api.rest.RestHttpMethod;
import org.vitrivr.cineast.api.rest.handlers.abstracts.ParsingActionHandler;
import org.vitrivr.cineast.core.data.entities.MediaObjectDescriptor;
import org.vitrivr.cineast.core.data.entities.MediaSegmentDescriptor;
import org.vitrivr.cineast.core.db.dao.reader.MediaSegmentReader;
import org.vitrivr.cineast.standalone.config.Config;

public class FindSegmentsByObjectIdActionHandler extends ParsingActionHandler<AnyMessage, MediaSegmentDescriptor> {

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
        final MediaSegmentReader sl = new MediaSegmentReader(Config.sharedConfig().getDatabase().getSelectorSupplier().get());
        final List<MediaSegmentDescriptor> list = sl.lookUpSegmentsOfObject(objectId);
        sl.close();
        return list;
    }

    @Override
    public Class<AnyMessage> inClass() {
        return AnyMessage.class;
    }

    @Override
    public String getRoute() {
        return "find/segments/all/object/"+ID_NAME;
    }

    @Override
    public String getDescription(RestHttpMethod method) {
        return "Find segments for object id";
    }

    @Override
    public Class<MediaSegmentDescriptor> outClass() {
        return MediaSegmentDescriptor.class;
    }

    @Override
    public boolean isResponseCollection() {
        return true;
    }
}
