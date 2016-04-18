package ch.unibas.cs.dbis.cineast.core.config;

import com.eclipsesource.json.JsonObject;

public final class DecoderConfig { //TODO add decoder class

	private final int maxFrameWidth;
	private final int maxFrameHeight;
	
	public static final int DEFAULT_MAX_FRAME_WIDTH = Integer.MAX_VALUE;
	public static final int DEFAULT_MAX_FRAME_HEIGHT = Integer.MAX_VALUE;
	
	public DecoderConfig(int maxFrameWidth, int maxFrameHeight){
		this.maxFrameWidth = maxFrameWidth;
		this.maxFrameHeight = maxFrameHeight;
	}
	
	public DecoderConfig(){
		this(DEFAULT_MAX_FRAME_WIDTH, DEFAULT_MAX_FRAME_HEIGHT);
	}
	
	public int getMaxFrameWidth(){
		return this.maxFrameWidth;
	}
	
	public int getMaxFrameHeight(){
		return this.maxFrameHeight;
	}
	
	/**
	 * expects a json object of the follwing form:
	 * <pre>
	 * {
	 * 	"maxFrameWidth" : (int)
	 * 	"maxFrameHeight" : (int)
	 * }
	 * </pre>
	 * @throws NullPointerException in case the given object is null
	 * @throws IllegalArgumentException in case the specified frame width or height are not positive integers
	 */
	public static DecoderConfig parse(JsonObject obj) throws NullPointerException, IllegalArgumentException{
		if(obj == null){
			throw new NullPointerException("JsonObject was null");
		}
		
		int maxFrameWidth = DEFAULT_MAX_FRAME_WIDTH;
		if(obj.get("maxFrameWidth") != null){
			try{
				maxFrameWidth = obj.get("maxFrameWidth").asInt();
			}catch(UnsupportedOperationException e){
				throw new IllegalArgumentException("'maxFrameWidth' was not an integer in decoder configuration");
			}
			
			if(maxFrameWidth <= 0){
				throw new IllegalArgumentException("'maxFrameWidth' must be > 0");
			}
		}
		
		int maxFrameHeight = DEFAULT_MAX_FRAME_HEIGHT;
		if(obj.get("maxFrameHeight") != null){
			try{
				maxFrameHeight = obj.get("maxFrameHeight").asInt();
			}catch(UnsupportedOperationException e){
				throw new IllegalArgumentException("'maxFrameHeight' was not an integer in decoder configuration");
			}
			
			if(maxFrameHeight <= 0){
				throw new IllegalArgumentException("'maxFrameHeight' must be > 0");
			}
		}
		
		return new DecoderConfig(maxFrameWidth, maxFrameHeight);
		
	}
	
}
