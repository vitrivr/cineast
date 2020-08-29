package org.vitrivr.cineast.core.features.pose;

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

public class HandPoseEmbeddingMulti extends MultiFeature<HandPoseEmbedding> {
  private static final Logger LOGGER = LogManager.getLogger();
  final private HandPoseEmbedding leftHandEmbedding;
  final private HandPoseEmbedding rightHandEmbedding;

  public HandPoseEmbeddingMulti() {
    this.rightHandEmbedding = new HandPoseEmbedding(false);
    this.leftHandEmbedding = new HandPoseEmbedding(true);
  }

  @Override
  public List<ScoreElement> getSimilar(SegmentContainer sc, ReadableQueryConfig qc) {
    Optional<String> poseModel = sc.getPoseModel();
    if (!poseModel.isPresent()) {
      LOGGER.warn("Tried to getSimilar(...) with a SegmentContainer without a pose model. Returning empty list.");
      return Collections.emptyList();
    }
    HandPoseEmbedding embedder;
    if (poseModel.get().equals("LEFT_HAND_IN_BODY_25")) {
      embedder = this.leftHandEmbedding;
    } else if (poseModel.get().equals("RIGHT_HAND_IN_BODY_25")) {
      embedder = this.rightHandEmbedding;
    } else {
      LOGGER.warn(
          "Tried to getSimilar(...) with an unsupported pose model: {}. " +
          "Only LEFT_ and RIGHT_ ...HAND_IN_BODY_25 are supported. Returning empty list.",
          poseModel.get()
      );
      return Collections.emptyList();
    }
    return embedder.getSimilar(sc, qc);
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
