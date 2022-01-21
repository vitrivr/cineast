package org.vitrivr.cineast.core.db.dao.writer;

import org.vitrivr.cineast.core.data.entities.MediaSegmentMetadataDescriptor;
import org.vitrivr.cineast.core.db.PersistencyWriter;
import org.vitrivr.cineast.core.db.PersistentTuple;


public class MediaSegmentMetadataWriter extends AbstractBatchedEntityWriter<MediaSegmentMetadataDescriptor> {

  private final String tableName;

  public MediaSegmentMetadataWriter(PersistencyWriter<?> writer, int batchsize) {
    this(writer, batchsize, MediaSegmentMetadataDescriptor.ENTITY);
  }

  public <R> MediaSegmentMetadataWriter(PersistencyWriter<R> writer, int batchsize, String testSegMetaTableName) {
    super(writer, batchsize, false);
    this.tableName = testSegMetaTableName;
    this.init();
  }

  @Override
  protected void init() {
    this.writer.setFieldNames(MediaSegmentMetadataDescriptor.FIELDNAMES);
    this.writer.open(tableName);
  }

  @Override
  public PersistentTuple generateTuple(MediaSegmentMetadataDescriptor entity) {
    return this.writer.generateTuple(entity.getSegmentId(), entity.getDomain(), entity.getKey(),
        entity.getValue());
  }

}
