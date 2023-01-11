package org.vitrivr.cineast.core.render.lwjgl.glmodel;

import java.nio.ByteBuffer;
import org.vitrivr.cineast.core.data.m3d.texturemodel.Texture;
import org.lwjgl.opengl.GL30;
import org.lwjgl.stb.STBImage;
import org.lwjgl.system.MemoryStack;

public class GlTexture {

  private int textureId;
  private final Texture texture;


  public GlTexture(Texture texture) {
    this.texture = texture;
    try (var memoryStack = MemoryStack.stackPush()) {
      var w = memoryStack.mallocInt(1);
      var h = memoryStack.mallocInt(1);
      var channels = memoryStack.mallocInt(1);

      var imageBuffer = STBImage.stbi_load(this.texture.getTexturePath(), w, h, channels, 4);
      if (imageBuffer == null) {
        throw new RuntimeException("Could not load texture file: " + this.texture.getTexturePath());
      }
      this.generateTexture(w.get(), h.get(), imageBuffer);
      STBImage.stbi_image_free(imageBuffer);
    }
  }

  public void bind() {
    GL30.glBindTexture(GL30.GL_TEXTURE_2D, this.textureId);
  }

  public void cleanup() {
    GL30.glDeleteTextures(this.textureId);
  }

  private void generateTexture(int width, int height, ByteBuffer texture) {
    this.textureId = GL30.glGenTextures();
    GL30.glBindTexture(GL30.GL_TEXTURE_2D, this.textureId);
    GL30.glPixelStorei(GL30.GL_UNPACK_ALIGNMENT, 1);
    GL30.glTexParameteri(GL30.GL_TEXTURE_2D, GL30.GL_TEXTURE_MIN_FILTER, GL30.GL_NEAREST);
    GL30.glTexParameteri(GL30.GL_TEXTURE_2D, GL30.GL_TEXTURE_MAG_FILTER, GL30.GL_NEAREST);
    GL30.glTexImage2D(GL30.GL_TEXTURE_2D, 0, GL30.GL_RGBA, width, height, 0, GL30.GL_RGBA, GL30.GL_UNSIGNED_BYTE,
        texture);
    GL30.glGenerateMipmap(GL30.GL_TEXTURE_2D);
  }

  public String getTexturePath() {
    return this.texture.getTexturePath();
  }
}

