package org.vitrivr.cineast.api.rest.handlers;

import org.vitrivr.cineast.core.data.api.Empty;
import org.vitrivr.cineast.core.data.api.Status;
import org.vitrivr.cineast.api.rest.handlers.basic.ParsingActionHandler;

import java.util.Map;

/**
 * @author rgasser
 * @version 1.0
 * @created 09.01.17
 */
public class StatusInvokationHandler extends ParsingActionHandler<Empty> {
    @Override
    public Status invoke(Empty type, Map<String, String> parameters) {
        return new Status("OK");
    }

    @Override
    public Class<Empty> inClass() {
        return Empty.class;
    }
}
