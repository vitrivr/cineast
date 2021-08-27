package org.vitrivr.cineast.core.db.polypheny;

import java.util.LinkedList;
import java.util.List;
import org.vitrivr.cineast.core.config.DatabaseConfig;
import org.vitrivr.cineast.core.data.entities.MediaObjectDescriptor;
import org.vitrivr.cineast.core.data.entities.MediaObjectMetadataDescriptor;
import org.vitrivr.cineast.core.data.entities.MediaSegmentDescriptor;
import org.vitrivr.cineast.core.data.entities.MediaSegmentMetadataDescriptor;
import org.vitrivr.cineast.core.db.setup.AttributeDefinition;
import org.vitrivr.cineast.core.db.setup.EntityCreator;
import org.vitrivr.cineast.core.db.setup.EntityDefinition;

import java.sql.SQLException;
import java.sql.Statement;
import java.util.Arrays;
import java.util.HashMap;

import static org.vitrivr.cineast.core.db.setup.AttributeDefinition.AttributeType.VECTOR;
import static org.vitrivr.cineast.core.util.CineastConstants.GENERIC_ID_COLUMN_QUALIFIER;

/**
 * An {@link EntityCreator} implementation used to create the Cineast dada model in Polypheny DB.
 *
 * @author Ralph Gasser
 * @version 1.0.0
 */
public final class PolyphenyEntityCreator implements EntityCreator {

    /** Internal reference to the {@link PolyphenyWrapper} used by this {@link PolyphenyEntityCreator}. */
    private final PolyphenyWrapper wrapper;

    /** Hint used to indicate primary key fields. */
    private final static String PK_HINT = "pk";

    /** Hint used to indicate that a field is nullable. */
    private final static String NULLABLE_HINT = "nullable";

    /**
     * Constructor
     *
     * @param config
     */
    public PolyphenyEntityCreator(DatabaseConfig config) {
        this.wrapper = new PolyphenyWrapper(config);
        init();
    }

    /**
     * Constructor
     *
     * @param cottontailWrapper The {@link PolyphenyWrapper} to create this {@link PolyphenyEntityCreator} with.
     */
    public PolyphenyEntityCreator(PolyphenyWrapper cottontailWrapper) {
        this.wrapper = cottontailWrapper;
        init();
    }

    /**
     * Makes sure that schema 'cineast' is available.
     */
    private void init() {
        try (final Statement stmt= this.wrapper.connection.createStatement()) {
            stmt.execute("CREATE SCHEMA IF NOT EXISTS " + PolyphenyWrapper.CINEAST_SCHEMA);
        } catch (SQLException e) {
            LOGGER.error("Error occurred during schema initialization", e);
        }
    }

    @Override
    public boolean createMultiMediaObjectsEntity() {
        final String entityName = PolyphenyWrapper.CINEAST_SCHEMA + "." + MediaObjectDescriptor.ENTITY;
        try (final Statement stmt = this.wrapper.connection.createStatement()) {
            stmt.execute("CREATE TABLE " + entityName + " (" +
                MediaObjectDescriptor.FIELDNAMES[0] + " VARCHAR(255) NOT NULL," + /* object_id */
                MediaObjectDescriptor.FIELDNAMES[1] + " INT NOT NULL," +
                MediaObjectDescriptor.FIELDNAMES[2] + " VARCHAR(255) NOT NULL ," +
                MediaObjectDescriptor.FIELDNAMES[3] + " VARCHAR(255) NOT NULL," +
                "PRIMARY KEY (" + MediaObjectDescriptor.FIELDNAMES[0] + ")" +
            ") ON STORE " + PolyphenyWrapper.STORE_NAME_POSTGRESQL);

            /* TODO: Create index. */
            return true;
        } catch (SQLException e) {
            LOGGER.error("Error occurred while creating entity {}: {}", entityName, e);
            return false;
        }
    }

    @Override
    public boolean createSegmentEntity() {
        final String entityName = PolyphenyWrapper.CINEAST_SCHEMA + "." + MediaSegmentDescriptor.ENTITY;
        try (final Statement stmt = this.wrapper.connection.createStatement()) {
            stmt.execute("CREATE TABLE " + entityName + " (" +
                    MediaSegmentDescriptor.FIELDNAMES[0] + " VARCHAR(255) NOT NULL," + /* segment_id */
                    MediaSegmentDescriptor.FIELDNAMES[1] + " VARCHAR(255) NOT NULL," + /* object_id */
                    MediaSegmentDescriptor.FIELDNAMES[2] + " INT NOT NULL ," +
                    MediaSegmentDescriptor.FIELDNAMES[3] + " INT NOT NULL," +
                    MediaSegmentDescriptor.FIELDNAMES[4] + " INT NOT NULL," +
                    MediaSegmentDescriptor.FIELDNAMES[5] + " REAL NOT NULL," +
                    MediaSegmentDescriptor.FIELDNAMES[6] + " REAL NOT NULL," +
                    "PRIMARY KEY (" + MediaSegmentDescriptor.FIELDNAMES[0] + ")" +
                    ") ON STORE " + PolyphenyWrapper.STORE_NAME_POSTGRESQL);

            /* TODO: Create index on segment_id and object_id. */
            return true;
        } catch (SQLException e) {
            LOGGER.error("Error occurred while creating entity {}: {}", entityName, e);
            return false;
        }
    }

