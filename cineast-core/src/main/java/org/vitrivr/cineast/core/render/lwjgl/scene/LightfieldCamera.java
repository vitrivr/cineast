package org.vitrivr.cineast.core.render.lwjgl.scene;

import java.awt.image.BufferedImage;
import java.nio.FloatBuffer;
import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL30;
import org.vitrivr.cineast.core.render.lwjgl.window.WindowOptions;


/**
 * The LightfieldCamera class is used to take a picture of the current rendered scene.
 * The picture is stored as a BufferedImage.
 */
public class LightfieldCamera {

  /**
   * The WindowOptions class is used to set the width and height of the resulting image.
   */
  private final WindowOptions opts;
  /**
   * The BufferedImage that is used to store the image data.
   */
  private final BufferedImage lightfieldImage;
  /**
   * The FloatBuffer from openGL that holds the image data.
   */
  private final FloatBuffer imageData;


  /**
   * Initializes the LightfieldCamera with the given WindowOptions.
   * Creates a new BufferedImage with the given width and height.
   * Reads the image data from the current openGL context.
   * @param opts The WindowOptions that are used to set the width and height of the resulting image.
   */
  public LightfieldCamera(WindowOptions opts) {
    this.opts = opts;
    this.lightfieldImage = new BufferedImage(opts.width, opts.height, BufferedImage.TYPE_INT_RGB);
    this.imageData = BufferUtils.createFloatBuffer(opts.width * opts.height * 3);
    GL30.glReadPixels(0, 0, opts.width, opts.height, GL30.GL_RGB, GL30.GL_FLOAT, this.imageData);
    this.imageData.rewind();
  }

  /**
   * Takes a picture of the current rendered scene.
   * Updates the image data of the BufferedImage.
   * Returns the image data as a BufferedImage.
   * @return The RenderedScene as a BufferedImage.
   */
  public BufferedImage takeLightfieldImage() {
    this.takePicture();
    return this.lightfieldImage;
  }

  /**
   * This method start calculating the pixels of the BufferedImage.
   */
  private void takePicture() {
    this.lightfieldImage.setRGB(0, 0, opts.width, opts.height, this.getRgbData(), 0, opts.width);
  }


  /**
   * This method converts the pixels of the BufferedImage.
   * R, G, B values are merged into one int value.
   * E.g.
   * <pre>
   * R = 0xAA -> AA0000, G = 0xBB -> 0x00BB00, B = 0xCC -> 0x0000CC
   * R+ G + B = 0xAABBCC
   * </pre>
   *
   * @return The image data as an int array.
   */
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
