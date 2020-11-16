package org.vitrivr.cineast.core.data.entities;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.nio.file.Path;
import org.apache.commons.lang3.builder.ReflectionToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.vitrivr.cineast.core.data.ExistenceCheck;
import org.vitrivr.cineast.core.data.MediaType;
import org.vitrivr.cineast.core.db.dao.reader.MediaObjectReader;
import org.vitrivr.cineast.core.db.setup.EntityDefinition;
import org.vitrivr.cineast.core.extraction.idgenerator.ObjectIdGenerator;

/**
 * @author rgasser
 * @created 10.01.17
 */
public class MediaObjectDescriptor implements ExistenceCheck {

  /**
   * Name of the entity in the persistence layer.
   */
  public static final String ENTITY = "cineast_multimediaobject";

  /**
   * Field names in the persistence layer.
   */
  public static final String[] FIELDNAMES = {"objectid", "mediatype", "name", "path"};


  private final String objectId;
  private final String name, path;
  private final boolean exists;
  private final int mediatypeId;
  private final String contentURL;

  /**
   * Convenience method to create a MediaObjectDescriptor marked as new. The method will assign a new ID to this MediaObjectDescriptor using the provided ObjectIdGenerator.
   *
   * @param generator ObjectIdGenerator used for ID generation.
   * @param path The Path that points to the file for which a new MediaObjectDescriptor should be created.
   * @param type MediaType of the new MediaObjectDescriptor
   * @param lookup MediaObjectReader to prevent the assignment of already used ids
   * @return A new MediaObjectDescriptor
   */
  public static MediaObjectDescriptor newMultimediaObjectDescriptor(
      ObjectIdGenerator generator, Path path, MediaType type, MediaObjectReader lookup) {
    String objectId;
    do {
      objectId = generator.next(path, type);
    } while (lookup != null && lookup.lookUpObjectById(objectId).exists());

    return new MediaObjectDescriptor(objectId,
        getFileName(path), path.toString(), type, false);
  }

  public static String cleanPath(Path path) {
    return path.toString().replace('\\', '/');
  }

  private static String getFileName(Path path) {
    return cleanPath(path);
  }

  public MediaObjectDescriptor(Path path) {
    this(null, path.getFileName().toString().replace('\\', '/'), getFileName(path), null, false);
  }

  /**
   * Completely empty Descriptor
   */
  public MediaObjectDescriptor() {
    this("", "", "", MediaType.UNKNOWN, false);
  }

  @JsonCreator
  public MediaObjectDescriptor(@JsonProperty("objectId") String objectId,
      @JsonProperty("name") String name, @JsonProperty("path") String path,
      @JsonProperty(value = "mediatype", defaultValue = "UNKNOWN") MediaType mediatypeId,
      @JsonProperty(value = "exists", defaultValue = "false") boolean exists) {
    this.objectId = objectId;
    this.name = name;
    if (path == null) {
      this.path = null;
    } else {
      this.path = path.replace('\\', '/');
    }
    if (mediatypeId == null) {
      this.mediatypeId = MediaType.UNKNOWN.getId();
    } else {
      this.mediatypeId = mediatypeId.getId();
    }
    this.exists = exists;
    this.contentURL = ""; //FIXME this probably need some kind of resolver to be constructed properly
    // Config.sharedConfig().getApi().getObjectLocation() + path;
  }

  @JsonProperty
  public final String getObjectId() {
    return objectId;
  }

  @JsonProperty
  public final String getName() {
    return name;
  }

  @JsonProperty
  public final String getPath() {
    return path;
  }

  @JsonProperty
  public final MediaType getMediatype() {
    return MediaType.fromId(this.mediatypeId);
  }

  @JsonProperty
  public String getContentURL() {
    return contentURL;
  }

  @JsonIgnore
  public final int getMediatypeId() {
    return this.mediatypeId;
  }

  @Override
  @JsonIgnore
  public boolean exists() {
    return this.exists;
  }

  @Override
  public String toString() {
    return ReflectionToStringBuilder.toString(this, ToStringStyle.JSON_STYLE);
  }

}
