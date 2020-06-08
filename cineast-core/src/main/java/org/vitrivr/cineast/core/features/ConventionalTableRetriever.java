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
 *
 *
 * </p>
 */
public class ConventionalTableRetriever implements Retriever {

    public static final String ENTITY_NAME_PROPERTY = "entity.name";

    private static final Logger LOGGER = LogManager.getLogger(ConventionalTableRetriever.class);
    private final String entity;
    private final HashMap<String, String> properties = new HashMap<>();
    private DBSelector selector;

    /**
     * @param properties
     */
    public ConventionalTableRetriever(LinkedHashMap<String, String> properties) {
        if(properties.isEmpty() || properties.size() < 2){
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
        return null;
    }

    @Override
    public List<ScoreElement> getSimilar(String segmentId, ReadableQueryConfig qc) {
        return null;
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
        this.properties.forEach((key, value) -> colDefs.add(new AttributeDefinition(key, parseType(value))));
        boolean result = ec.createEntity(this.entity, colDefs.toArray(new AttributeDefinition[0]));
        LOGGER.info((result ? "Successfully ":"Failed to ")+"created entity "+entity);
        if(!result){
            throw new RuntimeException("Could not create the entity. Something went really wrong!");
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

    private static AttributeDefinition.AttributeType parseType(String name){
        AttributeDefinition.AttributeType type = AttributeDefinition.AttributeType.valueOf(name);
        switch(type){
            case LONG:
            case INT:
            case FLOAT:
            case DOUBLE:
            case STRING:
            case TEXT:
            case BOOLEAN:
                return type;
            default:
                throw new IllegalArgumentException("The given column type "+name+" could not be parsed as valid");
        }
    }
}
