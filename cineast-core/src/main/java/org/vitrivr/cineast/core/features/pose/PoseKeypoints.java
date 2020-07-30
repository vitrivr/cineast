package org.vitrivr.cineast.core.features.pose;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.vitrivr.cineast.core.config.QueryConfig;
import org.vitrivr.cineast.core.config.ReadableQueryConfig;
import org.vitrivr.cineast.core.config.ReadableQueryConfig.Distance;
import org.vitrivr.cineast.core.data.FloatVectorImpl;
import org.vitrivr.cineast.core.data.ReadableFloatVector;
import org.vitrivr.cineast.core.data.raw.images.MultiImage;
import org.vitrivr.cineast.core.data.score.ScoreElement;
import org.vitrivr.cineast.core.data.segments.SegmentContainer;
import org.vitrivr.cineast.core.features.abstracts.AbstractFeatureModule;
import org.vitrivr.cineast.core.util.pose.PoseNormalize;
import org.vitrivr.cineast.core.util.pose.PoseSpec;
import org.vitrivr.cineast.core.util.pose.PoseSpecs;

public class PoseKeypoints extends AbstractFeatureModule {

  private static final Logger LOGGER = LogManager.getLogger();
  static public final String POSE_KEYPOINTS_TABLE_START = "features_PoseKeypoints_";
  private final String poseSpecName;
  private final PoseSpec poseSpec;

  public PoseKeypoints(String poseSpecName) {
    super(
      POSE_KEYPOINTS_TABLE_START + poseSpecName,
      4f,
      2 * PoseSpecs.getInstance().specs.get(poseSpecName).numNodes()
    );
    this.poseSpecName = poseSpecName;
    this.poseSpec = PoseSpecs.getInstance().specs.get(poseSpecName);
  }

  private List<FloatVectorImpl> procPoses(SegmentContainer sc) {
    float[][][] poses = sc.getPose();
    ArrayList<FloatVectorImpl> poseKeypointContainers = new ArrayList<>();
    for (float[][] pose : poses) {
      Optional<float[]> flatPose = PoseNormalize.pipeline(this.poseSpec, pose);
      if (!flatPose.isPresent()) {
        continue;
      }
      poseKeypointContainers.add(new FloatVectorImpl(flatPose.get()));
    }
    return poseKeypointContainers;
  }

  @Override
  public void processSegment(SegmentContainer shot) {
    if (shot.getMostRepresentativeFrame().getImage() == MultiImage.EMPTY_MULTIIMAGE) {
      return;
    }
    if (!phandler.idExists(shot.getId())) {
      persist(shot.getId(), procPoses(shot));
    }
  }

  @Override
  public List<ScoreElement> getSimilar(SegmentContainer sc, ReadableQueryConfig qcIn) {
    QueryConfig qc = new QueryConfig(qcIn);
    qc.setDistanceIfEmpty(Distance.keypoints2d);
    ArrayList<ScoreElement> results = new ArrayList<>();
    for (FloatVectorImpl query : procPoses(sc)) {
      results.addAll(getSimilar(ReadableFloatVector.toArray(query), qc));
    }
    return results;
  }

}
