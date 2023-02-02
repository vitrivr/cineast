package org.vitrivr.cineast.core.data.m3d.texturemodel;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.joml.Vector3f;
import org.joml.Vector4f;
import org.vitrivr.cineast.core.data.m3d.texturemodel.util.MinimalBoundingBox;

public class Material {
  private static final Logger LOGGER = LogManager.getLogger();
  private final List<Mesh> meshes;
  private Texture texture;
  public static final Vector4f DEFAULT_COLOR = new Vector4f(0.0f, 0.0f, 0.0f, 1.0f);
  private Vector4f diffuseColor;

  public static final Material EMPTY = new Material();

  public Material() {
    this.meshes = new ArrayList<>();
    this.texture = new Texture();
    this.diffuseColor = DEFAULT_COLOR;
  }

  /**
   * @return A MinimalBoundingBox which enclose all MinimalBoundingBoxes
   * from containing meshes
   */
  public MinimalBoundingBox getMinimalBoundingBox(){
    var mmb = new MinimalBoundingBox();
    for (var mesh : this.meshes) {
      mmb.merge(mesh.getMinimalBoundingBox());
    }
    return mmb;
  }

  /**
   * @return the scaling factor to norm 1 size from all containing meshes merged
   * @deprecated use {@link #getMinimalBoundingBox()} instead
   */
  @Deprecated
  public float getMaxNormalizedScalingFactor() {
    var min = Float.MAX_VALUE;
    for (var mesh : this.meshes) {
      min = Math.min(min, mesh.getNormalizedScalingFactor());
    }
    return min;
  }


  /**
   * @return the translation to origin (0,0,0) from all containing meshes merged
   * @deprecated use {@link #getMinimalBoundingBox()} instead
   */
  @Deprecated
  public Vector3f getMaxNormalizedPosition() {
    var min = new Vector3f(0, 0, 0);
    for (var mesh : this.meshes) {
      min = min.length() > mesh.getNormalizedPosition().length() ? min : mesh.getNormalizedPosition();
    }
    return min;
  }

  public List<Mesh> getMeshes() {
    return Collections.unmodifiableList(this.meshes);
  }

  public void addMesh(Mesh mesh) {
    this.meshes.add(mesh);
  }

  public Texture getTexture() {
    return this.texture;
  }

  public void setTexture(Texture texture) {
    this.texture = texture;
  }

  public Vector4f getDiffuseColor() {
    return this.diffuseColor;
  }

  public void setDiffuseColor(Vector4f diffuseColor) {
    this.diffuseColor = diffuseColor;
  }

  public void close() {
    this.meshes.stream().forEach(Mesh::close);
    this.meshes.clear();
    this.texture.close();
    this.texture = null;
    this.diffuseColor = null;
    LOGGER.trace("Closed Material");
  }
}
