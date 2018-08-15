package org.vitrivr.cineast.api.rest.handlers.actions;

import com.google.common.collect.Lists;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.vitrivr.cineast.api.rest.handlers.abstracts.ParsingActionHandler;
import org.vitrivr.cineast.core.data.entities.MultimediaObjectDescriptor;
import org.vitrivr.cineast.core.data.messages.lookup.IdList;
import org.vitrivr.cineast.core.data.messages.result.ObjectQueryResult;
import org.vitrivr.cineast.core.db.dao.reader.MultimediaObjectLookup;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Map;

/**
 * @author rgasser
 * @version 1.0
 * @created 10.01.17
 */
public class FindObjectByActionHandler extends ParsingActionHandler<IdList> {

    private final static String ATTRIBUTE_NAME = ":attribute";
    private final static String VALUE_NAME = ":value";

    private static final Logger LOGGER = LogManager.getLogger();

    /**
     * Processes a HTTP GET request.
     *
     * @param parameters Map containing named parameters in the URL.
     * @return {@link ObjectQueryResult}
     */
    @Override
    public ObjectQueryResult doGet(Map<String, String> parameters) {
        String attribute = parameters.get(ATTRIBUTE_NAME);
        String value = parameters.get(VALUE_NAME);

        MultimediaObjectLookup ol = new MultimediaObjectLookup();
        MultimediaObjectDescriptor object = null;

        switch (attribute.toLowerCase()) {
            case "id": {
                object = ol.lookUpObjectById(value);
                break;
            }
            case "name": {
                object = ol.lookUpObjectByName(value);
                break;
            }
            case "path": {
                object = ol.lookUpObjectByPath(value);
                break;
            }
            default: {
                LOGGER.error("Unknown attribute '{}' in FindObjectByActionHandler", attribute);
            }
        }

        ol.close();
        return new ObjectQueryResult("",Lists.newArrayList(object));
    }

    /**
     * Processes a HTTP GET request.
     *
     * @param parameters Map containing named parameters in the URL.
     * @return {@link ObjectQueryResult}
     */
    @Override
    public ObjectQueryResult doPost(IdList context, Map<String, String> parameters) {
        if(context == null || context.getIds().length == 0){
            return new ObjectQueryResult("",new ArrayList<>(0));
        }
        final MultimediaObjectLookup ol = new MultimediaObjectLookup();
        final Map<String, MultimediaObjectDescriptor> objects = ol.lookUpObjects(Arrays.asList(context.getIds()));
        ol.close();
        return new ObjectQueryResult("",new ArrayList<>(objects.values()));
    }

    @Override
    public Class<IdList> inClass() {
        return IdList.class;
    }
}
