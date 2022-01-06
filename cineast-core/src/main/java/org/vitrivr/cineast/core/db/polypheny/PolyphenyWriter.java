package org.vitrivr.cineast.core.db.polypheny;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.List;
import org.vitrivr.cineast.core.data.ReadableFloatVector;
import org.vitrivr.cineast.core.db.AbstractPersistencyWriter;
import org.vitrivr.cineast.core.db.PersistentTuple;

/**
 * A {@link org.vitrivr.cineast.core.db.PersistencyWriter} implementation used to write data to Polypheny DB.
 *
 * @author Ralph Gasser
 * @version 1.0.0
 */
public final class PolyphenyWriter extends AbstractPersistencyWriter<PreparedStatement> {

  /**
   * Internal reference to the {@link PolyphenyWrapper} used by this {@link PolyphenyWriter}.
   */
  private final PolyphenyWrapper wrapper;

  /**
   * The fully qualified name of the entity handled by this {@link PolyphenyWriter}.
   */
  private String fqn;

  public PolyphenyWriter(PolyphenyWrapper wrapper) {
    this.wrapper = wrapper;
  }

  @Override
  public boolean open(String name) {
    this.fqn = this.wrapper.fqnInput(name);
    return true;
  }

  @Override
  public void close() {
    this.wrapper.close();
  }

  @Override
  public boolean exists(String key, String value) {
    try (final PreparedStatement stmt = this.wrapper.connection.prepareStatement("SELECT EXISTS(SELECT " + key + " FROM " + this.fqn + " WHERE " + key + " = ?)")) {
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
      stmt.executeBatch(); /* Execute INSERTs. */
      this.wrapper.connection.commit();
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
    final StringBuilder insert = new StringBuilder("INSERT INTO ").append(this.fqn).append(" (");
    final StringBuilder values = new StringBuilder(") VALUES (");
    int index = 0;
    for (Object ignored : tuple.getElements()) {
      insert.append("\"").append(this.names[index++]).append("\"");
      values.append("?");
      if ((index) < tuple.getElements().size()) {
        insert.append(",");
        values.append(",");
      }
    }
    values.append(")");
    return insert.append(values).toString();
  }

  /**
   * Binds the provided {@link PersistentTuple} to the given {@link PreparedStatement}.
   *
   * @param stmt  The {@link PreparedStatement} to bind values to.
   * @param tuple The {@link PersistentTuple} containing the values.
   * @return True on success, false otherwise.
   */
  private PreparedStatement bindInsertStatement(PreparedStatement stmt, PersistentTuple tuple) throws SQLException {
    int index = 1;
    for (Object o : tuple.getElements()) {
      if (o instanceof Long) {
        stmt.setLong(index++, (Long) o);
      } else if (o instanceof Integer) {
        stmt.setInt(index++, (Integer) o);
      } else if (o instanceof Float) {
        stmt.setFloat(index++, (Float) o);
      } else if (o instanceof Double) {
        stmt.setDouble(index++, (Double) o);
      } else if (o instanceof Boolean) {
        stmt.setBoolean(index++, (Boolean) o);
      } else if (o instanceof String) {
        stmt.setString(index++, (String) o);
      } else if (o instanceof float[]) {
        final Object[] list = new Object[((float[]) o).length];
        for (int i = 0; i < ((float[]) o).length; i++) {
          list[i] = ((float[]) o)[i];
        }
        stmt.setArray(index++, this.wrapper.connection.createArrayOf("REAL", list));
      } else if (o instanceof int[]) {
        final Object[] list = new Object[((int[]) o).length];
        for (int i = 0; i < ((int[]) o).length; i++) {
          list[i] = ((int[]) o)[i];
        }
        stmt.setArray(index++, this.wrapper.connection.createArrayOf("INTEGER", list));
      } else if (o instanceof ReadableFloatVector) {
        final Object[] list = new Object[((ReadableFloatVector) o).getElementCount()];
        for (int i = 0; i < ((ReadableFloatVector) o).getElementCount(); i++) {
          list[i] = ((ReadableFloatVector) o).getElement(i);
        }
        stmt.setArray(index++, this.wrapper.connection.createArrayOf("REAL", list));
      } else if (o == null) {
        stmt.setNull(index++, Types.NULL);
      } else {
        LOGGER.error("Error occurred while binding value to INSERT on {}: Encountered unsupported value {}.", this.fqn, o.toString());
      }
    }
    return stmt;
  }
}
