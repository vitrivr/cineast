package org.vitrivr.cineast.core.render.lwjgl.scene;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.FloatBuffer;
import java.nio.file.Path;
import javax.imageio.ImageIO;
import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL30;
import org.vitrivr.cineast.core.render.lwjgl.window.WindowOptions;


public class LightfieldCamera {

  private final WindowOptions opts;
  private final BufferedImage lightfieldImage;
  private final FloatBuffer imageData;
  private static String name;
  private static int count;

  private final Path imagePath;

  public LightfieldCamera(WindowOptions opts, String name) {
    this.name = name;
    this.opts = opts;
    this.imagePath = Path.of(
        "C:\\Users\\rapha\\Documents\\myRepo\\ch.unibas\\Class\\vitrivr\\bsc-raphael-waltenspuel\\datasets_git\\TestSet3D\\cineast_out\\lightfieldImages");
    this.lightfieldImage = new BufferedImage(opts.width, opts.height, BufferedImage.TYPE_INT_RGB);

    this.imageData = BufferUtils.createFloatBuffer(opts.width * opts.height * 3);
    GL30.glReadPixels(0, 0, opts.width, opts.height, GL30.GL_RGB, GL30.GL_FLOAT, this.imageData);
    this.imageData.rewind();

  }

  public BufferedImage takeLightfieldImage() {
    this.takePicture().storePicture();
    return this.lightfieldImage;
  }

  private LightfieldCamera takePicture() {

    this.lightfieldImage.setRGB(0, 0, opts.width, opts.height, this.getRgbData(), 0, opts.width);
    return this;

  }

  public LightfieldCamera setName(String name) {
    if (LightfieldCamera.name == null || !LightfieldCamera.name.equals(name)) {
      LightfieldCamera.name = name;
      LightfieldCamera.count = 0;
    }
    return this;
  }

  private LightfieldCamera storePicture() {
    this.count++;
    var outputfile = new File(this.imagePath.toString() + "\\" + this.name + "_" + this.count + ".png");
    try {
      ImageIO.write(this.lightfieldImage, "png", outputfile);
    } catch (IOException e) {
      e.printStackTrace();
    }
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