    @Override
    public boolean createMetadataEntity() {
        final String entityName = PolyphenyWrapper.CINEAST_SCHEMA + "." + MediaObjectMetadataDescriptor.ENTITY;
        try (final Statement stmt = this.wrapper.connection.createStatement()) {
            stmt.execute("CREATE TABLE " + entityName + " (" +
                    MediaObjectMetadataDescriptor.FIELDNAMES[0] + " VARCHAR(255) NOT NULL," + /* object_id */
                    MediaObjectMetadataDescriptor.FIELDNAMES[1] + " VARCHAR(255) NOT NULL," + /* domain */
                    MediaObjectMetadataDescriptor.FIELDNAMES[2] + " VARCHAR(255) NOT NULL," + /* key */
                    "\"" + MediaObjectMetadataDescriptor.FIELDNAMES[3] + "\"" + " VARCHAR(255) NOT NULL," + /* value */
                    "PRIMARY KEY (" + MediaObjectMetadataDescriptor.FIELDNAMES[0] + "," + MediaObjectMetadataDescriptor.FIELDNAMES[1] + "," + MediaObjectMetadataDescriptor.FIELDNAMES[2] + ")" +
                    ") ON STORE " + PolyphenyWrapper.STORE_NAME_POSTGRESQL);

            /* TODO: Create index on object_id and domain. */
            return true;
        } catch (SQLException e) {
            LOGGER.error("Error occurred while creating entity {}: {}", entityName, e);
            return false;
        }
    }

    @Override
    public boolean createSegmentMetadataEntity() {
        final String entityName = PolyphenyWrapper.CINEAST_SCHEMA + "." + MediaSegmentMetadataDescriptor.ENTITY;
        try (final Statement stmt = this.wrapper.connection.createStatement()) {
            stmt.execute("CREATE TABLE " + entityName + " (" +
                    MediaSegmentMetadataDescriptor.FIELDNAMES[0] + " VARCHAR(255) NOT NULL," + /* segment_id */
                    MediaSegmentMetadataDescriptor.FIELDNAMES[1] + " VARCHAR(255) NOT NULL," + /* domain */
                    MediaSegmentMetadataDescriptor.FIELDNAMES[2] + " VARCHAR(255) NOT NULL ," + /* key */
                    "\"" + MediaObjectMetadataDescriptor.FIELDNAMES[3] + "\"" + " VARCHAR(255) NOT NULL," + /* value */
                    "PRIMARY KEY (" + MediaSegmentMetadataDescriptor.FIELDNAMES[0] + "," + MediaSegmentMetadataDescriptor.FIELDNAMES[1] + "," + MediaSegmentMetadataDescriptor.FIELDNAMES[2] + ")" +
                    ") ON STORE " + PolyphenyWrapper.STORE_NAME_POSTGRESQL);

            /* TODO: Create index on object_id and domain. */
            return true;
        } catch (SQLException e) {
            LOGGER.error("Error occurred while creating entity {}: {}", entityName, e);
            return false;
        }
    }

    @Override
    public boolean createFeatureEntity(String featureEntityName, boolean unique, int length, String... featureNames) {
        final AttributeDefinition[] attributes = Arrays.stream(featureNames).map(s -> new AttributeDefinition(s, VECTOR, length)).toArray(AttributeDefinition[]::new);
        return this.createFeatureEntity(featureEntityName, unique, attributes);
    }

    @Override
    public boolean createFeatureEntity(String featureEntityName, boolean unique, AttributeDefinition... attributes) {
        final AttributeDefinition[] extended = new AttributeDefinition[attributes.length + 1];
        final HashMap<String, String> hints = new HashMap<>(2);
        if (unique) {
            hints.put(PK_HINT, Boolean.TRUE.toString());
            hints.put(NULLABLE_HINT, Boolean.FALSE.toString());
        }
        extended[0] = new AttributeDefinition(GENERIC_ID_COLUMN_QUALIFIER, AttributeDefinition.AttributeType.STRING, hints);
        System.arraycopy(attributes, 0, extended, 1, attributes.length);
        return this.createEntity(featureEntityName, extended);
    }

