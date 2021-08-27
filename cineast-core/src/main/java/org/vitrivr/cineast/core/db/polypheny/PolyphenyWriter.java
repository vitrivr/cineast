package org.vitrivr.cineast.core.db.polypheny;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import org.vitrivr.cineast.core.data.ReadableFloatVector;
import org.vitrivr.cineast.core.db.AbstractPersistencyWriter;
import org.vitrivr.cineast.core.db.PersistentTuple;

import java.util.List;

/**
 * A {@link org.vitrivr.cineast.core.db.PersistencyWriter} implementation used to write data to Polypheny DB.
 *
 * @author Ralph Gasser
 * @version 1.0.0
 */
public final class PolyphenyWriter extends AbstractPersistencyWriter<PreparedStatement> {

    /** Internal reference to the {@link PolyphenyWrapper} used by this {@link PolyphenyWriter}. */
    private final PolyphenyWrapper wrapper;

    /** The fully qualified name of the entity handled by this {@link PolyphenyWriter}. */
    private String fqn;

    public PolyphenyWriter(PolyphenyWrapper wrapper) {
        this.wrapper = wrapper;
    }

    @Override
    public boolean open(String name) {
        return true;
    }

    @Override
    public boolean close() {
        return false;
    }

    @Override
    public boolean exists(String key, String value) {
        try (final PreparedStatement stmt = this.wrapper.connection.prepareStatement("SELECT EXISTS(*) FROM " + this.fqn + " WHERE " + key + " = ?")) {
            stmt.setString(1, value);
            final ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getBoolean(1);
            } else {
                return false;
            }
        } catch (SQLException e) {
            LOGGER.error("Error occurred while executing query {}: {}", this.fqn, e);
            return false;
        }
    }

    @Override
    public boolean persist(List<PersistentTuple> tuples) {
        long start = System.currentTimeMillis();
        int size = tuples.size();
        final String query = this.createInsertStatement(tuples.get(0));
        try (final PreparedStatement stmt = this.wrapper.connection.prepareStatement(query)) {
            while (!tuples.isEmpty()) {
                bindInsertStatement(stmt, tuples.remove(0));
                stmt.addBatch();
            }
            long stop = System.currentTimeMillis();
            LOGGER.trace("Completed insert of {} elements in {} ms", size, stop - start);
            return true;
        } catch (SQLException e) {
            LOGGER.error("Error occurred while executing INSERT on {}: {}.", this.fqn, e.getMessage());
            return false;
        }
    }

    @Override
    public PreparedStatement getPersistentRepresentation(PersistentTuple tuple) {
        final String query = this.createInsertStatement(tuple);
        try (final PreparedStatement stmt = this.wrapper.connection.prepareStatement(query)) {
            return bindInsertStatement(stmt, tuple);
        } catch (SQLException e) {
            LOGGER.error("Error occurred while constructing INSERT on {}: {}", this.fqn, e);
            return null;
        }
    }

    /**
     * Constructs and returns an INSERT query for a persistent tuple.
     *
     * @param tuple The {@link PersistentTuple} to convert.
     * @return Resulting INSERT query as string.
     */
    private String createInsertStatement(PersistentTuple tuple) {
        final StringBuilder insert = new StringBuilder("INSERT INTO " + this.fqn  + "(");
        final StringBuilder values = new StringBuilder(") VALUES ( ");
        int index = 0;
        for (Object ignored : tuple.getElements()) {
            insert.append(this.names[index++]);
            values.append("?");
            if ((index) < tuple.getElements().size() - 1) {
                insert.append(",");
                insert.append(",");
            }
        }
        values.append(")");
        return insert.append(values).toString();
    }

    /**
     * Binds the provided {@link PersistentTuple} to the given {@link PreparedStatement}.
     *
     * @param stmt The {@link PreparedStatement} to bind values to.
     * @param  tuple The {@link PersistentTuple} containing the values.
     * @return True on success, false otherwise.
     */
    private PreparedStatement bindInsertStatement(PreparedStatement stmt, PersistentTuple tuple) throws SQLException {
        int index = 0;
        for (Object o : tuple.getElements()) {
            if(o instanceof Long){
                stmt.setLong(index++, (Long) o);
            } else if(o instanceof Integer){
                stmt.setInt(index++, (Integer) o);
            } else if(o instanceof Float){
                stmt.setFloat(index++, (Float) o);
            } else if(o instanceof Double){
                stmt.setDouble(index++, (Double) o);
            } else if(o instanceof Boolean){
                stmt.setBoolean(index++, (Boolean) o);
            } else if(o instanceof String){
                stmt.setString(index++, (String) o);
            } else if(o instanceof float[]){
                stmt.setObject(index++, o);
            } else if(o instanceof int[]){
                stmt.setObject(index++, o);
            } else if (o instanceof ReadableFloatVector){
                float[] array = ReadableFloatVector.toArray((ReadableFloatVector) o);
                stmt.setObject(index++, array);
            } else if (o == null) {
                stmt.setNull(index++, Types.NULL);
            } else {
                LOGGER.error("Error occurred while binding value to INSERT on {}: Encountered unsupported value {}.", this.fqn, o.toString());
            }
        }
        return stmt;
    }
}
