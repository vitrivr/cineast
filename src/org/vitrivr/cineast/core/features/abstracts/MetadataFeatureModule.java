package org.vitrivr.cineast.core.features.abstracts;

import static com.google.common.base.Preconditions.checkState;

import java.nio.file.Path;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;
import org.vitrivr.cineast.core.config.Config;
import org.vitrivr.cineast.core.config.QueryConfig;
import org.vitrivr.cineast.core.config.ReadableQueryConfig;
import org.vitrivr.cineast.core.config.ReadableQueryConfig.Distance;
import org.vitrivr.cineast.core.data.CorrespondenceFunction;
import org.vitrivr.cineast.core.data.ReadableFloatVector;
import org.vitrivr.cineast.core.data.distance.DistanceElement;
import org.vitrivr.cineast.core.data.distance.ObjectDistanceElement;
import org.vitrivr.cineast.core.data.entities.MultimediaMetadataDescriptor;
import org.vitrivr.cineast.core.data.entities.SegmentDescriptor;
import org.vitrivr.cineast.core.data.entities.SimpleFeatureDescriptor;
import org.vitrivr.cineast.core.data.score.ScoreElement;
import org.vitrivr.cineast.core.data.segments.SegmentContainer;
import org.vitrivr.cineast.core.db.DBSelector;
import org.vitrivr.cineast.core.db.DBSelectorSupplier;
import org.vitrivr.cineast.core.db.PersistencyWriter;
import org.vitrivr.cineast.core.db.dao.reader.SegmentLookup;
import org.vitrivr.cineast.core.db.dao.writer.SimpleFeatureDescriptorWriter;
import org.vitrivr.cineast.core.features.retriever.Retriever;
import org.vitrivr.cineast.core.metadata.MetadataExtractor;
import org.vitrivr.cineast.core.metadata.MetadataFeatureExtractor;
import org.vitrivr.cineast.core.setup.EntityCreator;

/**
 * Feature module that bases its feature data on the object (usually metadata) itself instead of
 * individual segments by combining {@link MetadataFeatureExtractor} and {@link Retriever}.
 *
 * @param <T> the specific type of the feature data
 */
public abstract class MetadataFeatureModule<T extends ReadableFloatVector>
    implements MetadataFeatureExtractor<T>, Retriever {
  private static final String ID_COLUMN_NAME = "id"; // Constant used in ADAMproEntityCreator
  private static final String FEATURE_COLUMN_NAME = "feature";
  private static final int WRITER_BATCHSIZE = 10; // Taken from AbstractFeatureModule

  private SimpleFeatureDescriptorWriter featureWriter;
  private DBSelector dbSelector;
  private SegmentLookup segmentLookup;

  protected MetadataFeatureModule() {}

  /** Returns the name of the feature entity as stored in the persistent layer. */
  public abstract String featureEntityName();

  /** Returns the default distance if none is set. */
  public abstract Distance defaultDistance();

  /** Returns the default correspondence function if none is set. */
  public abstract CorrespondenceFunction defaultCorrespondence();

  /**
   * Returns an {@link Optional} containing the extracted feature data from the segment container,
   * if available, otherwise an empty {@code Optional}.
   */
  public abstract Optional<T> extractFeature(SegmentContainer segmentContainer);

  @Override
  public void initalizePersistentLayer(Supplier<EntityCreator> supply) {
    supply.get().createFeatureEntity(this.featureEntityName(), true, FEATURE_COLUMN_NAME);
  }

  @Override
  public void dropPersistentLayer(Supplier<EntityCreator> supply) {
    supply.get().dropEntity(this.featureEntityName());
  }

  @Override
  public void init() {
    PersistencyWriter<?> writer = Config.sharedConfig().getDatabase().getWriterSupplier().get();
    this.featureWriter = new SimpleFeatureDescriptorWriter(writer, this.featureEntityName(),
        WRITER_BATCHSIZE);
    this.featureWriter.init();
  }

  @Override
  public void init(DBSelectorSupplier selectorSupply) {
    this.dbSelector = selectorSupply.get();
    this.dbSelector.open(this.featureEntityName());
    this.segmentLookup = new SegmentLookup(selectorSupply.get());
  }

  public boolean isExtractorInitialized() {
    return this.featureWriter != null;
  }

  public boolean isRetrieverInitialized() {
    return this.dbSelector != null && this.segmentLookup != null;
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

  /**
   * Extracts the feature data, <i>stores it</i> and returns a list of descriptors from the feature.
   */
  @Override
  public List<MultimediaMetadataDescriptor> extract(String objectId, Path path) {
    checkState(this.isExtractorInitialized(), "extract called before init");
    Optional<T> feature = this.extractFeature(path);
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
    this.checkIfRetrieverInitialized();
    return this.extractFeature(sc)
        .map(floatVector -> this.getSimilar(ReadableFloatVector.toArray(floatVector), qc))
        .orElse(Collections.emptyList());
  }

  /**
   * Returns similar <i>objects</i> to the object of the given segment.
   *
   * <p>Note that this methods performs a {@link SegmentLookup} in order to retrieve the object id
   * of the given segment.
   */
  @Override
  public List<ScoreElement> getSimilar(String segmentId, ReadableQueryConfig rqc) {
    this.checkIfRetrieverInitialized();
    return this.segmentLookup.lookUpSegment(segmentId)
        .map(SegmentDescriptor::getObjectId)
        .map(objId -> this.dbSelector.getFeatureVectors(ID_COLUMN_NAME, objId, FEATURE_COLUMN_NAME))
        .flatMap(features -> features.stream().findFirst()) // Feature vectors are unique per id
        .map(feature -> this.getSimilar(feature, rqc))
        .orElse(Collections.emptyList());
  }

  private void checkIfRetrieverInitialized() {
    checkState(this.isRetrieverInitialized(), "getSimilar called before init");
  }

  private List<ScoreElement> getSimilar(float[] feature, ReadableQueryConfig rqc) {
    QueryConfig qc = QueryConfig.clone(rqc).setDistanceIfEmpty(this.defaultDistance());
    int maxResultsPerModule = Config.sharedConfig().getRetriever().getMaxResultsPerModule();

    List<ObjectDistanceElement> distances = this.dbSelector.getNearestNeighbours(
        maxResultsPerModule,
        feature,
        FEATURE_COLUMN_NAME,
        ObjectDistanceElement.class,
        qc
    );

    CorrespondenceFunction correspondence = qc.getCorrespondenceFunction()
        .orElse(this.defaultCorrespondence());
    return DistanceElement.toScore(distances, correspondence);
  }
}
