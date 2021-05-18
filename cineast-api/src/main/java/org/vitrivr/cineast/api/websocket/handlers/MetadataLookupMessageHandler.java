package org.vitrivr.cineast.api.websocket.handlers;

import java.util.List;
import org.eclipse.jetty.websocket.api.Session;
import org.vitrivr.cineast.api.messages.lookup.MetadataLookup;
import org.vitrivr.cineast.api.messages.result.MediaObjectMetadataQueryResult;
import org.vitrivr.cineast.api.websocket.handlers.abstracts.StatelessWebsocketMessageHandler;
import org.vitrivr.cineast.core.data.entities.MediaObjectMetadataDescriptor;
import org.vitrivr.cineast.core.db.dao.reader.MediaObjectMetadataReader;
import org.vitrivr.cineast.standalone.config.Config;

/**
 * This class extends the {@link StatelessWebsocketMessageHandler} abstract class and handles messages of type {@link MetadataLookup}.
 */
public class MetadataLookupMessageHandler extends StatelessWebsocketMessageHandler<MetadataLookup> {

  /**
   * Invoked when a Message of type MetadataLookup arrives and requires handling. Looks up the MultimediaMetadataDescriptors of the requested objects, wraps them in a MediaObjectMetadataQueryResult object and writes them to the stream.
   *
   * @param session WebSocketSession for which the message arrived.
   * @param message Message of type a that needs to be handled.
   */
  @Override
  public void handle(Session session, MetadataLookup message) {
    Thread.currentThread().setName("metadata-lookup-handler");
    MediaObjectMetadataReader reader = new MediaObjectMetadataReader(Config.sharedConfig().getDatabase().getSelectorSupplier().get());
    List<MediaObjectMetadataDescriptor> descriptors = reader.lookupMultimediaMetadata(message.getIds());
    this.write(session, new MediaObjectMetadataQueryResult("", descriptors));
    reader.close();
  }
}
