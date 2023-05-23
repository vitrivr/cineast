package org.vitrivr.cineast.core.data.m3d.texturemodel;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.joml.Vector3f;
import org.joml.Vector4f;
import org.vitrivr.cineast.core.data.m3d.texturemodel.util.MinimalBoundingBox;

/**
 * The Material contains all meshes and the texture that are drawn with on the meshes Further it contains the diffuse
 * color of the material
 */
public class Material {

  private static final Logger LOGGER = LogManager.getLogger();

  /**
   * List of {@link Mesh} objects that define the appearance of the model.
   */
  private final List<Mesh> meshes;

  /**
   * Texture that drawn on all meshes
   */
  private Texture texture;
  private Texture normalMapTexture;

  /**
   * DEFAULT_COLOR is black and 100% opaque
   */
  public static final Vector4f DEFAULT_COLOR = new Vector4f(0.0f, 0.0f, 0.0f, 1.0f);

  /**
   * diffuseColor is the color that is drawn on the meshes when no texture is present
   */
  private final Vector4f diffuseColor;

  private final Vector4f ambientColor;
  private float reflectance;
  private final Vector4f specularColor;

  /**
   * Empty material that can be used as a placeholder.
   */
  public static final Material EMPTY = new Material();

  /**
   * Constructor for Material.
   */
  public Material() {
    this.meshes = new ArrayList<>();
    this.texture = new Texture();
    this.normalMapTexture = null;
    this.diffuseColor = DEFAULT_COLOR;
    this.ambientColor = new Vector4f(0.0f, 0.0f, 0.0f, 1.0f);;
    this.specularColor = DEFAULT_COLOR;
  }

  /**
   * @return A MinimalBoundingBox which enclose all MinimalBoundingBoxes from containing meshes
   */
  public MinimalBoundingBox getMinimalBoundingBox() {
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

  /**
   * @return an unmodifiable list of meshes
   */
  public List<Mesh> getMeshes() {
    return Collections.unmodifiableList(this.meshes);
  }

  /**
   * @param mesh adds a mesh to the material
   */
  public void addMesh(Mesh mesh) {
    this.meshes.add(mesh);
  }

  /**
   * @return the texture to this material
   */
  public Texture getTexture() {
    return this.texture;
  }

  /**
   * @param texture sets the texture to this material
   */
  public void setTexture(Texture texture) {
    this.texture = texture;
  }


  public boolean hasNonDefaultTexture(){
    return !this.texture.isDefault();
  }

  /**
   * @param texture sets the texture to this material
   */
  public void setNormalTexture(Texture texture) {
    this.normalMapTexture = texture;
  }

  public Texture getNormalMapTexture() {
    return this.normalMapTexture;
  }

  public boolean hasNormalMapTexture() {
    return this.normalMapTexture != null;
  }

  /**
   * @return the diffuse color of this material
   */
  public Vector4f getDiffuseColor() {
    return this.diffuseColor;
  }

  /**
   * @param diffuseColor sets the diffuse color of this material
   */
  public void setDiffuseColor(Vector4f diffuseColor) {
    this.diffuseColor.set(diffuseColor);
  }


  public Vector4f getAmbientColor() {
    return this.ambientColor;
  }

  public void setAmbientColor(Vector4f ambientColor) {
    this.ambientColor.set(ambientColor);
  }

  public float getReflectance() {
    return this.reflectance;
  }

  public void setReflectance(float reflectance) {
    this.reflectance = reflectance;
  }

  public Vector4f getSpecularColor() {
    return this.specularColor;
  }

  public void setSpecularColor(Vector4f specularColor) {
    this.specularColor.set(specularColor);
  }

  /**
   * closes all resources the material uses Calls close on all containing classes
   */
  public void close() {
    this.meshes.forEach(Mesh::close);
    this.meshes.clear();
    this.texture.close();
    this.texture = null;
    LOGGER.trace("Closed Material");
  }
}
