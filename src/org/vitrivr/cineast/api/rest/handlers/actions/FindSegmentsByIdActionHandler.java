package org.vitrivr.cineast.api.rest.handlers.actions;

import java.util.Arrays;
import java.util.Collections;
import java.util.Map;

import org.vitrivr.cineast.api.rest.exceptions.ActionHandlerException;
import org.vitrivr.cineast.api.rest.handlers.abstracts.ParsingActionHandler;
import org.vitrivr.cineast.core.data.entities.SegmentDescriptor;
import org.vitrivr.cineast.core.data.messages.lookup.IdList;
import org.vitrivr.cineast.core.db.dao.reader.SegmentLookup;

public class FindSegmentsByIdActionHandler extends ParsingActionHandler<IdList> {

  @Override
  public Object invoke(IdList context, Map<String, String> parameters)
      throws ActionHandlerException {
    if(context == null || context.getIds().length == 0){
      return Collections.emptyMap();
    }
    SegmentLookup sl = new SegmentLookup();
    
    Map<String, SegmentDescriptor> segments = sl.lookUpSegments(Arrays.asList(context.getIds()));
    
    sl.close();
    
    return segments;
  }

  @Override
  public Class<IdList> inClass() {
    return IdList.class;
  }

}
