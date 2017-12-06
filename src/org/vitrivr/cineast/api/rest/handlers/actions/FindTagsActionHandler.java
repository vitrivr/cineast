package org.vitrivr.cineast.api.rest.handlers.actions;

import java.util.Map;

import org.vitrivr.cineast.api.rest.exceptions.ActionHandlerException;
import org.vitrivr.cineast.api.rest.handlers.abstracts.ParsingActionHandler;
import org.vitrivr.cineast.core.data.messages.lookup.IdList;
import org.vitrivr.cineast.core.db.dao.TagHandler;

public class FindTagsActionHandler extends ParsingActionHandler<IdList> {

  private static TagHandler tagHandler = new TagHandler();
  
  public FindTagsActionHandler(){
    tagHandler.initCache();
  }
  
  @Override
  public Object invoke(IdList context, Map<String, String> parameters)
      throws ActionHandlerException {

    return tagHandler.getAllCached();
    
  }

  @Override
  public Class<IdList> inClass() {
    return IdList.class;
  }

}
