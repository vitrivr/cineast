package org.vitrivr.cineast.core.features;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.vitrivr.cineast.core.config.ReadableQueryConfig;
import org.vitrivr.cineast.core.data.score.ScoreElement;
import org.vitrivr.cineast.core.data.segments.SegmentContainer;
import org.vitrivr.cineast.core.db.DBSelector;
import org.vitrivr.cineast.core.db.DBSelectorSupplier;
import org.vitrivr.cineast.core.db.cottontaildb.CottontailEntityCreator;
import org.vitrivr.cineast.core.db.cottontaildb.CottontailSelector;
import org.vitrivr.cineast.core.db.setup.AttributeDefinition;
import org.vitrivr.cineast.core.db.setup.EntityCreator;
import org.vitrivr.cineast.core.features.retriever.Retriever;
import org.vitrivr.cottontail.grpc.CottontailGrpc;

import java.util.*;
import java.util.function.Supplier;

/**
 * Conventional generic table for LSC / Boolean retrieval.
 * <p>
 * <h2>Usage</h2>
 * Configured as retriever in config.
 * <h3>Entity Name</h3>
 * The unique entity name is the property with key {@link ConventionalTableRetriever#ENTITY_NAME_PROPERTY}
 * <h3>Column Definitions</h3>
 * Specify column definitions as {@code properties} where the key is the column name and value is the column type {@link org.vitrivr.cineast.core.db.setup.AttributeDefinition}.
 * Currently, these types are supported:
 * <ul>
 *  <li>{@link org.vitrivr.cineast.core.db.setup.AttributeDefinition.AttributeType#LONG},</li>
 *  <li>{@link org.vitrivr.cineast.core.db.setup.AttributeDefinition.AttributeType#INT},</li>
 *  <li>{@link org.vitrivr.cineast.core.db.setup.AttributeDefinition.AttributeType#FLOAT},</li>
 *  <li>{@link org.vitrivr.cineast.core.db.setup.AttributeDefinition.AttributeType#DOUBLE},</li>
 *  <li>{@link org.vitrivr.cineast.core.db.setup.AttributeDefinition.AttributeType#STRING},</li>
 *  <li>{@link org.vitrivr.cineast.core.db.setup.AttributeDefinition.AttributeType#TEXT},</li>
 *  <li>{@link org.vitrivr.cineast.core.db.setup.AttributeDefinition.AttributeType#BOOLEAN}</li>
 * </ul>
 * <h3>Index Request</h3>
 * Special entries for index requests exist.
 * I.e. entires in {@code properties} with a key in format {@linkplain ConventionalTableRetriever#IDX_PREFIX}{@code COLNAME}
 * request an index for that column. Supported are these indices:
 * <ul>
 *     <li>{@link org.vitrivr.cottontail.grpc.CottontailGrpc.Index.IndexType#HASH}</li>
 *     <li>{@link org.vitrivr.cottontail.grpc.CottontailGrpc.Index.IndexType#HASH_UQ}</li>
 * </ul>
 *
 * <h2>Example</h2>
 * See this example for a conventional table configured in the cineast config json:
 * <pre>
 *     {
 *       "feature": "ConventionalTableRetriever",
 *       "weight": 1.0,
 *       "properties": {
 *         "entity.name": "features_table_lsc20meta",
 *         "id": "STRING",
 *         "idx.id": "HASH_UQ",
 *         "name": "STRING",
 *         "value": "INT"
 *     }
 * </pre>
 *
 * </p>
 */
public class ConventionalTableRetriever implements Retriever {

    public static final String ENTITY_NAME_PROPERTY = "entity.name";
    public static final String IDX_PREFIX = "idx.";

    private static final Logger LOGGER = LogManager.getLogger(ConventionalTableRetriever.class);
    private final String entity;
    private final HashMap<String, String> properties = new HashMap<>();
    private DBSelector selector;

