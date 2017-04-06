package org.vitrivr.cineast.api.rest.handlers.actions;

import org.vitrivr.cineast.api.rest.handlers.abstracts.ParsingActionHandler;
import org.vitrivr.cineast.core.data.entities.MultimediaObjectDescriptor;
import org.vitrivr.cineast.core.data.messages.general.AnyMessage;
import org.vitrivr.cineast.core.db.dao.reader.MultimediaObjectLookup;

import java.util.List;
import java.util.Map;

/**
 * @author rgasser
 * @version 1.0
 * @created 10.01.17
 */
public class FindObjectAllActionHandler extends ParsingActionHandler<AnyMessage> {
  @Override
  public Object invoke(AnyMessage type, Map<String, String> parameters) {

    MultimediaObjectLookup ol = new MultimediaObjectLookup();
    List<MultimediaObjectDescriptor> multimediaobjectIds = ol.getAllObjects();
    ol.close();
    return multimediaobjectIds.toArray(new MultimediaObjectDescriptor[multimediaobjectIds.size()]);
  }

  @Override
  public Class<AnyMessage> inClass() {
    return AnyMessage.class;
  }
}
