package org.vitrivr.cineast.standalone.importer.redhen;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.concurrent.LinkedBlockingQueue;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.vitrivr.cineast.core.data.entities.MediaSegmentDescriptor;
import org.vitrivr.cineast.core.util.pose.PoseHdf5Reader;
import org.vitrivr.cineast.core.util.pose.PoseIterator;
import org.vitrivr.cineast.core.util.pose.PoseIterator.IndexedPose;
import org.vitrivr.cineast.core.util.pose.PoseNormalize;
import org.vitrivr.cineast.core.util.pose.PoseSpec;
import org.vitrivr.cineast.core.util.pose.PoseSpecs;

public class PoseHdf5Generator implements AutoCloseable, Runnable {

  // This should be a reasonable size since the current approach just blocks on the slowest consumer
  public final int QUEUE_LENGTH = 8;
  private final PoseHdf5Reader hdf5Reader;
  private final Iterator<MediaSegmentDescriptor> segments;
  private final PoseIterator poseIter;
  private final PoseSpecs poseSpecs;
  private MediaSegmentDescriptor curSegment = null;
  private HashMap<String, LinkedBlockingQueue<Optional<Pair<String, float[]>>>> specQueues;

  public PoseHdf5Generator(Path input, List<MediaSegmentDescriptor> segments) {
    this.segments = segments.listIterator();
    if (this.segments.hasNext()) {
      this.curSegment = this.segments.next();
    }
    this.hdf5Reader = new PoseHdf5Reader(input);
    this.poseIter = new PoseIterator(this.hdf5Reader.unsegFrameIterator());
    this.poseSpecs = PoseSpecs.getInstance();
    this.specQueues = new HashMap<>(this.poseSpecs.specs.size());
    for (Entry<String, PoseSpec> poseSpecEntry : this.poseSpecs) {
      this.specQueues.put(poseSpecEntry.getKey(), new LinkedBlockingQueue<>(QUEUE_LENGTH));
    }
  }

  private Pair<String, float[][]> readSegmentPose() {
    if (!this.poseIter.hasNext()) {
      return null;
    }
    IndexedPose curPose = this.poseIter.next();
    // Cineast frames are 1-indexed versus 0-indexed frames from PoseIterator
    int frame1 = curPose.frameIdx + 1;
    if (this.curSegment == null) {
      return null;
    } else if (frame1 > this.curSegment.getEnd()) {
      if (!this.segments.hasNext()) {
        return null;
      }
      // Assuming segments are contiguous and non-empty
      this.curSegment = this.segments.next();
    }
    return new ImmutablePair<>(this.curSegment.getSegmentId(), curPose.pose);
  }

  private Pair<String, ArrayList<Pair<String, float[]>>> readSegmentPreproc() {
    Pair<String, float[][]> segPose = this.readSegmentPose();
    if (segPose == null) {
      return null;
    }
    ArrayList<Pair<String, float[]>> preproccessed = new ArrayList<>();
    for (Entry<String, PoseSpec> poseSpec : this.poseSpecs.specs.entrySet()) {
      float[][] pose = segPose.getRight();
      PoseSpec spec = poseSpec.getValue();
      Optional<float[]> flatPose = PoseNormalize.pipeline(spec, pose);
      if (!flatPose.isPresent()) {
        return null;
      }
      preproccessed.add(new ImmutablePair<>(poseSpec.getKey(), flatPose.get()));
    }
    return new ImmutablePair<>(segPose.getLeft(), preproccessed);
  }

  @Override
  public void close() {
    this.hdf5Reader.close();
  }

  @Override
  public void run() {
    boolean wasInterrupted = false;
    try {
      while (true) {
        Pair<String, ArrayList<Pair<String, float[]>>> segmentPreprocPair = readSegmentPreproc();
        if (segmentPreprocPair == null) {
          break;
        }
        for (Pair<String, float[]> specNamePosePair : segmentPreprocPair.getRight()) {
          this.specQueues.get(specNamePosePair.getLeft()).put(
              Optional.of(
                new ImmutablePair<>(
                  segmentPreprocPair.getLeft(),
                  specNamePosePair.getRight()
                )
              )
          );
        }
      }
    } catch (InterruptedException e) {
      wasInterrupted = true;
    } finally {
      for (LinkedBlockingQueue<Optional<Pair<String, float[]>>> queue : this.specQueues.values()) {
        try {
          queue.put(Optional.empty());
        } catch (InterruptedException e) {
          wasInterrupted = true;
        }
      }
    }
    if (wasInterrupted) {
      throw new RuntimeException("Was interrupted while running");
    }
  }

  public Optional<Pair<String, float[]>> takeFrom(String poseSpecName) throws InterruptedException {
    return this.specQueues.get(poseSpecName).take();
  }
}
