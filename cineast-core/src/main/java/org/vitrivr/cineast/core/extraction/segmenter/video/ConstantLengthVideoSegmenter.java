package org.vitrivr.cineast.core.extraction.segmenter.video;

import java.util.Map;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import org.vitrivr.cineast.core.data.entities.MediaObjectDescriptor;
import org.vitrivr.cineast.core.data.frames.VideoFrame;
import org.vitrivr.cineast.core.data.segments.SegmentContainer;
import org.vitrivr.cineast.core.data.segments.VideoSegment;
import org.vitrivr.cineast.core.extraction.ExtractionContextProvider;
import org.vitrivr.cineast.core.extraction.decode.general.Decoder;
import org.vitrivr.cineast.core.extraction.segmenter.general.Segmenter;

public class ConstantLengthVideoSegmenter implements Segmenter<VideoFrame> {

  private static final int SEGMENT_QUEUE_LENGTH = 10;
  private static final int SEGMENT_POLLING_TIMEOUT = 10000;

  /**
   * Key in the configuration map used to configure the length setting.
   */
  private static final String PROPERTY_LENGTH_KEY = "length";

  /**
   * Key in the configuration map used to configure the length setting.
   */
  private static final float PROPERTY_LENGTH_DEFAULT = 1.0f;

  /**
   * Key in the configuration map used to configure the overlap setting.
   */
  private static final Float PROPERTY_OVERLAP_DEFAULT = 1.0f;

  private final LinkedBlockingQueue<SegmentContainer> outputQueue = new LinkedBlockingQueue<>(SEGMENT_QUEUE_LENGTH);

  /**
   * A flag indicating whether the segmenter has completed its work.
   */
  private final AtomicBoolean complete = new AtomicBoolean(false);

  private Decoder<VideoFrame> decoder;

  private final float length;

  public ConstantLengthVideoSegmenter(float targetSegmentLengthSeconds) {
    if (targetSegmentLengthSeconds <= 0f) {
      throw new IllegalArgumentException("Segment length must be positive");
    }
    this.length = targetSegmentLengthSeconds;
  }

  public ConstantLengthVideoSegmenter(ExtractionContextProvider context, Map<String, String> properties) {
    this(Float.parseFloat(properties.getOrDefault(PROPERTY_LENGTH_KEY, Float.toString(PROPERTY_LENGTH_DEFAULT))));
  }

  @Override
  public void init(Decoder<VideoFrame> decoder, MediaObjectDescriptor object) {
    this.decoder = decoder;
    this.outputQueue.clear();
    this.complete.set(false);
  }

  @Override
  public SegmentContainer getNext() throws InterruptedException {
    SegmentContainer segment = this.outputQueue.poll(SEGMENT_POLLING_TIMEOUT, TimeUnit.MILLISECONDS);
    if (segment == null) {
      this.complete.set(this.decoder.complete());
    }
    return segment;
  }

  @Override
  public boolean complete() {
    return this.outputQueue.isEmpty() && this.complete.get();
  }

  @Override
  public void close() {

  }

  @Override
  public void run() {

    VideoSegment currentSegment = new VideoSegment();

    float lastBoundary = 0;

    while (!decoder.complete()) {

      VideoFrame frame = decoder.getNext();

      if (frame == null) {
        break;
      }

      currentSegment.addVideoFrame(frame);

      if (frame.getTimestampSeconds() >= (lastBoundary + length)) {
        lastBoundary = frame.getTimestampSeconds();
        try {
          this.outputQueue.put(currentSegment);
        } catch (InterruptedException e) {
          //can be ignored
        }
        currentSegment = new VideoSegment();
      }

    }

    if (currentSegment.getNumberOfFrames() > 0) {
      try {
        this.outputQueue.put(currentSegment);
      } catch (InterruptedException e) {
        //can be ignored
      }
    }

    this.complete.set(true);

  }
}
