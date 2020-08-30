package org.vitrivr.cineast.core.features.pose;

import static org.vitrivr.cineast.core.util.pose.PoseFlip.flip;
import static org.vitrivr.cineast.core.util.pose.PoseFlip.modelFlip;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.vitrivr.cineast.core.config.ReadableQueryConfig;
import org.vitrivr.cineast.core.data.score.ScoreElement;
import org.vitrivr.cineast.core.data.segments.SegmentContainer;
import org.vitrivr.cineast.core.features.multi.MultiFeature;
import org.vitrivr.cineast.core.util.pose.PoseSpecs;

public class PoseKeypointsMulti extends MultiFeature<PoseKeypoints> {
  private static final Logger LOGGER = LogManager.getLogger();
  final private HashMap<String, PoseKeypoints> poseSpecFeatures;

  public PoseKeypointsMulti() {
    this.poseSpecFeatures = new HashMap<>();
    for (String poseSpecName : PoseSpecs.getInstance().specs.keySet()) {
      this.poseSpecFeatures.put(poseSpecName,  new PoseKeypoints(poseSpecName));
    }
  }

  @Override
  public List<ScoreElement> getSimilar(SegmentContainer sc, ReadableQueryConfig qc) {
    Optional<String> poseModel = sc.getPoseModel();
    if (!poseModel.isPresent()) {
      LOGGER.warn("Tried to getSimilar(...) with a SegmentContainer without a pose model. Returning empty list.");
      return Collections.emptyList();
    }
    float[][][] poses = sc.getPose();
    ArrayList<ScoreElement> results = new ArrayList<>();
    boolean[] orientations = sc.getOrientations();
    if (orientations[0]) {
      results.addAll(this.poseSpecFeatures.get(poseModel.get()).getSimilar(poses, qc));
    }
    if (orientations[1]) {
      results.addAll(this.poseSpecFeatures.get(modelFlip(poseModel.get())).getSimilar(flip(poses), qc));
    }
    return results;
  }

  @Override
  public List<ScoreElement> getSimilar(String segmentId, ReadableQueryConfig qc) {
    LOGGER.warn("Similarity search between frames not implemented for pose. Returning empty list.");
    return Collections.emptyList();
  }

  @Override
  public Iterator<PoseKeypoints> getSubOperators() {
    return this.poseSpecFeatures.values().iterator();
  }
}
