package org.vitrivr.cineast.core.features.exporter;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.BufferOverflowException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.HashMap;
import org.vitrivr.cineast.core.data.frames.AudioFrame;
import org.vitrivr.cineast.core.data.segments.SegmentContainer;
import org.vitrivr.cineast.core.features.abstracts.AbstractSegmentExporter;
import org.vitrivr.cineast.core.util.LogHelper;

/**
 * Exports the audio in a given segment as mono WAV file.
 */
public class AudioSegmentExporter extends AbstractSegmentExporter {

  @Override
  protected String getFileExtension() {
    return "wav";
  }

  @Override
  protected String getDataUrlPrefix() {
    return "data:audio/wav;base64,";
  }

  /**
   * Default constructor
   */
  public AudioSegmentExporter() {
    this(new HashMap<>());
  }

  /**
   * Default constructor. The AudioSegmentExport can be configured via named properties in the provided HashMap.
   * <p>
   * Supported parameters:
   *
   * <ol>
   *      <li>destination: Path where files should be stored.</li>
   * </ol>
   *
   * @param properties HashMap containing named properties
   */
  public AudioSegmentExporter(HashMap<String, String> properties) {
    super(properties);
  }



  /**
   * Processes a SegmentContainer: Extract audio-data and writes to a WAVE file.
   *
   * @param shot SegmentContainer to process.
   */
  @Override
  public void exportToStream(SegmentContainer shot, OutputStream stream) {
    try {

      /* Extract mean samples and perpare byte buffer. */
      short[] data = shot.getMeanSamplesAsShort();
      ByteBuffer buffer = ByteBuffer.allocate(44 + data.length * 2).order(ByteOrder.LITTLE_ENDIAN);

      /* Write header of WAV file. */
      this.writeWaveHeader(buffer, shot.getSamplingrate(), (short) 1, data.length);

      /* Write actual data. */
      for (short sample : data) {
        buffer.putShort(sample);
      }

      stream.write(buffer.array());
    } catch (IOException | BufferOverflowException e) {
      LOGGER.fatal("Could not export audio segment {} due to a serious IO error ({}).", shot.getId(), LogHelper.getStackTrace(e));
    }
  }

  @Override
  public boolean isExportable(SegmentContainer sc) {
    return sc.getNumberOfSamples() > 0;
  }

  /**
   * Writes the WAV header to the ByteBuffer (1 channel).
   *
   * @param buffer       The buffer to which to write the header.
   * @param channels     The number of channels in the WAV file.
   * @param samplingrate Samplingrate of the output file.
   * @param length       Length in bytes of the frames data
   */
  private void writeWaveHeader(ByteBuffer buffer, float samplingrate, short channels, int length) {
    /* Length of the subChunk2. */
    final int subChunk2Length = length * channels * (AudioFrame.BITS_PER_SAMPLE / 8); /* Number of bytes for audio data: NumSamples * NumChannels * BitsPerSample/8. */

    /* RIFF Chunk. */
    buffer.put("RIFF".getBytes());
    buffer.putInt(36 + subChunk2Length);
    buffer.put("WAVE".getBytes()); /* WAV format. */

    /* Format chunk. */
    buffer.put("fmt ".getBytes()); /* Begin of the format chunk. */
    buffer.putInt(16); /* Length of the Format chunk. */
    buffer.putShort((short) 1); /* Format: 1 = Raw PCM (linear quantization). */
    buffer.putShort((short) 1); /* Number of channels. */
    buffer.putInt((int) samplingrate); /* Samplingrate. */
    buffer.putInt((int) (samplingrate * channels * (AudioFrame.BITS_PER_SAMPLE / 8))); /* Byte rate: SampleRate * NumChannels * BitsPerSample/8 */
    buffer.putShort((short) (channels * (AudioFrame.BITS_PER_SAMPLE / 8))); /* Block align: NumChannels * BitsPerSample/8. */
    buffer.putShort((short) (AudioFrame.BITS_PER_SAMPLE)) /* Bits per sample. */;

    /* Data chunk */
    buffer.put("data".getBytes()); /* Begin of the data chunk. */
    buffer.putInt(subChunk2Length); /* Length of the data chunk. */
  }

}
