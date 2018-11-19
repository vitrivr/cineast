package org.vitrivr.cineast.core.features;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import org.tensorflow.Tensor;
import org.tensorflow.types.UInt8;
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
import org.vitrivr.cineast.core.features.neuralnet.tf.models.deeplab.DeepLabCityscapes;
import org.vitrivr.cineast.core.features.neuralnet.tf.models.deeplab.DeepLabLabel;
import org.vitrivr.cineast.core.features.neuralnet.tf.models.deeplab.DeepLabPascalVoc;
import org.vitrivr.cineast.core.util.GridPartitioner;

public class ConceptMasks extends AbstractFeatureModule {

  private static final int GRID_PARTITIONS = 16;

  private final DeepLab ade20k, cityscapes, pascalvoc;

  public ConceptMasks() {
    super("features_conceptmasks", 1);
    this.correspondence = CorrespondenceFunction.hyperbolic(10); //TODO determine distance
    this.ade20k = new DeepLabAde20k();
    this.cityscapes = new DeepLabCityscapes();
    this.pascalvoc = new DeepLabPascalVoc();
  }

  private static List<DeepLabLabel> linearize(DeepLabLabel[][] labels) {
    ArrayList<DeepLabLabel> list = new ArrayList<>(labels.length * labels[0].length);
    for (int i = 0; i < labels.length; ++i) {
      for (int j = 0; j < labels[0].length; ++j) {
        list.add(labels[i][j]);
      }
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

    Tensor<UInt8> inputTensor = DeepLab
        .prepareImage(shot.getMostRepresentativeFrame().getImage().getBufferedImage());

    int[][] tmp = this.ade20k.processImage(inputTensor);

    List<DeepLabLabel> ade20kLabels = linearize(
        DeepLabLabel.fromAde20kId(tmp));
    List<DeepLabLabel> cityscapesLabels = linearize(
        DeepLabLabel.fromCityscapesId(this.cityscapes.processImage(inputTensor)));
    List<DeepLabLabel> pascalvocLabels = linearize(
        DeepLabLabel.fromPascalVocId(this.pascalvoc.processImage(inputTensor)));

    inputTensor.close();

    ArrayList<LinkedList<DeepLabLabel>> ade20kPartitions = GridPartitioner
        .partition(ade20kLabels, tmp.length, tmp[0].length, GRID_PARTITIONS, GRID_PARTITIONS);

    ArrayList<LinkedList<DeepLabLabel>> cityscapesPartitions = GridPartitioner
        .partition(cityscapesLabels, tmp.length, tmp[0].length, GRID_PARTITIONS, GRID_PARTITIONS);

    ArrayList<LinkedList<DeepLabLabel>> pascalvocLabelsPartitions = GridPartitioner
        .partition(pascalvocLabels, tmp.length, tmp[0].length, GRID_PARTITIONS, GRID_PARTITIONS);

    float[] vector = new float[2 * GRID_PARTITIONS * GRID_PARTITIONS];

    for (int i = 0; i < GRID_PARTITIONS * GRID_PARTITIONS; ++i) {
      ArrayList<DeepLabLabel> labels = new ArrayList<>();
      labels.addAll(ade20kPartitions.get(i));
      labels.addAll(cityscapesPartitions.get(i));
      labels.addAll(pascalvocLabelsPartitions.get(i));

      DeepLabLabel dominantLabel = DeepLabLabel.getDominantLabel(labels);
      vector[2 * i] = dominantLabel.getEmbeddX();
      vector[2 * i + 1] = dominantLabel.getEmbeddY();

    }

    persist(shot.getId(), new FloatVectorImpl(vector));
  }

  @Override
  public List<ScoreElement> getSimilar(SegmentContainer sc, ReadableQueryConfig qc) {


    Optional<SemanticMap> optional = sc.getSemanticMap();
    if (!optional.isPresent()){
      return Collections.emptyList();
    }

    DeepLabLabel[][] labels = optional.get().getLabels();

    List<DeepLabLabel> list = linearize(labels);

    ArrayList<LinkedList<DeepLabLabel>> partitions = GridPartitioner
        .partition(list, labels.length, labels[0].length, GRID_PARTITIONS, GRID_PARTITIONS);

    float[] vector = new float[2 * GRID_PARTITIONS * GRID_PARTITIONS];
    float[] weights = new float[GRID_PARTITIONS * GRID_PARTITIONS];

    for (int i = 0; i < GRID_PARTITIONS * GRID_PARTITIONS; ++i) {
      DeepLabLabel dominantLabel = DeepLabLabel.getDominantLabel(partitions.get(i));
      weights[i] = dominantLabel == DeepLabLabel.NOTHING ? 0f : 1f; //TODO expose this to the API
      vector[2 * i] = dominantLabel.getEmbeddX();
      vector[2 * i + 1] = dominantLabel.getEmbeddY();

    }
    return this.getSimilar(vector, new QueryConfig(qc).setDistanceWeights(weights));

  }

}
