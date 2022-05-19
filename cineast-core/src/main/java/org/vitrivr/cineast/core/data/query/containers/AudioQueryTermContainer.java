package org.vitrivr.cineast.core.data.query.containers;

import java.util.List;
import java.util.Objects;
import org.vitrivr.cineast.core.data.frames.AudioDescriptor;
import org.vitrivr.cineast.core.data.frames.AudioFrame;
import org.vitrivr.cineast.core.util.dsp.fft.STFT;
import org.vitrivr.cineast.core.util.dsp.fft.windows.WindowFunction;
import org.vitrivr.cineast.core.util.web.AudioParser;


public class AudioQueryTermContainer extends AbstractQueryTermContainer {

  /**
   * List of {@link AudioFrame}s.
   */
  private final List<AudioFrame> frames;

  /**
   * Total number of samples in the AudioSegment.
   */
  private int totalSamples;

  /**
   * Total duration of the AudioSegment in seconds.
   */
  private float totalDuration;

  /**
   * {@link AudioDescriptor} describing the properties of the underlying audio stream.
   */
  private AudioDescriptor descriptor;

  /**
   * Constructs an {@link AudioQueryTermContainer} from base 64 encoded wave audio data. The constructor assumes the following audio settings: 22050Hz, 1 Channel, 16bit PCM
   *
   * @param data The audio data that should be converted.
   */
  public AudioQueryTermContainer(String data) {
    this(AudioParser.parseWaveAudio(data, 22050.0f, 1));
  }

  /**
   * Returns a list of audio-frames contained in the AudioSegment. The default implementation returns a list containing one, empty frame.
   * <p>
   * List auf audio-frames in the audio-segment.
   */
  public AudioQueryTermContainer(List<AudioFrame> frames) {
    this.frames = frames;
    for (AudioFrame frame : this.frames) {
      if (this.descriptor == null) {
        this.descriptor = frame.getDescriptor();
      }
      if (!this.descriptor.equals(frame.getDescriptor())) {
        throw new IllegalArgumentException("All the provided AudioFrames must share the same AudioDescriptor!");
      }
      this.totalSamples += frame.numberOfSamples();
      this.totalDuration += frame.getDuration();
    }
  }

  @Override
  public List<AudioFrame> getAudioFrames() {
    return this.frames;
  }

  /**
   * Getter for the total number of samples in the AudioSegment.
   */
  @Override
  public int getNumberOfSamples() {
    return this.totalSamples;
  }

  /**
   * Getter for the total duration of the AudioSegment.
   */
  @Override
  public float getAudioDuration() {
    return totalDuration;
  }

  /**
   * Calculates and returns the Short-term Fourier Transform of the current AudioSegment.
   *
   * @param windowsize Size of the window used during STFT. Must be a power of two.
   * @param overlap    Overlap in samples between two subsequent windows.
   * @param padding    Zero-padding before and after the actual sample data. Causes the window to contain (windowsize-2*padding) data-points..
   * @param function   WindowFunction to apply before calculating the STFT.
   * @return STFT of the current AudioSegment.
   */
  @Override
  public STFT getSTFT(int windowsize, int overlap, int padding, WindowFunction function) {
    if (2 * padding >= windowsize) {
      throw new IllegalArgumentException("The combined padding must be smaller than the sample window.");
    }
    STFT stft = new STFT(windowsize, overlap, padding, function, this.descriptor.getSamplingrate());
    stft.forward(this.getMeanSamplesAsDouble());
    return stft;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    if (!super.equals(o)) {
      return false;
    }
    AudioQueryTermContainer that = (AudioQueryTermContainer) o;
    return totalSamples == that.totalSamples && Float.compare(that.totalDuration, totalDuration) == 0 && Objects.equals(frames, that.frames) && Objects.equals(descriptor, that.descriptor);
  }

  @Override
  public int hashCode() {
    return Objects.hash(super.hashCode(), frames, totalSamples, totalDuration, descriptor);
  }
}
