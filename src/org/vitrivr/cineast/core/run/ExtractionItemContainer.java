package org.vitrivr.cineast.core.run;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import org.vitrivr.cineast.core.data.MediaType;
import org.vitrivr.cineast.core.data.entities.MultimediaMetadataDescriptor;
import org.vitrivr.cineast.core.data.entities.MultimediaObjectDescriptor;

/**
 * An {@link ExtractionItemContainer} contains all information for ONE item which is supposed to be
 * extracted. A container MUST contain a {@link #path} linking to the item to be extracted. The
 * corresponding {@link #object} MUST contain the {@link MultimediaObjectDescriptor#getMediatype()}
 * so an item can be handed out to different extractionhandlers.
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

  @Override
  public String toString() {
    return "ExtractionItemContainer{" +
        "object=" + object +
        ", metadata=" + Arrays.toString(metadata) +
        ", path=" + path +
        '}';
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
    this.object = object;
    this.metadata = metadata == null ? new MultimediaMetadataDescriptor[0] : metadata;
    this.path = path;
    if (this.path == null) {
      throw new RuntimeException("Path was null for object " + object);
    }
  }
}
