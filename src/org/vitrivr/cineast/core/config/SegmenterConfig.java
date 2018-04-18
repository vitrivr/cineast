package org.vitrivr.cineast.core.config;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

import org.vitrivr.cineast.core.data.MediaType;

import org.vitrivr.cineast.core.run.ExtractionContextProvider;

import org.vitrivr.cineast.core.segmenter.audio.ConstantLengthAudioSegmenter;
import org.vitrivr.cineast.core.segmenter.general.Segmenter;
import org.vitrivr.cineast.core.segmenter.video.VideoHistogramSegmenter;

import org.vitrivr.cineast.core.util.ReflectionHelper;

import java.util.HashMap;
import java.util.Map;

public final class SegmenterConfig {

    /** Name of the {@link Segmenter} that should be used. Can be null in case there is no specific segmenter class. */
    private final String name;

    /** Properties that should be passed to the {@link Segmenter} upon initialisation. */
    private final Map<String,String> properties;

    /**
     * Constructor for {@link SegmenterConfig}. Creates a new default {@link SegmenterConfig} for the
     * specified {@link MediaType}.
     *
     * @param type {@link MediaType} for which to create a {@link SegmenterConfig}.
     */
    public SegmenterConfig(MediaType type) {
        this.properties = new HashMap<>();
        if(type==null){
            this.name = null;
            return;
        }
        switch (type) {
            case AUDIO:
                this.name = ConstantLengthAudioSegmenter.class.getName();
                break;
            case VIDEO:
                this.name = VideoHistogramSegmenter.class.getName();
                break;
            case IMAGE:
            case MODEL3D:
            default:
              this.name = null;
        }
    }

    /**
     * Constructor for {@link SegmenterConfig}. Used for deserialization of a {@link SegmenterConfig} instance from a configuration file.
     *
     * @param name The FQN of the {@link Segmenter} class.
     * @param properties Properties that should be used to setup the {@link Segmenter} class.
     */
    @JsonCreator
    public SegmenterConfig(@JsonProperty(value = "name", required = true) String name,
                           @JsonProperty(value = "properties", required = true) Map<String,String> properties) {
        this.name = name;
        this.properties = properties;
    }

    /**
     * Getter for {@link SegmenterConfig#name}
     *
     * @return  {@link SegmenterConfig#name}
     */
    public String getName() {
        return this.name;
    }

    /**
     * Getter for {@link SegmenterConfig#properties}
     *
     * @return {@link SegmenterConfig#properties}
     */
    public Map<String, String> getProperties() {
        return this.properties;
    }

    /**
     * Returns a new {@link Segmenter} based on this {@link SegmenterConfig} or null, if the new {@link Segmenter} could not be created.
     *
     * @return New {@link Segmenter} or null.
     */
    @JsonIgnore
    public <T> Segmenter<T> newSegmenter(ExtractionContextProvider contextProvider) {
        if (this.name != null) {
            return ReflectionHelper.newSegmenter(this.name, this.properties, contextProvider);
        } else {
            return null;
        }
    }
}
