package org.vitrivr.cineast.core.db.polypheny;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.concurrent.TimeUnit;
import java.util.stream.StreamSupport;
import org.apache.commons.lang3.time.StopWatch;
import org.vitrivr.cineast.core.config.ReadableQueryConfig;
import org.vitrivr.cineast.core.data.distance.DistanceElement;
import org.vitrivr.cineast.core.data.providers.primitive.PrimitiveTypeProvider;
import org.vitrivr.cineast.core.db.DBSelector;
import org.vitrivr.cineast.core.db.RelationalOperator;

import java.util.List;
import java.util.Map;
import org.vitrivr.cottontail.client.TupleIterator;

/**
 * A {@link DBSelector} implementation used to read data from Polypheny DB.
 *
 * @author Ralph Gasser
 * @version 1.0.0
 */
public final class PolyphenySelector implements DBSelector {

    /** Internal reference to the {@link PolyphenyWrapper} used by this {@link PolyphenyEntityCreator}. */
    private final PolyphenyWrapper wrapper;

    /** The fully qualified name of the entity handled by this {@link PolyphenySelector}. */
    private String fqn;

    public PolyphenySelector(PolyphenyWrapper wrapper) {
        this.wrapper = wrapper;
    }

    @Override
    public boolean open(String name) {
        this.fqn = this.wrapper.fqnInput(name);
        return false;
    }

    @Override
    public boolean close() {
        this.wrapper.close();
        return true;
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
        final Object[] mapped = StreamSupport.stream(values.spliterator(), false).map(PrimitiveTypeProvider::toObject).toArray();
        try (final PreparedStatement statement = this.wrapper.connection.prepareStatement("SELECT * FROM " + this.fqn + " WHERE " + fieldName + " IN ?")) {
            /* TODO: Bind values. */
            try (final ResultSet rs = statement.executeQuery()) {
                return processResults(rs, new HashMap<>(0));
            }
        } catch (SQLException e) {
            LOGGER.warn("Error occurred during query execution in getRows(): {}", e.getMessage());
            return new ArrayList<>(0);
        }
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
    public List<Map<String, PrimitiveTypeProvider>> getAll(List<String> columns, int limit) {
        try (final Statement statement = this.wrapper.connection.createStatement()) {
            if (limit > 0) {
                try (final ResultSet rs = statement.executeQuery("SELECT " + String.join(",", columns) + " FROM " + this.fqn + " LIMIT " + limit)) {
                    return processResults(rs, new HashMap<>(0));
                }
            } else {
                try (final ResultSet rs = statement.executeQuery("SELECT " + String.join(",", columns) + " FROM " + this.fqn)) {
                    return processResults(rs, new HashMap<>(0));
                }
            }
        } catch (SQLException e) {
            LOGGER.warn("Error occurred during query execution in getAll(): {}", e.getMessage());
            return new ArrayList<>(0);
        }
    }

    @Override
    public List<PrimitiveTypeProvider> getAll(String column) {
        try (final Statement statement = this.wrapper.connection.createStatement()) {
            try (final ResultSet rs = statement.executeQuery("SELECT " + column + " FROM " + this.fqn)) {
                return processSingleColumnResult(rs);
            }
        } catch (SQLException e) {
            LOGGER.warn("Error occurred during query execution in getAll(): {}", e.getMessage());
            return new ArrayList<>(0);
        }
    }

    @Override
    public List<Map<String, PrimitiveTypeProvider>> getAll() {
        try (final Statement statement = this.wrapper.connection.createStatement()) {
            try (final ResultSet rs = statement.executeQuery("SELECT * FROM " + this.fqn)) {
                return processResults(rs, new HashMap<>(0));
            }
        } catch (SQLException e) {
            LOGGER.warn("Error occurred during query execution in getAll(): {}", e.getMessage());
            return new ArrayList<>(0);
        }
    }

    @Override
    public boolean existsEntity(String name) {
        try {
            try (final ResultSet rs = this.wrapper.connection.getMetaData().getTables("%", PolyphenyWrapper.CINEAST_SCHEMA, name, null)) {
                return rs.next();
            }
        } catch (SQLException e) {
            LOGGER.warn("Error occurred during query execution in existsEntity(): {}", e.getMessage());
            return false;
        }
    }

    @Override
    public boolean ping() {
        try (final Statement statement = this.wrapper.connection.createStatement()) {
            try (final ResultSet rs = statement.executeQuery("SELECT 1")) {
                return rs.next();
            }
        } catch (SQLException e) {
            LOGGER.warn("Error occurred during query execution in ping(): {}", e.getMessage());
            return false;
        }
    }

    /**
     * Converts a {@link ResultSet} response generated by Polypheny DB into a {@link List} {@link PrimitiveTypeProvider}s that contain the results of a single column query.
     *
     * @param results {@link TupleIterator} to gather the results from.
     * @return {@link List} of {@link Map}s that contains the results.
     * @throws SQLException If result set processing fails.
     */
    private static List<PrimitiveTypeProvider> processSingleColumnResult(ResultSet results) throws SQLException {
        final List<PrimitiveTypeProvider> _return = new LinkedList<>();
        while (results.next()) {
            _return.add(PrimitiveTypeProvider.fromObject(results.getObject(1)));
        }
        return _return;
    }

    /**
     * Converts a {@link ResultSet} response generated by Polypheny DB into a {@link List} of column to {@link PrimitiveTypeProvider}s that contain the results of a query.
     *
     * @param results The {@link ResultSet} to convert.
     * @param mappings Custom column name mappings.
     * @return A {@link} of column name to {@link PrimitiveTypeProvider} mappings for each row.
     * @throws SQLException If result set processing fails.
     */
    private static List<Map<String, PrimitiveTypeProvider>> processResults(ResultSet results, Map<String, String> mappings) throws SQLException {
        final List<Map<String, PrimitiveTypeProvider>> _return = new LinkedList<>();
        final StopWatch watch = StopWatch.createStarted();
        final ResultSetMetaData rsmd = results.getMetaData();
        while (results.next()) {
            final Map<String, PrimitiveTypeProvider> map = new HashMap<>(rsmd.getColumnCount());
            for (int index = 1; index <= rsmd.getColumnCount(); index++) {
                final String label = rsmd.getColumnLabel(index);
                map.put(mappings.getOrDefault(label, label), PrimitiveTypeProvider.fromObject(results.getObject(index)));
            }
            _return.add(map);
        }
        LOGGER.trace("Processed {} results in {} ms", _return.size(), watch.getTime(TimeUnit.MILLISECONDS));
        return _return;
    }
}
