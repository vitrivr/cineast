package org.vitrivr.cineast.core.db.dao.writer;

import org.vitrivr.cineast.core.data.entities.TagInstance;
import org.vitrivr.cineast.core.data.tag.WeightedTag;
import org.vitrivr.cineast.core.db.PersistencyWriter;
import org.vitrivr.cineast.core.db.PersistentTuple;

public class BatchedTagWriter extends AbstractBatchedEntityWriter<TagInstance> {

  private final String entityname;

  public BatchedTagWriter(PersistencyWriter<?> writer, String entityname) {
    this(writer, entityname, 1);
  }

  public BatchedTagWriter(PersistencyWriter<?> writer, String entityname, int batchsize) {
    super(writer, batchsize, false);
    this.entityname = entityname;
    this.init();
  }

  @Override
  public void init() {
    this.writer.setFieldNames("id", "tagid", "score");
    this.writer.open(this.entityname);
  }

  @Override
  protected PersistentTuple generateTuple(TagInstance entity) {
    float score = 1f;
    if(entity.tag instanceof WeightedTag){
      score = ((WeightedTag)entity.tag).getWeight();
    }
    return this.writer.generateTuple(entity.id, entity.tag.getId(), score);
  }

}

