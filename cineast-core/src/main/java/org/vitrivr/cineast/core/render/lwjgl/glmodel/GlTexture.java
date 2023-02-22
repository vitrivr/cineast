package org.vitrivr.cineast.core.render.lwjgl.glmodel;

import java.nio.ByteBuffer;
import org.vitrivr.cineast.core.data.m3d.texturemodel.Mesh;
import org.vitrivr.cineast.core.data.m3d.texturemodel.Texture;
import org.lwjgl.opengl.GL30;
import org.lwjgl.stb.STBImage;
import org.lwjgl.system.MemoryStack;

/**
 * The GlTexture class is a wrapper for the {@link Texture} class.
 * <ul>
 * <li>Texture -> GlTexture( Texture )</li>
 * </ul>
 * <p>
 * The purpose is to bring the generic Mesh in an OpenGl context
 * {@link Texture} -> {@link GlTexture}
 */
public class GlTexture {

  /**
   * The id of the texture used to bind the texture to the Gl context
   */
  private int textureId;
  /**
   * The wrapped generic texture in gl context
   */
  private final Texture texture;

  /**
   * Creates a new GlTexture from a texture.
   * <ol>
   *   <li>Load the texture from the file</li>
   *   <li>Alocate the texture buffer</li>
   *   <li>Load the texture into the buffer</li>
   * </ol>
   * @param texture The texture that is wrapped by this gl texture.
   */
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

  /**
   * Binds the GlTexture to the Gl context
   */
  public void bind() {
    GL30.glBindTexture(GL30.GL_TEXTURE_2D, this.textureId);
  }

  /**
   * Cleans the GlTexture
   * Does not affect the underlying texture
   * Removes the texture from the GPU
   */
  public void cleanup() {
    GL30.glDeleteTextures(this.textureId);
  }

  /**
   * Generates the texture in the Gl context
   * @param width The width of the texture
   * @param height The height of the texture
   * @param texture The texture buffer
   */
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

  /**
   * Returns the texture path of the underlying wrapped texture
   * @return The texture path of the underlying wrapped texture
   */
  public String getTexturePath() {
    return this.texture.getTexturePath();
  }
}

