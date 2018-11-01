package org.vitrivr.cineast.core.features;

import java.nio.file.Path;
import java.util.List;
import java.util.Optional;

import org.vitrivr.cineast.core.config.ReadableQueryConfig.Distance;
import org.vitrivr.cineast.core.data.CorrespondenceFunction;
import org.vitrivr.cineast.core.data.GpsData;
import org.vitrivr.cineast.core.data.InstantVector;
import org.vitrivr.cineast.core.data.entities.MediaObjectMetadataDescriptor;
import org.vitrivr.cineast.core.data.segments.SegmentContainer;
import org.vitrivr.cineast.core.features.abstracts.MetadataFeatureModule;

import com.google.common.collect.ImmutableList;

// TODO: Change from Instant to Range<Instant>

/**
 * A feature that calculates similarity based on the distance between the timestamps of two objects
 * measured in seconds.
 *
 * <p>It extracts the timestamp provided by Exif GPS data, if available,
 * or a supplementary JSON file. See {@link GpsData} for more information.
 *
 * <p>During retrieval, it does nearest neighbor search using the euclidean distance. As of now,
 * it uses a hyperbolic correspondence function scaled to days, i.e. 13h20min correspond to
 * 90% similarity, 5 days to 50% and 45 days to 10%. This correspondence is likely unfit for many
 * datasets and needs to be adjusted accordingly.
 */
public class TemporalDistance extends MetadataFeatureModule<InstantVector> {
  private static final String METADATA_DOMAIN = "TIME";
  private static final String FEATURE_NAME = "features_TemporalDistance";
  private static final float TIME_SCALE = 5 * 24 * 60 * 60 * 1000; // 5 days, 30 * 60 * 1000; // 30min in ms
  private static final CorrespondenceFunction HYPERBOLIC =
      CorrespondenceFunction.hyperbolic(TIME_SCALE);

  // Empty public constructor necessary for instantiation through reflection
  public TemporalDistance() {}

  @Override
  public String domain() {
    return METADATA_DOMAIN;
  }

  @Override
  public String featureEntityName() {
    return FEATURE_NAME;
  }

  @Override
  public Distance defaultDistance() {
    return Distance.euclidean;
  }

  @Override
  public CorrespondenceFunction defaultCorrespondence() {
    return HYPERBOLIC;
  }

  @Override
  public Optional<InstantVector> extractFeature(SegmentContainer segmentContainer) {
    return segmentContainer.getInstant().map(InstantVector::of);
  }

  /**
   * Extracts the timestamp from the specified path using Exif GPS data. If
   * not present, it retrieves the coordinates from a complementary JSON file named after the
   * original document. See {@link GpsData} for more information.
   *
   * @param object Path to the file for which the coordinates should be extracted.
   * @return an {@link Optional} containing an {@link InstantVector} based on the extracted Exif or
   *         JSON timestamp, if found, otherwise an empty {@code Optional}.
   */
  @Override
  public Optional<InstantVector> extractFeature(String objectId, Path object) {
    return GpsData.of(object).time().map(InstantVector::of);
  }

  @Override
  public List<MediaObjectMetadataDescriptor> createDescriptors(String objectId,
                                                               InstantVector feature) {
    return ImmutableList.of(MediaObjectMetadataDescriptor
        .of(objectId, this.domain(), GpsData.KEY_DATETIME, feature.getInstant().toString()));
  }
}
