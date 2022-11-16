package org.vitrivr.cineast.core.render.lwjgl.model;

import java.util.ArrayList;
import java.util.List;
import org.joml.Vector3f;
import org.lwjgl.opengl.GL30;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.system.MemoryUtil;

public class Mesh {

  private Vector3f color;
  private final int numVertices;
  private final List<Integer> vboIdList;
  private final int vaoId;

  public static final Mesh EMPTY = new Mesh(new float[0], new float[0], new int[0]);


  // TODO: remove from model GL30 dependency
  public Mesh(float[] positions, float[] textureCoordinates,  int[] idx) {
    this.numVertices = idx.length;

    this.vboIdList = new ArrayList<>();


    try (var memoryStack = MemoryStack.stackPush()) {

      this.vaoId = GL30.glGenVertexArrays();
      GL30.glBindVertexArray(this.vaoId);

      // Positions VBO
      int vboId = GL30.glGenBuffers();
      this.vboIdList.add(vboId);
      var positionsBuffer = memoryStack.callocFloat(positions.length);
      positionsBuffer.put(0, positions);
      GL30.glBindBuffer(GL30.GL_ARRAY_BUFFER, vboId);
      GL30.glBufferData(GL30.GL_ARRAY_BUFFER, positionsBuffer, GL30.GL_STATIC_DRAW);
      GL30.glEnableVertexAttribArray(0);
      GL30.glVertexAttribPointer(0, 3, GL30.GL_FLOAT, false, 0, 0);

      // Textures VBO (Vertex Buffer Object)
      vboId = GL30.glGenBuffers();
      this.vboIdList.add(vboId);
      var textureCoordinatesBuffer = MemoryUtil.memAllocFloat(textureCoordinates.length);
      textureCoordinatesBuffer.put(0, textureCoordinates);
      GL30.glBindBuffer(GL30.GL_ARRAY_BUFFER, vboId);
      GL30.glBufferData(GL30.GL_ARRAY_BUFFER, textureCoordinatesBuffer, GL30.GL_STATIC_DRAW);
      GL30.glEnableVertexAttribArray(1);
      GL30.glVertexAttribPointer(1, 2, GL30.GL_FLOAT, false, 0, 0);

      // Index VBO (Vertex Buffer Object)
      vboId = GL30.glGenBuffers();
      var idxBuffer = memoryStack.callocInt(idx.length);
      idxBuffer.put(0, idx);
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
    return this.numVertices;
  }

  public final int getVaoId() {
    return this.vaoId;
  }

}
