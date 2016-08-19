package ch.unibas.cs.dbis.cineast.core.config;

import java.io.File;

import com.eclipsesource.json.JsonObject;

import ch.unibas.cs.dbis.cineast.core.decode.video.JCodecVideoDecoder;
import ch.unibas.cs.dbis.cineast.core.decode.video.JLibAVVideoDecoder;
import ch.unibas.cs.dbis.cineast.core.decode.video.VideoDecoder;

public final class DecoderConfig { 

	private final int maxFrameWidth;
	private final int maxFrameHeight;
	private final Decoder decoder;
	
	public static enum Decoder{
		JCODEC,
		JLIBAV
	}
	
	public static final int DEFAULT_MAX_FRAME_WIDTH = Integer.MAX_VALUE;
	public static final int DEFAULT_MAX_FRAME_HEIGHT = Integer.MAX_VALUE;
	public static final Decoder DEFAULT_DECODER = Decoder.JLIBAV;
	
	public DecoderConfig(int maxFrameWidth, int maxFrameHeight, Decoder decoder){
		this.maxFrameWidth = maxFrameWidth;
		this.maxFrameHeight = maxFrameHeight;
		this.decoder = decoder;
	}
	
	public DecoderConfig(){
		this(DEFAULT_MAX_FRAME_WIDTH, DEFAULT_MAX_FRAME_HEIGHT, DEFAULT_DECODER);
	}
	
	public int getMaxFrameWidth(){
		return this.maxFrameWidth;
	}
	
	public int getMaxFrameHeight(){
		return this.maxFrameHeight;
	}
	
	public VideoDecoder newVideoDecoder(File file){
		switch(this.decoder){
		case JCODEC:
			return new JCodecVideoDecoder(file);
		case JLIBAV:
			return new JLibAVVideoDecoder(file);
		default:
			throw new IllegalArgumentException("trying to create invalid video decoder " + this.decoder);
		}
	}
	
	/**
	 * expects a json object of the follwing form:
	 * <pre>
	 * {
	 * 	"maxFrameWidth" : (int)
	 * 	"maxFrameHeight" : (int)
	 *  "decoder": JCODEC | JLIBAV
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
		
		Decoder decoder = DEFAULT_DECODER;
		if(obj.get("decoder") != null){
			String decoderName = "";
			try{
				decoderName = obj.get("decoder").asString();
				decoder = Decoder.valueOf(decoderName);
			} catch(UnsupportedOperationException notastring){
				throw new IllegalArgumentException("'decoder' was not a string in decoder configuration");
			} catch(IllegalArgumentException notawriter){
				throw new IllegalArgumentException("'" + decoderName + "' is not a valid value for 'decoder'");
			}
		}
		
		return new DecoderConfig(maxFrameWidth, maxFrameHeight, decoder);
		
	}
	
}
