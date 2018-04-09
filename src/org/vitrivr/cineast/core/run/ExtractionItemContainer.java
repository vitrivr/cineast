package org.vitrivr.cineast.core.run;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Path;
import java.nio.file.Paths;
import org.vitrivr.cineast.core.data.entities.MultimediaMetadataDescriptor;
import org.vitrivr.cineast.core.data.entities.MultimediaObjectDescriptor;

/**
 * An {@link ExtractionItemContainer} contains all information for ONE item which is supposed to be
 * extracted. All information may be lacking.
 *
 * @author silvan on 06.04.18.
 */
public class ExtractionItemContainer {

  private MultimediaObjectDescriptor object;

  private MultimediaMetadataDescriptor[] metadata;

  @JsonIgnore
  private Path path;

  /**
   * If a path has been specified directly, returns the path. If no path has been specified, tries
   * to construct a path from the path of the underlying {@link MultimediaObjectDescriptor}
   */
  public Path getPathForExtraction() {
    if (path == null) {
      return Paths.get(object.getPath());
    }
    return path;
  }

  public void setObject(MultimediaObjectDescriptor object) {
    this.object = object;
  }

  public void setMetadata(
      MultimediaMetadataDescriptor[] metadata) {
    this.metadata = metadata;
  }

  public MultimediaObjectDescriptor getObject() {
    return object;
  }

  public MultimediaMetadataDescriptor[] getMetadata() {
    return metadata;
  }

  /**
   * Json-Creator, only takes strings or json-compatible objects.
   *
   * @param uri used to create a path
   */
  @JsonCreator
  public ExtractionItemContainer(@JsonProperty("object") MultimediaObjectDescriptor object,
      @JsonProperty("metadata") MultimediaMetadataDescriptor[] metadata,
      @JsonProperty("uri") String uri)
      throws URISyntaxException {
    this(object, metadata, Paths.get(new URI(uri)));
  }

  public ExtractionItemContainer(
      MultimediaObjectDescriptor object,
      MultimediaMetadataDescriptor[] metadata, Path path) {
    this.object = object == null ? new MultimediaObjectDescriptor() : object;
    this.metadata = metadata == null ? new MultimediaMetadataDescriptor[0] : metadata;
    this.path = path;
  }
}
