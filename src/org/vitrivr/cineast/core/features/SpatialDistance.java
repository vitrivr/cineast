package org.vitrivr.cineast.core.features;

import com.drew.metadata.exif.GpsDirectory;
import com.google.common.collect.ImmutableList;
import java.nio.file.Path;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Supplier;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.vitrivr.cineast.core.config.Config;
import org.vitrivr.cineast.core.config.QueryConfig;
import org.vitrivr.cineast.core.config.ReadableQueryConfig;
import org.vitrivr.cineast.core.config.ReadableQueryConfig.Distance;
import org.vitrivr.cineast.core.data.CorrespondenceFunction;
import org.vitrivr.cineast.core.data.Location;
import org.vitrivr.cineast.core.data.ReadableFloatVector;
import org.vitrivr.cineast.core.data.distance.DistanceElement;
import org.vitrivr.cineast.core.data.distance.ObjectDistanceElement;
import org.vitrivr.cineast.core.data.entities.MultimediaMetadataDescriptor;
import org.vitrivr.cineast.core.data.entities.SimpleFeatureDescriptor;
import org.vitrivr.cineast.core.data.score.ScoreElement;
import org.vitrivr.cineast.core.data.segments.SegmentContainer;
import org.vitrivr.cineast.core.db.DBSelector;
import org.vitrivr.cineast.core.db.DBSelectorSupplier;
import org.vitrivr.cineast.core.db.PersistencyWriter;
import org.vitrivr.cineast.core.db.dao.reader.SegmentLookup;
import org.vitrivr.cineast.core.db.dao.writer.SimpleFeatureDescriptorWriter;
import org.vitrivr.cineast.core.features.retriever.Retriever;
import org.vitrivr.cineast.core.metadata.JsonMetadataExtractor;
import org.vitrivr.cineast.core.metadata.MetadataExtractor;
import org.vitrivr.cineast.core.setup.EntityCreator;
import org.vitrivr.cineast.core.util.OptionalUtil;
import org.vitrivr.cineast.core.util.images.MetadataUtil;

/**
 * A feature that calculates similarity based on an approximation of the great-circle distance
 * between two documents. The feature extraction and retrieval bases on objects rather than
 * segments.
 *
 * <p>As a {@link MetadataExtractor} it tries to extract the latitude and longitude coordinates from
 * the object itself, and storing the object id and the feature vector as
 * {@code [latitude, longitude]}.
 *
 * <p>It extracts the feature data using the GPS data of the Exif format. If not present, it tries
 * to retrieve the coordinates from a JSON file named after the original document,
 * e.g. {@code image_0001.json} for {@code image_0001.jpg}. To successfully extract the coordinates,
 * the JSON file must contain a top level object with {@code "latitude"} and {@code "longitude"}
 * key-value pairs, for example:
 *
 * <pre>
 *   {
 *     "latitude": 47.57,
 *     "longitude": 7.6
 *   }</pre>
 *
 * <p>During retrieval, it does a k-nearest neighbor search based on the coordinates by making use
 * of the <i>Haversine</i> distance. Because the {@link Retriever} interface features similarity
 * only on segments, during similarity search based on an existing segment (i.e. {@code segmentId})
 * the feature has to perform a {@link SegmentLookup} in order to retrieve the object id of the
 * given segment.
 *
 * <p>As of now, the feature uses a linear {@link CorrespondenceFunction} with a maximum distance of
 * {@code 1000m}. This correspondence <i>heavily</i> influences the quality of the retrieval and
 * is likely to be unfit for some data sets.
 */
public class SpatialDistance implements MetadataExtractor, Retriever {
  private static final Logger logger = LogManager.getLogger();

  private static final String METADATA_DOMAIN = "LOCATION";
  private static final String KEY_LATITUDE = "latitude";
  private static final String KEY_LONGITUDE = "longitude";

  private static final String ENTITY_NAME = "features_SpatialDistance";
  private static final String ID_COLUMN_NAME = "id"; // Constant used in ADAMproEntityCreator
  private static final String FEATURE_COLUMN_NAME = "feature";
  private static final int WRITER_BATCHSIZE = 10; // Taken from AbstractFeatureModule

  // TODO: Find most suitable maximum distance, maybe even as a property to the feature
  private static final double MAXIMUM_DISTANCE = 1000d;
  private static final CorrespondenceFunction CORRESPONDENCE =
      CorrespondenceFunction.linear(MAXIMUM_DISTANCE);

  private SimpleFeatureDescriptorWriter featureWriter;
  private DBSelector dbSelector;
  private SegmentLookup segmentLookup;

  // Empty public constructor necessary for instantiation through reflection
  public SpatialDistance() {}

  @Override
  public String domain() {
    return METADATA_DOMAIN;
  }

