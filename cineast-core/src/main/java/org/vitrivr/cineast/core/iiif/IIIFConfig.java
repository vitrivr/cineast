package org.vitrivr.cineast.core.iiif;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.vitrivr.cineast.core.iiif.imageapi.ImageApiVersion;

/**
 * IIIF configuration used to fetch media files from remote servers.
 *
 * @author singaltanmay
 * @version 1.0
 * @created 28.05.21
 */
@ToString
public class IIIFConfig {

  @Setter
  @JsonProperty
  private String imageApiVersion = "2.1.1";

  @Getter
  @Setter
  @JsonProperty(value = "url", required = true)
  private String baseUrl;

  @Getter
  @Setter
  @JsonProperty
  private String region;

  @Getter
  @Setter
  @JsonProperty
  private String size;

  @Getter
  @Setter
  @JsonProperty
  private Float rotation;

  @Getter
  @Setter
  @JsonProperty
  private String quality;

  @Getter
  @Setter
  @JsonProperty
  private String format;

  @Getter
  @Setter
  @JsonProperty(value = "items")
  private List<IIIFItem> iiifItems;

  public ImageApiVersion getImageApiVersion() {
    return ImageApiVersion.fromNumericString(imageApiVersion);
  }

  @ToString
  @NoArgsConstructor
  public static class IIIFItem {

    @Getter
    @Setter
    @JsonProperty
    private String identifier;

    @Getter
    @Setter
    @JsonProperty
    private String region;

    @Getter
    @Setter
    @JsonProperty
    private String size;

    @Getter
    @Setter
    @JsonProperty
    private Float rotation;

    @Getter
    @Setter
    @JsonProperty
    private String quality;

    @Getter
    @Setter
    @JsonProperty
    private String format;
  }

}
