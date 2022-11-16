package org.vitrivr.cineast.core.render.lwjgl.render;

import java.util.ArrayList;

import org.lwjgl.opengl.GL30;
import org.vitrivr.cineast.core.render.lwjgl.render.ShaderProgram.ShaderModuleData;
import org.vitrivr.cineast.core.render.lwjgl.scene.Scene;

public class SceneRender {

  private final ShaderProgram shaderProgram;
  private UniformsMap uniformsMap;

  public SceneRender() {
    var shaderModuleDataList = new ArrayList<ShaderProgram.ShaderModuleData>();
    shaderModuleDataList.add(new ShaderModuleData("./resources/renderer/lwjgl/shaders/scene.vert", GL30.GL_VERTEX_SHADER));
    shaderModuleDataList.add(new ShaderModuleData("./resources/renderer/lwjgl/shaders/scene.frag", GL30.GL_FRAGMENT_SHADER));
    this.shaderProgram = new ShaderProgram(shaderModuleDataList);
    this.createUniforms();
  }

  private void createUniforms() {
    this.uniformsMap = new UniformsMap(this.shaderProgram.getProgramId());
    this.uniformsMap.createUniform("projectionMatrix");
    this.uniformsMap.createUniform("modelMatrix");
    this.uniformsMap.createUniform("viewMatrix");
    this.uniformsMap.createUniform("txtSampler");
    this.uniformsMap.createUniform("material.diffuse");
  }

  public void cleanup() {
    this.shaderProgram.cleanup();
  }

  public void render(Scene scene) {
    this.shaderProgram.bind();

    this.uniformsMap.setUniform("projectionMatrix", scene.getProjection().getProjMatrix());
    this.uniformsMap.setUniform("viewMatrix", scene.getCamera().getViewMatrix());
    this.uniformsMap.setUniform("txtSampler", 0);

    var models = scene.getModels().values();
    var textures = scene.getTextureCache();

    for (var model : models) {
      var enteties = model.getEntities();
      for (var material : model.getMaterials()) {
        this.uniformsMap.setUniform("material.diffuse", material.getDiffuseColor());
        var texture = textures.getTexture(material.getTexturePath());
        texture.bind();
        for (var mesh : material.getMeshes()) {
          GL30.glBindVertexArray(mesh.getVaoId());
          for (var entity : enteties) {
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
