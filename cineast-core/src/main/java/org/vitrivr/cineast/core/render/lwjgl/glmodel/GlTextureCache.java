package org.vitrivr.cineast.core.render.lwjgl.glmodel;

import java.util.HashMap;
import java.util.Map;
import org.vitrivr.cineast.core.data.m3d.texturemodel.Texture;

public class GlTextureCache {
  public static final String DEFAULT_TEXTURE = "./src/main/resources/models/cube/cube.png";
  private final Map<String, GlTexture> textures;

  public GlTextureCache() {
    this.textures = new HashMap<>();
    this.textures.put(DEFAULT_TEXTURE, new GlTexture(new Texture(DEFAULT_TEXTURE)));
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
