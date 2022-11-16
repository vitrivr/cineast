package org.vitrivr.cineast.core.render.lwjgl.render;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import org.lwjgl.opengl.GL30;

public class ShaderProgram {

  private final int programId;

  public ShaderProgram(List<ShaderModuleData> shaderModuleDataList) {
    this.programId = GL30.glCreateProgram();
    if (this.programId == 0) {
      throw new RuntimeException("Could not Create Shader");
    }
    var shaderModules = new ArrayList<Integer>();
    shaderModuleDataList.forEach(s -> shaderModules.add(this.createShader(ShaderProgram.readShaderFile(s.shaderFile), s.shaderType)));
    this.link(shaderModules);
  }

  public void bind() {
    GL30.glUseProgram(this.programId);
  }

  public void unbind() {
    GL30.glUseProgram(0);
  }

  public void cleanup() {
    this.unbind();
    if (this.programId != 0) {
      GL30.glDeleteProgram(this.programId);
    }
  }

  protected int createShader(String shaderCode, int shaderType) {
    int shaderId = GL30.glCreateShader(shaderType);
    if (shaderId == 0) {
      throw new RuntimeException("Error creating Shader");
    }
    GL30.glShaderSource(shaderId, shaderCode);
    GL30.glCompileShader(shaderId);

    if (GL30.glGetShaderi(shaderId, GL30.GL_COMPILE_STATUS) == 0) {
      throw new RuntimeException("Error Comiling Shadert");
    }
    GL30.glAttachShader(this.programId, shaderId);
    return shaderId;
  }

  private void link(List<Integer> shaderModules) {
    GL30.glLinkProgram(this.programId);
    if (GL30.glGetProgrami(this.programId, GL30.GL_LINK_STATUS) == 0) {
      throw new RuntimeException("Error linking Shader");
    }
    shaderModules.forEach(s -> GL30.glDetachShader(this.programId, s));
    shaderModules.forEach(GL30::glDeleteShader);
  }

  public void validate() {
    GL30.glValidateProgram(this.programId);
    if (GL30.glGetProgrami(this.programId, GL30.GL_VALIDATE_STATUS) == 0) {
      throw new RuntimeException("Error validate Shader");
    }
  }

  public int getProgramId() {
    return this.programId;
  }

  public static String readShaderFile(String filePath) {
    String str;
    try {
      str = new String(Files.readAllBytes(Paths.get(filePath)));
    } catch (IOException ex) {
      throw new RuntimeException("Error reading file");
    }
    return str;
  }

  public record ShaderModuleData(String shaderFile, int shaderType) {

  }
}
