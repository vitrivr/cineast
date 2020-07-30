package org.vitrivr.cineast.core.features.multi;

import java.util.Iterator;
import org.vitrivr.cineast.core.data.segments.SegmentContainer;
import org.vitrivr.cineast.core.db.PersistencyWriterSupplier;
import org.vitrivr.cineast.core.features.extractor.Extractor;

/**
 * This abstract class enables creating extractors which wrap several sub-extractors.
 */
public interface MultiExtractor extends MultiPersistentOperator, Extractor {
  Iterator<? extends Extractor> getSubOperators();

  @Override
  default void init(PersistencyWriterSupplier phandlerSupply, int batchSize) {
    for (Iterator<? extends Extractor> it = this.getSubOperators(); it.hasNext(); ) {
      Extractor extractor = it.next();
      extractor.init(phandlerSupply, batchSize);
    }
  }

  @Override
  default void processSegment(SegmentContainer shot) {
    for (Iterator<? extends Extractor> it = this.getSubOperators(); it.hasNext(); ) {
      Extractor extractor = it.next();
      extractor.processSegment(shot);
    }
  }

  @Override
  default void finish() {
    for (Iterator<? extends Extractor> it = this.getSubOperators(); it.hasNext(); ) {
      Extractor extractor = it.next();
      extractor.finish();
    }
  }
}
