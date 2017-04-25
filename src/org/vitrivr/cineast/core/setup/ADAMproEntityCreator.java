package org.vitrivr.cineast.core.setup;

import java.util.ArrayList;

import org.vitrivr.adampro.grpc.AdamGrpc.AckMessage;
import org.vitrivr.adampro.grpc.AdamGrpc.AckMessage.Code;
import org.vitrivr.adampro.grpc.AdamGrpc.AttributeDefinitionMessage;
import org.vitrivr.adampro.grpc.AdamGrpc.AttributeType;
import org.vitrivr.adampro.grpc.AdamGrpc.CreateEntityMessage;
import org.vitrivr.cineast.core.data.entities.MultimediaMetadataDescriptor;
import org.vitrivr.cineast.core.data.entities.MultimediaObjectDescriptor;
import org.vitrivr.cineast.core.data.entities.SegmentDescriptor;
import org.vitrivr.cineast.core.db.ADAMproWrapper;

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
    public boolean createMultiMediaObjectsEntity(){
        ArrayList<AttributeDefinitionMessage> attributes = new ArrayList<>(8);

        AttributeDefinitionMessage.Builder builder = AttributeDefinitionMessage.newBuilder();

        attributes.add(builder.setName("objectid").setAttributetype(AttributeType.STRING).putAllParams(ImmutableMap.of("indexed", "true")).build());
        attributes.add(builder.setName("mediatype").setAttributetype(AttributeType.INT).putAllParams(ImmutableMap.of("indexed", "true")).build());
        attributes.add(builder.setName("name").setAttributetype(AttributeType.STRING).build());
        attributes.add(builder.setName("path").setAttributetype(AttributeType.STRING).build());

        CreateEntityMessage message = CreateEntityMessage.newBuilder().setEntity(MultimediaObjectDescriptor.ENTITY).addAllAttributes(attributes).build();

        AckMessage ack = adampro.createEntityBlocking(message);

        if(ack.getCode() == AckMessage.Code.OK){
            LOGGER.info("Successfully created multimedia object entity.");
        }else{
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
        ArrayList<AttributeDefinitionMessage> fields = new ArrayList<>(4);

        AttributeDefinitionMessage.Builder builder = AttributeDefinitionMessage.newBuilder();
        fields.add(builder.setName("objectid").setAttributetype(AttributeType.STRING).putAllParams(ImmutableMap.of("indexed", "true")).build());
        fields.add(builder.setName("domain").setAttributetype(AttributeType.STRING).putAllParams(ImmutableMap.of("indexed", "true")).build());
        fields.add(builder.setName("key").setAttributetype(AttributeType.STRING).putAllParams(ImmutableMap.of("indexed", "true")).build());
        fields.add(builder.setName("value").setAttributetype(AttributeType.STRING).build());

        CreateEntityMessage message = CreateEntityMessage.newBuilder().setEntity(MultimediaMetadataDescriptor.ENTITY).addAllAttributes(fields).build();

        AckMessage ack = adampro.createEntityBlocking(message);

        if(ack.getCode() == AckMessage.Code.OK){
            LOGGER.info("Successfully created metadata entity.");
        }else{
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
  public boolean createSegmentEntity(){
		ArrayList<AttributeDefinitionMessage> fields = new ArrayList<>(4);
		
		AttributeDefinitionMessage.Builder builder = AttributeDefinitionMessage.newBuilder();

		fields.add(builder.setName(SegmentDescriptor.FIELDNAMES[0]).setAttributetype(AttributeType.STRING).putAllParams(ImmutableMap.of("indexed", "true")).build());
		fields.add(builder.setName(SegmentDescriptor.FIELDNAMES[1]).setAttributetype(AttributeType.STRING).putAllParams(ImmutableMap.of("indexed", "true")).build());
		fields.add(builder.setName(SegmentDescriptor.FIELDNAMES[2]).setAttributetype(AttributeType.INT).build());
		fields.add(builder.setName(SegmentDescriptor.FIELDNAMES[3]).setAttributetype(AttributeType.INT).build());
		fields.add(builder.setName(SegmentDescriptor.FIELDNAMES[4]).setAttributetype(AttributeType.INT).build());
		fields.add(builder.setName(SegmentDescriptor.FIELDNAMES[5]).setAttributetype(AttributeType.FLOAT).build());
		fields.add(builder.setName(SegmentDescriptor.FIELDNAMES[6]).setAttributetype(AttributeType.FLOAT).build());

		CreateEntityMessage message = CreateEntityMessage.newBuilder().setEntity(SegmentDescriptor.ENTITY).addAllAttributes(fields).build();
		
		AckMessage ack = adampro.createEntityBlocking(message);
		
		if(ack.getCode() == AckMessage.Code.OK){
			LOGGER.info("Successfully created segment entity.");
		}else{
			LOGGER.error("Error occurred during creation of segment entity: {}", ack.getMessage());
		}
		
		return ack.getCode() == Code.OK;
	}

	/**
	 * Drops the main entity holding information about multimedia objects
	 */
	@Override
	public boolean dropMultiMediaObjectsEntity() {
		if (this.dropEntity(MultimediaObjectDescriptor.ENTITY)) {
			LOGGER.info("Successfully dropped multimedia object entity.");
			return true;
		} else {
			LOGGER.error("Error occurred while dropping multimedia object entity");
			return false;
		}
	}

	/**
	 * Drops the entity responsible for holding information about segments of a multimedia object
	 */
	@Override
	public boolean dropSegmentEntity() {
		if (this.dropEntity(SegmentDescriptor.ENTITY)) {
			LOGGER.info("Successfully dropped segment entity.");
			return true;
		} else {
			LOGGER.error("Error occurred while dropping segment entity");
			return false;
		}
	}

	/**
	 * Drops the entity responsible for holding metadata information about multimedia objects.
	 */
	@Override
	public boolean dropMetadataEntity() {
		if (this.dropEntity(MultimediaMetadataDescriptor.ENTITY)) {
			LOGGER.info("Successfully dropped metadata entity.");
			return true;
		} else {
			LOGGER.error("Error occurred while dropping metadata entity");
			return false;
		}
	}

	/* (non-Javadoc)
   * @see org.vitrivr.cineast.core.setup.IEntityCreator#createFeatureEntity(java.lang.String, boolean)
   */
	@Override
  public boolean createFeatureEntity(String featurename, boolean unique){
		return createFeatureEntity(featurename, unique, "feature");
 
	}
	
	/* (non-Javadoc)
   * @see org.vitrivr.cineast.core.setup.IEntityCreator#createFeatureEntity(java.lang.String, boolean, java.lang.String)
   */
	@Override
  public boolean createFeatureEntity(String featurename, boolean unique, String...featrueNames){
		ArrayList<AttributeDefinitionMessage> fields = new ArrayList<>();
		
		AttributeDefinitionMessage.Builder builder = AttributeDefinitionMessage.newBuilder();
		
		fields.add(builder.setName("id").setAttributetype(AttributeType.STRING).putAllParams(ImmutableMap.of("indexed", "true")).build());
		for(String feature : featrueNames){
			fields.add(builder.setName(feature).setAttributetype(AttributeType.VECTOR).build());
		}
		
		CreateEntityMessage message = CreateEntityMessage.newBuilder().setEntity(featurename.toLowerCase()).addAllAttributes(fields).build();
		
		AckMessage ack = adampro.createEntityBlocking(message);
		
		if(ack.getCode() == AckMessage.Code.OK){
			LOGGER.info("successfully created feature entity {}", featurename);
		}else{
			LOGGER.error("error creating feature entity {}: {}", featurename, ack.getMessage());
		}
		
		return ack.getCode() == Code.OK;
 
	}
	
	/* (non-Javadoc)
   * @see org.vitrivr.cineast.core.setup.IEntityCreator#createFeatureEntity(java.lang.String, boolean, org.vitrivr.cineast.core.setup.EntityCreator.AttributeDefinition)
   */
	@Override
  public boolean createFeatureEntity(String featurename, boolean unique, AttributeDefinition... attributes) {
		ArrayList<AttributeDefinitionMessage> fields = new ArrayList<>();

		AttributeDefinitionMessage.Builder builder = AttributeDefinitionMessage.newBuilder();

		fields.add(builder.setName("id").setAttributetype(AttributeType.STRING).putAllParams(ImmutableMap.of("indexed", "true")).build());
		
		for(AttributeDefinition attribute : attributes){
			fields.add(builder.setName(attribute.name).setAttributetype(mapAttributeType(attribute.type)).build());
		}
		
		CreateEntityMessage message = CreateEntityMessage.newBuilder().setEntity(featurename.toLowerCase()).addAllAttributes(fields).build();
		
		AckMessage ack = adampro.createEntityBlocking(message);
		
		if(ack.getCode() == AckMessage.Code.OK){
			LOGGER.info("successfully created feature entity {}", featurename);
		}else{
			LOGGER.error("error creating feature entity {}: {}", featurename, ack.getMessage());
		}
		
		return ack.getCode() == Code.OK;
		
	}
	
	/* (non-Javadoc)
   * @see org.vitrivr.cineast.core.setup.IEntityCreator#createIdEntity(java.lang.String, org.vitrivr.cineast.core.setup.EntityCreator.AttributeDefinition)
   */
	@Override
  public boolean createIdEntity(String entityName, AttributeDefinition...attributes){
		ArrayList<AttributeDefinitionMessage> fieldList = new ArrayList<>();

		AttributeDefinitionMessage.Builder builder = AttributeDefinitionMessage.newBuilder();

		fieldList.add(builder.setName("id").setAttributetype(AttributeType.STRING).putAllParams(ImmutableMap.of("indexed", "true")).build());
		for(AttributeDefinition attribute : attributes){
			fieldList.add(builder.setName(attribute.name).setAttributetype(mapAttributeType(attribute.type)).build());
		}

		CreateEntityMessage message = CreateEntityMessage.newBuilder().setEntity(entityName.toLowerCase()).addAllAttributes(fieldList).build();

		AckMessage ack = adampro.createEntityBlocking(message);

		if(ack.getCode() == AckMessage.Code.OK){
			LOGGER.info("successfully created feature entity {}", entityName);
		}else{
			LOGGER.error("error creating feature entity {}: {}", entityName, ack.getMessage());
		}

		return ack.getCode() == Code.OK;
	}

	/* (non-Javadoc)
   * @see org.vitrivr.cineast.core.setup.IEntityCreator#existsEntity(java.lang.String)
   */
	@Override
  public boolean existsEntity(String entityName){
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
  public void close(){
		this.adampro.close();
	}
	
	public static final AttributeType mapAttributeType(org.vitrivr.cineast.core.setup.AttributeDefinition.AttributeType type){
	  switch(type){
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
