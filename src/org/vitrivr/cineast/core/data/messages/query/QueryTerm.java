package org.vitrivr.cineast.core.data.messages.query;

import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import org.vitrivr.cineast.core.data.GpsData;
import org.vitrivr.cineast.core.data.MultiImageFactory;
import org.vitrivr.cineast.core.data.frames.AudioFrame;
import org.vitrivr.cineast.core.data.m3d.Mesh;
import org.vitrivr.cineast.core.data.query.containers.AudioQueryContainer;
import org.vitrivr.cineast.core.data.query.containers.ImageQueryContainer;
import org.vitrivr.cineast.core.data.query.containers.InstantQueryContainer;
import org.vitrivr.cineast.core.data.query.containers.LocationQueryContainer;
import org.vitrivr.cineast.core.data.query.containers.ModelQueryContainer;
import org.vitrivr.cineast.core.data.query.containers.MotionQueryContainer;
import org.vitrivr.cineast.core.data.query.containers.QueryContainer;
import org.vitrivr.cineast.core.util.json.JacksonJsonProvider;
import org.vitrivr.cineast.core.util.web.AudioParser;
import org.vitrivr.cineast.core.util.web.DataURLParser;
import org.vitrivr.cineast.core.util.web.ImageParser;
import org.vitrivr.cineast.core.util.web.MeshParser;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * @author rgasser
 * @version 1.0
 * @created 11.01.17
 */
public class QueryTerm {
  /**
   * List of categories defined as part of the query-term. This ultimately selects the
   * feature-vectors used for retrieval.
   */
  private final String[] categories;

  /** Denotes the type of QueryTerm. */
  private final QueryTermType type;

  /** Base64 encoded representation of the query-object associated with this query term. */
  private final String data;

  /** Cached version of the QueryContainer representation of this QueryTerm. */
  private QueryContainer cachedQueryContainer;
  
  private static final JacksonJsonProvider jsonProvider = new JacksonJsonProvider();

  /**
   *
   * @param categories
   */
  @JsonCreator
  public QueryTerm(@JsonProperty("type") QueryTermType type, @JsonProperty("data") String data,
      @JsonProperty("categories") String[] categories) {
    this.type = type;
    this.categories = categories;
    this.data = data;
  }

  /**
   * Getter for feature categories.
   * 
   * @return
   */
  public List<String> getCategories() {
    if (this.categories != null) {
      return Arrays.asList(this.categories);
    } else {
      return new ArrayList<>(0);
    }
  }

  /**
   * Getter for type.
   *
   * @return
   */
  public QueryTermType getType() {
    return type;
  }

  /**
   * Converts the QueryTerm to a QueryContainer that can be processed by the retrieval pipeline.
   * This includes conversion of query-objects from the Base64 encoded representation.
   *
   * IMPORTANT: Subsequent calls to this method will return a cached version of the original
   * QueryContainer.
   *
   * @return QueryContainer representation of the QueryTerm.
   */
  public QueryContainer toContainer() {
    if (this.cachedQueryContainer == null) {
      if (this.data == null)
        return null;
      switch (this.type) {
      case IMAGE:
        BufferedImage image = ImageParser.dataURLtoBufferedImage(this.data);
        if (image != null)
          this.cachedQueryContainer = new ImageQueryContainer(
              MultiImageFactory.newInMemoryMultiImage(image));
        break;
      case AUDIO:
        List<AudioFrame> lists = AudioParser.parseWaveAudio(this.data, 22050.0f, 1);
        if (lists != null)
          this.cachedQueryContainer = new AudioQueryContainer(lists);
        break;
      case MODEL3D:
        if (MeshParser.isValidThreeJSV4Geometry(this.data)) {
          Mesh mesh = MeshParser.parseThreeJSV4Geometry(this.data);
          if (mesh != null)
            this.cachedQueryContainer = new ModelQueryContainer(mesh);
        } else if (ImageParser.isValidImage(this.data)) {
          BufferedImage img = ImageParser.dataURLtoBufferedImage(this.data);
          if (img != null)
            this.cachedQueryContainer = new ImageQueryContainer(
                MultiImageFactory.newInMemoryMultiImage(img));
        }
        break;
      case MOTION:
        this.cachedQueryContainer = DataURLParser.dataURLtoJsonNode(this.data)
            .map(MotionQueryContainer::fromJson).orElse(null);
        break;
      case LOCATION:
        return Optional.ofNullable(jsonProvider.toJsonNode(this.data))
            .flatMap(GpsData::parseLocationFromJson).map(LocationQueryContainer::of).orElse(null);
      case TIME:
        return GpsData.parseInstant(this.data).map(InstantQueryContainer::of).orElse(null);
      default:
        break;
      }
    }
    return this.cachedQueryContainer;
  }
}
