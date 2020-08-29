package org.vitrivr.cineast.standalone.importer.redhen;

import com.google.common.collect.LinkedListMultimap;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.vitrivr.cineast.core.data.entities.MediaSegmentDescriptor;
import org.vitrivr.cineast.core.data.providers.primitive.FloatArrayTypeProvider;
import org.vitrivr.cineast.core.data.providers.primitive.PrimitiveTypeProvider;
import org.vitrivr.cineast.core.data.providers.primitive.StringTypeProvider;
import org.vitrivr.cineast.core.importer.Importer;
import org.vitrivr.cineast.core.util.pose.JsonPreEmbeddingReader;
import org.vitrivr.cineast.core.util.pose.PreEmbeddingReader;
import org.vitrivr.cineast.core.util.pose.PreEmbeddingReaderInterface;

public class HandPreEmbedImporter implements Importer<Pair<String, float[]>> {

  private final Iterator<ImmutablePair<Integer, float[][]>> frameIterator;
  private final PreEmbeddingReaderInterface reader;
  private ImmutablePair<Integer, float[][]> curFrame;
  private int curPoseIdx;
  private final ListIterator<MediaSegmentDescriptor> segments;
  private MediaSegmentDescriptor curSegment = null;
  private boolean exhausted = false;

  public HandPreEmbedImporter(Path input, List<MediaSegmentDescriptor> segments, boolean useJson) {
    if (useJson) {
      this.reader = new JsonPreEmbeddingReader(input);
    } else {
      this.reader = new PreEmbeddingReader(input);
    }
    this.frameIterator = this.reader.frameIterator();
    this.curFrame = this.frameIterator.next();
    this.curPoseIdx = 0;
    this.segments = segments.listIterator();
    if (this.segments.hasNext()) {
      this.curSegment = this.segments.next();
    }
  }

  @Override
  public Pair<String, float[]> readNext() {
    if (this.curSegment == null || this.exhausted) {
      return null;
    }
    // Cineast frames are 1-indexed versus 0-indexed frames from PoseIterator
    int frame1 = this.curFrame.left + 1;
    while (this.curSegment != null && frame1 > this.curSegment.getEnd()) {
      if (!this.segments.hasNext()) {
        System.out.println("no more segments");
        this.exhausted = true;
        return null;
      }
      this.curSegment = this.segments.next();
    }
    ImmutablePair<String, float[]> result = new ImmutablePair<>(
      this.curSegment.getSegmentId(),
      this.curFrame.right[this.curPoseIdx]
    );
    // Get next
    this.curPoseIdx++;
    if (this.curPoseIdx >= this.curFrame.right.length) {
      this.curPoseIdx = 0;
      if (!this.frameIterator.hasNext()) {
        System.out.println("no more frames");
        this.exhausted = true;
        return null;
      }
      this.curFrame = this.frameIterator.next();
    }
    return result;
  }

  @Override
  public Map<String, PrimitiveTypeProvider> convert(Pair<String, float[]> data) {
    HashMap<String, PrimitiveTypeProvider> map = new HashMap<>(2);
    map.put("id", new StringTypeProvider(data.getLeft()));
    map.put("feature", new FloatArrayTypeProvider(data.getRight()));
    return map;
  }
}
