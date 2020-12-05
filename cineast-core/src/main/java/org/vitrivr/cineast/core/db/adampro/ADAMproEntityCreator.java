package org.vitrivr.cineast.core.db.adampro;

import static org.vitrivr.cineast.core.util.CineastConstants.GENERIC_ID_COLUMN_QUALIFIER;

import com.google.common.collect.ImmutableMap;
import org.vitrivr.adampro.grpc.AdamGrpc.AckMessage;
import org.vitrivr.adampro.grpc.AdamGrpc.AckMessage.Code;
import org.vitrivr.adampro.grpc.AdamGrpc.AttributeDefinitionMessage;
import org.vitrivr.adampro.grpc.AdamGrpc.AttributeType;
import org.vitrivr.adampro.grpc.AdamGrpc.CreateEntityMessage;
import org.vitrivr.cineast.core.data.entities.MediaObjectDescriptor;
import org.vitrivr.cineast.core.data.entities.MediaObjectMetadataDescriptor;
import org.vitrivr.cineast.core.data.entities.MediaSegmentDescriptor;
import org.vitrivr.cineast.core.data.entities.MediaSegmentMetadataDescriptor;
import org.vitrivr.cineast.core.db.setup.AttributeDefinition;
import org.vitrivr.cineast.core.db.setup.EntityCreator;
import org.vitrivr.cineast.core.db.setup.EntityDefinition;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

public class ADAMproEntityCreator implements EntityCreator {
    /**
     * Wrapper used to send messages to ADAM pro.
     */
    private final ADAMproWrapper adampro;

    public ADAMproEntityCreator(ADAMproWrapper wrapper){
        this.adampro = wrapper;
    }

    /**
     * Initialises the main entity holding information about multimedia objects in the ADAMpro
     * storage engine.
     */
    @Override
    public boolean createMultiMediaObjectsEntity() {
        ArrayList<AttributeDefinitionMessage> attributes = new ArrayList<>(8);

        AttributeDefinitionMessage.Builder builder = AttributeDefinitionMessage.newBuilder();

        attributes.add(builder.setName(MediaObjectDescriptor.FIELDNAMES[0]).setAttributetype(AttributeType.STRING).putAllParams(ImmutableMap.of("indexed", "true")).build());
        attributes.add(builder.setName(MediaObjectDescriptor.FIELDNAMES[1]).setAttributetype(AttributeType.INT).putAllParams(ImmutableMap.of("indexed", "true")).build());

        builder.clear(); /* Clear builder to erase the indexed flag. */

        attributes.add(builder.setName(MediaObjectDescriptor.FIELDNAMES[2]).setAttributetype(AttributeType.STRING).build());
        attributes.add(builder.setName(MediaObjectDescriptor.FIELDNAMES[3]).setAttributetype(AttributeType.STRING).build());

        CreateEntityMessage message = CreateEntityMessage.newBuilder().setEntity(MediaObjectDescriptor.ENTITY).addAllAttributes(attributes).build();

        AckMessage ack = adampro.createEntityBlocking(message);

        if (ack.getCode() == AckMessage.Code.OK) {
            LOGGER.info("Successfully created multimedia object entity.");
        } else {
            LOGGER.error("Error occurred during creation of multimedia object entity: {}", ack.getMessage());
        }

        return ack.getCode() == Code.OK;
    }

    /**
     * Initialises the entity responsible for holding metadata information about multimedia objects in a ADAMpro
     * storage.
     *
     * @see EntityCreator
     */
    @Override
    public boolean createMetadataEntity() {
        final ArrayList<AttributeDefinitionMessage> fields = new ArrayList<>(4);

        final AttributeDefinitionMessage.Builder builder = AttributeDefinitionMessage.newBuilder();
        fields.add(builder.setName(MediaObjectMetadataDescriptor.FIELDNAMES[0]).setAttributetype(AttributeType.STRING).putAllParams(ImmutableMap.of("indexed", "true")).build());
        fields.add(builder.setName(MediaObjectMetadataDescriptor.FIELDNAMES[1]).setAttributetype(AttributeType.STRING).putAllParams(ImmutableMap.of("indexed", "true")).build());
        fields.add(builder.setName(MediaObjectMetadataDescriptor.FIELDNAMES[2]).setAttributetype(AttributeType.STRING).putAllParams(ImmutableMap.of("indexed", "true")).build());

        builder.clear(); /* Clear builder to erase the indexed flag. */

        fields.add(builder.setName(MediaObjectMetadataDescriptor.FIELDNAMES[3]).setAttributetype(AttributeType.STRING).build());

        final CreateEntityMessage message = CreateEntityMessage.newBuilder().setEntity(MediaObjectMetadataDescriptor.ENTITY).addAllAttributes(fields).build();
        final AckMessage ack = adampro.createEntityBlocking(message);

        if (ack.getCode() == AckMessage.Code.OK) {
            LOGGER.info("Successfully created metadata entity.");
        } else {
            LOGGER.error("Error occurred during creation of metadata entity: {}", ack.getMessage());
        }

        return ack.getCode() == Code.OK;
    }

