package org.vitrivr.cineast.core.data.query.containers;

import java.awt.image.BufferedImage;
import org.vitrivr.cineast.core.data.m3d.Mesh;
import org.vitrivr.cineast.core.data.raw.CachedDataFactory;
import org.vitrivr.cineast.core.data.raw.images.MultiImage;
import org.vitrivr.cineast.core.util.mesh.MeshTransformUtil;
import org.vitrivr.cineast.core.util.web.ImageParser;
import org.vitrivr.cineast.core.util.web.MeshParser;


public class ModelQueryTermContainer extends AbstractQueryTermContainer {

  /**
   * Original Mesh as transferred by the client.
   */
  private final Mesh mesh;

  /**
   * KHL transformed version of the original Mesh.
   */
  private final Mesh normalizedMesh;

  /**
   * Image containing a 2D sketch of the 3D model in question.
   */
  private final MultiImage image;

  /**
   * Constructs an {@link ModelQueryTermContainer} from base 64 encoded JSON data. The constructor assumes either the JSV4 JSON format for Meshes OR a valid image (for 2D sketch to 3D model lookup).
   *
   * @param data    The 3D model data that should be converted.
   * @param factory The {@link CachedDataFactory} used to create images.
   */
  public ModelQueryTermContainer(String data, CachedDataFactory factory) {
    if (MeshParser.isValidThreeJSV4Geometry(data)) {
      this.mesh = MeshParser.parseThreeJSV4Geometry(data);
      this.normalizedMesh = MeshTransformUtil.khlTransform(mesh, 1.0f);
      this.image = MultiImage.EMPTY_MULTIIMAGE;
    } else if (ImageParser.isValidImage(data)) {
      final BufferedImage img = ImageParser.dataURLtoBufferedImage(data);
      this.image = factory.newMultiImage(img);
      this.mesh = Mesh.EMPTY;
      this.normalizedMesh = Mesh.EMPTY;
    } else {
      throw new IllegalArgumentException("The provided data could not be converted to a Mesh.");
    }
  }

  public ModelQueryTermContainer(String data) {
    this(data, CachedDataFactory.getDefault());
  }

  /**
   * Constructor for {@link ModelQueryTermContainer} with a Mesh. Used for Query-by-Example.
   *
   * @param mesh Mesh for which to create a {@link ModelQueryTermContainer}.
   */
  public ModelQueryTermContainer(Mesh mesh) {
    this.mesh = new Mesh(mesh);
    this.normalizedMesh = MeshTransformUtil.khlTransform(mesh, 1.0f);
    this.image = MultiImage.EMPTY_MULTIIMAGE;
  }

  /**
   * Constructor for {@link ModelQueryTermContainer} with ab image. Used for Query-by-Sketch (2d sketch to 3d model).
   *
   * @param image   BufferedImage for which to create a {@link ModelQueryTermContainer}.
   * @param factory The {@link CachedDataFactory} to create the {@link MultiImage} with.
   */
  public ModelQueryTermContainer(BufferedImage image, CachedDataFactory factory) {
    this.image = factory.newMultiImage(image);
    this.mesh = Mesh.EMPTY;
    this.normalizedMesh = Mesh.EMPTY;
  }

  /**
   * Constructor for {@link ModelQueryTermContainer} constructor with an image (treated as 2D sketch). Used for Query-by-2D-Sketch.
   *
   * @param image Image for which to create a {@link ModelQueryTermContainer}.
   */
  public ModelQueryTermContainer(MultiImage image) {
    this.image = image;
    this.mesh = Mesh.EMPTY;
    this.normalizedMesh = Mesh.EMPTY;
  }

  @Override
  public Mesh getMesh() {
    return this.mesh;
  }

  @Override
  public Mesh getNormalizedMesh() {
    return this.normalizedMesh;
  }

  @Override
  public MultiImage getAvgImg() {
    return this.image;
  }

  @Override
  public MultiImage getMedianImg() {
    return this.image;
  }
}
