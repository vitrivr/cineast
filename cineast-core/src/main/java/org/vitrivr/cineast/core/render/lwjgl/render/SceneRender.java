package org.vitrivr.cineast.core.render.lwjgl.render;

import java.util.ArrayList;
import org.lwjgl.opengl.GL30;
import org.vitrivr.cineast.core.render.lwjgl.glmodel.GLScene;
import org.vitrivr.cineast.core.render.lwjgl.glmodel.GLTexture;
import org.vitrivr.cineast.core.render.lwjgl.render.ShaderProgram.ShaderModuleData;

/**
 * SceneRender
 * <ul>
 * <li>Renders the scene</li>
 * <li>Loads the scene shader</li>
 * <li>Creates the uniforms </li>
 * <li>Binds the  Model</li>
 * <li>Binds the Texture</li>
 * </ul>
 */
public class SceneRender {

  /**
   * Instance of the scene shader program
   */
  private final ShaderProgram shaderProgram;
  /**
   * Uniforms for the scene shader
   */
  private UniformsMap uniformsMap;

  /**
   * SceneRender. During construction: Loads the scene shader from the resources
   */
  public SceneRender() {
    var shaderModuleDataList = new ArrayList<ShaderProgram.ShaderModuleData>();
    shaderModuleDataList.add(new ShaderModuleData("./resources/renderer/lwjgl/shaders/scene.vert", GL30.GL_VERTEX_SHADER));
    shaderModuleDataList.add(new ShaderModuleData("./resources/renderer/lwjgl/shaders/scene.frag", GL30.GL_FRAGMENT_SHADER));
    this.shaderProgram = new ShaderProgram(shaderModuleDataList);
    this.createUniforms();
  }

  /**
   * Creates the uniforms for the scene shader creates the following uniforms:
   * <ul>
   *   <li>projectionMatrix</li>
   *   <li>modelMatrix</li>
   *   <li>viewMatrix</li>
   *   <li>txtSampler</li>
   *   <li>material.diffuse</li>
   * </ul>
   */
  private void createUniforms() {
    this.uniformsMap = new UniformsMap(this.shaderProgram.getProgramId());
    this.uniformsMap.createUniform("projectionMatrix");
    this.uniformsMap.createUniform("modelMatrix");
    this.uniformsMap.createUniform("viewMatrix");
    this.uniformsMap.createUniform("txtSampler");
    this.uniformsMap.createUniform("material.diffuse");
  }

  /**
   * Releases all resources
   * <ul>
   *   <li>Releases the shader program</li>
   *   <li>Releases the uniforms</li>
   * </ul>
   */
  public void cleanup() {
    this.shaderProgram.cleanup();
    this.uniformsMap.cleanup();
    this.uniformsMap = null;
  }

  /**
   * Renders the Models in the scene
   * Creates standard render options
   * @param scene Scene to render
   */
  public void render(GLScene scene) {
    this.render(scene, new RenderOptions());
  }

  /**
   * Renders the Models in the scene
   * <ul>
   *   <li>Binds projection matrix</li>
   *   <li>Binds view matrix</li>
   *   <li>Binds texture sampler</li>
   * </ul>
   *   Further, iterate over all models in the scene
 *   <ul>
 *     <li>Iterate over all materials in the model</li>
 *     <li>Sets texture or color function</li>
 *     <li>Iterate over all meshes in the material</li>
 *     <li>Binds the mesh</li>
 *     <li>Iterate over all entities to draw the mesh</li>
 *     <li>Binds the model matrix</li>
 *     <li>Draws the mesh</li>
 *     <li>Unbinds</li>
 *   </ul>
   * @param scene Scene to render
   * @param opt Render options
   */
  public void render(GLScene scene, RenderOptions opt) {
    this.shaderProgram.bind();

    this.uniformsMap.setUniform("projectionMatrix", scene.getProjection().getProjMatrix());
    this.uniformsMap.setUniform("viewMatrix", scene.getCamera().getViewMatrix());
    this.uniformsMap.setUniform("txtSampler", 0);

    var models = scene.getModels().values();
    var textures = scene.getTextureCache();

    for (var model : models) {
      var entities = model.getEntities();
      for (var material : model.getMaterials()) {
        GLTexture texture;
        // Either draw texture or use color function
        if (opt.showTextures) {
          this.uniformsMap.setUniform("material.diffuse", material.getDiffuseColor());
          texture = textures.getTexture(material.getTexture().getTexturePath());
        } else {
          this.uniformsMap.setUniform("material.diffuse", opt.colorfunction.apply(1f));
          texture = textures.getTexture("default");
        }
        GL30.glActiveTexture(GL30.GL_TEXTURE0);
        texture.bind();
        for (var mesh : material.getMeshes()) {
          GL30.glBindVertexArray(mesh.getVaoId());
          for (var entity : entities) {
            this.uniformsMap.setUniform("modelMatrix", entity.getModelMatrix());
            GL30.glDrawElements(GL30.GL_TRIANGLES, mesh.getNumVertices(), GL30.GL_UNSIGNED_INT, 0);
          }
        }
      }
    }
    GL30.glBindVertexArray(0);

    this.shaderProgram.unbind();
  }
}
