package org.vitrivr.cineast.core.features;

import java.util.Map;
import org.vitrivr.cineast.core.data.entities.SimpleFulltextFeatureDescriptor;
import org.vitrivr.cineast.core.data.segments.SegmentContainer;
import org.vitrivr.cineast.core.features.abstracts.AbstractTextRetriever;

public class SubtitleFulltextSearch extends AbstractTextRetriever {

  private static final String SUBTITLE_TABLE_NAME = "features_subtitles";

  /**
   * Default constructor for {@link SubtitleFulltextSearch}.
   */
  public SubtitleFulltextSearch() {
    super(SUBTITLE_TABLE_NAME);
  }

  public SubtitleFulltextSearch(Map<String, String> properties) {
    super(SUBTITLE_TABLE_NAME, properties);
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