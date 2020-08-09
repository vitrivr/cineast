package org.vitrivr.cineast.core.features;

import org.vitrivr.cineast.core.features.abstracts.QueuedTextRetriever;

public class AudioTranscriptionSearch extends QueuedTextRetriever {

  public static final String AUDIO_TRANSCRIPTION_TABLE_NAME = "features_audiotranscription";

  /**
   * Default constructor for {@link AudioTranscriptionSearch}.
   */
  public AudioTranscriptionSearch() {
    super(AudioTranscriptionSearch.AUDIO_TRANSCRIPTION_TABLE_NAME);
  }
}