    /**
     * Properties has to be a {@link LinkedHashMap}, since deserialization from the config results in a linkedhashmap
     */
    public ConventionalTableRetriever(LinkedHashMap<String, String> properties) {
        if (properties.isEmpty() || properties.size() < 2) {
            throw new IllegalArgumentException("Properties are empty. Cannot create an empty table");
        }
        if (!properties.containsKey(ENTITY_NAME_PROPERTY)) {
            throw new IllegalArgumentException("Requires property " + ENTITY_NAME_PROPERTY);
        }
        this.entity = properties.get(ENTITY_NAME_PROPERTY);
        /* Remove name for table init*/
        properties.remove(ENTITY_NAME_PROPERTY);
        this.properties.putAll(properties);
    }

    private static AttributeDefinition.AttributeType parseType(String name) {
        AttributeDefinition.AttributeType type = AttributeDefinition.AttributeType.valueOf(name);
        switch (type) {
            case LONG:
            case INT:
            case FLOAT:
            case DOUBLE:
            case STRING:
            case TEXT:
            case BOOLEAN:
                return type;
            default:
                throw new IllegalArgumentException("The given column type " + name + " could not be parsed as valid");
        }
    }

    @Override
    public void init(DBSelectorSupplier selectorSupply) {
        if (selectorSupply.get() == null || !(selectorSupply.get() instanceof CottontailSelector)) {
            throw new UnsupportedOperationException("Cannot handle NULL or non cottontail DBSelector");
        }
        this.selector = selectorSupply.get();
        this.selector.open(entity);
    }

    @Override
    public List<ScoreElement> getSimilar(SegmentContainer sc, ReadableQueryConfig qc) {
        throw new UnsupportedOperationException("Not yet implemented!");
    }

    @Override
    public List<ScoreElement> getSimilar(String segmentId, ReadableQueryConfig qc) {
        throw new UnsupportedOperationException("Not yet implemented!");
    }

    @Override
    public void finish() {
        this.selector.close();
    }

    @Override
    public void initalizePersistentLayer(Supplier<EntityCreator> supply) {
        if (supply.get() == null || !(supply.get() instanceof CottontailEntityCreator)) {
            throw new UnsupportedOperationException("Cannot handle NULL or non cottontail EntityCreator");
        }
        CottontailEntityCreator ec = (CottontailEntityCreator) supply.get();
        ArrayList<AttributeDefinition> colDefs = new ArrayList<>();
        this.properties.entrySet().stream().filter(entry -> !entry.getKey().startsWith(IDX_PREFIX)).forEach((entry) -> colDefs.add(new AttributeDefinition(entry.getKey(), parseType(entry.getValue()))));
        boolean result = ec.createEntity(this.entity, colDefs.toArray(new AttributeDefinition[0]));
        this.properties.entrySet().stream().filter(entry -> entry.getKey().startsWith(IDX_PREFIX)).forEach(entry -> ec.createIndex(this.entity, entry.getKey().substring(IDX_PREFIX.length()), parseIndex(entry.getValue())));
        LOGGER.info((result ? "Successfully " : "Failed to ") + "created entity " + entity);
        if (!result) {
            throw new RuntimeException("Could not create the entity. Something went really wrong!");
        }
    }

    private static CottontailGrpc.Index.IndexType parseIndex(String value) {
        final CottontailGrpc.Index.IndexType idx = CottontailGrpc.Index.IndexType.valueOf(value);
        if(idx == CottontailGrpc.Index.IndexType.HASH || idx == CottontailGrpc.Index.IndexType.HASH_UQ){
            return idx;
        }else{
            throw new IllegalArgumentException("Cannot parse index type "+value);
        }
    }

    @Override
    public void dropPersistentLayer(Supplier<EntityCreator> supply) {
        if (supply.get() == null || !(supply.get() instanceof CottontailEntityCreator)) {
            throw new UnsupportedOperationException("Cannot handle NULL or non cottontail EntityCreator");
        }
        supply.get().dropEntity(this.entity);
    }

    @Override
    public List<String> getTableNames() {
        return Collections.singletonList(entity);
    }
}
