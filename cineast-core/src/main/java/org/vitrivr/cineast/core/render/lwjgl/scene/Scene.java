package org.vitrivr.cineast.core.render.lwjgl.scene;

import java.util.HashMap;
import java.util.Map;
import org.vitrivr.cineast.core.data.m3d.texturemodel.Entity;
import org.vitrivr.cineast.core.data.m3d.texturemodel.IModel;

/**
 * The Scene class will hold 3D scene elements (models, etc.).
 */
public class Scene {

  private final Map<String, IModel> models;
  private final Projection projection;
  private final Camera camera;


  public Scene(int width, int height) {
    this.models = new HashMap<>();
    this.projection = new Projection(width, height);
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

  public void addModel(IModel model) {
    this.models.put(model.getId(), model);
  }

  public Map<String, IModel> getModels() {
    return this.models;
  }

  public Projection getProjection() {
    return this.projection;
  }

  public Camera getCamera() {
    return this.camera;
  }

  public void resize(int width, int height) {
    this.projection.updateProjMatrix(width, height);
  }

}
