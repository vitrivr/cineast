package org.vitrivr.cineast.core.render.lwjgl.model;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.joml.Vector3fc;
import org.joml.Vector3i;
import org.joml.Vector4i;
import org.vitrivr.cineast.core.data.m3d.ReadableMesh;
import org.vitrivr.cineast.core.data.m3d.WritableMesh;

public class Model implements IModel {

  private final String id;

  private List<Entity> entities;
  private List<Material> materials;

  public static final Model EMPTY = new Model("empty",(new ArrayList<>(){{add(Material.EMPTY);}}));

  public Model(String id, List<Material> materials) {
    this.id = id;
    this.entities = new ArrayList<>();
    this.materials = materials;
  }

  public List<Entity> getEntities() {
    return this.entities;
  }

  public void cleanup() {
    this.materials.stream().forEach(Material::cleanup);
  }

  public String getId() {
    return this.id;
  }

  public List<Material> getMaterials() {
    return this.materials;
  }
}
