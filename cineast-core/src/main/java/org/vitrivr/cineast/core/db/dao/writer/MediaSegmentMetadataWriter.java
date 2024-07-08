package org.vitrivr.cineast.core.db.dao.writer;

import org.vitrivr.cineast.core.data.entities.MediaSegmentMetadataDescriptor;
import org.vitrivr.cineast.core.db.PersistencyWriter;
import org.vitrivr.cineast.core.db.PersistentTuple;


public class MediaSegmentMetadataWriter extends AbstractBatchedEntityWriter<MediaSegmentMetadataDescriptor> {

  private final String entityName;

  public <R> MediaSegmentMetadataWriter(PersistencyWriter<R> writer, String testSegMetaEntityName) {
    super(writer);
    this.entityName = testSegMetaEntityName;
    this.init();
  }

  @Override
  protected void init() {
    this.writer.setFieldNames(MediaSegmentMetadataDescriptor.FIELDNAMES);
    this.writer.open(entityName);
  }

  @Override
  public PersistentTuple generateTuple(MediaSegmentMetadataDescriptor entity) {
    return this.writer.generateTuple(entity.getSegmentId(), entity.getDomain(), entity.getKey(),
        entity.getValue());
  }

}
