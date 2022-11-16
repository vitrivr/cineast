package org.vitrivr.cineast.core.render.lwjgl.model;

import java.util.HashMap;
import java.util.Map;

public class TextureCache {

  public static final String DEFAULT_TEXTURE = "./resources/renderer/lwjgl/models/default/default.png";
  private final Map<String, Texture> textures;

  public TextureCache() {
    this.textures = new HashMap<>();
    this.textures.put(DEFAULT_TEXTURE, new Texture(DEFAULT_TEXTURE));
  }

  public void cleanup() {
    this.textures.values().stream().forEach(Texture::cleanup);
  }

  public Texture createTexture(String texturePath) {
    return this.textures.computeIfAbsent(texturePath, Texture::new);
  }

  public void copyFromTemporaryTextureCache(TextureCache other) {
    if (other == null) {
      return;
    }
    for (var kv : other.textures.entrySet()) {
      if(!this.textures.containsKey(kv.getKey())){
        this.textures.put(kv.getKey(), kv.getValue());
      }
    }
  }

  public Texture getTexture(String texturePath) {
    Texture texture = null;
    if (texturePath != null) {
      texture = this.textures.get(texturePath);

    }
    if (texture == null) {
      texture = this.textures.get(DEFAULT_TEXTURE);
    }
    return texture;
  }
}
