package org.vitrivr.cineast.core.render.lwjgl.glmodel;

import java.util.HashMap;
import java.util.Map;
import org.vitrivr.cineast.core.data.m3d.texturemodel.Texture;

/**
 * A cache for textures
 * Prevents the same texture from being loaded multiple times
 */
public class GlTextureCache {

  /**
   * The cache of textures
   */
  private final Map<String, GlTexture> textures;

  /**
   * Creates a new texture cache
   * Adds a default texture to the cache
   */
  public GlTextureCache() {
    this.textures = new HashMap<>();
    var texture = new Texture();
    this.textures.put(texture.getTexturePath(), new GlTexture(texture));
    this.textures.put("default", new GlTexture(texture));
  }

  /**
   * Cleans up the texture cache
   * Removes only the references to wrapped cached
   * TODO: Check probably optimize by removing the mapped values
   */
  public void cleanup() {
    this.textures.values().forEach(GlTexture::cleanup);
  }

  /**
   * Adds a texture to the cache if it is not already present
   *
   * @param texture Texture to add
   */
  public void addTextureIfAbsent(GlTexture texture) {
    this.textures.putIfAbsent(texture.getTexturePath(), texture);
  }

  /**
   * Returns the gl texture with the given texture path
   *
   * @param texturePath Path of the texture
   * @return The texture with the given texture path
   */
  public GlTexture getTexture(String texturePath) {
    return this.textures.get(texturePath);
  }
}
