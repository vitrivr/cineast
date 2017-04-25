package org.vitrivr.cineast.core.features;

import com.drew.imaging.ImageMetadataReader;
import com.drew.metadata.Metadata;
import com.drew.metadata.exif.GpsDirectory;
import com.google.common.collect.ImmutableList;
import java.io.IOException;
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
    Optional<Metadata> exifData = Optional.empty();
    try {
      exifData = Optional.of(ImageMetadataReader.readMetadata(objectPath.toFile()));
    } catch (com.drew.imaging.ImageProcessingException | IOException e) {
      logger.info("Error while reading exif data", e);
    }

    return exifData
        .map(metadata -> metadata.getFirstDirectoryOfType(GpsDirectory.class))
        .map(directory -> directory.getGeoLocation())
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

  @Override
  public List<ScoreElement> getSimilar(String segmentId, ReadableQueryConfig rqc) {
    return this.segmentLookup.lookUpSegment(segmentId)
        .map(descriptor -> descriptor.getObjectId())
        .map(objId -> this.dbSelector.getFeatureVectors(ID_COLUMN_NAME, objId, FEATURE_COLUMN_NAME))
        .flatMap(features -> features.stream().findFirst())
        .map(feature -> Location.ofFloatArray(feature))
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
