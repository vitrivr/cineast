package org.vitrivr.cineast.core.db.dao.writer;

import org.vitrivr.cineast.core.data.entities.SimpleFulltextFeatureDescriptor;
import org.vitrivr.cineast.core.db.PersistencyWriter;
import org.vitrivr.cineast.core.db.PersistentTuple;

public class SimpleFulltextFeatureDescriptorWriter extends AbstractBatchedEntityWriter<SimpleFulltextFeatureDescriptor> {

  private final String entityname;

  public SimpleFulltextFeatureDescriptorWriter(PersistencyWriter<?> writer, String entityname) {
    this(writer, entityname, 1);
  }

  public SimpleFulltextFeatureDescriptorWriter(PersistencyWriter<?> writer, String entityname, int batchsize) {
    super(writer, batchsize, false);
    this.entityname = entityname;
    this.init();
  }

  @Override
  public void init() {
    this.writer.open(this.entityname);
  }

  @Override
  protected PersistentTuple generateTuple(SimpleFulltextFeatureDescriptor entity) {
    return this.writer.generateTuple(entity.getSegmentId(), entity.getFeature());
  }
}
