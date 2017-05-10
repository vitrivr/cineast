package org.vitrivr.cineast.api.rest.handlers.actions;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.vitrivr.cineast.api.rest.exceptions.ActionHandlerException;
import org.vitrivr.cineast.api.rest.handlers.abstracts.ParsingActionHandler;
import org.vitrivr.cineast.core.data.entities.MultimediaMetadataDescriptor;
import org.vitrivr.cineast.core.data.messages.lookup.MetadataLookup;
import org.vitrivr.cineast.core.db.dao.reader.MultimediaMetadataReader;

public class FindMetadatasByIdActionHandler extends ParsingActionHandler<MetadataLookup> {

  @Override
  public Object invoke(MetadataLookup context, Map<String, String> parameters)
      throws ActionHandlerException {
    if(context == null || context.getObjectids().length == 0 ){
      return Collections.emptyMap();
    }
    
    MultimediaMetadataReader reader = new MultimediaMetadataReader();
    List<MultimediaMetadataDescriptor> descriptors = reader.lookupMultimediaMetadata(context.getObjectids());
    reader.close();
    
    HashMap<String, ArrayList<MultimediaMetadataDescriptor>> map = new HashMap<>();
    for(MultimediaMetadataDescriptor d : descriptors){
      if(!map.containsKey(d.getObjectId())){
        map.put(d.getObjectId(), new ArrayList<>());
      }
      map.get(d.getObjectId()).add(d);
    }
    
    return map;
  }

  @Override
  public Class<MetadataLookup> inClass() {
    return MetadataLookup.class;
  }

}
