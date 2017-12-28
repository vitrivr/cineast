package org.vitrivr.cineast.api.rest.handlers.actions;

import org.vitrivr.cineast.api.rest.handlers.abstracts.ParsingActionHandler;
import org.vitrivr.cineast.core.data.entities.MultimediaMetadataDescriptor;
import org.vitrivr.cineast.core.data.entities.MultimediaObjectDescriptor;
import org.vitrivr.cineast.core.data.messages.general.AnyMessage;
import org.vitrivr.cineast.core.db.dao.reader.MultimediaMetadataReader;

import java.util.List;
import java.util.Map;

/**
 * Retrieves all the {@link MultimediaMetadataDescriptor}s for the given ID of a {@link MultimediaObjectDescriptor}
 */
public class FindMetadataByObjectIdActionHandler extends ParsingActionHandler<AnyMessage> {

    private static final String ATTRIBUTE_ID = ":id";

    /**
     * Processes the HTTP request.
     *
     * @param message An {@link AnyMessage} instance.
     * @param parameters Map containing named parameters in the URL.
     * @return Array of {@link MultimediaObjectDescriptor}s
     */
    @Override
    public MultimediaMetadataDescriptor[] invoke(AnyMessage message, Map<String, String> parameters) {
        final String objectId = parameters.get(ATTRIBUTE_ID);
        final MultimediaMetadataReader reader = new MultimediaMetadataReader();
        final List<MultimediaMetadataDescriptor> descriptors = reader.lookupMultimediaMetadata(objectId);
        reader.close();
        return descriptors.toArray(new MultimediaMetadataDescriptor[descriptors.size()]);
    }

    /**
     * Class of the message this {@link ParsingActionHandler} can process.
     *
     * @return Class<AnyMessage>
     */
    @Override
    public Class<AnyMessage> inClass() {
        return AnyMessage.class;
    }
}
