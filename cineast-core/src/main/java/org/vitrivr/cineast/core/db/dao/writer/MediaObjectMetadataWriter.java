package org.vitrivr.cineast.core.db.dao.writer;

import org.vitrivr.cineast.core.data.entities.MediaObjectMetadataDescriptor;
import org.vitrivr.cineast.core.data.providers.primitive.NothingProvider;
import org.vitrivr.cineast.core.db.PersistencyWriter;
import org.vitrivr.cineast.core.db.PersistentTuple;


public class MediaObjectMetadataWriter extends AbstractBatchedEntityWriter<MediaObjectMetadataDescriptor> {

  private final String entityName;

  public MediaObjectMetadataWriter(PersistencyWriter<?> writer) {
    this(writer, MediaObjectMetadataDescriptor.ENTITY);
  }

  public MediaObjectMetadataWriter(PersistencyWriter<?> writer, String entityName) {
    super(writer);
    this.entityName = entityName;
    this.writer.setFieldNames(MediaObjectMetadataDescriptor.FIELDNAMES);
    this.writer.open(entityName);
  }

  @Override
  protected void init() {
  }

  @Override
  protected PersistentTuple generateTuple(MediaObjectMetadataDescriptor entity) {
    if (entity.getValueProvider() instanceof NothingProvider) {
      return null;
    }
    return this.writer.generateTuple(entity.getObjectid(), entity.getDomain(), entity.getKey(),
        entity.getValue());
  }
}