    @Override
    public boolean createSegmentMetadataEntity() {
        final ArrayList<AttributeDefinitionMessage> fields = new ArrayList<>(4);

        final AttributeDefinitionMessage.Builder builder = AttributeDefinitionMessage.newBuilder();
        fields.add(builder.setName(MediaSegmentMetadataDescriptor.FIELDNAMES[0]).setAttributetype(AttributeType.STRING).putAllParams(ImmutableMap.of("indexed", "true")).build());
        fields.add(builder.setName(MediaSegmentMetadataDescriptor.FIELDNAMES[1]).setAttributetype(AttributeType.STRING).putAllParams(ImmutableMap.of("indexed", "true")).build());
        fields.add(builder.setName(MediaSegmentMetadataDescriptor.FIELDNAMES[2]).setAttributetype(AttributeType.STRING).putAllParams(ImmutableMap.of("indexed", "true")).build());

        builder.clear(); /* Clear builder to erase the indexed flag. */

        fields.add(builder.setName(MediaSegmentMetadataDescriptor.FIELDNAMES[3]).setAttributetype(AttributeType.STRING).build());

        final CreateEntityMessage message = CreateEntityMessage.newBuilder().setEntity(MediaSegmentMetadataDescriptor.ENTITY).addAllAttributes(fields).build();
        final AckMessage ack = adampro.createEntityBlocking(message);

        if (ack.getCode() == AckMessage.Code.OK) {
            LOGGER.info("Successfully created metadata entity.");
        } else {
            LOGGER.error("Error occurred during creation of metadata entity: {}", ack.getMessage());
        }

        return ack.getCode() == Code.OK;
    }

    /**
     * Initialises the entity responsible for holding information about segments of a multimedia object in the
     * ADAMpro storage engine.
     *
     * @see EntityCreator
     */
    @Override
    public boolean createSegmentEntity() {
        final ArrayList<AttributeDefinitionMessage> fields = new ArrayList<>(6);

        final AttributeDefinitionMessage.Builder builder = AttributeDefinitionMessage.newBuilder();

        fields.add(builder.setName(MediaSegmentDescriptor.FIELDNAMES[0]).setAttributetype(AttributeType.STRING).putAllParams(ImmutableMap.of("indexed", "true")).build());
        fields.add(builder.setName(MediaSegmentDescriptor.FIELDNAMES[1]).setAttributetype(AttributeType.STRING).putAllParams(ImmutableMap.of("indexed", "true")).build());

        builder.clear(); /* Clear builder to erase the indexed flag. */

        fields.add(builder.setName(MediaSegmentDescriptor.FIELDNAMES[2]).setAttributetype(AttributeType.INT).build());
        fields.add(builder.setName(MediaSegmentDescriptor.FIELDNAMES[3]).setAttributetype(AttributeType.INT).build());
        fields.add(builder.setName(MediaSegmentDescriptor.FIELDNAMES[4]).setAttributetype(AttributeType.INT).build());
        fields.add(builder.setName(MediaSegmentDescriptor.FIELDNAMES[5]).setAttributetype(AttributeType.DOUBLE).build());
        fields.add(builder.setName(MediaSegmentDescriptor.FIELDNAMES[6]).setAttributetype(AttributeType.DOUBLE).build());

        final CreateEntityMessage message = CreateEntityMessage.newBuilder().setEntity(MediaSegmentDescriptor.ENTITY).addAllAttributes(fields).build();

        final AckMessage ack = adampro.createEntityBlocking(message);

        if (ack.getCode() == AckMessage.Code.OK) {
            LOGGER.info("Successfully created segment entity.");
        } else {
            LOGGER.error("Error occurred during creation of segment entity: {}", ack.getMessage());
        }

        return ack.getCode() == Code.OK;
    }



    /**
     * Creates and initializes a new feature entity with the provided name and the provided attributes. The new entity will have a field
     * called "id", which is of type "string" and has an index. Also, for each of the provided feature attribute a field of the type "vector"
     * will be created.
     *
     * @param featureEntityName ame of the new entity.
     * @param unique Whether or not the provided feature should be unique per id.
     * @param featureNames List of the feature names.
     * @return True on success, false otherwise.
     */
    @Override
    public boolean createFeatureEntity(String featureEntityName, boolean unique, int length,
        String... featureNames) {
        final AttributeDefinition[] attributes = Arrays.stream(featureNames)
            .map(s -> new AttributeDefinition(s, AttributeDefinition.AttributeType.VECTOR))
            .toArray(AttributeDefinition[]::new);
        return this.createFeatureEntity(featureEntityName, unique, attributes);
    }

