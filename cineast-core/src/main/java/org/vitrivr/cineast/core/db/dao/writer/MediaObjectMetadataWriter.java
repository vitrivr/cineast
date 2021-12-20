package org.vitrivr.cineast.core.db.dao.writer;

import org.vitrivr.cineast.core.data.entities.MediaObjectMetadataDescriptor;
import org.vitrivr.cineast.core.data.providers.primitive.NothingProvider;
import org.vitrivr.cineast.core.db.PersistencyWriter;
import org.vitrivr.cineast.core.db.PersistentTuple;


public class MediaObjectMetadataWriter extends AbstractBatchedEntityWriter<MediaObjectMetadataDescriptor> {

  private final String tableName;

  public MediaObjectMetadataWriter(PersistencyWriter<?> writer, int batchsize) {
    this(writer, batchsize, MediaObjectMetadataDescriptor.ENTITY);
  }

  public MediaObjectMetadataWriter(PersistencyWriter<?> writer, int batchsize, String tableName) {
    super(writer, batchsize, false);
    this.tableName = tableName;
    this.init();
  }

  @Override
  protected void init() {
    this.writer.setFieldNames(MediaObjectMetadataDescriptor.FIELDNAMES);
    this.writer.open(tableName);
  }

  @Override
  protected PersistentTuple generateTuple(MediaObjectMetadataDescriptor entity) {
    if (entity.getValueProvider() instanceof NothingProvider) {
      return null;
    }
    return this.writer.generateTuple(entity.getObjectId(), entity.getDomain(), entity.getKey(),
        entity.getValue());
  }
}
