package org.vitrivr.cineast.core.db.dao.reader;

import java.util.Arrays;
import java.util.stream.Collectors;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.vitrivr.cineast.core.data.MediaType;
import org.vitrivr.cineast.core.data.entities.MediaObjectDescriptor;
import org.vitrivr.cineast.core.data.providers.primitive.PrimitiveTypeProvider;
import org.vitrivr.cineast.core.data.providers.primitive.ProviderDataType;
import org.vitrivr.cineast.core.data.providers.primitive.StringTypeProvider;
import org.vitrivr.cineast.core.db.DBSelector;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MediaObjectReader extends AbstractEntityReader {

  private static final Logger LOGGER = LogManager.getLogger();

    /**
     * Constructor for MediaObjectReader
     *
     * @param selector DBSelector to use for the MediaObjectMetadataReader instance.
     */
    public MediaObjectReader(DBSelector selector) {
      super(selector);
      this.selector.open(MediaObjectDescriptor.ENTITY);
    }

  public MediaObjectDescriptor lookUpObjectById(String objectId) {
    List<Map<String, PrimitiveTypeProvider>> result = selector.getRows(MediaObjectDescriptor.FIELDNAMES[0], new StringTypeProvider(objectId));

    if (result.isEmpty()) {
      return new MediaObjectDescriptor();
    }

    return mapToDescriptor(result.get(0));
  }

  private MediaObjectDescriptor mapToDescriptor(Map<String, PrimitiveTypeProvider> map) {
    PrimitiveTypeProvider idProvider = map.get(MediaObjectDescriptor.FIELDNAMES[0]);
    PrimitiveTypeProvider typeProvider = map.get(MediaObjectDescriptor.FIELDNAMES[1]);
    PrimitiveTypeProvider nameProvider = map.get(MediaObjectDescriptor.FIELDNAMES[2]);
    PrimitiveTypeProvider pathProvider = map.get(MediaObjectDescriptor.FIELDNAMES[3]);


    if (!checkProvider(MediaObjectDescriptor.FIELDNAMES[0], idProvider, ProviderDataType.STRING)) {
      return new MediaObjectDescriptor();
    }

    if (!checkProvider(MediaObjectDescriptor.FIELDNAMES[1], typeProvider, ProviderDataType.INT)) {
      return new MediaObjectDescriptor();
    }

    if (!checkProvider(MediaObjectDescriptor.FIELDNAMES[2], nameProvider, ProviderDataType.STRING)) {
      return new MediaObjectDescriptor();
    }

    if (!checkProvider(MediaObjectDescriptor.FIELDNAMES[3], pathProvider, ProviderDataType.STRING)) {
      return new MediaObjectDescriptor();
    }



    return new MediaObjectDescriptor(idProvider.getString(), nameProvider.getString(), pathProvider.getString(), MediaType.fromId(typeProvider.getInt()), true);

  }

  private boolean checkProvider(String name, PrimitiveTypeProvider provider,
      ProviderDataType expectedType) {
    if (provider == null) {
      LOGGER.error("no {} in multimedia object", name);
      return false;
    }

    if (provider.getType() != expectedType) {
      LOGGER.error("invalid data type for field {} in multimedia object, expected {}, got {}", name,
          expectedType, provider.getType());
      return false;
    }
    return true;
  }

  public MediaObjectDescriptor lookUpObjectByName(String name) {
    List<Map<String, PrimitiveTypeProvider>> result = selector.getRows(MediaObjectDescriptor.FIELDNAMES[2], new StringTypeProvider(name));

    if (result.isEmpty()) {
      return new MediaObjectDescriptor();
    }

    return mapToDescriptor(result.get(0));
  }
  
  public MediaObjectDescriptor lookUpObjectByPath(String path) {
    List<Map<String, PrimitiveTypeProvider>> result = selector.getRows(MediaObjectDescriptor.FIELDNAMES[3], new StringTypeProvider(path));

    if (result.isEmpty()) {
      return new MediaObjectDescriptor();
    }

    return mapToDescriptor(result.get(0));
  }

  public Map<String, MediaObjectDescriptor> lookUpObjects(String... videoIds) {
    if (videoIds == null || videoIds.length == 0) {
      return new HashMap<>();
    }

    HashMap<String, MediaObjectDescriptor> _return = new HashMap<>();

    List<Map<String, PrimitiveTypeProvider>> results = selector.getRows(MediaObjectDescriptor.FIELDNAMES[0], Arrays.stream(videoIds).map(StringTypeProvider::new).collect(Collectors.toList()));

    if (results.isEmpty()) {
      return new HashMap<>();
    }

    for (Map<String, PrimitiveTypeProvider> map : results) {
      MediaObjectDescriptor d = mapToDescriptor(map);
      _return.put(d.getObjectId(), d);
    }

    return _return;

  }

  public Map<String, MediaObjectDescriptor> lookUpObjects(Iterable<String> videoIds) {
    if (videoIds == null) {
      return new HashMap<>();
    }

    HashMap<String, MediaObjectDescriptor> _return = new HashMap<>();

    List<PrimitiveTypeProvider> in = new ArrayList<>();

    for (String s : videoIds) {
      in.add(new StringTypeProvider(s));
    }

    List<Map<String, PrimitiveTypeProvider>> results = selector.getRows(MediaObjectDescriptor.FIELDNAMES[0], in);

    if (results.isEmpty()) {
      return new HashMap<>();
    }

    for (Map<String, PrimitiveTypeProvider> map : results) {
      MediaObjectDescriptor d = mapToDescriptor(map);
      _return.put(d.getObjectId(), d);
    }

    return _return;

  }

  public List<MediaObjectDescriptor> getAllObjects() {
    List<Map<String, PrimitiveTypeProvider>> all = selector.getAll();
    List<MediaObjectDescriptor> _return = new ArrayList<>(all.size());
    for (Map<String, PrimitiveTypeProvider> map : all) {
      _return.add(mapToDescriptor(map));
    }
    return _return;
  }

}
