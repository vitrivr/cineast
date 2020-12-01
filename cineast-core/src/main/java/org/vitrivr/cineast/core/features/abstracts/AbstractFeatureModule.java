package org.vitrivr.cineast.core.features.abstracts;

import static org.vitrivr.cineast.core.util.CineastConstants.FEATURE_COLUMN_QUALIFIER;
import static org.vitrivr.cineast.core.util.CineastConstants.GENERIC_ID_COLUMN_QUALIFIER;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.vitrivr.cineast.core.config.QueryConfig;
import org.vitrivr.cineast.core.config.ReadableQueryConfig;
import org.vitrivr.cineast.core.data.CorrespondenceFunction;
import org.vitrivr.cineast.core.data.ReadableFloatVector;
import org.vitrivr.cineast.core.data.distance.DistanceElement;
import org.vitrivr.cineast.core.data.distance.SegmentDistanceElement;
import org.vitrivr.cineast.core.data.entities.SimpleFeatureDescriptor;
import org.vitrivr.cineast.core.data.entities.SimplePrimitiveTypeProviderFeatureDescriptor;
import org.vitrivr.cineast.core.data.providers.primitive.FloatArrayProvider;
import org.vitrivr.cineast.core.data.providers.primitive.FloatArrayTypeProvider;
import org.vitrivr.cineast.core.data.providers.primitive.PrimitiveTypeProvider;
import org.vitrivr.cineast.core.data.providers.primitive.StringTypeProvider;
import org.vitrivr.cineast.core.data.score.ScoreElement;
import org.vitrivr.cineast.core.db.DBSelector;
import org.vitrivr.cineast.core.db.DBSelectorSupplier;
import org.vitrivr.cineast.core.db.PersistencyWriter;
import org.vitrivr.cineast.core.db.PersistencyWriterSupplier;
import org.vitrivr.cineast.core.db.dao.writer.PrimitiveTypeProviderFeatureDescriptorWriter;
import org.vitrivr.cineast.core.db.dao.writer.SimpleFeatureDescriptorWriter;
import org.vitrivr.cineast.core.db.setup.EntityCreator;
import org.vitrivr.cineast.core.features.extractor.Extractor;
import org.vitrivr.cineast.core.features.retriever.Retriever;

public abstract class AbstractFeatureModule implements Extractor, Retriever {

    private static final Logger LOGGER = LogManager.getLogger();
    protected SimpleFeatureDescriptorWriter writer;
    protected PrimitiveTypeProviderFeatureDescriptorWriter primitiveWriter;
    protected DBSelector selector;
    protected final float maxDist;
    protected final int vectorLength;
    protected final String tableName;
    protected PersistencyWriter<?> phandler;
    protected CorrespondenceFunction correspondence;

    protected AbstractFeatureModule(String tableName, float maxDist, int vectorLength) {
        this.tableName = tableName;
        this.maxDist = maxDist;
        this.vectorLength = vectorLength;
        this.correspondence = CorrespondenceFunction.linear(maxDist);
    }

    @Override
    public List<String> getTableNames() {
        if (this.tableName == null) {
            return new ArrayList<>();
        }
        return Collections.singletonList(this.tableName);
    }

    @Override
    public void init(PersistencyWriterSupplier phandlerSupply, int batchSize) {
        this.phandler = phandlerSupply.get();
        this.writer = new SimpleFeatureDescriptorWriter(this.phandler, this.tableName, batchSize);
        this.primitiveWriter = new PrimitiveTypeProviderFeatureDescriptorWriter(this.phandler, this.tableName, batchSize);
    }

    @Override
    public void init(DBSelectorSupplier selectorSupply) {
        this.selector = selectorSupply.get();
        this.selector.open(this.tableName);
    }

    protected void persist(String shotId, ReadableFloatVector fv) {
        SimpleFeatureDescriptor descriptor = new SimpleFeatureDescriptor(shotId, fv);
        this.writer.write(descriptor);
    }

    protected void persist(String shotId, PrimitiveTypeProvider fv) {
        SimplePrimitiveTypeProviderFeatureDescriptor descriptor = new SimplePrimitiveTypeProviderFeatureDescriptor(shotId, fv);
        this.primitiveWriter.write(descriptor);
    }

    protected void persist(String shotId, List<ReadableFloatVector> fvs) {
        List<SimpleFeatureDescriptor> entities = fvs.stream().map(fv -> new SimpleFeatureDescriptor(shotId, fv)).collect(Collectors.toList());
        this.writer.write(entities);
    }