  /* Initialization */
  @Override
  public void init() {
    PersistencyWriter<?> writer = Config.sharedConfig().getDatabase().getWriterSupplier().get();
    this.featureWriter = new SimpleFeatureDescriptorWriter(writer, ENTITY_NAME, WRITER_BATCHSIZE);
    this.featureWriter.init();
  }

  @Override
  public void init(DBSelectorSupplier selectorSupply) {
    this.dbSelector = selectorSupply.get();
    this.dbSelector.open(ENTITY_NAME);
    this.segmentLookup = new SegmentLookup(selectorSupply.get());
  }

  @Override
  public void initalizePersistentLayer(Supplier<EntityCreator> supply) {
    supply.get().createFeatureEntity(ENTITY_NAME, true, FEATURE_COLUMN_NAME);
  }

  /* Finishing */
  @Override
  public void dropPersistentLayer(Supplier<EntityCreator> supply) {
    supply.get().dropEntity(ENTITY_NAME);
  }

  @Override
  public void finish() {
    if (this.featureWriter != null) {
      this.featureWriter.close();
      this.featureWriter = null;
    }

    if (this.dbSelector != null) {
      this.dbSelector.close();
      this.dbSelector = null;
    }

    if (this.segmentLookup != null) {
      this.segmentLookup.close();
      this.segmentLookup = null;
    }
  }

  /* Extraction */
  /**
   * Extracts the latitude and longitude coordinates from the specified path using Exif data or a
   * supplementary JSON file (see the class description for more details), stores them in the
   * feature table with the given {@code objectId} and returns two descriptors based on the
   * coordinates.
   *
   * @param objectId ID of the multimedia object for which metadata will be generated.
   * @param objectPath Path to the file for which the coordinates should be extracted.
   * @return a list of two {@code MultimediaMetadataDescriptor}s containing the two coordinates, if
   *         found and successfully extracted from the path, otherwise an empty list.
   */
  @Override
  public List<MultimediaMetadataDescriptor> extract(String objectId, Path objectPath) {
    Optional<Location> location = this.extractLocation(objectId, objectPath);
    location.ifPresent(l -> this.storeLocation(objectId, l));
    return location
        .map(l -> this.createLocationDescriptors(objectId, l))
        .orElse(Collections.emptyList());
  }

  private Optional<Location> extractLocation(String objectId, Path objectPath) {
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

  private void storeLocation(String objectId, Location location) {
    this.featureWriter.write(new SimpleFeatureDescriptor(objectId, location));
  }

  private List<MultimediaMetadataDescriptor> createLocationDescriptors(String objectId,
      Location location) {
    return ImmutableList.of(
        MultimediaMetadataDescriptor
            .newMultimediaMetadataDescriptor(objectId, this.domain(),
                KEY_LATITUDE, location.getLatitude()),
        MultimediaMetadataDescriptor
            .newMultimediaMetadataDescriptor(objectId, this.domain(),
                KEY_LONGITUDE, location.getLongitude())
    );
  }

  /* Retrieval */
  @Override
  public List<ScoreElement> getSimilar(SegmentContainer sc, ReadableQueryConfig rqc) {
    return sc.getLocation()
        .map(location -> this.getSimilar(location, rqc))
        .orElse(Collections.emptyList());
  }

  /**
   * Performs the similarity search based on an existing segment. Because feature data is based on
   * objects, this function performs a {@link SegmentLookup} to retrieve the object id of the given
   * segment.
   */
  @Override
  public List<ScoreElement> getSimilar(String segmentId, ReadableQueryConfig rqc) {
    return this.segmentLookup.lookUpSegment(segmentId)
        .map(descriptor -> descriptor.getObjectId())
        .map(objId -> this.dbSelector.getFeatureVectors(ID_COLUMN_NAME, objId, FEATURE_COLUMN_NAME))
        .flatMap(features -> features.stream().findFirst())
        .map(feature -> Location.of(feature))
        .map(location -> this.getSimilar(location, rqc))
        .orElse(Collections.emptyList());
  }

  private List<ScoreElement> getSimilar(Location location, ReadableQueryConfig rqc) {
    QueryConfig qc = QueryConfig.clone(rqc).setDistanceIfEmpty(Distance.haversine);
    int maxResultsPerModule = Config.sharedConfig().getRetriever().getMaxResultsPerModule();
    List<ObjectDistanceElement> distances = this.dbSelector.getNearestNeighbours(
        maxResultsPerModule,
        ReadableFloatVector.toArray(location),
        FEATURE_COLUMN_NAME,
        ObjectDistanceElement.class,
        qc
    );

    CorrespondenceFunction correspondence = qc.getCorrespondenceFunction().orElse(CORRESPONDENCE);
    return DistanceElement.toScore(distances, correspondence);
  }
}
