package org.vitrivr.cineast.core.render.lwjgl.model;

import java.util.ArrayList;
import java.util.List;
import org.joml.Vector4f;

public class Material {

  private final List<Mesh> meshes;
  private String texturePath;
  private TextureCache temporaryTextureCache;
  public static final Vector4f DEFAULT_COLOR = new Vector4f(0.0f, 0.0f, 0.0f, 1.0f);
  private Vector4f diffuseColor;

  public static final Material EMPTY = new Material();

  public Material() {
    this.meshes = new ArrayList<>();
    this.diffuseColor = DEFAULT_COLOR;
  }
  public Material(Mesh mesh) {
    this.meshes = new ArrayList<>();
    this.meshes.add(mesh);
    this.diffuseColor = DEFAULT_COLOR;
  }

  public void cleanup() {
    this.meshes.stream().forEach(Mesh::cleanup);
  }
  public List<Mesh> getMeshes() {
    return this.meshes;
  }
  public String getTexturePath() {
    return this.texturePath;
  }
  public void setTexturePath(String texturePath) {
    this.texturePath = texturePath;
    this.temporaryTextureCache = new TextureCache();
    this.temporaryTextureCache.createTexture(texturePath);
  }

  public TextureCache getTemporaryTextureCache() {
    return this.temporaryTextureCache;
  }

  public Vector4f getDiffuseColor() {
    return this.diffuseColor;
  }
  public void setDiffuseColor(Vector4f diffuseColor) {
    this.diffuseColor = diffuseColor;
  }
}
