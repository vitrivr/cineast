package org.vitrivr.cineast.core.db.dao.reader;

import java.util.Map;
import org.vitrivr.cineast.core.data.entities.MediaSegmentMetadataDescriptor;
import org.vitrivr.cineast.core.data.providers.primitive.PrimitiveTypeProvider;
import org.vitrivr.cineast.core.db.DBSelector;

public class MediaSegmentMetadataReader extends AbstractMetadataReader<MediaSegmentMetadataDescriptor> {

  public MediaSegmentMetadataReader(DBSelector selector) {
    this(selector, MediaSegmentMetadataDescriptor.ENTITY);
  }

  public MediaSegmentMetadataReader(DBSelector selector, String tableName) {
    super(selector, tableName, MediaSegmentMetadataDescriptor.FIELDNAMES[0]);
  }

  @Override
  MediaSegmentMetadataDescriptor resultToDescriptor(Map<String, PrimitiveTypeProvider> result) throws DatabaseLookupException {
    return new MediaSegmentMetadataDescriptor(result);
  }
}
