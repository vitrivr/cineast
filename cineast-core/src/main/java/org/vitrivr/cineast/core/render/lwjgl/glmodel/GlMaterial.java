package org.vitrivr.cineast.core.render.lwjgl.glmodel;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.tensorflow.op.math.Log;
import org.vitrivr.cineast.core.data.m3d.texturemodel.Material;
import org.joml.Vector4f;

public class GlMaterial {
  private static final Logger LOGGER = LogManager.getLogger();
  private final List<GlMesh> meshes;
  private final GlTexture texture;
  private final Material material;

  public GlMaterial(Material material) {
    this.meshes = new ArrayList<>();
    this.material = material;
    this.material.getMeshes().stream().forEach(mesh -> this.meshes.add(new GlMesh(mesh)));

    this.texture = new GlTexture(this.material.getTexture());
  }


  /**
   * Cleans the GlMaterial
   * Does not affect the underlying material
   */
  public void cleanup() {
    this.meshes.stream().forEach(GlMesh::cleanup);
    this.meshes.clear();
    this.texture.cleanup();
    LOGGER.info("Cleaned-up GlMaterial}");
  }


  public List<GlMesh> getMeshes() {
    return Collections.unmodifiableList(this.meshes);
  }

  public GlTexture getTexture() {
    return this.texture;
  }

  public Vector4f getDiffuseColor() {
    return this.material.getDiffuseColor();
  }

}
