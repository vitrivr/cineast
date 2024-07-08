package org.vitrivr.cineast.core.render.lwjgl.scene;

import java.util.HashMap;
import java.util.Map;
import org.vitrivr.cineast.core.data.m3d.texturemodel.Entity;
import org.vitrivr.cineast.core.data.m3d.texturemodel.IModel;
import org.vitrivr.cineast.core.render.lwjgl.scene.lights.SceneLights;

/**
 * The Scene class holds generic 3D scene elements (models, etc.). A scene consists of a model, a camera and a
 * projection.
 */
public class Scene {

  /**
   * Map of generic models in the scene.
   */
  private final Map<String, IModel> models;
  /**
   * Projection of the scene.
   */
  private final Projection projection;
  /**
   * Camera of the scene.
   */
  private final Camera camera;

  /**
   * SceneLights Class holds all lights of the scene.
   */
  private SceneLights sceneLights;

  /**
   * Creates a new Scene.
   *
   * @param width  Width of the scene.
   * @param height Height of the scene.
   */
  public Scene(int width, int height) {
    this.models = new HashMap<>();
    this.projection = new Projection(width, height);
    this.camera = new Camera();
  }

  /**
   * Add an entity to a contained corresponding model. Can be used to resize the scene before gl context is created.
   */
  @SuppressWarnings("unused")
  public void addEntity(Entity entity) {
    var modelId = entity.getModelId();
    var model = this.models.get(modelId);
    if (model == null) {
      throw new RuntimeException("Model not found: " + modelId);
    }
    model.getEntities().add(entity);
  }

  /**
   * Add a model to the scene.
   */
  public void addModel(IModel model) {
    this.models.put(model.getId(), model);
  }

  /**
   * Get a model from the scene.
   */
  public Map<String, IModel> getModels() {
    return this.models;
  }

  /**
   * Get the projection of the scene.
   */
  public Projection getProjection() {
    return this.projection;
  }

  /**
   * Get the camera of the scene.
   */
  public Camera getCamera() {
    return this.camera;
  }

  /**
   * Get the scene lights.
   */
  public SceneLights getSceneLights() {
    return this.sceneLights;
  }

  /**
   * Set the scene lights.
   */
  public void setSceneLights(SceneLights sceneLights) {
    this.sceneLights = sceneLights;
  }

  /**
   * Resizes the scene. Can be used to resize the scene before gl context is created.
   *
   * @param width  Width of the scene.
   * @param height Height of the scene.
   */
  @SuppressWarnings("unused")
  public void resize(int width, int height) {
    this.projection.updateProjMatrix(width, height);
  }

}
