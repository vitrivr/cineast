package org.vitrivr.cineast.api.websocket.handlers;

import org.eclipse.jetty.websocket.api.Session;
import org.vitrivr.cineast.api.websocket.handlers.abstracts.StatelessWebsocketMessageHandler;

import org.vitrivr.cineast.core.data.entities.MultimediaMetadataDescriptor;

import org.vitrivr.cineast.core.data.messages.lookup.MetadataLookup;
import org.vitrivr.cineast.core.data.messages.result.MetadataQueryResult;
import org.vitrivr.cineast.core.db.dao.reader.MultimediaMetadataReader;

import java.util.List;

/**
 * @author rgasser
 * @version 1.0
 * @created 10.02.17
 */
public class MetadataLookupMessageHandler extends StatelessWebsocketMessageHandler<MetadataLookup> {

    /**
     * Invoked when a Message of type MetadataLookup arrives and requires handling. Looks up the
     * MultimediaMetadataDescriptors of the requested objects, wraps them in a MetadataQueryResult object
     * and writes them to the stream.
     *
     * @param session WebSocketSession for which the message arrived.
     * @param message Message of type a that needs to be handled.
     */
    @Override
    public void handle(Session session, MetadataLookup message) {
        MultimediaMetadataReader reader = new MultimediaMetadataReader();
        List<MultimediaMetadataDescriptor> descriptors = reader.lookupMultimediaMetadata(message.getObjectids());
        this.write(session, new MetadataQueryResult("test", descriptors));
    }
}
