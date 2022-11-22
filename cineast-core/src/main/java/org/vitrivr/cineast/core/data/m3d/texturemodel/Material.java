package org.vitrivr.cineast.core.data.m3d.texturemodel;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.joml.Vector4f;

public class Material {

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
}
