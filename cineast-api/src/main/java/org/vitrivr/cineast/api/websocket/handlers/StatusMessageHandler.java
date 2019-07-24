package org.vitrivr.cineast.api.websocket.handlers;

import org.eclipse.jetty.websocket.api.Session;
import org.vitrivr.cineast.api.websocket.handlers.abstracts.StatelessWebsocketMessageHandler;
import org.vitrivr.cineast.core.data.messages.general.Ping;

/**
 * @author rgasser
 * @version 1.0
 * @created 19.01.17
 */
public class StatusMessageHandler extends StatelessWebsocketMessageHandler<Ping> {
    @Override
    public void handle(Session session, Ping message) {
       message.setStatus(Ping.StatusEnum.OK);
       this.write(session, message);
    }
}
