package org.vitrivr.cineast.api.rest.handlers.actions;


import org.vitrivr.cineast.api.rest.handlers.abstracts.ParsingActionHandler;
import org.vitrivr.cineast.api.messages.general.AnyMessage;
import org.vitrivr.cineast.api.messages.general.Ping;

import java.util.Map;

/**
 * @author rgasser
 * @version 1.0
 * @created 09.01.17
 */
public class StatusInvokationHandler extends ParsingActionHandler<AnyMessage> {
    /**
     * Processes a HTTP GET request. Returns a {@link Ping} object
     *
     * @param parameters Map containing named parameters in the URL.
     * @return {@link Ping}
     */
    @Override
    public Ping doGet(Map<String, String> parameters) {
        return new Ping();
    }

    @Override
    public Class<AnyMessage> inClass() {
        return AnyMessage.class;
    }
}
