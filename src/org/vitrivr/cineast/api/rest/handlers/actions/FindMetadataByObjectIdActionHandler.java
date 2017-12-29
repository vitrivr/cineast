package org.vitrivr.cineast.api.rest.handlers.actions;

import org.vitrivr.cineast.api.rest.exceptions.ActionHandlerException;
import org.vitrivr.cineast.api.rest.handlers.abstracts.ParsingActionHandler;
import org.vitrivr.cineast.core.data.entities.MultimediaMetadataDescriptor;
import org.vitrivr.cineast.core.data.entities.MultimediaObjectDescriptor;
import org.vitrivr.cineast.core.data.messages.lookup.MetadataLookup;
import org.vitrivr.cineast.core.db.dao.reader.MultimediaMetadataReader;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Retrieves all the {@link MultimediaMetadataDescriptor}s for the given ID of a {@link MultimediaObjectDescriptor}
 */
public class FindMetadataByObjectIdActionHandler extends ParsingActionHandler<MetadataLookup> {

    private static final String ATTRIBUTE_ID = ":id";

    /**
     * Processes a HTTP GET request.
     *
     * @param parameters Map containing named parameters in the URL.
     * @return List of {@link MultimediaObjectDescriptor}s
     */
    @Override
    public List<MultimediaMetadataDescriptor> doGet(Map<String, String> parameters) {
        final String objectId = parameters.get(ATTRIBUTE_ID);
        final MultimediaMetadataReader reader = new MultimediaMetadataReader();
        final List<MultimediaMetadataDescriptor> descriptors = reader.lookupMultimediaMetadata(objectId);
        reader.close();
        return descriptors;
    }

    /**
     * Processes a HTTP POST request.
     *
     * @param context Object that is handed to the invocation, usually parsed from the request body. May be NULL!
     * @param parameters Map containing named parameters in the URL.
     * @return List of {@link MultimediaObjectDescriptor}s
     */
    @Override
    public List<MultimediaMetadataDescriptor> doPost(MetadataLookup context, Map<String, String> parameters) {
        if(context == null || context.getIds().size() == 0 ){
            return new ArrayList<>(0);
        }
        final MultimediaMetadataReader reader = new MultimediaMetadataReader();
        final List<MultimediaMetadataDescriptor> descriptors = reader.lookupMultimediaMetadata(context.getIds());
        reader.close();
        return descriptors;
    }

    /**
     * Class of the message this {@link ParsingActionHandler} can process.
     *
     * @return Class<AnyMessage>
     */
    @Override
    public Class<MetadataLookup> inClass() {
        return MetadataLookup.class;
    }
}
