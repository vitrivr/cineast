package org.vitrivr.cineast.core.features.multi;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import org.vitrivr.cineast.core.config.ReadableQueryConfig;
import org.vitrivr.cineast.core.data.score.ScoreElement;
import org.vitrivr.cineast.core.data.segments.SegmentContainer;
import org.vitrivr.cineast.core.db.DBSelectorSupplier;
import org.vitrivr.cineast.core.features.retriever.Retriever;

public interface MultiRetriever extends MultiPersistentOperator, Retriever {
  Iterator<? extends Retriever> getSubOperators();

  @Override
  default void init(DBSelectorSupplier selectorSupply) {
    for (Iterator<? extends Retriever> it = this.getSubOperators(); it.hasNext(); ) {
      Retriever retriever = it.next();
      retriever.init(selectorSupply);
    }
  }

  @Override
  default List<ScoreElement> getSimilar(SegmentContainer sc, ReadableQueryConfig qc) {
    ArrayList<ScoreElement> result = new ArrayList<>();
    for (Iterator<? extends Retriever> it = this.getSubOperators(); it.hasNext(); ) {
      Retriever retriever = it.next();
      result.addAll(retriever.getSimilar(sc, qc));
    }
    return result;
  }

  @Override
  default List<ScoreElement> getSimilar(String segmentId, ReadableQueryConfig qc) {
    ArrayList<ScoreElement> result = new ArrayList<>();
    for (Iterator<? extends Retriever> it = this.getSubOperators(); it.hasNext(); ) {
      Retriever retriever = it.next();
      result.addAll(retriever.getSimilar(segmentId, qc));
    }
    return result;
  }

  @Override
  default void finish() {
    for (Iterator<? extends Retriever> it = this.getSubOperators(); it.hasNext(); ) {
      Retriever retriever = it.next();
      retriever.finish();
    }
  }
}
