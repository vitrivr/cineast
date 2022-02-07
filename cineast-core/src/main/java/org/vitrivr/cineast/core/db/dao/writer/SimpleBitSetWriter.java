package org.vitrivr.cineast.core.db.dao.writer;

import com.googlecode.javaewah.datastructure.BitSet;
import org.apache.commons.lang3.tuple.Pair;
import org.vitrivr.cineast.core.db.PersistencyWriter;
import org.vitrivr.cineast.core.db.PersistentTuple;


public class SimpleBitSetWriter extends AbstractBatchedEntityWriter<Pair<String, BitSet>> {

  private final String entityName;

  public SimpleBitSetWriter(PersistencyWriter<?> writer, String entityName) {
    super(writer);
    this.entityName = entityName;
    this.init();
  }

  @Override
  protected void init() {
    this.writer.open(this.entityName);
  }

  @Override
  protected PersistentTuple generateTuple(Pair<String, BitSet> entity) {
    return this.writer.generateTuple(entity.getKey(), entity.getValue());
  }
}

