package org.vitrivr.cineast.core.extraction.segmenter.image;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import org.vitrivr.cineast.core.data.MediaType;
import org.vitrivr.cineast.core.data.Pair;
import org.vitrivr.cineast.core.data.entities.MediaObjectDescriptor;
import org.vitrivr.cineast.core.data.raw.CachedDataFactory;
import org.vitrivr.cineast.core.data.segments.ImageSegment;
import org.vitrivr.cineast.core.data.segments.SegmentContainer;
import org.vitrivr.cineast.core.extraction.ExtractionContextProvider;
import org.vitrivr.cineast.core.extraction.decode.general.Decoder;
import org.vitrivr.cineast.core.extraction.decode.image.ImageSequence;
import org.vitrivr.cineast.core.extraction.idgenerator.FileNameObjectIdGenerator;
import org.vitrivr.cineast.core.extraction.segmenter.general.Segmenter;

import java.awt.image.BufferedImage;

import java.nio.file.Path;
import java.util.Optional;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * A {@link Segmenter} for {@link ImageSequence}s.
 *
 */
public class ImageSequenceSegmenter implements Segmenter<ImageSequence> {

  /** Logger facility. */
  private static final Logger LOGGER = LogManager.getLogger();

  /** List of {@link BufferedImage}s currently processed.*/
  private final Queue<SegmentContainer> segments = new ConcurrentLinkedQueue<>();

  /** The {@link CachedDataFactory} that is used to create {@link org.vitrivr.cineast.core.data.raw.images.MultiImage}s. */
  private final CachedDataFactory factory;

  /**
   * Instance of {@link FileNameObjectIdGenerator}.
   *
   * Only Used in case that IDs should be derived from filenames which in the case of {@link MediaType.IMAGE_SEQUENCE} should also be done for the segments.
   */
  private final FileNameObjectIdGenerator idgenerator;

  /** {@link Decoder} instance used to decode {@link ImageSequence}. */
  private Decoder<ImageSequence> decoder = null;

  /** Internal flag used to signal that segmentation is complete. */
  private volatile boolean complete = false;

  /** Internal flag used to signal that this {@link ImageSequenceSegmenter} is still running. */
  private volatile boolean running = false;

  /**
   *
   * @param context
   */
  public ImageSequenceSegmenter(ExtractionContextProvider context){
    this.factory = context.cacheConfig().sharedCachedDataFactory();
    if (context.objectIdGenerator() instanceof FileNameObjectIdGenerator) {
      this.idgenerator = (FileNameObjectIdGenerator)context.objectIdGenerator();
    } else {
      this.idgenerator = null;
    }
  }

  @Override
  public synchronized void init(Decoder<ImageSequence> decoder, MediaObjectDescriptor object) {
    this.decoder = decoder;
    this.complete = false;
  }

  @Override
  public SegmentContainer getNext() {
    final SegmentContainer nextContainer = this.segments.poll();
    if (nextContainer == null) {
      synchronized (this) {
        this.complete = !this.running && this.decoder.complete();
      }
    }
    return nextContainer;
  }

  @Override
  public synchronized boolean complete() {
    return this.complete;
  }

  @Override
  public synchronized void close() {
    if (!this.running) {
      if(this.decoder != null){
        this.decoder.close();
      }
    }
  }

  @Override
  public void run() {
    /* Sets the running flag to true. */
    synchronized (this) {
      this.running = true;
    }

    while (!this.decoder.complete()) {
      final ImageSequence sequence = this.decoder.getNext();
      Pair<Path, Optional<BufferedImage>> next;
      while ((next = sequence.pop()) != null)
        if (next.second.isPresent()) {
          final ImageSegment segment = new ImageSegment(next.second.get(), this.factory);
          if (this.idgenerator != null) {
            segment.setId(this.idgenerator.next(next.first, MediaType.IMAGE_SEQUENCE));
          }
          this.segments.offer(segment);
        }
    }

    /* Sets the running flag to false. */
    synchronized (this) {
      this.running = false;
    }
  }
}
