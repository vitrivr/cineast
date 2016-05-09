package ch.unibas.cs.dbis.cineast.core.setup;

import java.util.ArrayList;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import ch.unibas.cs.dbis.cineast.core.db.ADAMproWrapper;
import ch.unibas.dmi.dbis.adam.http.Grpc.AckMessage;
import ch.unibas.dmi.dbis.adam.http.Grpc.AckMessage.Code;
import ch.unibas.dmi.dbis.adam.http.Grpc.CreateEntityMessage;
import ch.unibas.dmi.dbis.adam.http.Grpc.FieldDefinitionMessage;
import ch.unibas.dmi.dbis.adam.http.Grpc.FieldDefinitionMessage.Builder;
import ch.unibas.dmi.dbis.adam.http.Grpc.FieldDefinitionMessage.FieldType;

public class EntityCreator {

	private static final Logger LOGGER = LogManager.getLogger();
	
	/**
	 * Initialises the main entity holding information about mutlimedia objects
	 */
	public AckMessage createMultiMediaObjectsEntity(){
		ArrayList<FieldDefinitionMessage> fields = new ArrayList<>(8);
		
		Builder builder = FieldDefinitionMessage.newBuilder();
		
		fields.add(builder.setName("id").setFieldtype(FieldType.STRING).setPk(true).setIndexed(true).build());
		fields.add(builder.setName("type").setFieldtype(FieldType.INT).setPk(false).setIndexed(true).build());
		fields.add(builder.setName("name").setFieldtype(FieldType.STRING).setPk(false).setIndexed(false).build());
		fields.add(builder.setName("path").setFieldtype(FieldType.STRING).setPk(false).setIndexed(false).build());
		fields.add(builder.setName("width").setFieldtype(FieldType.INT).setPk(false).setIndexed(false).build());
		fields.add(builder.setName("height").setFieldtype(FieldType.INT).setPk(false).setIndexed(false).build());
		fields.add(builder.setName("framecount").setFieldtype(FieldType.INT).setPk(false).setIndexed(false).build());
		fields.add(builder.setName("duration").setFieldtype(FieldType.FLOAT).setPk(false).setIndexed(false).build());

		CreateEntityMessage message = CreateEntityMessage.newBuilder().setEntity("multimediaobject").addAllFields(fields).build();
		
		AckMessage ack = ADAMproWrapper.getInstance().createEntityBlocking(message);
		
		if(ack.getCode() == Code.OK){
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
		ArrayList<FieldDefinitionMessage> fields = new ArrayList<>(4);
		
		Builder builder = FieldDefinitionMessage.newBuilder();

		fields.add(builder.setName("id").setFieldtype(FieldType.STRING).setPk(true).setIndexed(true).build());
		fields.add(builder.setName("multimediaobject").setFieldtype(FieldType.LONG).setPk(false).setIndexed(true).build());
		fields.add(builder.setName("segmentstart").setFieldtype(FieldType.INT).setPk(false).setIndexed(false).build());
		fields.add(builder.setName("segmentend").setFieldtype(FieldType.INT).setPk(false).setIndexed(false).build());

		CreateEntityMessage message = CreateEntityMessage.newBuilder().setEntity("segment").addAllFields(fields).build();
		
		AckMessage ack = ADAMproWrapper.getInstance().createEntityBlocking(message);
		
		if(ack.getCode() == Code.OK){
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
		ArrayList<FieldDefinitionMessage> fields = new ArrayList<>(1);
		
		Builder builder = FieldDefinitionMessage.newBuilder();
		
		fields.add(builder.setName("id").setFieldtype(FieldType.STRING).setPk(unique).setIndexed(true).build());
		for(String feature : featrueNames){
			fields.add(builder.setName(feature).setFieldtype(FieldType.FEATURE).setPk(false).setIndexed(false).build());
		}
		
		CreateEntityMessage message = CreateEntityMessage.newBuilder().setEntity(featurename.toLowerCase()).addAllFields(fields).build();
		
		AckMessage ack = ADAMproWrapper.getInstance().createEntityBlocking(message);
		
		if(ack.getCode() == Code.OK){
			LOGGER.info("successfully created feature entity {}", featurename);
		}else{
			LOGGER.error("error creating feature entity {}: {}", featurename, ack.getMessage());
		}
		
		return ack;
 
	}
	
}
