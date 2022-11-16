package org.vitrivr.cineast.core.render.lwjgl.model;

import java.nio.ByteBuffer;
import org.lwjgl.opengl.GL30;
import org.lwjgl.stb.STBImage;
import org.lwjgl.system.MemoryStack;

public class Texture {

  private int textureId;
  private String texturePath;

  public Texture(int width, int height, ByteBuffer buffer) {
    this.texturePath = "";
    this.generateTexture(width, height, buffer);
  }

  public Texture(String texturePath) {
    this.texturePath = texturePath;
    try (var memoryStack = MemoryStack.stackPush()) {
      var w = memoryStack.mallocInt(1);
      var h = memoryStack.mallocInt(1);
      var channels = memoryStack.mallocInt(1);

      var buffer = STBImage.stbi_load(texturePath, w, h, channels, 4);
      if (buffer == null) {
        throw new RuntimeException("Could not load texture file: " + texturePath);
      }

      var width = w.get();
      var height = h.get();

      this.generateTexture(width, height, buffer);
      STBImage.stbi_image_free(buffer);

    }
  }

  public void bind() {
    GL30.glBindTexture(GL30.GL_TEXTURE_2D, this.textureId);
  }

  public void cleanup() {
    GL30.glDeleteTextures(this.textureId);
  }

  private void generateTexture(int width, int height, ByteBuffer buffer) {
    this.textureId = GL30.glGenTextures();
    GL30.glBindTexture(GL30.GL_TEXTURE_2D, this.textureId);
    GL30.glPixelStorei(GL30.GL_UNPACK_ALIGNMENT, 1);
    GL30.glTexParameteri(GL30.GL_TEXTURE_2D, GL30.GL_TEXTURE_MIN_FILTER, GL30.GL_NEAREST);
    GL30.glTexParameteri(GL30.GL_TEXTURE_2D, GL30.GL_TEXTURE_MAG_FILTER, GL30.GL_NEAREST);
    GL30.glTexImage2D(GL30.GL_TEXTURE_2D, 0, GL30.GL_RGBA, width, height, 0, GL30.GL_RGBA, GL30.GL_UNSIGNED_BYTE,
        buffer);
    GL30.glGenerateMipmap(GL30.GL_TEXTURE_2D);
  }

  public String getTexturePath() {
    return this.texturePath;
  }
}
