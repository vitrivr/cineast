package org.vitrivr.cineast.core.features;

import java.util.LinkedHashMap;
import org.vitrivr.cineast.core.data.entities.SimpleFulltextFeatureDescriptor;
import org.vitrivr.cineast.core.data.segments.SegmentContainer;
import org.vitrivr.cineast.core.features.abstracts.AbstractTextRetriever;

public class AudioTranscriptionSearch extends AbstractTextRetriever {

  public static final String AUDIO_TRANSCRIPTION_TABLE_NAME = "features_audiotranscription";

  /**
   * Default constructor for {@link AudioTranscriptionSearch}.
   */
  public AudioTranscriptionSearch() {
    super(AudioTranscriptionSearch.AUDIO_TRANSCRIPTION_TABLE_NAME);
  }

  public AudioTranscriptionSearch(LinkedHashMap<String, String> properties) {
    super(AUDIO_TRANSCRIPTION_TABLE_NAME, properties);
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