package org.vitrivr.cineast.core.render.lwjgl.glmodel;

import java.util.HashMap;
import java.util.Map;
import org.vitrivr.cineast.core.data.m3d.texturemodel.Texture;

public class GlTextureCache {

  private final Map<String, GlTexture> textures;

  public GlTextureCache() {
    this.textures = new HashMap<>();
    var texture = new Texture();
    this.textures.put(texture.getTexturePath(), new GlTexture(texture));
    this.textures.put("default", new GlTexture(texture));
  }

  public void cleanup() {
    this.textures.values().stream().forEach(GlTexture::cleanup);
  }


  public void addTextureIfAbsent(GlTexture texture) {
    this.textures.putIfAbsent(texture.getTexturePath(), texture);
  }

  public GlTexture getTexture(String texturePath) {
    return this.textures.get(texturePath);
  }
}
