package org.vitrivr.cineast.core.render.lwjgl.glmodel;

import java.util.HashMap;
import java.util.Map;
import org.vitrivr.cineast.core.data.m3d.texturemodel.Entity;
import org.vitrivr.cineast.core.data.m3d.texturemodel.IModel;
import org.vitrivr.cineast.core.render.lwjgl.scene.Camera;
import org.vitrivr.cineast.core.render.lwjgl.scene.Projection;
import org.vitrivr.cineast.core.render.lwjgl.scene.Scene;
import org.vitrivr.cineast.core.render.lwjgl.scene.lights.LightingOptions;
import org.vitrivr.cineast.core.render.lwjgl.scene.lights.SceneLights;

/**
 * The GLScene class ist the top most class of the gl model hierarchy. The gl model hierarchy is used as a wrapper for
 * the model hierarchy. Therefore, each gl class has a corresponding model class. The generic class has to be provided
 * in the constructor.
 * <ul>
 * <li>Scene -> GLScene( Scene )</li>
 * <li>Model -> GlModel( IModel )</li>
 * <li>Material -> GLMaterial( Material )</li>
 * <li>Mesh -> GLMesh( Mesh )</li>
 * <li>Texture -> GLTexture( Texture )</li>
 * </ul>
 * <p>
 * The purpose is to bring the generic model in an OpenGl context
 * {@link Scene} -> {@link GLScene}
 */
public class GLScene {

  /**
   * The scene that is wrapped by this gl scene.
   */
  private final Scene scene;
  /**
   * The wrapped GlModels that are wrapped by this gl scene.
   */
  private final Map<String, IGLModel> models;

  /**
   * The texture cache that is used by this gl scene. Textures are cached to avoid loading the same texture multiple
   * times. Has no corresponding generic class.
   */
  private final GLTextureCache textureCache;

  /**
   * Creates a new GLScene from a scene.
   *
   * @param scene The scene that is wrapped by this gl scene.
   */
  public GLScene(Scene scene) {
    this.models = new HashMap<>();
    this.textureCache = new GLTextureCache();
    this.scene = scene;
    this.updateGlSceneFromScene();
  }

  /**
   * Adds a model to the scene.
   *
   * @param model The model that is added to the scene.
   */
  public void addModel(IModel model) {
    this.scene.addModel(model);
    this.updateGlSceneFromScene();
  }

  /**
   * Updates the gl scene from the scene. It updates the gl scene content to match the scene content.
   */
  private void updateGlSceneFromScene() {
    this.scene.getModels().forEach((k, v) -> this.models.putIfAbsent(k, new GLModel(v)));
    this.models.forEach(
        (k, v) -> this.models.get(k).getMaterials()
            .forEach(mat -> {
              this.textureCache.addTextureIfAbsent(mat.getTexture());
              if (mat.hasNormalMapTexture()) {
                this.textureCache.addTextureIfAbsent(mat.getNormalMapTexture());
              }
            }));
  }

  /**
   * Adds an entity to the corresponding model.
   *
   * @param entity The entity that is added to the model.
   */
  public void addEntity(Entity entity) {
    var modelId = entity.getModelId();
    var model = this.models.get(modelId);
    if (model == null) {
      throw new RuntimeException("Model not found: " + modelId);
    }
    model.addEntity(entity);
  }

  /**
   * Returns the gl models of the gl scene.
   *
   * @return The gl models of the gl scene.
   */
  public Map<String, IGLModel> getModels() {
    return this.models;
  }

  /**
   * Returns the texture cache of the gl scene.
   *
   * @return The texture cache of the gl scene.
   */
  public GLTextureCache getTextureCache() {
    return this.textureCache;
  }

  /**
   * Returns the projection of the wrapped generic scene.
   *
   * @return The projection of the wrapped generic scene.
   */
  public Projection getProjection() {
    return this.scene.getProjection();
  }

  /**
   * Returns the camera of the wrapped generic scene.
   *
   * @return The camera of the wrapped generic scene.
   */
  public Camera getCamera() {
    return this.scene.getCamera();
  }

  /**
   * Clears the models of the gl scene but not containing resources. Removes the references to the wrapped generic
   * models and textures. Hence, the models could be used by another extraction task this method does not close the
   * models or textures. Can be used to only remove Models temporarily from gl scene.
   */
  @SuppressWarnings("unused")
  public void clearModels() {
    this.cleanup();
    this.models.clear();
  }

  /**
   * Cleans up the gl scene and calls all underlying cleanup methods. Removes only the references to wrapped generic
   * models and textures. Hence, the model could be used by another extraction task this method does not close the
   * generic models or textures.
   */
  public void cleanup() {
    this.models.values().forEach(IGLModel::cleanup);
    this.models.clear();
    this.textureCache.cleanup();
  }

  /**
   * Resizes the projection of the wrapped generic scene.
   *
   * @param width  The new width of the projection.
   * @param height The new height of the projection.
   */
  public void resize(int width, int height) {
    this.scene.getProjection().updateProjMatrix(width, height);
  }

  /**
   * Get the scene lights.
   */
  public SceneLights getSceneLights() {
    return this.scene.getSceneLights();
  }

  /**
   * Get the scene lights.
   */
  public void setSceneLights(SceneLights sceneLights) {
    this.scene.setSceneLights(sceneLights);
  }

  /**
   * Get the scene lights.
   */
  public void setSceneLights(LightingOptions options) {
    this.scene.setSceneLights(options.getSceneLigths(this.scene));
  }
}
