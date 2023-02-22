package org.vitrivr.cineast.core.render.lwjgl.glmodel;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.joml.Vector4f;
import org.vitrivr.cineast.core.data.m3d.texturemodel.Material;


/**
 * The GlMaterial class is a wrapper for the {@link Material} class.
 * <ul>
 * <li>Material -> GlMaterial( Material )</li>
 * </ul>
 * <p>
 * The purpose is to bring the generic Material in an OpenGl context
 * {@link Material} -> {@link GlMaterial}
 */
public class GlMaterial {

  private static final Logger LOGGER = LogManager.getLogger();
  /**
   * The contained meshes in gl context
   */
  private final List<GlMesh> meshes;
  /**
   * The contained texture in gl context
   */
  private final GlTexture texture;
  /**
   * The material that is wrapped by this gl material.
   */
  private final Material material;

  /**
   * Creates a new GlMaterial from a material.
   *
   * @param material The material that is wrapped by this gl material.
   */
  public GlMaterial(Material material) {
    this.meshes = new ArrayList<>();
    this.material = material;
    this.material.getMeshes().forEach(mesh -> this.meshes.add(new GlMesh(mesh)));
    this.texture = new GlTexture(this.material.getTexture());
  }

  /**
   * Cleans up the gl material and calls all underlying cleanup methods.
   * Removes only the references to wrapped generic meshes and texture.
   * Hence, the material could be used by another extraction task this method does not close the generic meshes or texture.
   */
  public void cleanup() {
    this.meshes.forEach(GlMesh::cleanup);
    this.meshes.clear();
    this.texture.cleanup();
    LOGGER.trace("Cleaned-up GlMaterial}");
  }

  /**
   * Returns the gl meshes of this gl material.
   *
   * @return The unmodifiable list of gl meshes of this gl material.
   */
  public List<GlMesh> getMeshes() {
    return Collections.unmodifiableList(this.meshes);
  }

  /**
   * Returns the gl texture of this gl material.
   *
   * @return The gl texture of this gl material.
   */
  public GlTexture getTexture() {
    return this.texture;
  }

  /**
   * Returns the color from wrapped generic material.
   *
   * @return The color from wrapped generic material. (r,g,b,opacity)
   */
  public Vector4f getDiffuseColor() {
    return this.material.getDiffuseColor();
  }
}
