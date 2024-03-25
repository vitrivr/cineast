package org.vitrivr.cineast.core.render.lwjgl.render;

import java.util.HashMap;
import java.util.Vector;
import org.joml.Matrix4f;
import org.joml.Vector3d;
import org.joml.Vector3f;
import org.joml.Vector4f;
import org.lwjgl.opengl.GL30;
import org.lwjgl.system.MemoryStack;

/**
 * Holds a hashmap for used uniforms are global variables in the shader e.g. projectionMatrix, modelMatrix, viewMatrix, txtSampler, material.diffuse
 *
 * @see <a href="https://www.khronos.org/opengl/wiki/Uniform_(GLSL)">https://www.khronos.org/opengl/wiki/Uniform_(GLSL)</a>
 * @see <a href= "./resources/renderer/lwjgl/shaders/scene.vert">"./resources/renderer/lwjgl/shaders/scene.vert"</a>
 * @see <a href= "./resources/renderer/lwjgl/shaders/scene.frag">"./resources/renderer/lwjgl/shaders/scene.frag"</a>
 */
public class UniformsMap {

  /**
   * HashMap for the uniforms Key: Uniform name Value: Uniform location in the shader
   */
  private final HashMap<String, Integer> uniforms;

  /**
   * Program id of the shader
   */
  private final int programId;

  /**
   * Instantiate a new UniformsMap
   *
   * @param programId Program id of the shader {@link ShaderProgram#getProgramId}
   */
  public UniformsMap(int programId) {
    this.programId = programId;
    this.uniforms = new HashMap<>();
  }

  /**
   * Creates a new uniform
   *
   * @param uniformName Name of the uniform
   */
  public void createUniform(String uniformName) {
    var uniformLocation = GL30.glGetUniformLocation(this.programId, uniformName);
    if (uniformLocation < 0) {
      throw new RuntimeException("Could not find uniform:" + uniformName);
    }
    this.uniforms.put(uniformName, uniformLocation);
  }

  /**
   * Sets the value of a uniform to gl context
   *
   * @param uniformName Name of the uniform
   * @param value       Value of the uniform
   */
  public void setUniform(String uniformName, int value) {
    GL30.glUniform1i(this.getUniformLocation(uniformName), value);
  }

  /**
   * Returns the location of the uniform from the hashmap
   *
   * @param uniformName name of the uniform
   * @return location of the uniform
   */
  private int getUniformLocation(String uniformName) {
    var location = this.uniforms.get(uniformName);
    if (location == null) {
      throw new RuntimeException("Could not find uniform:" + uniformName);
    }
    return location;
  }

  /**
   * Sets the value 4 float vector of a uniform to gl context
   *
   * @param uniformName Name of the uniform
   * @param value       Value of the uniform
   */
  public void setUniform(String uniformName, Vector4f value) {
    GL30.glUniform4f(this.getUniformLocation(uniformName), value.x, value.y, value.z, value.w);
  }

  /**
   * Sets the value 4*4 float matrix of a uniform to gl context
   *
   * @param uniformName Name of the uniform
   * @param value       Value of the uniform
   */
  public void setUniform(String uniformName, Matrix4f value) {
    try (var memoryStack = MemoryStack.stackPush()) {
      var location = this.uniforms.get(uniformName);
      if (location == null) {
        throw new RuntimeException("Could not find uniform:" + uniformName);
      }
      GL30.glUniformMatrix4fv(location, false, value.get(memoryStack.mallocFloat(16)));
    }
  }

  public void setUniform(String uniformName, float value) {
    GL30.glUniform1f(this.getUniformLocation(uniformName), value);
  }
  public void setUniform(String uniformName, Vector3f value) {
    GL30.glUniform3f(this.getUniformLocation(uniformName), value.x, value.y, value.z);
  }

  /**
   * Cleans up the uniforms
   */
  public void cleanup() {
    this.uniforms.clear();
  }
}
