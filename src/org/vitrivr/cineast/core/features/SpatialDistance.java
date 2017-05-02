package org.vitrivr.cineast.core.features;

import com.drew.metadata.exif.GpsDirectory;
import com.google.common.collect.ImmutableList;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.vitrivr.cineast.core.config.ReadableQueryConfig.Distance;
import org.vitrivr.cineast.core.data.CorrespondenceFunction;
import org.vitrivr.cineast.core.data.Location;
import org.vitrivr.cineast.core.data.entities.MultimediaMetadataDescriptor;
import org.vitrivr.cineast.core.data.segments.SegmentContainer;
import org.vitrivr.cineast.core.db.DBSelector;
import org.vitrivr.cineast.core.db.dao.reader.SegmentLookup;
import org.vitrivr.cineast.core.db.dao.writer.SimpleFeatureDescriptorWriter;
import org.vitrivr.cineast.core.features.abstracts.MetadataFeatureModule;
import org.vitrivr.cineast.core.features.retriever.Retriever;
import org.vitrivr.cineast.core.metadata.JsonMetadataExtractor;
import org.vitrivr.cineast.core.metadata.MetadataExtractor;
import org.vitrivr.cineast.core.util.OptionalUtil;
import org.vitrivr.cineast.core.util.images.MetadataUtil;

/**
 * A feature that calculates similarity based on an approximation of the great-circle distance
 * between two objects.
 *
 * <p>It extracts the latitude and longitude coordinates provided by Exif GPS data, if available,
 * or a supplementary JSON file. See {@link #extractFeature(Path)} for more information.
 *
 * <p>During retrieval, it does similarity search using the <i>Haversine</i> distance as an
 * approximation of the great-circle distance. As of now, the feature uses a linear
 * correspondence function with a maximum distance of {@code 1000m}. This correspondence
 * <i>heavily</i> influences the quality of the retrieval and is likely to be unfit for some data
 * sets.
 */
public class SpatialDistance extends MetadataFeatureModule<Location> {
  private static final String METADATA_DOMAIN = "LOCATION";
  private static final String KEY_LATITUDE = "latitude";
  private static final String KEY_LONGITUDE = "longitude";

  private static final String FEATURE_NAME = "features_SpatialDistance";

  // TODO: Find most suitable maximum distance, maybe even as a property to the feature
  private static final double MAXIMUM_DISTANCE = 1000d;
  private static final CorrespondenceFunction CORRESPONDENCE =
      CorrespondenceFunction.linear(MAXIMUM_DISTANCE);

  // Empty public constructor necessary for instantiation through reflection
  public SpatialDistance() {}

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
    return CORRESPONDENCE;
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
   *         coordinates, if found, otherwise an empty {@code Optional}.
   */
  @Override
  public Optional<Location> extractFeature(Path objectPath) {
    return OptionalUtil.or(this.extractExifGpsLocation(objectPath),
        () -> this.extractJsonMetadataLocation(objectPath));
  }

  private Optional<Location> extractExifGpsLocation(Path objectPath) {
    return MetadataUtil
        .getMetadataDirectoryOfType(objectPath, GpsDirectory.class)
        .map(gpsDirectory -> gpsDirectory.getGeoLocation())
        .map(geoLocation -> Location.of(geoLocation));
  }

  private Optional<Location> extractJsonMetadataLocation(Path objectPath) {
    return JsonMetadataExtractor
        .extractJsonMetadata(objectPath)
        .flatMap(values -> OptionalUtil.and(
            getFloatFromMap(values, KEY_LATITUDE),
            () -> getFloatFromMap(values, KEY_LONGITUDE))
        )
        .map(p -> Location.of(p.first, p.second));
  }

  private Optional<Float> getFloatFromMap(Map<String, Object> map, String key) {
    return Optional
        .ofNullable(map.get(key))
        .filter(o -> o instanceof Number)
        .map(o -> ((Number) o).floatValue());
  }

  @Override
  public List<MultimediaMetadataDescriptor> createDescriptors(String objectId, Location location) {
    return ImmutableList.of(
        MultimediaMetadataDescriptor
            .newMultimediaMetadataDescriptor(objectId, this.domain(),
                KEY_LATITUDE, location.getLatitude()),
        MultimediaMetadataDescriptor
            .newMultimediaMetadataDescriptor(objectId, this.domain(),
                KEY_LONGITUDE, location.getLongitude())
    );
  }
}
