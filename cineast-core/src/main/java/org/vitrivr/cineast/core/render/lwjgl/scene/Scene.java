package org.vitrivr.cineast.core.render.lwjgl.scene;

import java.util.HashMap;
import java.util.Map;
import org.vitrivr.cineast.core.render.lwjgl.model.Entity;
import org.vitrivr.cineast.core.render.lwjgl.model.Model;
import org.vitrivr.cineast.core.render.lwjgl.model.TextureCache;

/**
 * The Scene class will hold 3D scene elements (models, etc.).
 */
public class Scene {

  private final Map<String, Model> models;
  private final Projection projection;
  private final TextureCache textureCache;
  private final Camera camera;


  public Scene(int width, int height) {
    this.models = new HashMap<>();
    this.projection = new Projection(width, height);
    this.textureCache = new TextureCache();
    this.camera = new Camera();
  }

  public void addEntity(Entity entity) {
    var modelId = entity.getModelId();
    var model = this.models.get(modelId);
    if (model == null) {
      throw new RuntimeException("Model not found: " + modelId);
    }
    model.getEntities().add(entity);
  }

  public void addModel(Model model) {
    this.models.put(model.getId(), model);
    for (var mat : model.getMaterials()) {
      this.textureCache.copyFromTemporaryTextureCache(mat.getTemporaryTextureCache());
    }
  }

  public Map<String, Model> getModels() {
    return this.models;
  }

  public TextureCache getTextureCache() {
    return this.textureCache;
  }

  public Projection getProjection() {
    return this.projection;
  }

  public Camera getCamera() {
    return this.camera;
  }

  public void clearModels() {
    this.models.clear();
  }

  public void cleanup() {
    this.models.values().forEach(Model::cleanup);
  }

  public void resize(int width, int height) {
    this.projection.updateProjMatrix(width, height);
  }


}
