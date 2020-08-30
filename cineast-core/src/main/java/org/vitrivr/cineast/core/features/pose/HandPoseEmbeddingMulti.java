package org.vitrivr.cineast.core.features.pose;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.vitrivr.cineast.core.config.ReadableQueryConfig;
import org.vitrivr.cineast.core.data.score.ScoreElement;
import org.vitrivr.cineast.core.data.segments.SegmentContainer;
import org.vitrivr.cineast.core.features.multi.MultiFeature;
import static org.vitrivr.cineast.core.util.pose.PoseFlip.flip;
import static org.vitrivr.cineast.core.util.pose.PoseFlip.modelFlip;

public class HandPoseEmbeddingMulti extends MultiFeature<HandPoseEmbedding> {
  private static final Logger LOGGER = LogManager.getLogger();
  final private HandPoseEmbedding leftHandEmbedding;
  final private HandPoseEmbedding rightHandEmbedding;

  public HandPoseEmbeddingMulti() {
    this.rightHandEmbedding = new HandPoseEmbedding(false);
    this.leftHandEmbedding = new HandPoseEmbedding(true);
  }

  private HandPoseEmbedding embedderOfModelName(String modelName) {
    if (modelName.equals("LEFT_HAND_IN_BODY_25")) {
      return this.leftHandEmbedding;
    } else if (modelName.equals("RIGHT_HAND_IN_BODY_25")) {
      return this.rightHandEmbedding;
    } else {
      return null;
    }
  }

  private void warnModelName(String modelName) {
    LOGGER.warn(
        "Tried to getSimilar(...) with an unsupported pose model: {}. " +
            "Only LEFT_ and RIGHT_ ...HAND_IN_BODY_25 are supported. Returning empty list.",
        modelName
    );
  }

  @Override
  public List<ScoreElement> getSimilar(SegmentContainer sc, ReadableQueryConfig qc) {
    Optional<String> poseModel = sc.getPoseModel();
    if (!poseModel.isPresent()) {
      LOGGER.warn("Tried to getSimilar(...) with a SegmentContainer without a pose model. Returning empty list.");
      return Collections.emptyList();
    }
    String modelName = poseModel.get();
    float[][][] poses = sc.getPose();
    ArrayList<ScoreElement> results = new ArrayList<>();
    boolean[] orientations = sc.getOrientations();
    if (orientations[0]) {
      HandPoseEmbedding embedder = embedderOfModelName(modelName);
      if (embedder == null) {
        warnModelName(modelName);
        return Collections.emptyList();
      }
      results.addAll(embedder.getSimilar(poses, qc));
    }
    if (orientations[1]) {
      HandPoseEmbedding embedder = embedderOfModelName(modelFlip(modelName));
      if (embedder == null) {
        warnModelName(modelName);
        return Collections.emptyList();
      }
      results.addAll(embedder.getSimilar(flip(poses), qc));
    }
    return results;
  }

  @Override
  public List<ScoreElement> getSimilar(String segmentId, ReadableQueryConfig qc) {
    LOGGER.warn("Similarity search between frames not implemented for pose. Returning empty list.");
    return Collections.emptyList();
  }

  @Override
  public Iterator<HandPoseEmbedding> getSubOperators() {
    return Stream.of(this.leftHandEmbedding, this.rightHandEmbedding).iterator();
  }
}
