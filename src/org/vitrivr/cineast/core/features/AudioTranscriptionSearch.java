package org.vitrivr.cineast.core.features;

import org.vitrivr.cineast.core.config.ReadableQueryConfig;
import org.vitrivr.cineast.core.data.entities.SimpleFulltextFeatureDescriptor;
import org.vitrivr.cineast.core.data.segments.SegmentContainer;
import org.vitrivr.cineast.core.features.abstracts.SolrTextRetriever;

public class AudioTranscriptionSearch extends SolrTextRetriever {

  public static final String AUDIO_TRANSCRIPTION_TABLE_NAME = "features_audiotranscription";

  /**
   * Default constructor for {@link AudioTranscriptionSearch}.
   */
  public AudioTranscriptionSearch() {
    super(AudioTranscriptionSearch.AUDIO_TRANSCRIPTION_TABLE_NAME);
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