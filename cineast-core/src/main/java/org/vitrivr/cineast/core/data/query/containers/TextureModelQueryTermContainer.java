package org.vitrivr.cineast.core.data.query.containers;

import java.awt.image.BufferedImage;
import java.util.Objects;
import org.vitrivr.cineast.core.data.raw.CachedDataFactory;
import org.vitrivr.cineast.core.data.raw.images.MultiImage;
import org.vitrivr.cineast.core.data.m3d.texturemodel.Model;
import org.vitrivr.cineast.core.util.web.ImageParser;
import org.vitrivr.cineast.core.util.web.MeshParser;
import org.vitrivr.cineast.core.util.web.ModelParser;


public class TextureModelQueryTermContainer extends AbstractQueryTermContainer {

  /**
   * Original Mesh as transferred by the client.
   */
  private final Model model;

  /**
   * Image containing a 2D sketch of the 3D model in question.
   */
  private final MultiImage image;

  /**
   * Constructs an {@link TextureModelQueryTermContainer} from base 64 encoded JSON data. The constructor assumes either the JSV4 JSON format for Meshes OR a valid image (for 2D sketch to 3D model lookup).
   *
   * @param data    The 3D model data that should be converted.
   * @param factory The {@link CachedDataFactory} used to create images.
   */
  public TextureModelQueryTermContainer(String data, CachedDataFactory factory) {
    if (MeshParser.isValidThreeJSV4Geometry(data)) {
      this.model = ModelParser.parseThreeJSV4Geometry(data);
      this.image = MultiImage.EMPTY_MULTIIMAGE;
    } else if (ImageParser.isValidImage(data)) {
      final BufferedImage img = ImageParser.dataURLtoBufferedImage(data);
      this.image = factory.newMultiImage(img);
      this.model = Model.EMPTY;
    } else {
      throw new IllegalArgumentException("The provided data could not be converted to a Mesh.");
    }
  }

  public TextureModelQueryTermContainer(String data) {
    this(data, CachedDataFactory.getDefault());
  }

  /**
   * Constructor for {@link TextureModelQueryTermContainer} with a Mesh. Used for Query-by-Example.
   *
   * @param model Mesh for which to create a {@link TextureModelQueryTermContainer}.
   */
  public TextureModelQueryTermContainer(Model model) {
    this.model = model;
    this.image = MultiImage.EMPTY_MULTIIMAGE;
  }

  /**
   * Constructor for {@link TextureModelQueryTermContainer} with ab image. Used for Query-by-Sketch (2d sketch to 3d model).
   *
   * @param image   BufferedImage for which to create a {@link TextureModelQueryTermContainer}.
   * @param factory The {@link CachedDataFactory} to create the {@link MultiImage} with.
   */
  public TextureModelQueryTermContainer(BufferedImage image, CachedDataFactory factory) {
    this.image = factory.newMultiImage(image);
    this.model = Model.EMPTY;
  }

  /**
   * Constructor for {@link TextureModelQueryTermContainer} constructor with an image (treated as 2D sketch). Used for Query-by-2D-Sketch.
   *
   * @param image Image for which to create a {@link TextureModelQueryTermContainer}.
   */
  public TextureModelQueryTermContainer(MultiImage image) {
    this.image = image;
    this.model = Model.EMPTY;
  }

  @Override
  public Model getModel() {
    return this.model;
  }


  @Override
  public MultiImage getAvgImg() {
    return this.image;
  }

  @Override
  public MultiImage getMedianImg() {
    return this.image;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    if (!super.equals(o)) {
      return false;
    }
    TextureModelQueryTermContainer that = (TextureModelQueryTermContainer) o;
    return Objects.equals(model, that.model) && Objects.equals(image, that.image);
  }

  @Override
  public int hashCode() {
    return Objects.hash(super.hashCode(), this.model,this.image);
  }
}
