package org.vitrivr.cineast.api.rest.handlers;

import org.vitrivr.cineast.core.data.api.Empty;
import org.vitrivr.cineast.api.rest.handlers.basic.ParsingActionHandler;

import java.util.Map;

/**
 * @author rgasser
 * @version 1.0
 * @created 10.01.17
 */
public class FindObjectByActionHandler extends ParsingActionHandler<Empty> {

    private final static String ATTRIBUTE_NAME = ":attribute";
    private final static String VALUE_NAME = ":value";


    @Override
    public Object invoke(Empty type, Map<String, String> parameters) {
        String attribute = parameters.get(":id");
        String value = parameters.get(":value");

        /** TODO: Add logic to fetch object by attribute. */
        return null;
    }

    @Override
    public Class<Empty> inClass() {
        return Empty.class;
    }
}
