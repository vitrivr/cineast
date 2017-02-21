package org.vitrivr.cineast.api.rest.handlers.actions;


import org.vitrivr.cineast.api.rest.handlers.abstracts.ParsingActionHandler;
import org.vitrivr.cineast.core.data.messages.general.AnyMessage;
import org.vitrivr.cineast.core.data.messages.general.Ping;

import java.util.Map;

/**
 * @author rgasser
 * @version 1.0
 * @created 09.01.17
 */
public class StatusInvokationHandler extends ParsingActionHandler<AnyMessage> {
    @Override
    public Ping invoke(AnyMessage type, Map<String, String> parameters) {
        return new Ping();
    }

    @Override
    public Class<AnyMessage> inClass() {
        return AnyMessage.class;
    }
}
