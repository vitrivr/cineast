package org.vitrivr.cineast.core.db.dao.writer;

import org.vitrivr.cineast.core.data.entities.MediaSegmentDescriptor;
import org.vitrivr.cineast.core.db.PersistencyWriter;
import org.vitrivr.cineast.core.db.PersistentTuple;


public class MediaSegmentWriter extends AbstractBatchedEntityWriter<MediaSegmentDescriptor> {

  public MediaSegmentWriter(PersistencyWriter<?> writer) {
    super(writer);
  }

  @Override
  protected void init() {
    this.writer.setFieldNames(MediaSegmentDescriptor.FIELDNAMES);
    this.writer.open(MediaSegmentDescriptor.ENTITY);
  }

  @Override
  protected PersistentTuple generateTuple(MediaSegmentDescriptor entity) {
    return this.writer.generateTuple(entity.getSegmentId(), entity.getObjectId(), entity.getSequenceNumber(), entity.getStart(), entity.getEnd(), entity.getStartabs(), entity.getEndabs());
  }
}
