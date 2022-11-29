package org.vitrivr.cineast.core.render.lwjgl.glmodel;

import java.util.ArrayList;
import java.util.List;
import org.lwjgl.system.Configuration;
import org.vitrivr.cineast.core.data.m3d.texturemodel.Mesh;
import org.lwjgl.opengl.GL30;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.system.MemoryUtil;

public class GlMesh {


  private final Mesh mesh;
  private final List<Integer> vboIdList;
  private final int vaoId;


  public GlMesh(Mesh mesh) {
    this.mesh = mesh;
    this.vboIdList = new ArrayList<>();



    try (var memoryStack = MemoryStack.stackPush()) {

      this.vaoId = GL30.glGenVertexArrays();
      GL30.glBindVertexArray(this.vaoId);

      // Positions VBO
      int vboId = GL30.glGenBuffers();
      this.vboIdList.add(vboId);
      var positionsBuffer = memoryStack.callocFloat(this.mesh.getPositions().length);
      positionsBuffer.put(0, this.mesh.getPositions());
      GL30.glBindBuffer(GL30.GL_ARRAY_BUFFER, vboId);
      GL30.glBufferData(GL30.GL_ARRAY_BUFFER, positionsBuffer, GL30.GL_STATIC_DRAW);
      GL30.glEnableVertexAttribArray(0);
      GL30.glVertexAttribPointer(0, 3, GL30.GL_FLOAT, false, 0, 0);

      // Textures VBO (Vertex Buffer Object)
      vboId = GL30.glGenBuffers();
      this.vboIdList.add(vboId);
      var textureCoordinatesBuffer = MemoryUtil.memAllocFloat(this.mesh.getTextureCoords().length);
      textureCoordinatesBuffer.put(0, this.mesh.getTextureCoords());
      GL30.glBindBuffer(GL30.GL_ARRAY_BUFFER, vboId);
      GL30.glBufferData(GL30.GL_ARRAY_BUFFER, textureCoordinatesBuffer, GL30.GL_STATIC_DRAW);
      GL30.glEnableVertexAttribArray(1);
      GL30.glVertexAttribPointer(1, 2, GL30.GL_FLOAT, false, 0, 0);

      // Index VBO (Vertex Buffer Object)
      vboId = GL30.glGenBuffers();
      var idxBuffer = memoryStack.callocInt(this.mesh.getIdx().length);
      idxBuffer.put(0, this.mesh.getIdx());
      GL30.glBindBuffer(GL30.GL_ELEMENT_ARRAY_BUFFER, vboId);
      GL30.glBufferData(GL30.GL_ELEMENT_ARRAY_BUFFER, idxBuffer, GL30.GL_STATIC_DRAW);

      GL30.glBindBuffer(GL30.GL_ARRAY_BUFFER, 0);
      GL30.glBindVertexArray(0);
    }
  }

  public void cleanup() {
    this.vboIdList.stream().forEach(GL30::glDeleteBuffers);
    GL30.glBindVertexArray(this.vaoId);
  }


  public int getNumVertices() {
    return this.mesh.getNumVertices();
  }

  public final int getVaoId() {
    return this.vaoId;
  }

  public String getId() {
    return this.mesh.getId();
  }
}
