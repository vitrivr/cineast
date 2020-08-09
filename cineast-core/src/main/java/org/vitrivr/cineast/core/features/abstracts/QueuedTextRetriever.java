package org.vitrivr.cineast.core.features.abstracts;

import org.vitrivr.cineast.core.data.entities.SimpleFulltextFeatureDescriptor;
import org.vitrivr.cineast.core.data.segments.SegmentContainer;

abstract public class QueuedTextRetriever extends AbstractTextRetriever {

  /**
   * Constructor for {@link QueuedTextRetriever}
   *
   * @param tableName Name of the table/entity used to store the data
   */
  public QueuedTextRetriever(String tableName) {
    super(tableName);
  }

  /**
   * Extracts the subtitle text and ingests it using the {@link SimpleFulltextFeatureDescriptor}.
   *
   * @param shot The {@link SegmentContainer} that should be processed.
   */
  @Override
  public void processSegment(SegmentContainer shot) {
    shot.getSubtitleItems().stream()
        .map(s -> new SimpleFulltextFeatureDescriptor(shot.getId(), s.getText()))
        .forEach(s -> this.writer.write(s));
  }
}
