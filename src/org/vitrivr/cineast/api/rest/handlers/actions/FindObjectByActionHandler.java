package org.vitrivr.cineast.api.rest.handlers.actions;

import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.vitrivr.cineast.api.rest.handlers.abstracts.ParsingActionHandler;
import org.vitrivr.cineast.core.data.entities.MultimediaObjectDescriptor;
import org.vitrivr.cineast.core.data.messages.general.AnyMessage;
import org.vitrivr.cineast.core.data.messages.result.ObjectQueryResult;
import org.vitrivr.cineast.core.db.dao.reader.MultimediaObjectLookup;

import com.google.common.collect.Lists;

/**
 * @author rgasser
 * @version 1.0
 * @created 10.01.17
 */
public class FindObjectByActionHandler extends ParsingActionHandler<AnyMessage> {

  private final static String ATTRIBUTE_NAME = ":attribute";
  private final static String VALUE_NAME = ":value";

  private static final Logger LOGGER = LogManager.getLogger();
  
  @Override
  public ObjectQueryResult invoke(AnyMessage type, Map<String, String> parameters) {
    String attribute = parameters.get(ATTRIBUTE_NAME);
    String value = parameters.get(VALUE_NAME);
    
    MultimediaObjectLookup ol = new MultimediaObjectLookup();
    MultimediaObjectDescriptor object = null;
    
    switch(attribute.toLowerCase()){
      case "id":{
        object = ol.lookUpObjectById(value);
        break;
      }
      case "name":{
        object = ol.lookUpObjectByName(value);
        break;
      }
      case "path":{
        object = ol.lookUpObjectByPath(value);
        break;
      }
      default:{
        LOGGER.error("Unknown attribute '{}' in FindObjectByActionHandler", attribute);
      }
    }

    ol.close();
    return new ObjectQueryResult("", Lists.newArrayList(object));
  }

  @Override
  public Class<AnyMessage> inClass() {
    return AnyMessage.class;
  }
}
