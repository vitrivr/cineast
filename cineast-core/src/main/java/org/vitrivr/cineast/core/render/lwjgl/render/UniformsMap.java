package org.vitrivr.cineast.core.render.lwjgl.render;

import java.util.HashMap;
import org.joml.Matrix4f;
import org.joml.Vector4f;
import org.lwjgl.opengl.GL30;
import org.lwjgl.system.MemoryStack;

public class UniformsMap {

  private final HashMap<String, Integer> uniforms;
  private final int programId;

  public UniformsMap(int programId) {
    this.programId = programId;
    this.uniforms = new HashMap<>();
  }

  public void createUniform(String uniformName) {
    var uniformLocation = GL30.glGetUniformLocation(this.programId, uniformName);
    if (uniformLocation < 0) {
      throw new RuntimeException("Could not find uniform:" + uniformName);
    }
    this.uniforms.put(uniformName, uniformLocation);
  }

  public void setUniform(String uniformName, int value) {
    GL30.glUniform1i(this.getUniformLocation(uniformName), value);
  }

  private int getUniformLocation(String uniformName) {
    var location = this.uniforms.get(uniformName);
    if (location == null) {
      throw new RuntimeException("Could not find uniform:" + uniformName);
    }
    return location;
  }

  public void setUniform(String uniformName, Vector4f value) {
    GL30.glUniform4f(this.getUniformLocation(uniformName), value.x, value.y, value.z, value.w);
  }

  public void setUniform(String uniformName, Matrix4f value) {
    try (var memoryStack = MemoryStack.stackPush()) {
      var location = this.uniforms.get(uniformName);
      if (location == null) {
        throw new RuntimeException("Could not find uniform:" + uniformName);
      }
      GL30.glUniformMatrix4fv(location.intValue(), false, value.get(memoryStack.mallocFloat(16)));
    }
  }
}
