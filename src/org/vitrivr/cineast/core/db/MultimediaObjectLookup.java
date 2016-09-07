package org.vitrivr.cineast.core.db;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.vitrivr.cineast.core.config.Config;
import org.vitrivr.cineast.core.data.ExistenceCheck;
import org.vitrivr.cineast.core.data.providers.primitive.PrimitiveTypeProvider;
import org.vitrivr.cineast.core.data.providers.primitive.ProviderDataType;
import org.vitrivr.cineast.core.setup.EntityCreator;

public class MultimediaObjectLookup{
	
	private static final Logger LOGGER = LogManager.getLogger();
	
	private final DBSelector selector;
	
	public MultimediaObjectLookup(){
		this.selector = Config.getDatabaseConfig().getSelectorSupplier().get();
		this.selector.open(EntityCreator.CINEAST_MULTIMEDIAOBJECT);
	}
	
	
	public MultimediaObjectDescriptor lookUpObjectById(String objectId){
		List<Map<String, PrimitiveTypeProvider>> result = selector.getRows("id", objectId);
		
		if(result.isEmpty()){
			return new MultimediaObjectDescriptor();
		}
		
		return mapToDescriptor(result.get(0));		
	}
	
	private MultimediaObjectDescriptor mapToDescriptor(Map<String, PrimitiveTypeProvider> map){
		PrimitiveTypeProvider idProvider = map.get("id");
		PrimitiveTypeProvider nameProvider = map.get("name");
		PrimitiveTypeProvider pathProvider = map.get("path");
		PrimitiveTypeProvider widthProvider = map.get("width");
		PrimitiveTypeProvider heightProvider = map.get("height");
		PrimitiveTypeProvider framecountProvider = map.get("framecount");
		PrimitiveTypeProvider typeProvider = map.get("type");
		PrimitiveTypeProvider durationProvider = map.get("duration");
		
		if(!checkProvider("id", idProvider, ProviderDataType.STRING)){
			return new MultimediaObjectDescriptor();
		}
		
		if(!checkProvider("name", nameProvider, ProviderDataType.STRING)){
			return new MultimediaObjectDescriptor();
		}
		
		if(!checkProvider("path", pathProvider, ProviderDataType.STRING)){
			return new MultimediaObjectDescriptor();
		}
		
		if(!checkProvider("width", widthProvider, ProviderDataType.INT)){
			return new MultimediaObjectDescriptor();
		}
		
		if(!checkProvider("height", heightProvider, ProviderDataType.INT)){
			return new MultimediaObjectDescriptor();
		}
		
		if(!checkProvider("framecount", framecountProvider, ProviderDataType.INT)){
			return new MultimediaObjectDescriptor();
		}
		
		if(!checkProvider("type", typeProvider, ProviderDataType.INT)){
			return new MultimediaObjectDescriptor();
		}
		
		if(!checkProvider("duration", durationProvider, ProviderDataType.FLOAT)){
			return new MultimediaObjectDescriptor();
		}	
		
		
		return new MultimediaObjectDescriptor(
				idProvider.getString(),
				nameProvider.getString(),
				pathProvider.getString(),
				typeProvider.getInt(),
				widthProvider.getInt(),
				heightProvider.getInt(),
				framecountProvider.getInt(),
				durationProvider.getFloat(),
				true
				);
		
	}
	
	private boolean checkProvider(String name, PrimitiveTypeProvider provider, ProviderDataType expectedType){
		if(provider == null){
			LOGGER.error("no {} in multimedia object", name);
			return false;
		}
		
		if(provider.getType() != expectedType){
			LOGGER.error("invalid data type for field {} in multimedia object, expected {}, got {}", name, expectedType, provider.getType());
			return false;
		}
		return true;
	}
	
	public MultimediaObjectDescriptor lookUpObjectByName(String name){
		List<Map<String, PrimitiveTypeProvider>> result = selector.getRows("name", name);
		
		if(result.isEmpty()){
			return new MultimediaObjectDescriptor();
		}
		
		return mapToDescriptor(result.get(0));		
	}
	
	public Map<String, MultimediaObjectDescriptor> lookUpVideos(String... videoIds){ //TODO make more efficient
		if(videoIds == null || videoIds.length == 0){
			return new HashMap<>();
		}
		
		HashMap<String, MultimediaObjectDescriptor> _return = new HashMap<>();
		
		for(String id : videoIds){
			MultimediaObjectDescriptor descriptor = lookUpObjectById(id);
			if(descriptor.exists()){
				_return.put(id, descriptor);
			}
		}
		
		return _return;
	}

	public void close() {
		this.selector.close();
	}

	@Override
	protected void finalize() throws Throwable {
		this.close();
		super.finalize();
	}
	
public static class MultimediaObjectDescriptor implements ExistenceCheck{
		
		private final String videoId; 
		private final int width, height, framecount, type;
		private final float seconds;
		private final String name, path;
		private final boolean exists;
		
		public static MultimediaObjectDescriptor makeVideoDescriptor(String objectId, String name, String path, int width, int height, int framecount, float duration){
			return new MultimediaObjectDescriptor(objectId, name, path, 0, width, height, framecount, duration, true);
		}
		
		public static MultimediaObjectDescriptor makeImageDescriptor(String objectId, String name, String path, int width, int height){
			return new MultimediaObjectDescriptor(objectId, name, path, 1, width, height, 1, 0, true);
		}

		private MultimediaObjectDescriptor(String objectId, String name, String path, int type, int width, int height, int framecount, float duration, boolean exists){
			this.videoId = objectId;
			this.name = name;
			this.path = path;
			this.type = type;
			this.width = width;
			this.height = height;
			this.framecount = framecount;
			this.seconds = duration;
			this.exists = exists;
		}
		
		public MultimediaObjectDescriptor() {
			this("", "", "", 0, 0, 0, 0, 0, false);
		}

		
		public String getId() {
			return videoId;
		}

		public int getWidth() {
			return width;
		}

		public int getHeight() {
			return height;
		}

		public int getFramecount() {
			return framecount;
		}
		
		public float getSeconds() {
			return seconds;
		}

		public String getName() {
			return name;
		}

		public String getPath() {
			return path;
		}
		
		public float getFPS(){
			return this.framecount / this.seconds;
		}

		@Override
		public String toString() {
			return "MultimediaObjectDescriptor(" + videoId + ")";
		}

		@Override
		public boolean exists() {
			return this.exists;
		}
	}
}
