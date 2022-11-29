package org.vitrivr.cineast.core.render.lwjgl.glmodel;

import java.util.HashMap;
import java.util.Map;
import org.vitrivr.cineast.core.data.m3d.texturemodel.Entity;
import org.vitrivr.cineast.core.data.m3d.texturemodel.IModel;
import org.vitrivr.cineast.core.render.lwjgl.scene.Camera;
import org.vitrivr.cineast.core.render.lwjgl.scene.Projection;
import org.vitrivr.cineast.core.render.lwjgl.scene.Scene;

/**
 * The Scene class will hold 3D scene elements (models, etc.).
 */
public class GlScene {

  private final Scene scene;
  private final Map<String, IGlModel> models;
  private final GlTextureCache textureCache;

  public GlScene(Scene scene) {
    this.models = new HashMap<>();
    this.textureCache = new GlTextureCache();
    this.scene = scene;
    this.updateGlSceneFromScene();
  }

  public void addModel(IModel model) {
    this.scene.addModel(model);
    this.updateGlSceneFromScene();
  }

  private void updateGlSceneFromScene() {
    this.scene.getModels().forEach((k, v) -> this.models.putIfAbsent(k, new GLModel(v)));
    this.models.forEach(
        (k, v) -> this.models.get(k).getMaterials()
            .forEach(mat -> this.textureCache.addTextureIfAbsent(mat.getTexture())));
  }

  public void addEntity(Entity entity) {
    var modelId = entity.getModelId();
    var model = this.models.get(modelId);
    if (model == null) {
      throw new RuntimeException("Model not found: " + modelId);
    }
    model.addEntity(entity);
  }

  public Map<String, IGlModel> getModels() {
    return this.models;
  }

  public GlTextureCache getTextureCache() {
    return this.textureCache;
  }

  public Projection getProjection() {
    return this.scene.getProjection();
  }

  public Camera getCamera() {
    return this.scene.getCamera();
  }

  public void clearModels() {
    this.cleanup();
    this.models.clear();
  }

  public void cleanup() {
    this.models.values().forEach(IGlModel::cleanup);
  }

  public void resize(int width, int height) {
    this.scene.getProjection().updateProjMatrix(width, height);
  }

}