    @Override
    public boolean createIdEntity(String entityName, AttributeDefinition... attributes) {
        final AttributeDefinition[] extended = new AttributeDefinition[attributes.length + 1];
        final HashMap<String, String> hints = new HashMap<>(2);
        hints.put(PK_HINT, Boolean.TRUE.toString());
        hints.put(NULLABLE_HINT, Boolean.FALSE.toString());
        extended[0] = new AttributeDefinition(GENERIC_ID_COLUMN_QUALIFIER, AttributeDefinition.AttributeType.STRING, hints);
        System.arraycopy(attributes, 0, extended, 1, attributes.length);
        return this.createEntity(entityName, extended);
    }

    @Override
    public boolean createEntity(String entityName, AttributeDefinition... attributes) {
        return this.createEntity(new org.vitrivr.cineast.core.db.setup.EntityDefinition.EntityDefinitionBuilder(entityName).withAttributes(attributes).build());
    }

    @Override
    public boolean createEntity(EntityDefinition entityDefinition) {
        final String entityFullName = PolyphenyWrapper.CINEAST_SCHEMA + "." + entityDefinition.getEntityName();
        try (final Statement stmt = this.wrapper.connection.createStatement()) {
            final StringBuilder builder = new StringBuilder("CREATE TABLE " + entityFullName + " (");
            String store = PolyphenyWrapper.STORE_NAME_POSTGRESQL;
            List<String> pk = new LinkedList<>();
            int index = 0;
            for (AttributeDefinition attribute : entityDefinition.getAttributes()) {
                switch (attribute.getType()) {
                    case BOOLEAN:
                        builder.append(attribute.getName() + " BOOLEAN ");
                        break;
                    case DOUBLE:
                        builder.append(attribute.getName() + " DOUBLE ");
                        break;
                    case FLOAT:
                        builder.append(attribute.getName() + " REAL ");
                        break;
                    case INT:
                        builder.append(attribute.getName() + " INT ");
                        break;
                    case LONG:
                        builder.append(attribute.getName() + " LONG ");
                        break;
                    case STRING:
                        builder.append(attribute.getName() + " VARCHAR(255) ");
                        break;
                    case TEXT:
                        builder.append(attribute.getName() + " VARCHAR(65535) ");
                        break;
                    case VECTOR:
                        builder.append(attribute.getName() + " REAL ARRAY(1," + attribute.getLength() + ") ");
                        store = PolyphenyWrapper.STORE_NAME_COTTONTAIL;
                        break;
                    case BITSET:
                        builder.append(attribute.getName() + " BOOLEAN ARRAY(1," + attribute.getLength() + ") ");
                        store = PolyphenyWrapper.STORE_NAME_COTTONTAIL;
                        break;
                    default:
                        throw new RuntimeException("Type " + attribute.getType() + " has no matching analogue in Cottontail DB");
                }

                if (attribute.getHint(NULLABLE_HINT).map(h -> h.equals(Boolean.TRUE.toString())).orElse(true)) {
                    builder.append("NULL");
                } else {
                    builder.append("NOT NULL");
                }

                if (attribute.getHint(PK_HINT).map(h -> h.equals(Boolean.TRUE.toString())).orElse(false)) {
                    pk.add(attribute.getName());
                }
                if ((index++) < entityDefinition.getAttributes().size() - 1) {
                    builder.append(", ");
                }
            }

            /* Add PRIMARY KEY definition. */
            if (!pk.isEmpty()) {
                builder.append(", PRIMARY KEY(");
                builder.append(String.join(",", pk));
                builder.append(")");
            }

            /* Add ON STORE. */
            builder.append(") ON STORE " + store);
            stmt.execute(builder.toString());
            return true;
        } catch (SQLException e) {
            LOGGER.error("Error occurred while creating entity {}: {}", entityFullName, e);
            return false;
        }
    }

    @Override
    public boolean existsEntity(String entityName) {
        final String entityFullName = PolyphenyWrapper.CINEAST_SCHEMA + "." + entityName;
        try (final Statement stmt = this.wrapper.connection.createStatement()) {
            stmt.executeQuery("SELECT * FROM " + entityFullName);
            return true;
        } catch (SQLException e) {
            return false;
        }
    }

    @Override
    public boolean dropEntity(String entityName) {
        final String entityFullName = PolyphenyWrapper.CINEAST_SCHEMA + "." + entityName;
        try (final Statement stmt = this.wrapper.connection.createStatement()) {
            stmt.execute("DROP TABLE IF EXISTS " + entityFullName);
            return true;
        } catch (SQLException e) {
            LOGGER.error("Error occurred while creating entity {}: {}", entityName, e);
            return false;
        }
    }

    @Override
    public boolean createHashNonUniqueIndex(String entityName, String column) {
        return false;
    }

    @Override
    public void close() {
        this.wrapper.close();
    }
}
