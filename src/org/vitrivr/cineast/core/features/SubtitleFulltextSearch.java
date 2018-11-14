package org.vitrivr.cineast.core.features;

import org.vitrivr.cineast.core.config.ReadableQueryConfig;
import org.vitrivr.cineast.core.data.entities.SimpleFulltextFeatureDescriptor;
import org.vitrivr.cineast.core.data.segments.SegmentContainer;
import org.vitrivr.cineast.core.features.abstracts.SolrTextRetriever;

import java.util.Arrays;

public class SubtitleFulltextSearch extends SolrTextRetriever {

  /**
   * Default constructor for {@link SubtitleFulltextSearch}.
   */
  public SubtitleFulltextSearch() {
    super("features_asr");
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

  @Override
  protected String[] generateQuery(SegmentContainer sc, ReadableQueryConfig qc) {
    return new String[]{"\"" + sc.getText() + "\""};
  }
}