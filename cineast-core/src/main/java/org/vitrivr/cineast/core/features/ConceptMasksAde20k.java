package org.vitrivr.cineast.core.features;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.tensorflow.types.TUint8;
import org.vitrivr.cineast.core.config.QueryConfig;
import org.vitrivr.cineast.core.config.ReadableQueryConfig;
import org.vitrivr.cineast.core.data.CorrespondenceFunction;
import org.vitrivr.cineast.core.data.FloatVectorImpl;
import org.vitrivr.cineast.core.data.SemanticMap;
import org.vitrivr.cineast.core.data.frames.VideoFrame;
import org.vitrivr.cineast.core.data.score.ScoreElement;
import org.vitrivr.cineast.core.data.segments.SegmentContainer;
import org.vitrivr.cineast.core.features.abstracts.AbstractFeatureModule;
import org.vitrivr.cineast.core.features.neuralnet.tf.models.deeplab.DeepLab;
import org.vitrivr.cineast.core.features.neuralnet.tf.models.deeplab.DeepLabAde20k;
import org.vitrivr.cineast.core.features.neuralnet.tf.models.deeplab.DeepLabLabel;
import org.vitrivr.cineast.core.util.GridPartitioner;
import org.vitrivr.cineast.core.util.LogHelper;

public class ConceptMasksAde20k extends AbstractFeatureModule {

  private static final int GRID_PARTITIONS = 32;
  private static final Logger LOGGER = LogManager.getLogger();

  private DeepLab ade20k;

  public ConceptMasksAde20k() {
    super("features_conceptmasksade20k", 1, GRID_PARTITIONS * GRID_PARTITIONS * 2);
    this.correspondence = CorrespondenceFunction.hyperbolic(2000);

  }

  private static List<DeepLabLabel> linearize(DeepLabLabel[][] labels) {
    ArrayList<DeepLabLabel> list = new ArrayList<>(labels.length * labels[0].length);
    for (DeepLabLabel[] label : labels) {
      list.addAll(Arrays.asList(label).subList(0, labels[0].length));
    }
    return list;
  }

  @Override
  public synchronized void processSegment(SegmentContainer shot) {

    if (shot == null || shot.getMostRepresentativeFrame() == null
        || shot.getMostRepresentativeFrame().getImage() == null
        || shot.getMostRepresentativeFrame().getImage() == VideoFrame.EMPTY_VIDEO_FRAME) {
      return;
    }

    if (this.ade20k == null) {
      try {
        this.ade20k = new DeepLabAde20k();
      } catch (RuntimeException e) {
        LOGGER.error(LogHelper.getStackTrace(e));
        return;
      }
    }

    TUint8 inputTensor = DeepLab.prepareImage(shot.getMostRepresentativeFrame().getImage().getBufferedImage());

    int[][] tmp = this.ade20k.processImage(inputTensor);

    List<DeepLabLabel> ade20kLabels = linearize(
        DeepLabLabel.fromAde20kId(tmp));

    inputTensor.close();

    ArrayList<LinkedList<DeepLabLabel>> ade20kPartitions = GridPartitioner
        .partition(ade20kLabels, tmp.length, tmp[0].length, GRID_PARTITIONS, GRID_PARTITIONS);

    float[] vector = new float[2 * GRID_PARTITIONS * GRID_PARTITIONS];

    for (int i = 0; i < GRID_PARTITIONS * GRID_PARTITIONS; ++i) {
      DeepLabLabel dominantLabel = DeepLabLabel.getDominantLabel(ade20kPartitions.get(i));
      vector[2 * i] = dominantLabel.getEmbeddX();
      vector[2 * i + 1] = dominantLabel.getEmbeddY();

    }

    persist(shot.getId(), new FloatVectorImpl(vector));
  }

  @Override
  public List<ScoreElement> getSimilar(SegmentContainer sc, ReadableQueryConfig qc) {

    Optional<SemanticMap> optional = sc.getSemanticMap();
    if (!optional.isPresent()) {
      return Collections.emptyList();
    }

    DeepLabLabel[][] labels = optional.get().getLabels();

    List<DeepLabLabel> list = linearize(labels);

    ArrayList<LinkedList<DeepLabLabel>> ade20kPartitions = GridPartitioner
        .partition(list, labels.length, labels[0].length, GRID_PARTITIONS, GRID_PARTITIONS);

    float[] vector = new float[2 * GRID_PARTITIONS * GRID_PARTITIONS];
    float[] weights = new float[2 * GRID_PARTITIONS * GRID_PARTITIONS];

    for (int i = 0; i < GRID_PARTITIONS * GRID_PARTITIONS; ++i) {
      DeepLabLabel dominantLabel = DeepLabLabel.getDominantLabel(ade20kPartitions.get(i));
      float weight = dominantLabel == DeepLabLabel.NOTHING ? 0f : 1f; //TODO expose this to the API
      weights[2 * i] = weight;
      weights[2 * i + 1] = weight;
      vector[2 * i] = dominantLabel.getEmbeddX();
      vector[2 * i + 1] = dominantLabel.getEmbeddY();

    }
    return this.getSimilar(vector, new QueryConfig(qc).setDistanceWeights(weights));

  }

}
