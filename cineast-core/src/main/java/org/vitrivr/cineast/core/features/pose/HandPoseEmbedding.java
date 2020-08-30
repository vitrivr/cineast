package org.vitrivr.cineast.core.features.pose;

import com.google.common.collect.ImmutableMap;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
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
import org.pytorch.IValue;
import org.pytorch.Module;
import org.pytorch.Tensor;

public class HandPoseEmbedding extends AbstractFeatureModule {

  private Module mod = null;
  private final boolean isLeft;
  private static final Logger LOGGER = LogManager.getLogger();
  static public final String HAND_POSE_EMBEDDING_TABLE_START = "features_HandPoseEmbedding_";

  public HandPoseEmbedding(boolean isLeft) {
    super(
        HAND_POSE_EMBEDDING_TABLE_START + (isLeft ? "LEFT_HAND_IN_BODY_25"
            : "RIGHT_HAND_IN_BODY_25"),
        2f,
        64
    );
    this.isLeft = isLeft;
  }

  public PoseSpec getPoseSpec() {
    if (this.isLeft) {
      return PoseSpecs.getInstance().specs.get("LEFT_HAND_IN_BODY_25");
    } else {
      return PoseSpecs.getInstance().specs.get("RIGHT_HAND_IN_BODY_25");
    }
  }

  private List<FloatVectorImpl> procPoses(SegmentContainer sc) {
    if (this.mod == null) {
      this.mod = Module.load("resources/skelembed/hand.pt");
    }
    // XXX: Probably all this copying can be avoided somehow
    PoseSpec poseSpec = this.getPoseSpec();
    List<float[]> allKps = PoseNormalize.procSegmentContainer(poseSpec, sc)
        .collect(Collectors.toList());
    if (this.isLeft) {
      for (float[] kps : allKps) {
        // Flip on x-axis
        for (int i = 0; i < kps.length; i += 2) {
          kps[i] = -kps[i];
        }
      }
    }
    // N, C, T, V, M
    // N = id_within_minibatch (hint: use a DataLoader to make minibatches in the 1st dimension)
    // C = channels (x, y, score) OR (x, y) -- has to match num_channels
    // T = frame_num_aka_time
    // V = keypoint/joint (probably stands for vertex)
    // M = person ID (for when there are multiple people within a frame I would suppose)
    float[] kpsFlat = new float[42 * allKps.size()];
    for (int i = 0; i < allKps.size(); i++) {
      for (int j = 0; j < 2; j++) {
        for (int k = 0; k < 21; k++) {
          kpsFlat[42 * i + j * 21 + k] = allKps.get(i)[2 * k + j];
        }
      }
    }

    Tensor data =
        Tensor.fromBlob(
            kpsFlat, // data
            new long[]{allKps.size(), 2, 1, 21, 1}
        );
    IValue modelResult = mod.forward(IValue.from(data));
    Tensor output = modelResult.toTensor();
    long[] shape = output.shape();
    ArrayList<FloatVectorImpl> result = new ArrayList<>();
    if (shape.length != 2) {
      LOGGER.warn("Unexpected shape from hand pose embedder: {}", Arrays.toString(shape));
      return result;
    }
    float[] floatArr = output.getDataAsFloatArray();
    int numResults = (int)shape[0];
    int vecSize = (int)shape[1];
    for (int i = 0; i < numResults; i++) {
      float[] vec = new float[vecSize];
      System.arraycopy(floatArr, i * vecSize, vec, 0, vecSize);
      result.add(new FloatVectorImpl(vec));
    }
    return result;
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
    qc.setDistanceIfEmpty(Distance.cosine);
    ArrayList<ScoreElement> results = new ArrayList<>();
    for (FloatVectorImpl query : procPoses(sc)) {
      results.addAll(getSimilar(ReadableFloatVector.toArray(query), qc));
    }
    return results;
  }

  @Override
  public void finish() {
    super.finish();
    if (this.mod != null) {
      this.mod.destroy();
      this.mod = null;
    }
  }

  @Override
  protected void finalize() throws Throwable {
    super.finalize();
    if (this.mod != null) {
      LOGGER.warn("PyTorch module not destroyed until finalizer! Resource leak could occur...");
      this.mod.destroy();
      this.mod = null;
    }
  }
}
