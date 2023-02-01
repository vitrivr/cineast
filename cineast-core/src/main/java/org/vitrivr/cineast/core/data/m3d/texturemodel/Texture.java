package org.vitrivr.cineast.core.data.m3d.texturemodel;

import java.nio.ByteBuffer;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.lwjgl.stb.STBImage;
import org.lwjgl.system.MemoryStack;

public class Texture {
  private static final Logger LOGGER = LogManager.getLogger();

  private final String texturePath;

  public static final String DEFAULT_TEXTURE = "./resources/renderer/lwjgl/models/default/default.png";


  public Texture() {
    this.texturePath = DEFAULT_TEXTURE;
  }

  public Texture(String texturePath) {
    this.texturePath = texturePath;
  }


  public String getTexturePath() {
    return this.texturePath;
  }

  public void close() {
    // Nothing to do here.
    LOGGER.trace("Closing Texture");
  }
}
