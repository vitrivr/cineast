package org.vitrivr.cineast.api.rest.handlers.actions;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Map;

import org.vitrivr.cineast.api.rest.exceptions.ActionHandlerException;
import org.vitrivr.cineast.api.rest.handlers.abstracts.ParsingActionHandler;
import org.vitrivr.cineast.core.data.entities.MultimediaObjectDescriptor;
import org.vitrivr.cineast.core.data.messages.lookup.IdList;
import org.vitrivr.cineast.core.data.messages.result.ObjectQueryResult;
import org.vitrivr.cineast.core.db.dao.reader.MultimediaObjectLookup;

public class FindObjectsByIdActionHandler extends ParsingActionHandler<IdList> {

  @Override
  public ObjectQueryResult invoke(IdList context, Map<String, String> parameters)
      throws ActionHandlerException {
    if(context == null || context.getIds().length == 0){
      return new ObjectQueryResult("", Collections.emptyList());
    }
    MultimediaObjectLookup ol = new MultimediaObjectLookup();
    
    Map<String, MultimediaObjectDescriptor> objects = ol.lookUpObjects(Arrays.asList(context.getIds()));
    
    ol.close();
    
    return new ObjectQueryResult("", new ArrayList<MultimediaObjectDescriptor>(objects.values()));
  }

  @Override
  public Class<IdList> inClass() {
    return IdList.class;
  }

}
