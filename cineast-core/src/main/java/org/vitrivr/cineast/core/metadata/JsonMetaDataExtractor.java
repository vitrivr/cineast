package org.vitrivr.cineast.core.metadata;

import org.vitrivr.cineast.core.data.entities.MediaObjectMetadataDescriptor;
import org.vitrivr.cineast.core.util.json.JacksonJsonProvider;

import java.io.File;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class JsonMetaDataExtractor implements MetadataExtractor {

  private static final JacksonJsonProvider jsonProvider = new JacksonJsonProvider();

  @Override
  public List<MediaObjectMetadataDescriptor> extract(String objectId, Path path) {
    File file = path.toFile();
    File parent = file.getParentFile();
    File jsonFile = new File(parent, file.getName() + ".json");
    
    if(!jsonFile.exists()){
        jsonFile = new File(parent, com.google.common.io.Files.getNameWithoutExtension(file.getName()) + ".json");
    }
    
    if(!jsonFile.exists()){
      return new ArrayList<>(0);
    }
    
    @SuppressWarnings("unchecked")
    Map<String, Object> json = jsonProvider.toObject(jsonFile, Map.class);
    
    if(json == null || json.isEmpty()){
      return new ArrayList<>(0);
    }
    
    ArrayList<MediaObjectMetadataDescriptor> _return = new ArrayList<>(json.size());
    
    Set<String> keys = json.keySet();
    
    for(String key : keys){
      _return.add(
          MediaObjectMetadataDescriptor.of(objectId, domain(), key, json.get(key))
          );
    }
    
    return _return;
  }

  @Override
  public String domain() {
    return "JSON";
  }

}
