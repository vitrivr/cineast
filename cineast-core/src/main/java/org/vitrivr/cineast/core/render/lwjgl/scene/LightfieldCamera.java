package org.vitrivr.cineast.core.render.lwjgl.scene;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.FloatBuffer;
import java.nio.file.Path;
import javax.imageio.ImageIO;
import org.vitrivr.cineast.core.render.lwjgl.window.WindowOptions;
import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL30;



public class LightfieldCamera {

  private final WindowOptions opts;
  private final BufferedImage lightfieldImage;
  private final FloatBuffer imageData;

  private static int count;



  public LightfieldCamera(WindowOptions opts) {

    this.opts = opts;
    this.lightfieldImage = new BufferedImage(opts.width, opts.height, BufferedImage.TYPE_INT_RGB);
    this.imageData = BufferUtils.createFloatBuffer(opts.width * opts.height * 3);
    GL30.glReadPixels(0, 0, opts.width, opts.height, GL30.GL_RGB, GL30.GL_FLOAT, this.imageData);
    this.imageData.rewind();

  }

  public BufferedImage takeLightfieldImage() {
    this.takePicture();
    return this.lightfieldImage;
  }

  private LightfieldCamera takePicture() {
    this.lightfieldImage.setRGB(0, 0, opts.width, opts.height, this.getRgbData(), 0, opts.width);
    return this;
  }


  private int[] getRgbData() {
    int[] rgbArray = new int[this.opts.height * this.opts.width];

    for (int y = 0; y < this.opts.height; ++y) {
      for (int x = 0; x < this.opts.width; ++x) {
        int r = (int) (imageData.get() * 255) << 16;
        int g = (int) (imageData.get() * 255) << 8;
        int b = (int) (imageData.get() * 255);
        int i = ((this.opts.height - 1) - y) * this.opts.width + x;
        rgbArray[i] = r + g + b;
      }
    }
    return rgbArray;
  }
}
