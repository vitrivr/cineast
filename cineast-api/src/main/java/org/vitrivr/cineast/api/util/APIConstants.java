package org.vitrivr.cineast.api.util;

import java.util.ArrayList;
import java.util.List;
import org.vitrivr.cineast.core.db.dao.MetadataAccessSpecification;
import org.vitrivr.cineast.core.db.dao.MetadataType;

public class APIConstants {

  public static final String ID_QUALIFIER = "id";
  public static final String CATEGORY_NAME = "category";
  public static final String ENTITY_NAME = "entity";
  public static final List<MetadataAccessSpecification> ACCESS_ALL_METADATA;

  static {
    List<MetadataAccessSpecification> _spec = new ArrayList<>();
    _spec.add(new MetadataAccessSpecification(MetadataType.OBJECT, "*", "*"));
    _spec.add(new MetadataAccessSpecification(MetadataType.SEGMENT, "*", "*"));
    ACCESS_ALL_METADATA = _spec;
  }
}
