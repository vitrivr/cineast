package org.vitrivr.cineast.core.setup;

import java.util.ArrayList;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.vitrivr.adampro.grpc.AdamGrpc.AckMessage;
import org.vitrivr.adampro.grpc.AdamGrpc.AttributeDefinitionMessage;
import org.vitrivr.adampro.grpc.AdamGrpc.AttributeType;
import org.vitrivr.adampro.grpc.AdamGrpc.CreateEntityMessage;
import org.vitrivr.cineast.core.db.ADAMproWrapper;

import com.google.common.collect.ImmutableMap;

public class ADAMproEntityCreator implements EntityCreator {

	private static final Logger LOGGER = LogManager.getLogger();
	private ADAMproWrapper adampro = new ADAMproWrapper();
	
	/* (non-Javadoc)
   * @see org.vitrivr.cineast.core.setup.IEntityCreator#createMultiMediaObjectsEntity()
   */
	@Override
  public AckMessage createMultiMediaObjectsEntity(){
		ArrayList<AttributeDefinitionMessage> attributes = new ArrayList<>(8);
		
		AttributeDefinitionMessage.Builder builder = AttributeDefinitionMessage.newBuilder();
		
		attributes.add(builder.setName("id").setAttributetype(AttributeType.STRING).setPk(true).putAllParams(ImmutableMap.of("indexed", "true")).build());
		attributes.add(builder.setName("type").setAttributetype(AttributeType.INT).setPk(false).putAllParams(ImmutableMap.of("indexed", "true")).build());
		attributes.add(builder.setName("name").setAttributetype(AttributeType.STRING).setPk(false).build());
		attributes.add(builder.setName("path").setAttributetype(AttributeType.STRING).setPk(false).build());
		attributes.add(builder.setName("width").setAttributetype(AttributeType.INT).setPk(false).build());
		attributes.add(builder.setName("height").setAttributetype(AttributeType.INT).setPk(false).build());
		attributes.add(builder.setName("framecount").setAttributetype(AttributeType.INT).setPk(false).build());
		attributes.add(builder.setName("duration").setAttributetype(AttributeType.FLOAT).setPk(false).build());

		CreateEntityMessage message = CreateEntityMessage.newBuilder().setEntity(CINEAST_MULTIMEDIAOBJECT).addAllAttributes(attributes).build();
		
		AckMessage ack = adampro.createEntityBlocking(message);
		
		if(ack.getCode() == AckMessage.Code.OK){
			LOGGER.info("successfully created multimedia object entity");
		}else{
			LOGGER.error("error creating multimedia object entity: {}", ack.getMessage());
		}
		
		return ack;
	}
	
	/* (non-Javadoc)
   * @see org.vitrivr.cineast.core.setup.IEntityCreator#createSegmentEntity()
   */
	@Override
  public AckMessage createSegmentEntity(){
		ArrayList<AttributeDefinitionMessage> fields = new ArrayList<>(4);
		
		AttributeDefinitionMessage.Builder builder = AttributeDefinitionMessage.newBuilder();

		fields.add(builder.setName("id").setAttributetype(AttributeType.STRING).setPk(true).putAllParams(ImmutableMap.of("indexed", "true")).build());
		fields.add(builder.setName("multimediaobject").setAttributetype(AttributeType.STRING).setPk(false).putAllParams(ImmutableMap.of("indexed", "true")).build());
		fields.add(builder.setName("sequencenumber").setAttributetype(AttributeType.INT).setPk(false).build());
		fields.add(builder.setName("segmentstart").setAttributetype(AttributeType.INT).setPk(false).build());
		fields.add(builder.setName("segmentend").setAttributetype(AttributeType.INT).setPk(false).build());

		CreateEntityMessage message = CreateEntityMessage.newBuilder().setEntity(CINEAST_SEGMENT).addAllAttributes(fields).build();
		
		AckMessage ack = adampro.createEntityBlocking(message);
		
		if(ack.getCode() == AckMessage.Code.OK){
			LOGGER.info("successfully created segment entity");
		}else{
			LOGGER.error("error creating segment entity: {}", ack.getMessage());
		}
		
		return ack;
		
	}
	
	/* (non-Javadoc)
   * @see org.vitrivr.cineast.core.setup.IEntityCreator#createFeatureEntity(java.lang.String, boolean)
   */
	@Override
  public AckMessage createFeatureEntity(String featurename, boolean unique){
		return createFeatureEntity(featurename, unique, "feature");
 
	}
	
