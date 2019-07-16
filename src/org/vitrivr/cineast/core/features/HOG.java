package org.vitrivr.cineast.core.features;

import boofcv.abst.feature.dense.DescribeImageDense;
import boofcv.struct.feature.TupleDesc_F64;
import boofcv.struct.image.GrayU8;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.vitrivr.cineast.core.config.QueryConfig;
import org.vitrivr.cineast.core.config.ReadableQueryConfig;
import org.vitrivr.cineast.core.data.CorrespondenceFunction;
import org.vitrivr.cineast.core.data.FloatVectorImpl;
import org.vitrivr.cineast.core.data.distance.SegmentDistanceElement;
import org.vitrivr.cineast.core.data.frames.VideoFrame;
import org.vitrivr.cineast.core.data.score.ScoreElement;
import org.vitrivr.cineast.core.data.segments.SegmentContainer;
import org.vitrivr.cineast.core.features.abstracts.AbstractCodebookFeatureModule;
import org.vitrivr.cineast.core.util.images.HOGHelper;

/**
 * @author rgasser
 * @version 1.0
 * @created 24.01.17
 */
public abstract class HOG extends AbstractCodebookFeatureModule {

  private static final Logger LOGGER = LogManager.getLogger();

  /**
   *
   */
  protected HOG(String tableName, float maxDist, int vectorLength) {
    super(tableName, maxDist, vectorLength);
  }

  /**
   *
   */
  @Override
  public void processSegment(SegmentContainer shot) {
    if (shot.getMostRepresentativeFrame() == VideoFrame.EMPTY_VIDEO_FRAME) {
      return;
    }

    BufferedImage image = shot.getMostRepresentativeFrame().getImage().getBufferedImage();
    if (image != null) {
      DescribeImageDense<GrayU8, TupleDesc_F64> hog = HOGHelper.getHOGDescriptors(image);
      if (hog != null && hog.getDescriptions().size() > 0) {
        float[] histogram_f = this.histogram(true, hog.getDescriptions());
        this.persist(shot.getId(), new FloatVectorImpl(histogram_f));
      } else {
        LOGGER.warn("No HOG feature could be extracted for segment {}. This is not necessarily an error!", shot.getId());
      }
    }
  }


  /**
   * This method represents the first step that's executed when processing query. The associated SegmentContainer is examined and feature-vectors are being generated. The generated vectors are returned by this method together with an optional weight-vector.
   * <p>
   * <strong>Important: </strong> The weight-vector must have the same size as the feature-vectors returned by the method.
   *
   * @param sc SegmentContainer that was submitted to the feature module.
   * @param qc A QueryConfig object that contains query-related configuration parameters. Can still be edited.
   * @return List of feature vectors for lookup.
   */
  @Override
  protected List<float[]> preprocessQuery(SegmentContainer sc, ReadableQueryConfig qc) {
    /* Prepare feature pair. */
    List<float[]> features = new ArrayList<>(1);

    /* Extract features. */
    BufferedImage image = sc.getMostRepresentativeFrame().getImage().getBufferedImage();
    DescribeImageDense<GrayU8, TupleDesc_F64> hog = HOGHelper.getHOGDescriptors(image);
    if (hog != null && hog.getDescriptions().size() > 0) {
      features.add(this.histogram(true, hog.getDescriptions()));
    }

    return features;
  }

  /**
   * This method represents the last step that's executed when processing a query. A list of partial-results (DistanceElements) returned by the lookup stage is processed based on some internal method and finally converted to a list of ScoreElements. The filtered list of ScoreElements is returned by the feature module during retrieval.
   *
   * @param partialResults List of partial results returned by the lookup stage.
   * @param qc A ReadableQueryConfig object that contains query-related configuration parameters.
   * @return List of final results. Is supposed to be de-duplicated and the number of items should not exceed the number of items per module.
   */
  @Override
  protected List<ScoreElement> postprocessQuery(List<SegmentDistanceElement> partialResults, ReadableQueryConfig qc) {
    final CorrespondenceFunction function = qc.getCorrespondenceFunction().orElse(
        correspondence);
    return ScoreElement.filterMaximumScores(partialResults.stream().map(r -> r.toScore(function)));
  }

  /**
   * Merges the provided QueryConfig with the default QueryConfig enforced by the feature module.
   *
   * @param qc QueryConfig provided by the caller of the feature module.
   * @return Modified QueryConfig.
   */
  protected ReadableQueryConfig queryConfig(ReadableQueryConfig qc, float[] weights) {
    return new QueryConfig(qc).setCorrespondenceFunctionIfEmpty(this.correspondence).setDistanceIfEmpty(QueryConfig.Distance.chisquared).setDistanceWeights(weights);
  }
}
