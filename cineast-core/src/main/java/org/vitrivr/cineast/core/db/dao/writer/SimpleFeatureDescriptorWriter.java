package org.vitrivr.cineast.core.db.dao.writer;

import org.vitrivr.cineast.core.data.ReadableFloatVector;
import org.vitrivr.cineast.core.data.entities.SimpleFeatureDescriptor;
import org.vitrivr.cineast.core.db.PersistencyWriter;
import org.vitrivr.cineast.core.db.PersistentTuple;


public class SimpleFeatureDescriptorWriter extends AbstractBatchedEntityWriter<SimpleFeatureDescriptor> {

  private final String entityname;

  public SimpleFeatureDescriptorWriter(PersistencyWriter<?> writer, String entityname) {
    super(writer);
    this.entityname = entityname;
    this.init();
  }

  @Override
  public void init() {
    this.writer.open(this.entityname);
  }

  @Override
  protected PersistentTuple generateTuple(SimpleFeatureDescriptor entity) {
    float[] array = ReadableFloatVector.toArray(entity.getFeature());
    return this.writer.generateTuple(entity.getSegmentId(), array);
  }
}
