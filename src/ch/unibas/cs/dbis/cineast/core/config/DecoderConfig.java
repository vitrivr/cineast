package ch.unibas.cs.dbis.cineast.core.config;

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
	
}
