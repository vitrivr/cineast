package org.vitrivr.cineast.core.config;

import java.io.File;
import java.util.HashMap;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import org.vitrivr.cineast.core.decode.video.FFMpegVideoDecoder;
import org.vitrivr.cineast.core.decode.video.VideoDecoder;

public final class DecoderConfig {

	private String decoder = null;
	private HashMap<String, String> properties;

	@JsonCreator
	public DecoderConfig() {

	}

	@JsonProperty
	public String getDecoder() {
		return decoder;
	}
	public void setDecoder(String decoder) {
		this.decoder = decoder;
	}

	@JsonProperty
	public HashMap<String, String> getProperties() {
		return properties;
	}
	public void setDecoder(HashMap<String, String> properties) {
		this.properties = properties;
	}

	/**
	 * Returns a name decoder-property as Float. If the property is not set, the
	 * default value passed to the method is returned instead.
	 *
	 * @param name Name of the property.
	 * @param preference Preference value.
	 * @return Float value of the named property or its preference.
	 */
	public final Float namedAsFloat(String name, Float preference) {
		if (this.properties.containsKey(name)) {
			return Float.parseFloat(this.properties.get(name));
		} else {
			return preference;
		}
	}

	/**
	 * Returns a name decoder-property as Integer. If the property is not set, the
	 * default value passed to the method is returned instead.
	 *
	 * @param name Name of the property.
	 * @param preference Preference value.
	 * @return Integer value of the named property or its preference.
	 */
	public Integer namedAsInt(String name, Integer preference) {
		if (this.properties.containsKey(name)) {
			return Integer.parseInt(this.properties.get(name));
		} else {
			return preference;
		}
	}

	/**
	 * Returns a name decoder-property as String. If the property is not set, the
	 * default value passed to the method is returned instead.
	 *
	 * @param name Name of the property.
	 * @param preference Preference value.
	 * @return String value of the named property or its preference.
	 */
	public String namedAsString(String name, String preference) {
		if (this.properties.containsKey(name)) {
			return this.properties.get(name);
		} else {
			return preference;
		}
	}

	public VideoDecoder newVideoDecoder(File file) {
		return new FFMpegVideoDecoder(file);
	}
}
