package org.vitrivr.cineast.core.features;

import com.fathzer.soft.javaluator.DoubleEvaluator;
import com.google.common.collect.ImmutableList;
import java.nio.file.Path;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Optional;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.vitrivr.cineast.core.config.QueryConfig;
import org.vitrivr.cineast.core.config.ReadableQueryConfig;
import org.vitrivr.cineast.core.config.ReadableQueryConfig.Distance;
import org.vitrivr.cineast.core.data.CorrespondenceFunction;
import org.vitrivr.cineast.core.data.GpsData;
import org.vitrivr.cineast.core.data.Location;
import org.vitrivr.cineast.core.data.ReadableFloatVector;
import org.vitrivr.cineast.core.data.entities.MediaObjectMetadataDescriptor;
import org.vitrivr.cineast.core.data.query.containers.ParameterisedLocationQueryTermContainer;
import org.vitrivr.cineast.core.data.query.containers.ParameterisedLocationQueryTermContainer.ParameterisedLocation;
import org.vitrivr.cineast.core.data.score.ScoreElement;
import org.vitrivr.cineast.core.data.segments.SegmentContainer;
import org.vitrivr.cineast.core.features.abstracts.MetadataFeatureModule;

/**
 * A feature that calculates similarity based on an approximation of the great-circle distance
 * between two objects.
 *
 * <p>It extracts the latitude and longitude coordinates provided by Exif GPS data, if available,
 * or a supplementary JSON file. See {@link GpsData} for more information.
 *
 * <p>During retrieval, it does similarity search using the <i>Haversine</i> distance as an
 * approximation of the great-circle distance. As of now, the feature uses a linear correspondence
 * function with a maximum distance of {@code 1000m}. This correspondence
 * <i>heavily</i> influences the quality of the retrieval and is likely to be unfit for some data
 * sets.
 */
public class SpatialDistance extends MetadataFeatureModule<Location> {

  public static final String METADATA_DOMAIN = "LOCATION";
  public static final String FEATURE_NAME = "features_SpatialDistance";

  private static final Logger LOGGER = LogManager.getLogger();

  private final double halfeSimilarityDistance; // distance in meters where similarity equals 50% (default: 1000/3
  private final CorrespondenceFunction correspondenceFunction;

  // Empty public constructor necessary for instantiation through reflection
  public SpatialDistance() {
    super(2);
    halfeSimilarityDistance = 1000.0 / 3.0;
    correspondenceFunction = CorrespondenceFunction.hyperbolic(halfeSimilarityDistance);
  }

  public SpatialDistance(LinkedHashMap<String, String> properties) {
    super(2, properties);
    String halfSimDistFromConfig = properties.getOrDefault("halfSimilarityDistance", "1000.0/3.0");
    halfeSimilarityDistance = parseAndEvaluateHalfSimilarityDistance(halfSimDistFromConfig);
    LOGGER.debug("Half Similarity Distance (m): {} ({})", halfeSimilarityDistance,
        halfSimDistFromConfig);
    correspondenceFunction = CorrespondenceFunction.hyperbolic(halfeSimilarityDistance);
  }

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
    return Distance.haversine;
  }

  @Override
  public CorrespondenceFunction defaultCorrespondence() {
    return correspondenceFunction;
  }

  @Override
  public Optional<Location> extractFeature(SegmentContainer segmentContainer) {
    return segmentContainer.getLocation();
  }

  /**
   * Extracts the latitude and longitude coordinates from the specified path using Exif GPS data. If
   * not present, it retrieves the coordinates from a complementary JSON file named after the
   * original document, e.g. {@code image_0001.json} for {@code image_0001.jpg}, by parsing the
   * {@code "latitude"} and {@code "longitude"} key-value pairs. For example:
   *
   * <pre>
   *   {
   *     ...
   *     "latitude": 47.57,
   *     "longitude": 7.6
   *     ...
   *   }</pre>
   *
   * @param objectPath Path to the file for which the coordinates should be extracted.
   * @return an {@link Optional} containing a {@link Location} based on the extracted Exif or JSON
   * coordinates, if found, otherwise an empty {@code Optional}.
   */
  @Override
  public Optional<Location> extractFeature(String objectId, Path objectPath) {
    return GpsData.of(objectPath).location();
  }

  @Override
  public List<MediaObjectMetadataDescriptor> createDescriptors(String objectId, Location location) {
    return ImmutableList.of(
        MediaObjectMetadataDescriptor.of(objectId, this.domain(),
            GpsData.KEY_LATITUDE, Float.toString(location.getLatitude())),
        MediaObjectMetadataDescriptor.of(objectId, this.domain(),
            GpsData.KEY_LONGITUDE, Float.toString(location.getLongitude()))
    );
  }

  /**
   * Returns similar <i>objects</i> to the feature data contained in the given segment container.
   */
  @Override
  public List<ScoreElement> getSimilar(SegmentContainer sc, ReadableQueryConfig qc) {
    final QueryConfig mqc = QueryConfig.clone(qc);
    if (sc instanceof ParameterisedLocationQueryTermContainer) {
      if (((ParameterisedLocationQueryTermContainer) sc).getParameter().isPresent()) {
        final String param = ((ParameterisedLocationQueryTermContainer) sc).getParameter().get();
        mqc.setCorrespondenceFunctionIfEmpty(
            CorrespondenceFunction.hyperbolic(
                parseAndEvaluateHalfSimilarityDistance(
                    param
                )
            )
        );
      }
    }
    return this.extractFeature(sc)
        .map(ReadableFloatVector::toArray)
        .map(array -> this.getSimilar(array, new ReadableQueryConfig(mqc)))
        .orElse(Collections.emptyList());
  }

  private double parseAndEvaluateHalfSimilarityDistance(String dist) {
    return new DoubleEvaluator().evaluate(dist);
  }
}