    protected ReadableQueryConfig setQueryConfig(ReadableQueryConfig qc) {
        return new QueryConfig(qc).setCorrespondenceFunctionIfEmpty(this.correspondence);
    }

    @Override
    public List<ScoreElement> getSimilar(String segmentId, ReadableQueryConfig qc) {
        List<PrimitiveTypeProvider> list = this.selector.getFeatureVectorsGeneric(GENERIC_ID_COLUMN_QUALIFIER, new StringTypeProvider(segmentId), FEATURE_COLUMN_QUALIFIER);
        if (list.isEmpty()) {
            LOGGER.warn("No feature vector for shotId {} found, returning empty result-list", segmentId);
            return new ArrayList<>(0);
        }
        if (list.size() == 1) {
            return getSimilar(list.get(0), qc);
        }

        List<float[]> vectors = list.stream().map(FloatArrayProvider::getFloatArray).collect(Collectors.toList());
        List<SegmentDistanceElement> distances = this.selector.getBatchedNearestNeighbours(qc.getResultsPerModule(), vectors, FEATURE_COLUMN_QUALIFIER, SegmentDistanceElement.class, vectors.stream().map(x -> setQueryConfig(qc)).collect(Collectors.toList()));
        CorrespondenceFunction function = qc.getCorrespondenceFunction().orElse(correspondence);
        return ScoreElement.filterMaximumScores(DistanceElement.toScore(distances, function).stream());
    }

    public List<ScoreElement> getSimilar(List<String> segmentIds, ReadableQueryConfig qc) {
        List<PrimitiveTypeProvider> list =  this.selector.getRows(GENERIC_ID_COLUMN_QUALIFIER, segmentIds).stream().map(map -> map.get(FEATURE_COLUMN_QUALIFIER)).collect(Collectors.toList());

        if (list.isEmpty()) {
            LOGGER.warn("No feature vectors for segmentIds {} found, returning empty result-list", segmentIds);
            return new ArrayList<>(0);
        }
        if (list.size() == 1) {
            return getSimilar(list.get(0), qc);
        }

        List<float[]> vectors = list.stream().map(FloatArrayProvider::getFloatArray).collect(Collectors.toList());
        List<SegmentDistanceElement> distances = this.selector.getBatchedNearestNeighbours(qc.getResultsPerModule(), vectors, FEATURE_COLUMN_QUALIFIER, SegmentDistanceElement.class, vectors.stream().map(x -> setQueryConfig(qc)).collect(Collectors.toList()));
        CorrespondenceFunction function = qc.getCorrespondenceFunction().orElse(correspondence);
        return ScoreElement.filterMaximumScores(DistanceElement.toScore(distances, function).stream());
    }

        /**
         * helper function to retrieve elements close to a vector which has to be generated by the feature module.
         */
    protected List<ScoreElement> getSimilar(float[] vector, ReadableQueryConfig qc) {
        return getSimilar(new FloatArrayTypeProvider(vector), qc);
    }

    /**
     * Helper function to retrieve elements close to a generic primitive type
     */
    protected List<ScoreElement> getSimilar(PrimitiveTypeProvider queryProvider, ReadableQueryConfig qc) {
        ReadableQueryConfig qcc = setQueryConfig(qc);
        List<SegmentDistanceElement> distances = this.selector.getNearestNeighboursGeneric(qc.getResultsPerModule(), queryProvider, FEATURE_COLUMN_QUALIFIER, SegmentDistanceElement.class, qcc);
        CorrespondenceFunction function = qcc.getCorrespondenceFunction().orElse(correspondence);
        return DistanceElement.toScore(distances, function);
    }

    @Override
    public void finish() {
        if (this.writer != null) {
            this.writer.close();
            this.writer = null;
        }

        if (this.primitiveWriter != null) {
            this.primitiveWriter.close();
            this.primitiveWriter = null;
        }

        if (this.phandler != null) {
            this.phandler.close();
            this.phandler = null;
        }

        if (this.selector != null) {
            this.selector.close();
            this.selector = null;
        }
    }

    @Override
    public void initalizePersistentLayer(Supplier<EntityCreator> supply) {
        supply.get().createFeatureEntity(this.tableName, true, this.vectorLength);
    }

    @Override
    public void dropPersistentLayer(Supplier<EntityCreator> supply) {
        supply.get().dropEntity(this.tableName);
    }
}
