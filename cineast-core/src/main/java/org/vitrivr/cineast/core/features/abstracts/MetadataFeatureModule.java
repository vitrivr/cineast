package org.vitrivr.cineast.core.features.abstracts;


import static org.vitrivr.cineast.core.util.CineastConstants.FEATURE_COLUMN_QUALIFIER;
import static org.vitrivr.cineast.core.util.CineastConstants.GENERIC_ID_COLUMN_QUALIFIER;

import org.vitrivr.cineast.core.config.QueryConfig;
import org.vitrivr.cineast.core.config.ReadableQueryConfig;
import org.vitrivr.cineast.core.config.ReadableQueryConfig.Distance;
import org.vitrivr.cineast.core.data.CorrespondenceFunction;
import org.vitrivr.cineast.core.data.ReadableFloatVector;
import org.vitrivr.cineast.core.data.distance.DistanceElement;
import org.vitrivr.cineast.core.data.distance.ObjectDistanceElement;
import org.vitrivr.cineast.core.data.entities.MediaObjectMetadataDescriptor;
import org.vitrivr.cineast.core.data.entities.MediaSegmentDescriptor;
import org.vitrivr.cineast.core.data.entities.SimpleFeatureDescriptor;
import org.vitrivr.cineast.core.data.providers.primitive.StringTypeProvider;
import org.vitrivr.cineast.core.data.score.ScoreElement;
import org.vitrivr.cineast.core.data.segments.SegmentContainer;
import org.vitrivr.cineast.core.db.DBSelector;
import org.vitrivr.cineast.core.db.DBSelectorSupplier;
import org.vitrivr.cineast.core.db.PersistencyWriter;
import org.vitrivr.cineast.core.db.PersistencyWriterSupplier;
import org.vitrivr.cineast.core.db.dao.reader.MediaSegmentReader;
import org.vitrivr.cineast.core.db.dao.writer.SimpleFeatureDescriptorWriter;
import org.vitrivr.cineast.core.db.setup.EntityCreator;
import org.vitrivr.cineast.core.features.retriever.Retriever;
import org.vitrivr.cineast.core.extraction.metadata.MetadataFeatureExtractor;

import java.nio.file.Path;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;

/**
 * Feature module that bases its feature data on the object (usually metadata) itself instead of
 * individual segments by combining {@link MetadataFeatureExtractor} and {@link Retriever}.
 *
 * @param <T> the specific type of the feature data
 */
public abstract class MetadataFeatureModule<T extends ReadableFloatVector>
    implements MetadataFeatureExtractor<T>, Retriever {
  private static final String ID_COLUMN_NAME = GENERIC_ID_COLUMN_QUALIFIER;
  private static final String FEATURE_COLUMN_NAME = FEATURE_COLUMN_QUALIFIER;

  private SimpleFeatureDescriptorWriter featureWriter;
  private DBSelector dbSelector;
  private MediaSegmentReader mediaSegmentReader;
  private final int vectorLength;

  protected MetadataFeatureModule(int vectorLength) {
    this.vectorLength = vectorLength;
  }

  /** Returns the name of the feature entity as stored in the persistent layer. */
  public abstract String featureEntityName();

  /** Returns the default distance if none is set. */
  public abstract Distance defaultDistance();

  /** Returns the default correspondence function if none is set. */
  public abstract CorrespondenceFunction defaultCorrespondence();

  @Override
  public List<String> getTableNames() {
    return Collections.singletonList(featureEntityName());
  }

  /**
   * Returns an {@link Optional} containing the extracted feature data from the segment container,
   * if available, otherwise an empty {@code Optional}.
   */
  public abstract Optional<T> extractFeature(SegmentContainer segmentContainer);

  @Override
  public void initalizePersistentLayer(Supplier<EntityCreator> supply) {
    supply.get().createFeatureEntity(this.featureEntityName(), true, this.vectorLength, FEATURE_COLUMN_NAME);
  }

  @Override
  public void dropPersistentLayer(Supplier<EntityCreator> supply) {
    supply.get().dropEntity(this.featureEntityName());
  }

  public void init(PersistencyWriterSupplier supply, int batchSize) {
    init(); //from MetadataFeatureExtractor
    PersistencyWriter<?> writer = supply.get();
    this.featureWriter = new SimpleFeatureDescriptorWriter(writer, this.featureEntityName(), batchSize);
    this.featureWriter.init();
  }

  @Override
  public void init(DBSelectorSupplier selectorSupply) {
    this.dbSelector = selectorSupply.get();
    this.dbSelector.open(this.featureEntityName());
    this.mediaSegmentReader = new MediaSegmentReader(selectorSupply.get());
  }

  public boolean isExtractorInitialized() {
    return this.featureWriter != null;
  }

  public boolean isRetrieverInitialized() {
    return this.dbSelector != null && this.mediaSegmentReader != null;
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

    if (this.mediaSegmentReader != null) {
      this.mediaSegmentReader.close();
      this.mediaSegmentReader = null;
    }
  }

  /**
   * Extracts the feature data, <i>stores it</i> and returns a list of descriptors from the feature.
   */
  @Override
  public List<MediaObjectMetadataDescriptor> extract(String objectId, Path path) {
    Optional<T> feature = this.extractFeature(objectId, path);
    feature.ifPresent(v -> this.featureWriter.write(new SimpleFeatureDescriptor(objectId, v)));
    return feature
        .map(floatVector -> this.createDescriptors(objectId, floatVector))
        .orElse(Collections.emptyList());
  }

  /**
   * Returns similar <i>objects</i> to the feature data contained in the given segment container.
   */
  @Override
  public List<ScoreElement> getSimilar(SegmentContainer sc, ReadableQueryConfig qc) {
    return this.extractFeature(sc)
        .map(ReadableFloatVector::toArray)
        .map(array -> this.getSimilar(array, qc))
        .orElse(Collections.emptyList());
  }

  /**
   * Returns similar <i>objects</i> to the object of the given segment.
   *
   * <p>Note that this methods performs a {@link MediaSegmentReader} in order to retrieve the object id
   * of the given segment.
   */
  @Override
  public List<ScoreElement> getSimilar(String segmentId, ReadableQueryConfig rqc) {
    return this.mediaSegmentReader.lookUpSegment(segmentId)
        .map(MediaSegmentDescriptor::getObjectId)
        .map(objId -> this.dbSelector.getFeatureVectors(ID_COLUMN_NAME, new StringTypeProvider(objId), FEATURE_COLUMN_NAME))
        .flatMap(features -> features.stream().findFirst()) // Feature vectors are unique per id
        .map(feature -> this.getSimilar(feature, rqc))
        .orElse(Collections.emptyList());
  }

  private List<ScoreElement> getSimilar(float[] feature, ReadableQueryConfig rqc) {
    QueryConfig qc = QueryConfig.clone(rqc).setDistanceIfEmpty(this.defaultDistance());
    List<ObjectDistanceElement> distances = this.dbSelector.getNearestNeighboursGeneric(rqc.getResultsPerModule(), feature, FEATURE_COLUMN_NAME, ObjectDistanceElement.class, qc);
    CorrespondenceFunction correspondence = qc.getCorrespondenceFunction()
        .orElse(this.defaultCorrespondence());
    return DistanceElement.toScore(distances, correspondence);
  }
}
