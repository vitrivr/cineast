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

public class EntityCreator {

	public static final String CINEAST_SEGMENT = "cineast_segment";
	public static final String CINEAST_MULTIMEDIAOBJECT = "cineast_multimediaobject";
	private static final Logger LOGGER = LogManager.getLogger();
	private ADAMproWrapper adampro = new ADAMproWrapper();
	
	/**
	 * Initialises the main entity holding information about mutlimedia objects
	 */
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
	
	/**
	 * Initialises the entity responsible for holding information about segments of a mutlimedia object
	 */
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
	
	/**
	 * Initialises an entity for a feature module with default parameters
	 * @param featurename the name of the feature module
	 * @param unique true if the feature module produces at most one vector per segment
	 */
	public AckMessage createFeatureEntity(String featurename, boolean unique){
		return createFeatureEntity(featurename, unique, "feature");
 
	}
	
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

	public boolean existsEntity(String entityName){
		return this.adampro.existsEntity(entityName);
	}

	public void close(){
		this.adampro.close();
	}
	
	public static class AttributeDefinition{
		private final String name;
		private final AttributeType type; //FIXME should not directly reference ADAMpro classes in order to enable exchange
		
		
		public AttributeDefinition(String name, AttributeType type){
			this.name = name;
			this.type = type;
		}
	}
	
}
