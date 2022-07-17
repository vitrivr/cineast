package org.vitrivr.cineast.core.db.dao.writer;

import org.vitrivr.cineast.core.data.entities.SimpleFulltextFeatureDescriptor;
import org.vitrivr.cineast.core.db.PersistencyWriter;
import org.vitrivr.cineast.core.db.PersistentTuple;

public class SimpleFulltextFeatureDescriptorWriter extends AbstractBatchedEntityWriter<SimpleFulltextFeatureDescriptor> {

  private final String entityname;

  public SimpleFulltextFeatureDescriptorWriter(PersistencyWriter<?> writer, String entityname) {
    super(writer);
    if (entityname == null) {
      throw new IllegalArgumentException("An entity name cannot be null");
    }
    this.entityname = entityname;
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
