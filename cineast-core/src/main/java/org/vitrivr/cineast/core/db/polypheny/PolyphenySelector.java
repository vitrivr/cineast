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
import org.vitrivr.cineast.core.config.ReadableQueryConfig.Distance;
import org.vitrivr.cineast.core.data.distance.DistanceElement;
import org.vitrivr.cineast.core.data.providers.primitive.PrimitiveTypeProvider;
import org.vitrivr.cineast.core.db.DBSelector;
import org.vitrivr.cineast.core.db.RelationalOperator;

import java.util.List;
import java.util.Map;

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
        LOGGER.warn("Error occurred during query execution in getBatchedNearestNeighbours(): Not supported");
        return new ArrayList<>(0);
    }

    @Override
    public List<Map<String, PrimitiveTypeProvider>> getNearestNeighbourRows(int k, float[] vector, String column, ReadableQueryConfig config) {
        final Distance distance = config.getDistance().orElse(Distance.euclidean);
        try (final PreparedStatement statement = this.wrapper.connection.prepareStatement("SELECT id,distance(" + column + ",?," + toName(distance) + ") as distance FROM " + this.fqn + " ORDER BY distance LIMIT " + k)) {
            /* Execute query and return results. */
            try (final ResultSet rs = statement.executeQuery()) {
                return processResults(rs);
            }
        } catch (SQLException e) {
            LOGGER.warn("Error occurred during query execution in getFeatureVectors(): {}", e.getMessage());
            return new ArrayList<>(0);
        }
    }

    @Override
    public List<float[]> getFeatureVectors(String fieldName, PrimitiveTypeProvider value, String vectorName) {
        try (final PreparedStatement statement = this.wrapper.connection.prepareStatement("SELECT * FROM " + this.fqn + " WHERE " + fieldName + " = ?")) {
            /* Execute query and return results. */
            final List<float[]> _return = new LinkedList<>();
            try (final ResultSet rs = statement.executeQuery()) {
                while (rs.next()) {
                    final Object converted = rs.getArray(vectorName).getArray();
                    if (converted instanceof float[]) {
                        _return.add((float[])converted);
                    }
                }
            }
            return _return;
        } catch (SQLException e) {
            LOGGER.warn("Error occurred during query execution in getFeatureVectors(): {}", e.getMessage());
            return new ArrayList<>(0);
        }
    }

    @Override
    public List<Map<String, PrimitiveTypeProvider>> getRows(String fieldName, Iterable<PrimitiveTypeProvider> values) {
        final Object[] mapped = StreamSupport.stream(values.spliterator(), false).map(PrimitiveTypeProvider::toObject).toArray();
        if (mapped.length == 0) return new ArrayList<>(0);
        try (final PreparedStatement statement = this.wrapper.connection.prepareStatement("SELECT * FROM " + this.fqn + " WHERE " + fieldName + " IN ?")) {
            /* Bind array to statement. */
            this.bindArrayValue(1, mapped, statement);

            /* Execute query and return results. */
            try (final ResultSet rs = statement.executeQuery()) {
                return processResults(rs);
            }
        } catch (SQLException e) {
            LOGGER.warn("Error occurred during query execution in getRows(): {}", e.getMessage());
            return new ArrayList<>(0);
        }
    }

    @Override
    public List<Map<String, PrimitiveTypeProvider>> getFulltextRows(int rows, String fieldname, ReadableQueryConfig queryConfig, String... terms) {
        LOGGER.warn("Error occurred during query execution in getFulltextRows(): Not supported");
        return new ArrayList<>(0);
    }

    @Override
    public List<Map<String, PrimitiveTypeProvider>> getRows(String fieldName, RelationalOperator operator, Iterable<PrimitiveTypeProvider> values) {
        final Object[] mapped = StreamSupport.stream(values.spliterator(), false).map(PrimitiveTypeProvider::toObject).toArray();
        try (final PreparedStatement statement = this.wrapper.connection.prepareStatement("SELECT * FROM " + this.fqn + " WHERE " + fieldName + toPredicate(operator))) {
            if (operator == RelationalOperator.IN) {
                this.bindArrayValue(1, mapped, statement);
            } else if (operator == RelationalOperator.BETWEEN) {
                this.bindScalarValue(1, mapped[0], statement);
                this.bindScalarValue(2, mapped[1], statement);
            } else {
                this.bindScalarValue(1, mapped[0], statement);
            }
            try (final ResultSet rs = statement.executeQuery()) {
                return processResults(rs);
            }
        } catch (SQLException e) {
            LOGGER.warn("Error occurred during query execution in getRows(): {}", e.getMessage());
            return new ArrayList<>(0);
        }
    }

    @Override
    public List<Map<String, PrimitiveTypeProvider>> getAll(List<String> columns, int limit) {
        try (final Statement statement = this.wrapper.connection.createStatement()) {
            if (limit > 0) {
                try (final ResultSet rs = statement.executeQuery("SELECT " + String.join(",", columns) + " FROM " + this.fqn + " LIMIT " + limit)) {
                    return processResults(rs);
                }
            } else {
                try (final ResultSet rs = statement.executeQuery("SELECT " + String.join(",", columns) + " FROM " + this.fqn)) {
                    return processResults(rs);
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
                return processResults(rs);
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
     * @return A {@link} of column name to {@link PrimitiveTypeProvider} mappings for each row.
     * @throws SQLException If result set processing fails.
     */
    private static List<Map<String, PrimitiveTypeProvider>> processResults(ResultSet results) throws SQLException {
        final List<Map<String, PrimitiveTypeProvider>> _return = new LinkedList<>();
        final StopWatch watch = StopWatch.createStarted();
        final ResultSetMetaData rsmd = results.getMetaData();
        while (results.next()) {
            final Map<String, PrimitiveTypeProvider> map = new HashMap<>(rsmd.getColumnCount());
            for (int index = 1; index <= rsmd.getColumnCount(); index++) {
                final String label = rsmd.getColumnLabel(index);
                map.put(label, PrimitiveTypeProvider.fromObject(results.getObject(index)));
            }
            _return.add(map);
        }
        LOGGER.trace("Processed {} results in {} ms", _return.size(), watch.getTime(TimeUnit.MILLISECONDS));
        return _return;
    }


    /**
     * Converts a {@link Distance} to a name usable by Polypheny DB.
     *
     * @param distance {@link Distance} The distance to convert.
     * @return The resulting name.
     */
    private static String toName(Distance distance) {
        switch (distance) {
            case chisquared:
                return "ChiSquared";
            case cosine:
                return "Cosine";
            case manhattan:
                return "L1";
            case euclidean:
                return "L2";
            case squaredeuclidean:
                return "L2 squared";
            default:
                throw new IllegalArgumentException("Distance " + distance.name() + " is not supported by Polypheny DB.");
        }
    }

    /**
     * Converts a Cineast {@link RelationalOperator} into the corresponding Cottontail DB representation.
     *
     * @param op {@link RelationalOperator} to convert.
     * @return {@link String} representing the predicate part of a query.
     */
    private static String toPredicate(RelationalOperator op) {
        switch (op) {
            case EQ:
                return "= ?";
            case NEQ:
                return "!= ?";
            case GEQ:
                return ">= ?";
            case LEQ:
                return "<= ?";
            case GREATER:
                return "> ?";
            case LESS:
                return "< ?";
            case BETWEEN:
                return "BETWEEN ?,?";
            case LIKE:
                return "LIKE ?";
            case NLIKE:
                return "NOT LIKE ?";
            case ISNULL:
                return "IS NULL";
            case ISNOTNULL:
                return "IS NOT NULL";
            case IN:
                return "IN ?";
            default:
                throw new IllegalArgumentException("Operator '" + op + "' not supported by Cottontail DB.");
        }
    }

    /**
     * Binds a scalar value to a {@link PreparedStatement}.
     *
     * @param index Index of the placeholder to bind to.
     * @param object Array of values to bind.
     * @param statement {@link PreparedStatement} to bind values to.
     * @return {@link PreparedStatement}
     * @throws SQLException
     */
    private PreparedStatement bindScalarValue(int index, Object object, PreparedStatement statement) throws SQLException {
        /* Bind values. */
        if (object instanceof Double) {
            statement.setDouble(index, (Double)object);
        } else if (object instanceof Float) {
            statement.setFloat(index, (Float)object);
        } else if (object instanceof Long) {
            statement.setLong(index, (Long)object);
        } else if (object instanceof Integer) {
            statement.setInt(index, (Integer)object);
        } else if (object instanceof Short) {
            statement.setShort(index, (Short) object);
        } else if (object instanceof Byte) {
            statement.setByte(index, (Byte)object);
        } else if (object instanceof Boolean) {
            statement.setBoolean(index, (Boolean)object);
        } else if (object instanceof String) {
            statement.setString(index, (String) object);
        } else {
            LOGGER.warn("Error occurred during query execution in getRows(): {} not supported as parameter for IN query.", object);
        }
        return statement;
    }

    /**
     * Binds an array value to a {@link PreparedStatement}.
     *
     * @param index Index of the placeholder to bind to.
     * @param object Array of values to bind.
     * @param statement {@link PreparedStatement} to bind values to.
     * @return {@link PreparedStatement}
     * @throws SQLException
     */
    private PreparedStatement bindArrayValue(int index, Object[] object, PreparedStatement statement) throws SQLException {
        /* Bind values. */
        if (object[0] instanceof Double) {
            statement.setArray(index, this.wrapper.connection.createArrayOf("DOUBLE", object));
        } else if (object[0] instanceof Float) {
            statement.setArray(index, this.wrapper.connection.createArrayOf("REAL", object));
        } else if (object[0] instanceof Long) {
            statement.setArray(index, this.wrapper.connection.createArrayOf("BIGINT", object));
        } else if (object[0] instanceof Integer) {
            statement.setArray(index, this.wrapper.connection.createArrayOf("INTEGER", object));
        } else if (object[0] instanceof Short) {
            statement.setArray(index, this.wrapper.connection.createArrayOf("SMALLINT", object));
        } else if (object[0] instanceof Byte) {
            statement.setArray(index, this.wrapper.connection.createArrayOf("TINYINT", object));
        } else if (object[0] instanceof Boolean) {
            statement.setArray(index, this.wrapper.connection.createArrayOf("BOOLEAN", object));
        } else if (object[0] instanceof String) {
            statement.setArray(index, this.wrapper.connection.createArrayOf("VARCHAR", object));
        } else {
            LOGGER.warn("Error occurred during query execution in getRows(): {} not supported as parameter for IN query.", object);
        }
        return statement;
    }
}
