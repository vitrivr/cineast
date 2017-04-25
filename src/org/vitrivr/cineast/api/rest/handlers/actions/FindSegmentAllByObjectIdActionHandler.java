package org.vitrivr.cineast.api.rest.handlers.actions;

import java.util.List;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.vitrivr.cineast.api.rest.exceptions.ActionHandlerException;
import org.vitrivr.cineast.api.rest.handlers.abstracts.ParsingActionHandler;
import org.vitrivr.cineast.core.data.entities.SegmentDescriptor;
import org.vitrivr.cineast.core.data.messages.general.AnyMessage;
import org.vitrivr.cineast.core.db.dao.reader.SegmentLookup;

public class FindSegmentAllByObjectIdActionHandler extends ParsingActionHandler<AnyMessage> {

  private final static String ID_NAME = ":id";

  private static final Logger LOGGER = LogManager.getLogger();
  
  @Override
  public Object invoke(AnyMessage context, Map<String, String> parameters)
      throws ActionHandlerException {

    String objectId = parameters.get(ID_NAME);
    
    SegmentLookup sl = new SegmentLookup();
    
    List<SegmentDescriptor> list = sl.lookUpSegmentsOfObject(objectId);
    
    sl.close();
    
    return list;
  }

  @Override
  public Class<AnyMessage> inClass() {
    return AnyMessage.class;
  }

}
