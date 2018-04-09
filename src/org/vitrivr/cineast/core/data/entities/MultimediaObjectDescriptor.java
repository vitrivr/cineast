package org.vitrivr.cineast.core.data.entities;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.nio.file.Path;
import java.nio.file.Paths;
import org.apache.commons.lang3.StringUtils;
import org.vitrivr.cineast.core.config.Config;
import org.vitrivr.cineast.core.data.ExistenceCheck;
import org.vitrivr.cineast.core.data.MediaType;
import org.vitrivr.cineast.core.db.dao.reader.MultimediaObjectLookup;
import org.vitrivr.cineast.core.idgenerator.ObjectIdGenerator;
import org.vitrivr.cineast.core.run.ExtractionItemContainer;

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

  public static String cleanPath(Path path) {
    return path.toString().replace('\\', '/');
  }

  private static String getFileName(Path path) {
    return cleanPath(path);
  }

  public MultimediaObjectDescriptor(Path path) {
    this(null, path.getFileName().toString().replace('\\', '/'), getFileName(path), null, false);
  }

  /**
   * Completely empty Descriptor
   */
  public MultimediaObjectDescriptor() {
    this("", "", "", MediaType.UNKNOWN, false);
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
   * create a new descriptor based on the provided one.
   *
   * if an id is already given in the descriptor, it takes precedence over generating a new one. if
   * a new path is provided as an argument, it takes precedence over one which might already be
   * existing in the descriptor. if a new type is provided as an argument, it takes precedence over
   * one which might already be existing in the descriptor.
   *
   * The exists variable is taken from the provided descriptor, since that is more current than the
   * one provided in the item
   */
  public static MultimediaObjectDescriptor mergeItem(MultimediaObjectDescriptor descriptor,
      ObjectIdGenerator generator, ExtractionItemContainer item, MediaType type) {
    Path _path = item.getPathForExtraction() == null ? Paths.get(descriptor.getPath())
        : item.getPathForExtraction();
    String _name = StringUtils.isEmpty(item.getObject().getName()) ? descriptor.getName()
        : item.getObject().getName();
    boolean exists = descriptor.exists();
    MediaType _type = type == null ? descriptor.getMediatype() : type;
    String _id =
        StringUtils.isEmpty(item.getObject().getObjectId()) ?
            StringUtils.isEmpty(descriptor.getObjectId())
                ? generator.next(_path, _type) : descriptor.getObjectId()
            : item.getObject().getObjectId();
    String storagePath = StringUtils.isEmpty(item.getObject().getPath()) ? descriptor.getPath()
        : item.getObject().getPath();
    return new MultimediaObjectDescriptor(_id, _name, storagePath, _type, exists);
  }
}