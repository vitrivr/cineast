package ch.unibas.cs.dbis.cineast.core.setup;

import java.util.ArrayList;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import ch.unibas.cs.dbis.cineast.core.db.ADAMproWrapper;
import ch.unibas.dmi.dbis.adam.http.Adam.AckMessage;
import ch.unibas.dmi.dbis.adam.http.Adam.CreateEntityMessage;
import ch.unibas.dmi.dbis.adam.http.Adam.FieldDefinitionMessage;
import ch.unibas.dmi.dbis.adam.http.Adam.FieldDefinitionMessage.FieldType;

public class EntityCreator {

	public static final String CINEAST_SEGMENT = "cineast_segment";
	public static final String CINEAST_MULTIMEDIAOBJECT = "cineast_multimediaobject";
	private static final Logger LOGGER = LogManager.getLogger();
	private ADAMproWrapper adampro = new ADAMproWrapper();
	
	/**
	 * Initialises the main entity holding information about mutlimedia objects
	 */
	public AckMessage createMultiMediaObjectsEntity(){
		ArrayList<FieldDefinitionMessage> fields = new ArrayList<>(8);
		
		FieldDefinitionMessage.Builder builder = FieldDefinitionMessage.newBuilder();
		
		fields.add(builder.setName("id").setFieldtype(FieldType.STRING).setPk(true).setIndexed(true).build());
		fields.add(builder.setName("type").setFieldtype(FieldType.INT).setPk(false).setIndexed(true).build());
		fields.add(builder.setName("name").setFieldtype(FieldType.STRING).setPk(false).setIndexed(false).build());
		fields.add(builder.setName("path").setFieldtype(FieldType.STRING).setPk(false).setIndexed(false).build());
		fields.add(builder.setName("width").setFieldtype(FieldType.INT).setPk(false).setIndexed(false).build());
		fields.add(builder.setName("height").setFieldtype(FieldType.INT).setPk(false).setIndexed(false).build());
		fields.add(builder.setName("framecount").setFieldtype(FieldType.INT).setPk(false).setIndexed(false).build());
		fields.add(builder.setName("duration").setFieldtype(FieldType.FLOAT).setPk(false).setIndexed(false).build());

		CreateEntityMessage message = CreateEntityMessage.newBuilder().setEntity(CINEAST_MULTIMEDIAOBJECT).addAllFields(fields).build();
		
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
		ArrayList<FieldDefinitionMessage> fields = new ArrayList<>(4);
		
		FieldDefinitionMessage.Builder builder = FieldDefinitionMessage.newBuilder();

		fields.add(builder.setName("id").setFieldtype(FieldType.STRING).setPk(true).setIndexed(true).build());
		fields.add(builder.setName("multimediaobject").setFieldtype(FieldType.STRING).setPk(false).setIndexed(true).build());
		fields.add(builder.setName("segmentstart").setFieldtype(FieldType.INT).setPk(false).setIndexed(false).build());
		fields.add(builder.setName("segmentend").setFieldtype(FieldType.INT).setPk(false).setIndexed(false).build());

		CreateEntityMessage message = CreateEntityMessage.newBuilder().setEntity(CINEAST_SEGMENT).addAllFields(fields).build();
		
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
		ArrayList<FieldDefinitionMessage> fields = new ArrayList<>();
		
		FieldDefinitionMessage.Builder builder = FieldDefinitionMessage.newBuilder();
		
		fields.add(builder.setName("id").setFieldtype(FieldType.STRING).setPk(unique).setIndexed(true).build());
		for(String feature : featrueNames){
			fields.add(builder.setName(feature).setFieldtype(FieldType.FEATURE).setPk(false).setIndexed(false).build());
		}
		
		CreateEntityMessage message = CreateEntityMessage.newBuilder().setEntity(featurename.toLowerCase()).addAllFields(fields).build();
		
		AckMessage ack = adampro.createEntityBlocking(message);
		
		if(ack.getCode() == AckMessage.Code.OK){
			LOGGER.info("successfully created feature entity {}", featurename);
		}else{
			LOGGER.error("error creating feature entity {}: {}", featurename, ack.getMessage());
		}
		
		return ack;
 
	}
	
	public AckMessage createIdEntity(String entityName, FieldDefinition...fields){
		ArrayList<FieldDefinitionMessage> fieldList = new ArrayList<>();
		
		FieldDefinitionMessage.Builder builder = FieldDefinitionMessage.newBuilder();
		
		fieldList.add(builder.setName("id").setFieldtype(FieldType.STRING).setPk(true).setIndexed(true).build());
		for(FieldDefinition field : fields){
			fieldList.add(builder.setName(field.name).setFieldtype(field.type).setPk(false).setIndexed(false).build());
		}
		
		CreateEntityMessage message = CreateEntityMessage.newBuilder().setEntity(entityName.toLowerCase()).addAllFields(fieldList).build();
		
		AckMessage ack = adampro.createEntityBlocking(message);
		
		if(ack.getCode() == AckMessage.Code.OK){
			LOGGER.info("successfully created feature entity {}", entityName);
		}else{
			LOGGER.error("error creating feature entity {}: {}", entityName, ack.getMessage());
		}
		
		return ack;
	}
	
	public void close(){
		this.adampro.close();
	}
	
	public static class FieldDefinition{
		private final String name;
		private final FieldType type;
		
		public FieldDefinition(String name, FieldType type){
			this.name = name;
			this.type = type;
		}
	}
	
}
