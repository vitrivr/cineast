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
  public static final String DOMAIN_NAME = "domain";
  public static final String KEY_NAME = "key";
  public static final String VALUE_NAME = "value";
  public static final String ATTRIBUTE_NAME = "attribute";
  public static final String LIMIT_NAME = "limit";
  public static final String SKIP_NAME = "skip";


  static {
    List<MetadataAccessSpecification> _spec = new ArrayList<>();
    _spec.add(new MetadataAccessSpecification(MetadataType.OBJECT, "*", "*"));
    _spec.add(new MetadataAccessSpecification(MetadataType.SEGMENT, "*", "*"));
    ACCESS_ALL_METADATA = _spec;
  }
}
