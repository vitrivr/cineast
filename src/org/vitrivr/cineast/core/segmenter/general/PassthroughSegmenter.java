package org.vitrivr.cineast.core.segmenter.general;

import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.TimeUnit;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.vitrivr.cineast.core.data.entities.MediaObjectDescriptor;
import org.vitrivr.cineast.core.data.segments.SegmentContainer;
import org.vitrivr.cineast.core.decode.general.Decoder;
import org.vitrivr.cineast.core.segmenter.video.TRECVidMSRSegmenter;

/**
 * A simple segmenter that passes the output from the decoder straight back to the orchestrator.
 *
 * No aggregation or post-processing will take place besides wrapping of the content in a SegmentContainer.
 *
 * @author rgasser
 * @version 1.0
 * @created 14.01.17
 */
public abstract class PassthroughSegmenter<T> implements Segmenter<T> {

  /**
   *
   */
  private static final Logger LOGGER = LogManager.getLogger();

  /**
   * Decoder<T> used for file decoding.
   */
  private Decoder<T> decoder;

  /**
   * A SynchronousQueue used to pass the element to the orchestrating thread.
   */
  private final SynchronousQueue<T> queue = new SynchronousQueue<T>();

  /**
   * Internal flag that indicates whether the {@link PassthroughSegmenter} is still running (= true).
   */
  private volatile boolean running = false;

  /**
   * A flag indicating whether more {@link SegmentContainer}s are to be expected from this {@link TRECVidMSRSegmenter}.
   */
  private volatile boolean complete = false;


  /**
   * Method used to initialize the {@link PassthroughSegmenter}. A class implementing the {@link Decoder} interface with the same type must be provided.
   *
   * @param decoder Decoder used for media-decoding.
   * @param object Media object that is about to be segmented.
   */
  @Override
  public void init(Decoder<T> decoder, MediaObjectDescriptor object) {
    this.decoder = decoder;
    this.complete = false;
  }

  /**
   * Returns the next SegmentContainer from the source OR null if there are no more segments in the queue. Because a SynchronizedQueue is used for the handover, this method will block until the SegmementContainer is made available in the run method (on another thread).
   */
  @Override
  public SegmentContainer getNext() throws InterruptedException {
    final T content = this.queue.poll(5, TimeUnit.SECONDS);
    if (content == null) {
      synchronized (this) {
        if (!this.running) {
          this.complete = true;
          return null;
        } else {
          return null;
        }
      }
    }
    return this.getSegmentFromContent(content);
  }

  /**
   * Indicates whether the Segmenter is complete i.e. no new segments are to be expected. extraction_images.json
   *
   * @return true if work is complete, false otherwise.
   */
  @Override
  public synchronized boolean complete() {
    return this.complete;
  }

  /**
   * Closes the decoder.
   */
  @Override
  public synchronized void close() {
    if (!this.running) {
      if (this.decoder != null) {
        this.decoder.close();
      }
    } else {
      throw new IllegalStateException("You cannot close the PassthroughSegmenter while it is running.");
    }
  }

  /**
   * When an object implementing interface <code>Runnable</code> is used to create a thread, starting the thread causes the object's
   * <code>run</code> method to be called in that separately executing
   * thread.
   * <p>
   * The general contract of the method <code>run</code> is that it may take any action whatsoever.
   *
   * @see Thread#run()
   */
  @Override
  public void run() {
    /* Sets the running flag to true. */
    synchronized (this) {
      this.running = true;
    }

    try {
      while (!this.decoder.complete()) {
        final T t = this.decoder.getNext();
        if (t != null) {
          this.queue.put(t);
        }
      }
    } catch (InterruptedException e) {
      LOGGER.log(Level.ERROR, "The thread that runs the PassthroughSegmenter was interrupted: {}", e);
    }

    /* Sets the running flag to false. */
    synchronized (this) {
      this.running = false;
    }
  }

  /**
   *
   */
  protected abstract SegmentContainer getSegmentFromContent(T content);
}
