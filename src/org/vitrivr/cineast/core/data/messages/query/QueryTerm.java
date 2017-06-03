package org.vitrivr.cineast.core.data.messages.query;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.awt.image.BufferedImage;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import javax.annotation.Nullable;

import org.vitrivr.cineast.core.data.GpsData;
import org.vitrivr.cineast.core.data.MultiImageFactory;
import org.vitrivr.cineast.core.data.frames.AudioFrame;
import org.vitrivr.cineast.core.data.m3d.Mesh;
import org.vitrivr.cineast.core.data.query.containers.*;
import org.vitrivr.cineast.core.util.json.JacksonJsonProvider;
import org.vitrivr.cineast.core.util.web.AudioParser;
import org.vitrivr.cineast.core.util.web.ImageParser;
import org.vitrivr.cineast.core.util.web.MeshParser;

/**
 * @author rgasser
 * @version 1.0
 * @created 11.01.17
 */
public class QueryTerm {
    private static final JacksonJsonProvider jsonProvider = new JacksonJsonProvider();

    /**
     * List of categories defined as part of the query-term. This ultimately selects the feature-vectors
     * used for retrieval.
     */
    private final String[] categories;

    /** Denotes the type of QueryTerm. */
    private final QueryTermType type;

    /** Base64 encoded representation of the query-object associated with this query term. */
    private final String data;

    /** Cached version of the QueryContainer representation of this QueryTerm. */
    private QueryContainer cachedQueryContainer;

    @JsonCreator
    public QueryTerm(@JsonProperty("type") QueryTermType type, @JsonProperty("data") String data,
                     @JsonProperty("categories") String[] categories) {
        this.type = type;
        this.categories = categories;
        this.data = data;
    }

    public List<String> getCategories() {
        return Arrays.asList(this.categories);
    }

    public QueryTermType getType() {
        return type;
    }

    @Nullable
    public QueryContainer toContainer() {
        if (this.cachedQueryContainer == null) {
            if (this.data == null) return null;
            switch (this.type) {
                case IMAGE:
                    BufferedImage image = ImageParser.dataURLtoBufferedImage(this.data);
                    this.cachedQueryContainer = new ImageQueryContainer(MultiImageFactory.newInMemoryMultiImage(image));
                    break;
                case MOTION:
                    return MotionQueryContainer.fromJson(jsonProvider.toJsonNode(this.data));
                case AUDIO:
                    List<AudioFrame> lists = AudioParser.parseWaveAudio(this.data, 22050.0f, 1);
                    this.cachedQueryContainer = new AudioQueryContainer(lists);
                    break;
                case MODEL3D:
                    Mesh mesh = MeshParser.parseThreeJSV4Geometry(this.data);
                    this.cachedQueryContainer = new ModelQueryContainer(mesh);
                    break;
                case LOCATION:
                    this.cachedQueryContainer = Optional
                            .ofNullable(jsonProvider.toJsonNode(this.data))
                            .flatMap(GpsData::parseLocationFromJson)
                            .map(LocationQueryContainer::of)
                            .orElse(null);
                case TIME:
                    this.cachedQueryContainer = GpsData.parseInstant(this.data)
                            .map(InstantQueryContainer::of)
                            .orElse(null);
                default:
                    return null;
            }
        }
        return this.cachedQueryContainer;
    }
}
