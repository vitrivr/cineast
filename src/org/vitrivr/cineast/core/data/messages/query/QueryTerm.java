package org.vitrivr.cineast.core.data.messages.query;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.awt.image.BufferedImage;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import javax.annotation.Nullable;
import org.vitrivr.cineast.api.WebUtils;
import org.vitrivr.cineast.core.data.GpsData;
import org.vitrivr.cineast.core.data.MultiImageFactory;
import org.vitrivr.cineast.core.data.frames.AudioFrame;
import org.vitrivr.cineast.core.data.query.containers.AudioQueryContainer;
import org.vitrivr.cineast.core.data.query.containers.ImageQueryContainer;
import org.vitrivr.cineast.core.data.query.containers.InstantQueryContainer;
import org.vitrivr.cineast.core.data.query.containers.LocationQueryContainer;
import org.vitrivr.cineast.core.data.query.containers.MotionQueryContainer;
import org.vitrivr.cineast.core.data.query.containers.QueryContainer;
import org.vitrivr.cineast.core.util.json.JacksonJsonProvider;

/**
 * @author rgasser
 * @version 1.0
 * @created 11.01.17
 */
public class QueryTerm {
  private static final JacksonJsonProvider jsonProvider = new JacksonJsonProvider();

  /**
   * List of categories defined as part of the query-term. This ultimately selects the
   * feature-vectors used for retrieval.
   */
  private final String[] categories;

  private final QueryTermType type;

  private final String data;

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
    switch (this.type) {
      case IMAGE:
        // FIXME: image can be null
        BufferedImage image = WebUtils.dataURLtoBufferedImage(this.data);
        return new ImageQueryContainer(MultiImageFactory.newInMemoryMultiImage(image));
      case MOTION:
        return MotionQueryContainer.fromJson(jsonProvider.toJsonNode(this.data));
      case AUDIO:
        List<AudioFrame> lists = WebUtils.dataURLtoAudioFrames(this.data);
        return new AudioQueryContainer(lists);
      case LOCATION:
        return Optional
            .ofNullable(jsonProvider.toJsonNode(this.data))
            .flatMap(GpsData::parseLocationFromJson)
            .map(LocationQueryContainer::of)
            .orElse(null);
      case TIME:
        return GpsData.parseInstant(this.data)
            .map(InstantQueryContainer::of)
            .orElse(null);
      default:
        return null;
    }
  }
}
