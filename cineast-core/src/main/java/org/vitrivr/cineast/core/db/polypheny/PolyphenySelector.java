package org.vitrivr.cineast.core.db.polypheny;

import org.vitrivr.cineast.core.config.ReadableQueryConfig;
import org.vitrivr.cineast.core.data.distance.DistanceElement;
import org.vitrivr.cineast.core.data.providers.primitive.PrimitiveTypeProvider;
import org.vitrivr.cineast.core.db.DBSelector;
import org.vitrivr.cineast.core.db.RelationalOperator;

import java.util.List;
import java.util.Map;

public final class PolyphenySelector implements DBSelector {

    /** Internal reference to the {@link PolyphenyWrapper} used by this {@link PolyphenyEntityCreator}. */
    private final PolyphenyWrapper wrapper;

    public PolyphenySelector(PolyphenyWrapper wrapper) {
        this.wrapper = wrapper;
    }

    @Override
    public boolean open(String name) {
        return false;
    }

    @Override
    public boolean close() {
        return false;
    }

    @Override
    public <T extends DistanceElement> List<T> getBatchedNearestNeighbours(int k, List<float[]> vectors, String column, Class<T> distanceElementClass, List<ReadableQueryConfig> configs) {
        return null;
    }

    @Override
    public List<Map<String, PrimitiveTypeProvider>> getNearestNeighbourRows(int k, float[] vector, String column, ReadableQueryConfig config) {
        return null;
    }

    @Override
    public List<float[]> getFeatureVectors(String fieldName, PrimitiveTypeProvider value, String vectorName) {
        return null;
    }

    @Override
    public List<Map<String, PrimitiveTypeProvider>> getRows(String fieldName, Iterable<PrimitiveTypeProvider> values) {
        return null;
    }

    @Override
    public List<Map<String, PrimitiveTypeProvider>> getFulltextRows(int rows, String fieldname, ReadableQueryConfig queryConfig, String... terms) {
        return null;
    }

    @Override
    public List<Map<String, PrimitiveTypeProvider>> getRows(String fieldName, RelationalOperator operator, Iterable<PrimitiveTypeProvider> values) {
        return null;
    }

    @Override
    public List<PrimitiveTypeProvider> getAll(String column) {
        return null;
    }

    @Override
    public List<Map<String, PrimitiveTypeProvider>> getAll() {
        return null;
    }

    @Override
    public boolean existsEntity(String name) {
        return false;
    }

    @Override
    public boolean ping() {
        return false;
    }
}
