package org.vitrivr.cineast.core.data.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.vitrivr.cineast.core.data.ExistenceCheck;
import org.vitrivr.cineast.core.data.MediaType;

/**
 * @author rgasser
 * @version 1.0
 * @created 10.01.17
 */

/* TODO: Review with Luca. */
public class MultimediaObjectDescriptor implements ExistenceCheck {
    private final String objectId;
    private final String name, path;
    private final boolean exists;
    private final int mediatypeId;

    @Deprecated
    public static MultimediaObjectDescriptor makeVideoDescriptor(String objectId, String name, String path, int width, int height, int framecount, float duration) {
      return new MultimediaObjectDescriptor(objectId, name, path, MediaType.VIDEO, true);
    }

    @Deprecated
    public static MultimediaObjectDescriptor makeImageDescriptor(String objectId, String name, String path, int width, int height) {
      return new MultimediaObjectDescriptor(objectId, name, path, MediaType.IMAGE, true);
    }

    public static MultimediaObjectDescriptor makeMultimediaDescriptor(String objectId, String name, String path, MediaType type) {
        return new MultimediaObjectDescriptor(objectId, name, path, type, true);
    }

    /**
     * Default constructor for an empty MultimediaObjectDescriptor.
     */
    public MultimediaObjectDescriptor() {
        this("", "", "", MediaType.VIDEO, false);
    }

    /**
     * Default constructor for an empty MultimediaObjectDescriptor.
     */
    public MultimediaObjectDescriptor(String objectId, String name, String path, MediaType mediatypeId, boolean exists) {
      this.objectId = objectId;
      this.name = name;
      this.path = path;
      this.mediatypeId = mediatypeId.getId();
      this.exists = exists;
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
      return "MultimediaObjectDescriptor(" + objectId + ")";
    }
}
