package org.vitrivr.cineast.core.data.m3d.texturemodel;

import java.nio.ByteBuffer;
import org.lwjgl.stb.STBImage;
import org.lwjgl.system.MemoryStack;

public class Texture {

  private final String texturePath;
  public ByteBuffer imageBuffer;

  public static final String DEFAULT_TEXTURE = "./resources/renderer/lwjgl/models/default/default.png";
  private  int width;
  private  int height;

  public Texture() {
    var defaultText = ModelLoader.loadTexture(DEFAULT_TEXTURE);
    this.imageBuffer = defaultText.imageBuffer;
    this.width = defaultText.getWidth();
    this.height = defaultText.height;
    this.texturePath =defaultText.getTexturePath();
  }

  public Texture(String texturePath, ByteBuffer imageBuffer, int w, int h) {
    this.texturePath = texturePath;
    this.imageBuffer = imageBuffer;
    this.width = w;
    this.height = h;
  }


  public String getTexturePath() {
    return this.texturePath;
  }

  public int getWidth() {
    return this.height;
  }

  public int getHeight(){
    return this.height;
  }

  public ByteBuffer getImageBuffer(){
    return this.imageBuffer;
  }
}
