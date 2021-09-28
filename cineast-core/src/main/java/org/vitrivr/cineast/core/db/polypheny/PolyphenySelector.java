package org.vitrivr.cineast.core.db.polypheny;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import org.apache.commons.lang3.time.StopWatch;
import org.vitrivr.cineast.core.config.ReadableQueryConfig;
import org.vitrivr.cineast.core.config.ReadableQueryConfig.Distance;
import org.vitrivr.cineast.core.data.distance.DistanceElement;
import org.vitrivr.cineast.core.data.providers.primitive.*;
import org.vitrivr.cineast.core.db.DBSelector;
import org.vitrivr.cineast.core.db.RelationalOperator;

import static org.vitrivr.cineast.core.util.CineastConstants.GENERIC_ID_COLUMN_QUALIFIER;

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
    public <E extends DistanceElement> List<E> getNearestNeighboursGeneric(int k, float[] vector, String column, Class<E> distanceElementClass, ReadableQueryConfig config) {
        final Distance distance = config.getDistance().orElse(Distance.euclidean);
        try (final PreparedStatement statement = this.wrapper.connection.prepareStatement("SELECT id, distance(" + column + "," + toVectorString(vector) + ",'" + toName(distance) + "') as dist FROM " + this.fqn + " ORDER BY dist ASC LIMIT " + k)) {
            /* Execute query and return results. */
            try (final ResultSet rs = statement.executeQuery()) {
                return processResults(rs).stream().map( e -> {
                    final String id = e.get(GENERIC_ID_COLUMN_QUALIFIER).getString();
                    double d = e.get("dist").getDouble(); /* This should be fine. */
                    return DistanceElement.create(distanceElementClass, id, d);
                }).collect(Collectors.toList());
            }
        } catch (SQLException e) {
            LOGGER.error("Error occurred during query execution in getFeatureVectors(): {}", e.getMessage());
            return new ArrayList<>(0);
        }
    }

    @Override
    public <T extends DistanceElement> List<T> getBatchedNearestNeighbours(int k, List<float[]> vectors, String column, Class<T> distanceElementClass, List<ReadableQueryConfig> configs) {
        LOGGER.warn("Error occurred during query execution in getBatchedNearestNeighbours(): Not supported");
        return new ArrayList<>(0);
    }

    @Override
    public List<Map<String, PrimitiveTypeProvider>> getNearestNeighbourRows(int k, float[] vector, String column, ReadableQueryConfig config) {
        final Distance distance = config.getDistance().orElse(Distance.euclidean);

        try (final PreparedStatement statement = this.wrapper.connection.prepareStatement("SELECT id, distance(" + column + "," + toVectorString(vector) + ",'" + toName(distance) + "') as dist FROM " + this.fqn + " ORDER BY dist ASC LIMIT " + k)) {
            /* Execute query and return results. */
            try (final ResultSet rs = statement.executeQuery()) {
                return processResults(rs);
            }
        } catch (SQLException e) {
            LOGGER.error("Error occurred during query execution in getFeatureVectors(): {}", e.getMessage());
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
        try (final PreparedStatement statement = this.prepareInStatement(fieldName, values)) {
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
        try (final PreparedStatement statement = this.prepareStatement(fieldName, operator, values)) {
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
     * @param results {@link ResultSet} to gather the results from.
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
     * Converts a flat vector to a string representation usable by Polypheny DB.
     *
     * @param vector {@link Distance} The float vector to convert.
     * @return The resulting name.
     */
    private static String toVectorString(float[] vector) {
        final StringBuilder arrayString = new StringBuilder("ARRAY[");
        int i = 0;
        for (float v : vector) {
            if (i++ > 0) arrayString.append(",");
            arrayString.append(v);
        }
        arrayString.append("]");
        return arrayString.toString();
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
     * @param value {@link PrimitiveTypeProvider} of values to bind.
     * @param statement {@link PreparedStatement} to bind values to.
     */
    private void bindScalarValue(int index, PrimitiveTypeProvider value, PreparedStatement statement) throws SQLException {
        /* Bind values. */
        if (value instanceof DoubleProviderImpl) {
            statement.setDouble(index, value.getDouble());
        } else if (value instanceof FloatProviderImpl) {
            statement.setFloat(index, value.getFloat());
        } else if (value instanceof LongProviderImpl) {
            statement.setLong(index, value.getLong());
        } else if (value instanceof IntProviderImpl) {
            statement.setInt(index, value.getInt());
        } else if (value instanceof ShortProviderImpl) {
            statement.setShort(index, value.getShort());
        } else if (value instanceof ByteProviderImpl) {
            statement.setByte(index, value.getByte());
        } else if (value instanceof BooleanProviderImpl) {
            statement.setBoolean(index, value.getBoolean());
        } else if (value instanceof StringProviderImpl) {
            statement.setString(index, value.getString());
        } else {
            LOGGER.warn("Error occurred during query execution in getRows(): {} not supported as parameter for IN query.", value);
        }
    }

    /**
     * Prepares a prepared statement {@link PreparedStatement} for a query with a single IN predicate.
     *
     * @param fieldName Name of the field that should be queried.
     * @param values Values to use in query.
     * @return {@link PreparedStatement}
     */
    private PreparedStatement prepareStatement(String fieldName,  RelationalOperator operator, Iterable<PrimitiveTypeProvider> values) throws SQLException {
        final Object[] mapped;
        final PreparedStatement statement;
        switch (operator) {
            case ISNOTNULL:
            case ISNULL:
                return this.wrapper.connection.prepareStatement("SELECT * FROM " + this.fqn + " WHERE " + fieldName + toPredicate(operator));
            case EQ:
            case NEQ:
            case GEQ:
            case LEQ:
            case GREATER:
            case LESS:
            case LIKE:
            case NLIKE:
                mapped = StreamSupport.stream(values.spliterator(), false).limit(1).toArray();
                statement = this.wrapper.connection.prepareStatement("SELECT * FROM " + this.fqn + " WHERE " + fieldName + toPredicate(operator));
                this.bindScalarValue(1, (PrimitiveTypeProvider)mapped[0], statement);
                return statement;
            case BETWEEN:
                mapped = StreamSupport.stream(values.spliterator(), false).limit(2).toArray();
                statement = this.wrapper.connection.prepareStatement("SELECT * FROM " + this.fqn + " WHERE " + fieldName + toPredicate(operator));
                this.bindScalarValue(1, (PrimitiveTypeProvider)mapped[0], statement);
                this.bindScalarValue(2, (PrimitiveTypeProvider)mapped[1], statement);
                return statement;
            case IN:
                return this.prepareInStatement(fieldName, values);
            default:
                throw new IllegalArgumentException("Operator '" + operator + "' not supported by Cottontail DB.");
        }
    }

    /**
     * Prepares a prepared statement {@link PreparedStatement} for a query with a single IN predicate.
     *
     * @param fieldName Name of the field that should be queried.
     * @param values Values to use in query.
     * @return {@link PreparedStatement}
     * @throws SQLException
     */
    private PreparedStatement prepareInStatement(String fieldName, Iterable<PrimitiveTypeProvider> values) throws SQLException {
        /* Prepare query (apparently, JDBC doesn't support value binding for IN predicates).*/
        final StringBuilder stringStatement = new StringBuilder("SELECT * FROM " + this.fqn + " WHERE " + fieldName + " IN (");
        int index = 0;
        for (PrimitiveTypeProvider v : values) {
            if (index++ > 0) stringStatement.append(",");
            if (v instanceof StringProviderImpl) {
                stringStatement.append("'");
                stringStatement.append(v.getString());
                stringStatement.append("'");
            } else {
                stringStatement.append(v.getString());
            }
        }
        stringStatement.append(")");

        return this.wrapper.connection.prepareStatement(stringStatement.toString());
    }
}
