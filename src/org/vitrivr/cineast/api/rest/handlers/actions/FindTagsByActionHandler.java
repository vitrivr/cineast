package org.vitrivr.cineast.api.rest.handlers.actions;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.vitrivr.cineast.api.rest.exceptions.ActionHandlerException;
import org.vitrivr.cineast.api.rest.handlers.abstracts.ParsingActionHandler;
import org.vitrivr.cineast.core.data.entities.Tag;
import org.vitrivr.cineast.core.data.messages.lookup.IdList;
import org.vitrivr.cineast.core.db.dao.TagHandler;

public class FindTagsByActionHandler extends ParsingActionHandler<IdList> {

  private static TagHandler tagHandler = new TagHandler();
  private static final Logger LOGGER = LogManager.getLogger();

  
  private static final  String ATTRIBUTE_NAME = ":attribute";
  private static final String VALUE_NAME = ":value";
  
  
  
  private final boolean post;
  
  public FindTagsByActionHandler(boolean post){
    this.post = post;
  }
  
  @Override
  public Object invoke(IdList context, Map<String, String> parameters)
      throws ActionHandlerException {

    if(post){
      
      if(context == null || context.getIds().length == 0){
        return Collections.emptyList();
      }
      
      return tagHandler.getTagsById(context.getIds());
      
    }else{
      String attribute = parameters.get(ATTRIBUTE_NAME);
      String value = parameters.get(VALUE_NAME);
      
      ArrayList<Tag> list = new ArrayList<>();
      
      switch(attribute.toLowerCase()){
      case "id":{
        list.add(tagHandler.getTagById(value));
        break;
      }
      case "name":{
        list.addAll(tagHandler.getTagsByName(value));
        break;
      }
      default:{
        LOGGER.error("Unknown attribute '{}' in FindTagsByActionHandler", attribute);
      }
      }
      return list;
    }
  }

  @Override
  public Class<IdList> inClass() {
    return IdList.class;
  }

}
