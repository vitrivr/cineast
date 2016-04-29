package ch.unibas.cs.dbis.cineast.core.setup;

import java.util.ArrayList;

import ch.unibas.dmi.dbis.adam.http.AdamDefinitionGrpc;
import ch.unibas.dmi.dbis.adam.http.AdamDefinitionGrpc.AdamDefinitionBlockingStub;
import ch.unibas.dmi.dbis.adam.http.Grpc.AckMessage;
import ch.unibas.dmi.dbis.adam.http.Grpc.CreateEntityMessage;
import ch.unibas.dmi.dbis.adam.http.Grpc.FieldDefinitionMessage;
import ch.unibas.dmi.dbis.adam.http.Grpc.FieldDefinitionMessage.Builder;
import ch.unibas.dmi.dbis.adam.http.Grpc.FieldDefinitionMessage.FieldType;
import io.grpc.ManagedChannel;

public class EntityCreator {

	private AdamDefinitionBlockingStub adamDefinition;
	
	public EntityCreator(ManagedChannel channel){
		this.adamDefinition = AdamDefinitionGrpc.newBlockingStub(channel);
	}
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
		
		return adamDefinition.createEntity(message);
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
		
		return adamDefinition.createEntity(message);
		
	}
	
	/**
	 * Initialises an entity for a feature module with default parameters
	 * @param featurename the name of the feature module
	 * @param unique true if the feature module produces at most one vector per segment
	 */
	public AckMessage createFeatureEntity(String featurename, boolean unique){
		ArrayList<FieldDefinitionMessage> fields = new ArrayList<>(1);
		
		Builder builder = FieldDefinitionMessage.newBuilder();
		
		fields.add(builder.setName("id").setFieldtype(FieldType.STRING).setPk(unique).setIndexed(true).build());
		
		CreateEntityMessage message = CreateEntityMessage.newBuilder().setEntity(featurename.toLowerCase()).addAllFields(fields).build();
		
		return adamDefinition.createEntity(message);
 
	}
	
}
