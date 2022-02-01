package org.vitrivr.cineast.core.db.dao.reader;

import java.util.Map;
import org.vitrivr.cineast.core.data.entities.MediaObjectMetadataDescriptor;
import org.vitrivr.cineast.core.data.providers.primitive.PrimitiveTypeProvider;
import org.vitrivr.cineast.core.db.DBSelector;

public class MediaObjectMetadataReader extends AbstractMetadataReader<MediaObjectMetadataDescriptor> {

  public MediaObjectMetadataReader(DBSelector selector) {
    this(selector, MediaObjectMetadataDescriptor.ENTITY);
  }

  public MediaObjectMetadataReader(DBSelector selector, String tableName) {
    super(selector, tableName, MediaObjectMetadataDescriptor.FIELDNAMES[0]);
  }

  @Override
  MediaObjectMetadataDescriptor resultToDescriptor(Map<String, PrimitiveTypeProvider> result) throws DatabaseLookupException {
    return new MediaObjectMetadataDescriptor(result);
  }
}
