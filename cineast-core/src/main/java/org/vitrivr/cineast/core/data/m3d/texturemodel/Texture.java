package org.vitrivr.cineast.core.data.m3d.texturemodel;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * This class represents a texture.
 * In the context free 3D model, a texture is basically a path to a texture file.
 */
public class Texture {
  private static final Logger LOGGER = LogManager.getLogger();

  /**
   * Path to the texture file.
   */
  private final String texturePath;

  /**
   * Default texture path.
   * Points to a png with one white pixel with 100% opacity.
   */
  public static final String DEFAULT_TEXTURE = "./resources/renderer/lwjgl/models/default/default.png";


  /**
   * Constructor for the Texture class.
   * Sets the texture path to the default texture path.
   */
  public Texture() {
    this.texturePath = DEFAULT_TEXTURE;
  }

  /**
   * Constructor for the Texture class.
   * Sets the texture path to the given texture path.
   *
   * @param texturePath Path to the texture file.
   */
  public Texture(String texturePath) {
    this.texturePath = texturePath;
  }


  /**
   * @return Path to the texture file.
   */
  public String getTexturePath() {
    return this.texturePath;
  }

  /**
   * Releases all resources associated with this Texture.
   */
  public void close() {
    // Nothing to do here.
    LOGGER.trace("Closing Texture");
  }
}
