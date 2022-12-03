package org.vitrivr.cineast.core.render.lwjgl.glmodel;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.vitrivr.cineast.core.data.m3d.texturemodel.Material;
import org.joml.Vector4f;

public class GlMaterial {

  private final List<GlMesh> meshes;
  private final GlTexture texture;
  private final Material material;

  public GlMaterial(Material material) {
    this.meshes = new ArrayList<>();
    this.material = material;
    this.material.getMeshes().stream().forEach(mesh -> this.meshes.add(new GlMesh(mesh)));

    this.texture = new GlTexture(this.material.getTexture());
  }

  public void cleanup() {
    this.meshes.stream().forEach(GlMesh::cleanup);
    this.texture.cleanup();
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
