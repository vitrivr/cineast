package org.vitrivr.cineast.api.rest.handlers.actions;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.vitrivr.cineast.api.rest.exceptions.ActionHandlerException;
import org.vitrivr.cineast.api.rest.handlers.abstracts.ParsingActionHandler;
import org.vitrivr.cineast.core.data.entities.MultimediaMetadataDescriptor;
import org.vitrivr.cineast.core.data.messages.lookup.MetadataLookup;
import org.vitrivr.cineast.core.data.messages.result.MetadataQueryResult;
import org.vitrivr.cineast.core.db.dao.reader.MultimediaMetadataReader;

public class FindMetadatasByIdActionHandler extends ParsingActionHandler<MetadataLookup> {

  @Override
  public MetadataQueryResult invoke(MetadataLookup context, Map<String, String> parameters)
      throws ActionHandlerException {
    
    String queryId = ""; //we don't really need this here
    
    if(context == null || context.getIds().size() == 0 ){
      return new MetadataQueryResult(queryId, Collections.emptyList());
    }
    
    MultimediaMetadataReader reader = new MultimediaMetadataReader();
    List<MultimediaMetadataDescriptor> descriptors = reader.lookupMultimediaMetadata(context.getIds());
    reader.close();
    
    return new MetadataQueryResult(queryId, descriptors);
  }

  @Override
  public Class<MetadataLookup> inClass() {
    return MetadataLookup.class;
  }

}