	/* (non-Javadoc)
   * @see org.vitrivr.cineast.core.setup.IEntityCreator#createFeatureEntity(java.lang.String, boolean, java.lang.String)
   */
	@Override
  public AckMessage createFeatureEntity(String featurename, boolean unique, String...featrueNames){
		ArrayList<AttributeDefinitionMessage> fields = new ArrayList<>();
		
		AttributeDefinitionMessage.Builder builder = AttributeDefinitionMessage.newBuilder();
		
		fields.add(builder.setName("id").setAttributetype(AttributeType.STRING).setPk(unique).putAllParams(ImmutableMap.of("indexed", "true")).build());
		for(String feature : featrueNames){
			fields.add(builder.setName(feature).setAttributetype(AttributeType.FEATURE).setPk(false).build());
		}
		
		CreateEntityMessage message = CreateEntityMessage.newBuilder().setEntity(featurename.toLowerCase()).addAllAttributes(fields).build();
		
		AckMessage ack = adampro.createEntityBlocking(message);
		
		if(ack.getCode() == AckMessage.Code.OK){
			LOGGER.info("successfully created feature entity {}", featurename);
		}else{
			LOGGER.error("error creating feature entity {}: {}", featurename, ack.getMessage());
		}
		
		return ack;
 
	}
	
	/* (non-Javadoc)
   * @see org.vitrivr.cineast.core.setup.IEntityCreator#createFeatureEntity(java.lang.String, boolean, org.vitrivr.cineast.core.setup.EntityCreator.AttributeDefinition)
   */
	@Override
  public AckMessage createFeatureEntity(String featurename, boolean unique, AttributeDefinition... attributes) {
		ArrayList<AttributeDefinitionMessage> fields = new ArrayList<>();

		AttributeDefinitionMessage.Builder builder = AttributeDefinitionMessage.newBuilder();

		fields.add(builder.setName("id").setAttributetype(AttributeType.STRING).setPk(unique).putAllParams(ImmutableMap.of("indexed", "true")).build());
		
		for(AttributeDefinition attribute : attributes){
			fields.add(builder.setName(attribute.name).setAttributetype(attribute.type).setPk(false).build());
		}
		
		CreateEntityMessage message = CreateEntityMessage.newBuilder().setEntity(featurename.toLowerCase()).addAllAttributes(fields).build();
		
		AckMessage ack = adampro.createEntityBlocking(message);
		
		if(ack.getCode() == AckMessage.Code.OK){
			LOGGER.info("successfully created feature entity {}", featurename);
		}else{
			LOGGER.error("error creating feature entity {}: {}", featurename, ack.getMessage());
		}
		
		return ack;
		
	}
	
	/* (non-Javadoc)
   * @see org.vitrivr.cineast.core.setup.IEntityCreator#createIdEntity(java.lang.String, org.vitrivr.cineast.core.setup.EntityCreator.AttributeDefinition)
   */
	@Override
  public AckMessage createIdEntity(String entityName, AttributeDefinition...attributes){
		ArrayList<AttributeDefinitionMessage> fieldList = new ArrayList<>();

		AttributeDefinitionMessage.Builder builder = AttributeDefinitionMessage.newBuilder();

		fieldList.add(builder.setName("id").setAttributetype(AttributeType.STRING).setPk(true).putAllParams(ImmutableMap.of("indexed", "true")).build());
		for(AttributeDefinition attribute : attributes){
			fieldList.add(builder.setName(attribute.name).setAttributetype(attribute.type).setPk(false).build());
		}

		CreateEntityMessage message = CreateEntityMessage.newBuilder().setEntity(entityName.toLowerCase()).addAllAttributes(fieldList).build();

		AckMessage ack = adampro.createEntityBlocking(message);

		if(ack.getCode() == AckMessage.Code.OK){
			LOGGER.info("successfully created feature entity {}", entityName);
		}else{
			LOGGER.error("error creating feature entity {}: {}", entityName, ack.getMessage());
		}

		return ack;
	}

	/* (non-Javadoc)
   * @see org.vitrivr.cineast.core.setup.IEntityCreator#existsEntity(java.lang.String)
   */
	@Override
  public boolean existsEntity(String entityName){
		return this.adampro.existsEntity(entityName);
	}

	/* (non-Javadoc)
   * @see org.vitrivr.cineast.core.setup.IEntityCreator#close()
   */
	@Override
  public void close(){
		this.adampro.close();
	}
	
}
