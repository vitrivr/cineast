package org.vitrivr.cineast.core.config;

import java.io.File;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.vitrivr.cineast.core.decode.video.FFMpegVideoDecoder;
import org.vitrivr.cineast.core.decode.video.JCodecVideoDecoder;
import org.vitrivr.cineast.core.decode.video.JLibAVVideoDecoder;
import org.vitrivr.cineast.core.decode.video.VideoDecoder;

import com.eclipsesource.json.JsonObject;

public final class DecoderConfig {

	private int maxFrameWidth =  Integer.MAX_VALUE;
	private int maxFrameHeight = Integer.MAX_VALUE;
	private Decoder decoder = Decoder.FFMPEG;
	
	public static enum Decoder {
		JCODEC,
		JLIBAV,
		FFMPEG
	}

	@JsonCreator
	public DecoderConfig() {

	}

	@JsonProperty
	public void setMaxFrameWidth(int maxFrameWidth) {
		this.maxFrameWidth = maxFrameWidth;
	}
	public void setMaxFrameHeight(int maxFrameHeight) {
		this.maxFrameHeight = maxFrameHeight;
	}

	@JsonProperty
	public void setDecoder(Decoder decoder) {
		this.decoder = decoder;
	}
	public int getMaxFrameWidth(){
		return this.maxFrameWidth;
	}

	@JsonProperty
	public int getMaxFrameHeight(){
		return this.maxFrameHeight;
	}
	public Decoder getDecoder() {
		return decoder;
	}

	public VideoDecoder newVideoDecoder(File file){
		switch(this.decoder){
		case JCODEC:
			return new JCodecVideoDecoder(file);
		case JLIBAV:
			return new JLibAVVideoDecoder(file);
		case FFMPEG:
		  return new FFMpegVideoDecoder(file);
		default:
			throw new IllegalArgumentException("trying to create invalid video decoder " + this.decoder);
		}
	}
}
