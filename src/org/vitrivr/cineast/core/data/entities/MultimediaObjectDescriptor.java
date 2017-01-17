package org.vitrivr.cineast.core.data.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.vitrivr.cineast.core.data.ExistenceCheck;
import org.vitrivr.cineast.core.data.MediaType;

import java.io.File;
import java.nio.file.Path;
import java.util.UUID;

/**
 * @author rgasser
 * @version 1.0
 * @created 10.01.17
 */

/*
 * TODO #1: Review with Luca.
 * TODO #2: Define whether assigning a UUID is a good approach. Alternative: Hash the path.
 */
public class MultimediaObjectDescriptor implements ExistenceCheck {


    /** Name of the entity in the persistence layer. */
    public static final String ENTITY = "cineast_multimediaobject";

    /** Field names in the persistence layer. */
    public static final String[] FIELDNAMES = {"id", "mediatype", "name", "path", "preview", "segments"};

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
     * Convenience method to create a MultimediaObjectDescriptor marked as new. The method will assign
     * a new ID to this MultimediaObjectDescriptor.
     *
     * @param path The Path that points to the file for which a new MultimediaObjectDescriptor should be created.
     * @param type
     * @return
     */
    public static MultimediaObjectDescriptor newMultimediaObjectDescriptor(Path path, MediaType type) {
        String objectId = UUID.randomUUID().toString();
        return new MultimediaObjectDescriptor(objectId, path.getFileName().toString(), path.toString(), type, false);
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
