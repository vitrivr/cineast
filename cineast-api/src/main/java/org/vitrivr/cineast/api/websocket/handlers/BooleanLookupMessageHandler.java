package org.vitrivr.cineast.api.websocket.handlers;

import org.eclipse.jetty.websocket.api.Session;
import org.vitrivr.cineast.api.messages.lookup.BooleanLookup;
import org.vitrivr.cineast.api.messages.result.BooleanLookupResult;
import org.vitrivr.cineast.api.websocket.handlers.abstracts.StatelessWebsocketMessageHandler;
import org.vitrivr.cineast.core.db.dao.reader.BooleanReader;
import org.vitrivr.cineast.standalone.config.Config;

import java.util.List;

public class BooleanLookupMessageHandler  extends StatelessWebsocketMessageHandler<BooleanLookup> {
    /**
     * Invoked when a Message of type MetadataLookup arrives and requires handling. Looks up the MultimediaMetadataDescriptors of the requested objects, wraps them in a MediaObjectMetadataQueryResult object and writes them to the stream.
     *
     * @param session WebSocketSession for which the message arrived.
     * @param message Message of type a that needs to be handled.
     */
    @Override
    public void handle(Session session, BooleanLookup message) {
        Thread.currentThread().setName("boolean-lookup-handler");
        BooleanReader reader = new BooleanReader(Config.sharedConfig().getDatabase().getSelectorSupplier().get(), message.getEntity(), message.getQueryList());
        int numberresults;
        if (message.getType().equals("B_ALL")) {
            numberresults = reader.getTotalElements(); /*MUSS HIER ALLE ELEMENTE FINDEN in einem neuen reader*/
        }
        else{
            /*numberresults = reader.getElementsForAttribute();*/
            numberresults = reader.getElementsAND();
        }
        this.write(session, new BooleanLookupResult("", numberresults, message.getComponentID()));
        reader.close();
    }
}