    /**
     * Creates and initializes a new feature entity with the provided name and the provided attributes. The new entity will have a field
     * called "id", which is of type "string" and has an index.
     *
     * @param featureEntityName Name of the new entity.
     * @param unique Whether or not the provided feature should be unique per id.
     * @param attributes List of {@link AttributeDefinition} objects specifying the new entities attributes.
     * @return True on success, false otherwise.
     */
    @Override
    public boolean createFeatureEntity(String featureEntityName, boolean unique, AttributeDefinition... attributes) {
        final AttributeDefinition[] extended = new AttributeDefinition[attributes.length + 1];
        final HashMap<String,String> hints = new HashMap<>(1);
        hints.put("indexed", "true");
        String handler = "parquet";
        for(AttributeDefinition def : attributes){
          if(def.getType().equals(AttributeType.VECTOR) && def.hasHint("handler")){
            handler = def.getHint("handler").get();
            break;
          }
        }
        hints.put("handler", handler);
        extended[0] = new AttributeDefinition(GENERIC_ID_COLUMN_QUALIFIER, AttributeDefinition.AttributeType.STRING, hints);
        System.arraycopy(attributes, 0, extended, 1, attributes.length);
        return this.createEntity(featureEntityName, extended);
    }

    /**
     * Creates and initializes an entity with the provided name and the provided attributes. The new entity will have an additional field
     * prepended called "id", which is of type "string" and has an index.
     *
     * @param entityName Name of the new entity.
     * @param attributes List of {@link AttributeDefinition} objects specifying the new entities attributes.
     * @return True on success, false otherwise.
     */
    @Override
    public boolean createIdEntity(String entityName, AttributeDefinition... attributes) {
        final AttributeDefinition[] extended = new AttributeDefinition[attributes.length + 1];
        final HashMap<String,String> hints = new HashMap<>(1);
        hints.put("indexed", "true");
        extended[0] = new AttributeDefinition("id", AttributeDefinition.AttributeType.STRING, hints);
        System.arraycopy(attributes, 0, extended, 1, attributes.length);
        return this.createEntity(entityName, extended);
    }

    /**
     * Creates and initializes an entity with the provided name and the provided attributes.
     *
     * @param entityName Name of the new entity.
     * @param attributes List of {@link AttributeDefinition} objects specifying the new entities attributes.
     * @return True on success, false otherwise.
     */
    @Override
    public boolean createEntity(String entityName, AttributeDefinition... attributes) {
        return this.createEntity(
                new EntityDefinition.EntityDefinitionBuilder(entityName).withAttributes(attributes).build()
        );
    }

    @Override
    public boolean createEntity(EntityDefinition def){
        final ArrayList<AttributeDefinitionMessage> fieldList = new ArrayList<>();
        final AttributeDefinitionMessage.Builder builder = AttributeDefinitionMessage.newBuilder();

        for (AttributeDefinition attribute : def.getAttributes()) {
            builder.setName(attribute.getName()).setAttributetype(mapAttributeType(attribute.getType()));
            attribute.ifHintPresent("handler", builder::setHandler);
            //builder.setHandler("cassandra");
            attribute.ifHintPresent("indexed", h -> builder.putAllParams(ImmutableMap.of("indexed", h)));
            fieldList.add(builder.build());
            builder.clear();
        }

        final CreateEntityMessage message = CreateEntityMessage.newBuilder().setEntity(def.getEntityName().toLowerCase()).addAllAttributes(fieldList).build();
        final  AckMessage ack = adampro.createEntityBlocking(message);

        if (ack.getCode() == AckMessage.Code.OK) {
            LOGGER.info("Successfully created entity '{}'", def.getEntityName());
        } else {
            LOGGER.error("Error while creating entity {}: '{}'", def.getEntityName(), ack.getMessage());
        }

        return ack.getCode() == Code.OK;
    }

    /* (non-Javadoc)
     * @see org.vitrivr.cineast.core.db.setup.IEntityCreator#existsEntity(java.lang.String)
     */
    @Override
    public boolean existsEntity(String entityName) {
        return this.adampro.existsEntityBlocking(entityName);
    }

    @Override
    public boolean dropEntity(String entityName) {
        return this.adampro.dropEntityBlocking(entityName);
    }

    @Override
    public boolean createHashNonUniqueIndex(String entityName, String column) {
        /* If you are ever going to benchmark adampro vs cottontail, you should implement this method*/
        throw new UnsupportedOperationException();
    }

    public boolean dropIndex(String indexName){
        return this.adampro.dropIndexBlocking(indexName);
    }

    /* (non-Javadoc)
     * @see org.vitrivr.cineast.core.db.setup.IEntityCreator#close()
     */
    @Override
    public void close() {
        //this.adampro.close();
    }

    public static final AttributeType mapAttributeType(AttributeDefinition.AttributeType type) {
        switch (type) {
            case AUTO:
                return AttributeType.AUTO;
            case BOOLEAN:
                return AttributeType.BOOLEAN;
            case DOUBLE:
                return AttributeType.DOUBLE;
            case VECTOR:
                return AttributeType.VECTOR;
            case FLOAT:
                return AttributeType.FLOAT;
            case GEOGRAPHY:
                return AttributeType.GEOGRAPHY;
            case GEOMETRY:
                return AttributeType.GEOMETRY;
            case INT:
                return AttributeType.INT;
            case LONG:
                return AttributeType.LONG;
            case STRING:
                return AttributeType.STRING;
            case TEXT:
                return AttributeType.TEXT;
            default:
                return AttributeType.UNKOWNAT;
        }
    }

}
