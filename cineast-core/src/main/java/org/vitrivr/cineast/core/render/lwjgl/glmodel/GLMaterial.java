package org.vitrivr.cineast.core.render.lwjgl.glmodel;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.joml.Vector4f;
import org.vitrivr.cineast.core.data.m3d.texturemodel.Material;


/**
 * The GLMaterial class is a wrapper for the {@link Material} class.
 * <ul>
 * <li>Material -> GLMaterial( Material )</li>
 * </ul>
 * <p>
 * The purpose is to bring the generic Material in an OpenGl context
 * {@link Material} -> {@link GLMaterial}
 */
public class GLMaterial {

  private static final Logger LOGGER = LogManager.getLogger();
  /**
   * The contained meshes in gl context
   */
  private final List<GLMesh> meshes;
  /**
   * The contained texture in gl context
   */
  private final GLTexture texture;

  /**
   * The contained texture in gl context
   */
  private final GLTexture normalMapTexture;

  /**
   * The material that is wrapped by this gl material.
   */
  private final Material material;

  /**
   * Creates a new GLMaterial from a material.
   *
   * @param material The material that is wrapped by this gl material.
   */
  public GLMaterial(Material material) {
    this.meshes = new ArrayList<>();
    this.material = material;
    this.material.getMeshes().forEach(mesh -> this.meshes.add(new GLMesh(mesh)));
    this.texture = new GLTexture(this.material.getTexture());
    if (this.material.hasNormalMapTexture()){
      this.normalMapTexture = new GLTexture(this.material.getNormalMapTexture());
    } else {
      this.normalMapTexture = null;
    }
  }

  /**
   * Cleans up the gl material and calls all underlying cleanup methods.
   * Removes only the references to wrapped generic meshes and texture.
   * Hence, the material could be used by another extraction task this method does not close the generic meshes or texture.
   */
  public void cleanup() {
    this.meshes.forEach(GLMesh::cleanup);
    this.meshes.clear();
    this.texture.cleanup();
    LOGGER.trace("Cleaned-up GLMaterial");
  }

  /**
   * Returns the gl meshes of this gl material.
   *
   * @return The unmodifiable list of gl meshes of this gl material.
   */
  public List<GLMesh> getMeshes() {
    return Collections.unmodifiableList(this.meshes);
  }

  /**
   * Returns the gl texture of this gl material.
   *
   * @return The gl texture of this gl material.
   */
  public GLTexture getTexture() {
    return this.texture;
  }

  /**
   * Returns the gl texture of this gl material.
   *
   * @return The gl texture of this gl material.
   */
  public GLTexture getNormalMapTexture() {
    return this.normalMapTexture;
  }

  public boolean hasNormalMapTexture() {
    return this.normalMapTexture != null;
  }

  /**
   * Returns the color from wrapped generic material.
   *
   * @return The color from wrapped generic material. (r,g,b,opacity)
   */
  public Vector4f getDiffuseColor() {
    return this.material.getDiffuseColor();
  }
  /**
   * Returns the color from wrapped generic material.
   *
   * @return The color from wrapped generic material. (r,g,b,opacity)
   */
  public Vector4f getAmbientColor() {
    return this.material.getAmbientColor();
  }
  /**
   * Returns the color from wrapped generic material.
   *
   * @return The color from wrapped generic material. (r,g,b,opacity)
   */
  public Vector4f getSpecularColor() {
    return this.material.getSpecularColor();
  }
  /**
   * Returns the color from wrapped generic material.
   *
   * @return The color from wrapped generic material. (r,g,b,opacity)
   */
  public float getReflectance() {
    return this.material.getReflectance();
  }
}
