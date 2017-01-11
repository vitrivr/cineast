package org.vitrivr.cineast.core.data.entities;

import org.vitrivr.cineast.core.data.ExistenceCheck;

/**
 * @author rgasser
 * @version 1.0
 * @created 10.01.17
 */
public class MultimediaObjectDescriptor implements ExistenceCheck {
    private final String objectId;
    private final int width, height, framecount, type;
    private final float seconds;
    private final String name, path;
    private final boolean exists;

    public static MultimediaObjectDescriptor makeVideoDescriptor(String objectId, String name, String path, int width, int height, int framecount, float duration) {
      return new MultimediaObjectDescriptor(objectId, name, path, 0, width, height, framecount, duration, true);
    }

    public static MultimediaObjectDescriptor makeImageDescriptor(String objectId, String name, String path, int width, int height) {
      return new MultimediaObjectDescriptor(objectId, name, path, 1, width, height, 1, 0, true);
    }

    public MultimediaObjectDescriptor(String objectId, String name, String path, int type, int width, int height, int framecount, float duration, boolean exists) {
      this.objectId = objectId;
      this.name = name;
      this.path = path;
      this.type = type;
      this.width = width;
      this.height = height;
      this.framecount = framecount;
      this.seconds = duration;
      this.exists = exists;
    }


    /* TODO: @Ralph - Reiview and refactor. Move unnecessary attributes to MultimediaMetadataDescriptor. */
    public MultimediaObjectDescriptor() {
      this("", "", "", 0, 0, 0, 0, 0, false);
    }

    public String getId() {
      return objectId;
    }

    public int getWidth() {
      return width;
    }

    public int getHeight() {
      return height;
    }

    public int getFramecount() {
      return framecount;
    }

    public float getSeconds() {
      return seconds;
    }

    public String getName() {
      return name;
    }

    public String getPath() {
      return path;
    }

    public float getFPS() {
      return this.framecount / this.seconds;
    }

    @Override
    public String toString() {
      return "MultimediaObjectDescriptor(" + objectId + ")";
    }

    @Override
    public boolean exists() {
      return this.exists;
    }
}
