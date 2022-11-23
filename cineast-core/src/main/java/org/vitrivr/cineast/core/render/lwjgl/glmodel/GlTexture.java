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
    this.generateTexture(this.texture.getWidth(), this.texture.getHeight(), this.texture.getImageBuffer());
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
    STBImage.stbi_image_free(buffer);
  }

  public String getTexturePath() {
    return this.texture.getTexturePath();
  }
}
