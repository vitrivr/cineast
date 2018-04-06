package org.vitrivr.cineast.core.data.entities;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.nio.file.Path;
import org.vitrivr.cineast.core.config.Config;
import org.vitrivr.cineast.core.data.ExistenceCheck;
import org.vitrivr.cineast.core.data.MediaType;
import org.vitrivr.cineast.core.db.dao.reader.MultimediaObjectLookup;
import org.vitrivr.cineast.core.idgenerator.ObjectIdGenerator;

/**
 * @author rgasser
 * @created 10.01.17
 */
public class MultimediaObjectDescriptor implements ExistenceCheck {

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
   * Convenience method to create a MultimediaObjectDescriptor marked as new. The method will assign
   * a new ID to this MultimediaObjectDescriptor using the provided ObjectIdGenerator.
   *
   * @param generator ObjectIdGenerator used for ID generation.
   * @param path The Path that points to the file for which a new MultimediaObjectDescriptor should
   * be created.
   * @param type MediaType of the new MultimediaObjectDescriptor
   * @param lookup MultimediaObjectLookup to prevent the assignment of already used ids
   * @return A new MultimediaObjectDescriptor
   */
  public static MultimediaObjectDescriptor newMultimediaObjectDescriptor(
      ObjectIdGenerator generator, Path path, MediaType type, MultimediaObjectLookup lookup) {
    String objectId;
    do {
      objectId = generator.next(path, type);
    } while (lookup != null && lookup.lookUpObjectById(objectId).exists());

    return new MultimediaObjectDescriptor(objectId,
        getFileName(path), path.toString(), type, false);
  }

  private static String getFileName(Path path) {
    return path.getFileName().toString().replace('\\', '/');
  }

  public MultimediaObjectDescriptor(Path path) {
    this(null, null, getFileName(path), null, false);
  }

  public MultimediaObjectDescriptor() {
    this("", "", "", MediaType.VIDEO, false);
  }

  @JsonCreator
  public MultimediaObjectDescriptor(@JsonProperty("objectId") String objectId,
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
    this.contentURL = Config.sharedConfig().getApi().getObjectLocation() + path;
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
    return "MultimediaObjectDescriptor{" +
        "objectId='" + objectId + '\'' +
        ", name='" + name + '\'' +
        ", path='" + path + '\'' +
        ", exists=" + exists +
        ", mediatypeId=" + mediatypeId +
        '}';
  }

  /**
   * create a new descriptor based on the provided one. The given path has precedence over the one
   * in the given descriptor.
   */
  public static MultimediaObjectDescriptor fromExisting(MultimediaObjectDescriptor descriptor,
      ObjectIdGenerator generator, Path path, MediaType type, MultimediaObjectLookup objectReader) {
    throw new RuntimeException();
  }
}
