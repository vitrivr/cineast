package org.vitrivr.cineast.standalone.run;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import org.vitrivr.cineast.core.data.entities.MediaObjectDescriptor;
import org.vitrivr.cineast.core.data.entities.MediaObjectMetadataDescriptor;

/**
 * An {@link ExtractionItemContainer} contains all information for ONE item which is supposed to be
 * extracted. A container MUST contain a {@link #path} linking to the item to be extracted. The
 * corresponding {@link #object} MUST contain the {@link MediaObjectDescriptor#getMediatype()} so an
 * item can be handed out to different extractionhandlers.
 *
 */
public class ExtractionItemContainer {

  private MediaObjectDescriptor object;

  private List<MediaObjectMetadataDescriptor> metadata;

  private String uri;
  @JsonIgnore
  private Path path;

  /**
   * Json-Creator, only takes strings or json-compatible objects.
   *
   * @param uri used to create a path
   */
  @JsonCreator
  public ExtractionItemContainer(@JsonProperty("object") MediaObjectDescriptor object,
      @JsonProperty("metadata") List<MediaObjectMetadataDescriptor> metadata,
      @JsonProperty("uri") String uri)
      throws URISyntaxException {
    this(object, metadata, Paths.get(new URI(uri)));
  }

  @JsonIgnore
  public ExtractionItemContainer(
      MediaObjectDescriptor object,
      List<MediaObjectMetadataDescriptor> metadata, Path path) {
    this.object = object;
    this.metadata = metadata == null ? new ArrayList<>() : metadata;
    this.path = path;
    if (this.path == null) {
      throw new RuntimeException("Path was null for object " + object);
    }
  }

  /**
   * To make fasterxml/jackson happy.
   *
   * @return
   */
  public String getUri() {
    return uri;
  }

  /**
   * If a path has been specified directly, returns the path. If no path has been specified, tries
   * to construct a path from the path of the underlying {@link MediaObjectDescriptor}
   */
  @JsonIgnore
  public Path getPathForExtraction() {
    if (path == null) {
      return Paths.get(object.getPath());
    }
    return path;
  }

  public MediaObjectDescriptor getObject() {
    return object;
  }

  public void setObject(MediaObjectDescriptor object) {
    this.object = object;
  }

  public List<MediaObjectMetadataDescriptor> getMetadata() {
    return metadata;
  }

  public void setMetadata(
      List<MediaObjectMetadataDescriptor> metadata) {
    this.metadata = metadata;
  }

  @Override
  public String toString() {
    return "ExtractionItemContainer{" +
        "object=" + object +
        ", metadata=" + metadata +
        ", path=" + path +
        '}';
  }
}
