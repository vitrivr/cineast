package org.vitrivr.cineast.core.render.lwjgl.render;

import java.util.ArrayList;
import java.util.Vector;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.joml.Vector4f;
import org.lwjgl.opengl.GL30;
import org.vitrivr.cineast.core.render.lwjgl.glmodel.GLScene;
import org.vitrivr.cineast.core.render.lwjgl.glmodel.GLTexture;
import org.vitrivr.cineast.core.render.lwjgl.render.ShaderProgram.ShaderModuleData;
import org.vitrivr.cineast.core.render.lwjgl.scene.Scene;
import org.vitrivr.cineast.core.render.lwjgl.scene.lights.Attenuation;
import org.vitrivr.cineast.core.render.lwjgl.scene.lights.PointLight;
import org.vitrivr.cineast.core.render.lwjgl.scene.lights.SpotLight;

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

  private static final int MAX_POINT_LIGHTS = 5;
  private static final int MAX_SPOT_LIGHTS = 5;

  /**
   * Resource path to the scene shader program
   */
  private static final String VERTEX_SHADER_PATH = "./resources/renderer/lwjgl/shaders/scene.vert";
  /**
   * Resource path to the fragment shader program
   */
  private static final String FRAGMENT_SHADER_PATH = "./resources/renderer/lwjgl/shaders/scene.frag";
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
    shaderModuleDataList.add(new ShaderModuleData(VERTEX_SHADER_PATH, GL30.GL_VERTEX_SHADER));
    shaderModuleDataList.add(new ShaderModuleData(FRAGMENT_SHADER_PATH, GL30.GL_FRAGMENT_SHADER));
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
    this.uniformsMap.createUniform("normalMapSampler");
    this.uniformsMap.createUniform("txtSampler");

    this.uniformsMap.createUniform("material.ambient");
    this.uniformsMap.createUniform("material.diffuse");
    this.uniformsMap.createUniform("material.specular");
    this.uniformsMap.createUniform("material.reflectance");
    this.uniformsMap.createUniform("material.hasNormalMap");
    this.uniformsMap.createUniform("ambientLight.factor");
    this.uniformsMap.createUniform("ambientLight.color");

    for (var ic = 0; ic < MAX_POINT_LIGHTS; ic++) {
      var name = "pointLights[" + ic + "].";
      this.uniformsMap.createUniform(name + "position");
      this.uniformsMap.createUniform(name + "color");
      this.uniformsMap.createUniform(name + "intensity");
      this.uniformsMap.createUniform(name + "attenuation.constant");
      this.uniformsMap.createUniform(name + "attenuation.linear");
      this.uniformsMap.createUniform(name + "attenuation.exponent");
    }
    for (var ic = 0; ic < MAX_SPOT_LIGHTS; ic++) {
      var name = "spotLights[" + ic + "].";
      this.uniformsMap.createUniform(name + "pointLight.position");
      this.uniformsMap.createUniform(name + "pointLight.color");
      this.uniformsMap.createUniform(name + "pointLight.intensity");
      this.uniformsMap.createUniform(name + "pointLight.attenuation.constant");
      this.uniformsMap.createUniform(name + "pointLight.attenuation.linear");
      this.uniformsMap.createUniform(name + "pointLight.attenuation.exponent");
      this.uniformsMap.createUniform(name + "coneDirection");
      this.uniformsMap.createUniform(name + "cutOff");
    }

    this.uniformsMap.createUniform("directionalLight.color");
    this.uniformsMap.createUniform("directionalLight.direction");
    this.uniformsMap.createUniform("directionalLight.intensity");
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
   * Renders the Models in the scene Creates standard render options
   *
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
   *
   * @param scene Scene to render
   * @param opt   Render options
   */
  public void render(GLScene scene, RenderOptions opt) {
    this.shaderProgram.bind();

    this.uniformsMap.setUniform("projectionMatrix", scene.getProjection().getProjMatrix());
    this.uniformsMap.setUniform("viewMatrix", scene.getCamera().getViewMatrix());
    this.uniformsMap.setUniform("normalMapSampler", 1);
    this.uniformsMap.setUniform("txtSampler", 0);

    this.updateLights(scene);

    var models = scene.getModels().values();
    var textures = scene.getTextureCache();

    for (var model : models) {
      var entities = model.getEntities();
      for (var material : model.getMaterials()) {
        GLTexture texture;

        // Either draw texture or use color function
        if (opt.showTextures) {
          this.uniformsMap.setUniform("material.ambient", material.getAmbientColor());
          this.uniformsMap.setUniform("material.diffuse", material.getDiffuseColor());
          this.uniformsMap.setUniform("material.specular", material.getSpecularColor());
          this.uniformsMap.setUniform("material.reflectance", material.getReflectance());
          texture = textures.getTexture(material.getTexture().getTexturePath());

          // Check if there is a normal map, and load it if there is. Further set flag for shader program.
          if (material.hasNormalMapTexture()) {
            this.uniformsMap.setUniform("normalMapSampler", 1);
            var normalMapTexture = textures.getTexture(material.getTexture().getTexturePath());
            GL30.glActiveTexture(GL30.GL_TEXTURE1);
            normalMapTexture.bind();
          }else {
            this.uniformsMap.setUniform("normalMapSampler", 0);
          }
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

  private void updateLights(GLScene scene) {
    var viewMatrix = scene.getCamera().getViewMatrix();
    var sceneLights = scene.getSceneLights();

    var ambientLight = sceneLights.getAmbientLight();
    this.uniformsMap.setUniform("ambientLight.factor", ambientLight.getIntensity());
    this.uniformsMap.setUniform("ambientLight.color", ambientLight.getColor());

    var directionalLight = sceneLights.getDirectionalLight();
    var auxDir = new Vector4f(directionalLight.getDirection(), 0);
    auxDir.mul(viewMatrix);
    var direction = new Vector3f(auxDir.x, auxDir.y, auxDir.z);
    this.uniformsMap.setUniform("directionalLight.color", directionalLight.getColor());
    this.uniformsMap.setUniform("directionalLight.direction", direction);
    this.uniformsMap.setUniform("directionalLight.intensity", directionalLight.getIntensity());

    var pointLights = sceneLights.getPointLights();
    var numPointLight = pointLights.size();
    PointLight pointLight;
    for (var ic = 0; ic < MAX_POINT_LIGHTS; ic++) {
      if (ic < numPointLight) {
        pointLight = pointLights.get(ic);
      } else {
        pointLight = null;
      }
      var name = "pointLights[" + ic + "].";
      this.updatePointLight(pointLight, name, viewMatrix);
    }

    var spotLights = sceneLights.getSpotLights();
    var numSpotLight = spotLights.size();
    SpotLight spotLight;
    for (var ic = 0; ic < MAX_SPOT_LIGHTS; ic++) {
      if (ic < numSpotLight) {
        spotLight = spotLights.get(ic);
      } else {
        spotLight = null;
      }
      var name = "spotLights[" + ic + "].";
      this.updateSpotLight(spotLight, name, viewMatrix);
    }
  }

  private void updatePointLight(PointLight pointLight, String name, Matrix4f viewMatrix) {

    var aux = new Vector4f();
    var lightPos = new Vector3f();
    var color = new Vector3f();
    var intensity = 0.0f;
    var constant = 0.0f;
    var linear = 0.0f;
    var exponent = 0.0f;
    if (pointLight != null) {
      aux.set(pointLight.getPosition(), 1);
      aux.mul(viewMatrix);
      lightPos.set(aux.x, aux.y, aux.z);
      color.set(pointLight.getColor());
      intensity = pointLight.getIntensity();
      var attenuation = pointLight.getAttenuation();
      constant = attenuation.getConstant();
      linear = attenuation.getLinear();
      exponent = attenuation.getExponent();
    }

    this.uniformsMap.setUniform(name + "position", lightPos);
    this.uniformsMap.setUniform(name + "color", color);
    this.uniformsMap.setUniform(name + "intensity", intensity);
    this.uniformsMap.setUniform(name + "attenuation.constant", constant);
    this.uniformsMap.setUniform(name + "attenuation.linear", linear);
    this.uniformsMap.setUniform(name + "attenuation.exponent", exponent);
  }

  private void updateSpotLight(SpotLight spotLight, String name, Matrix4f viewMatrix) {
    PointLight pointLight = null;
    var coneDirection = new Vector3f();
    float cutoff = 0.0f;
    if (spotLight != null) {
      coneDirection = spotLight.getConeDirection();
      cutoff = spotLight.getCutOff();
      pointLight = spotLight.getPointLight();
    }
    this.uniformsMap.setUniform(name + "coneDirection", coneDirection);
    this.uniformsMap.setUniform(name + "cutOff", cutoff);
    this.updatePointLight(pointLight, name + "pointLight.", viewMatrix);
  }

}