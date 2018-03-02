package org.vitrivr.cineast.core.setup;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

import org.vitrivr.adampro.grpc.AdamGrpc.AckMessage;
import org.vitrivr.adampro.grpc.AdamGrpc.AckMessage.Code;
import org.vitrivr.adampro.grpc.AdamGrpc.AttributeDefinitionMessage;
import org.vitrivr.adampro.grpc.AdamGrpc.AttributeType;
import org.vitrivr.adampro.grpc.AdamGrpc.CreateEntityMessage;
import org.vitrivr.cineast.core.data.entities.MultimediaMetadataDescriptor;
import org.vitrivr.cineast.core.data.entities.MultimediaObjectDescriptor;
import org.vitrivr.cineast.core.data.entities.SegmentDescriptor;
import org.vitrivr.cineast.core.db.adampro.ADAMproWrapper;

import com.google.common.collect.ImmutableMap;

public class ADAMproEntityCreator implements EntityCreator {
    /**
     * Wrapper used to send messages to ADAM pro.
     */
    private ADAMproWrapper adampro = new ADAMproWrapper();

    /**
     * Initialises the main entity holding information about multimedia objects in the ADAMpro
     * storage engine.
     */
    @Override
    public boolean createMultiMediaObjectsEntity() {
        ArrayList<AttributeDefinitionMessage> attributes = new ArrayList<>(8);

        AttributeDefinitionMessage.Builder builder = AttributeDefinitionMessage.newBuilder();

        attributes.add(builder.setName(MultimediaObjectDescriptor.FIELDNAMES[0]).setAttributetype(AttributeType.STRING).putAllParams(ImmutableMap.of("indexed", "true")).build());
        attributes.add(builder.setName(MultimediaObjectDescriptor.FIELDNAMES[1]).setAttributetype(AttributeType.INT).putAllParams(ImmutableMap.of("indexed", "true")).build());

        builder.clear(); /* Clear builder to erase the indexed flag. */

        attributes.add(builder.setName(MultimediaObjectDescriptor.FIELDNAMES[2]).setAttributetype(AttributeType.STRING).build());
        attributes.add(builder.setName(MultimediaObjectDescriptor.FIELDNAMES[3]).setAttributetype(AttributeType.STRING).build());

        CreateEntityMessage message = CreateEntityMessage.newBuilder().setEntity(MultimediaObjectDescriptor.ENTITY).addAllAttributes(attributes).build();

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
        fields.add(builder.setName(MultimediaMetadataDescriptor.FIELDNAMES[0]).setAttributetype(AttributeType.STRING).putAllParams(ImmutableMap.of("indexed", "true")).build());
        fields.add(builder.setName(MultimediaMetadataDescriptor.FIELDNAMES[1]).setAttributetype(AttributeType.STRING).putAllParams(ImmutableMap.of("indexed", "true")).build());
        fields.add(builder.setName(MultimediaMetadataDescriptor.FIELDNAMES[2]).setAttributetype(AttributeType.STRING).putAllParams(ImmutableMap.of("indexed", "true")).build());

        builder.clear(); /* Clear builder to erase the indexed flag. */

        fields.add(builder.setName(MultimediaMetadataDescriptor.FIELDNAMES[3]).setAttributetype(AttributeType.STRING).build());

        final CreateEntityMessage message = CreateEntityMessage.newBuilder().setEntity(MultimediaMetadataDescriptor.ENTITY).addAllAttributes(fields).build();
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
        final ArrayList<AttributeDefinitionMessage> fields = new ArrayList<>(4);

        final AttributeDefinitionMessage.Builder builder = AttributeDefinitionMessage.newBuilder();

        fields.add(builder.setName(SegmentDescriptor.FIELDNAMES[0]).setAttributetype(AttributeType.STRING).putAllParams(ImmutableMap.of("indexed", "true")).build());
        fields.add(builder.setName(SegmentDescriptor.FIELDNAMES[1]).setAttributetype(AttributeType.STRING).putAllParams(ImmutableMap.of("indexed", "true")).build());

        builder.clear(); /* Clear builder to erase the indexed flag. */

        fields.add(builder.setName(SegmentDescriptor.FIELDNAMES[2]).setAttributetype(AttributeType.INT).build());
        fields.add(builder.setName(SegmentDescriptor.FIELDNAMES[3]).setAttributetype(AttributeType.INT).build());
        fields.add(builder.setName(SegmentDescriptor.FIELDNAMES[4]).setAttributetype(AttributeType.INT).build());
        fields.add(builder.setName(SegmentDescriptor.FIELDNAMES[5]).setAttributetype(AttributeType.FLOAT).build());
        fields.add(builder.setName(SegmentDescriptor.FIELDNAMES[6]).setAttributetype(AttributeType.FLOAT).build());

        final CreateEntityMessage message = CreateEntityMessage.newBuilder().setEntity(SegmentDescriptor.ENTITY).addAllAttributes(fields).build();

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
     * @param featurename ame of the new entity.
     * @param unique Whether or not the provided feature should be unique per id.
     * @param featureAttributes List of the feature names.
     * @return True on success, false otherwise.
     */
    @Override
    public boolean createFeatureEntity(String featurename, boolean unique, String... featureAttributes) {
        final AttributeDefinition[] attributes = Arrays.stream(featureAttributes)
            .map(s -> new AttributeDefinition(s, AttributeDefinition.AttributeType.VECTOR))
            .toArray(AttributeDefinition[]::new);
        return this.createFeatureEntity(featurename, unique, attributes);
    }

    /**
     * Creates and initializes a new feature entity with the provided name and the provided attributes. The new entity will have a field
     * called "id", which is of type "string" and has an index.
     *
     * @param featurename Name of the new entity.
     * @param unique Whether or not the provided feature should be unique per id.
     * @param attributes List of {@link AttributeDefinition} objects specifying the new entities attributes.
     * @return True on success, false otherwise.
     */
    @Override
    public boolean createFeatureEntity(String featurename, boolean unique, AttributeDefinition... attributes) {
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
        extended[0] = new AttributeDefinition("id", AttributeDefinition.AttributeType.STRING, hints);
        System.arraycopy(attributes, 0, extended, 1, attributes.length);
        return this.createEntity(featurename, extended);
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
        final ArrayList<AttributeDefinitionMessage> fieldList = new ArrayList<>();
        final AttributeDefinitionMessage.Builder builder = AttributeDefinitionMessage.newBuilder();

        for (AttributeDefinition attribute : attributes) {
            builder.setName(attribute.getName()).setAttributetype(mapAttributeType(attribute.getType()));
            attribute.ifHintPresent("handler", builder::setHandler);
            attribute.ifHintPresent("indexed", h -> builder.putAllParams(ImmutableMap.of("indexed", h)));
            fieldList.add(builder.build());
            builder.clear();
        }

        final CreateEntityMessage message = CreateEntityMessage.newBuilder().setEntity(entityName.toLowerCase()).addAllAttributes(fieldList).build();
        final  AckMessage ack = adampro.createEntityBlocking(message);

        if (ack.getCode() == AckMessage.Code.OK) {
            LOGGER.info("Successfully created entity '{}'", entityName);
        } else {
            LOGGER.error("Error while creating entity {}: '{}'", entityName, ack.getMessage());
        }

        return ack.getCode() == Code.OK;
    }

    /* (non-Javadoc)
     * @see org.vitrivr.cineast.core.setup.IEntityCreator#existsEntity(java.lang.String)
     */
    @Override
    public boolean existsEntity(String entityName) {
        return this.adampro.existsEntityBlocking(entityName);
    }

    @Override
    public boolean dropEntity(String entityName) {
        return this.adampro.dropEntityBlocking(entityName);
    }

    /* (non-Javadoc)
     * @see org.vitrivr.cineast.core.setup.IEntityCreator#close()
     */
    @Override
    public void close() {
        this.adampro.close();
    }

    public static final AttributeType mapAttributeType(org.vitrivr.cineast.core.setup.AttributeDefinition.AttributeType type) {
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
