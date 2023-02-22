package org.vitrivr.cineast.core.render.lwjgl.glmodel;

import java.util.ArrayList;
import java.util.List;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.lwjgl.opengl.GL30;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.system.MemoryUtil;
import org.vitrivr.cineast.core.data.m3d.texturemodel.Mesh;

/**
 * The GlMesh class is a wrapper for the {@link Mesh} class.
 * <ul>
 * <li>Mesh -> GlMesh( Mesh )</li>
 * </ul>
 * <p>
 * The purpose is to bring the generic Mesh in an OpenGl context
 * {@link Mesh} -> {@link GlMesh}
 */
public class GlMesh {

  private static final Logger LOGGER = LogManager.getLogger();

  /**
   * The wrapped generic mesh in gl context
   */
  private final Mesh mesh;
  /**
   * The list of <i>Vertex Buffer Object</i> (VBO) ids
   */
  private final List<Integer> vboIdList;
  /**
   * The <i>Vertex Array Object</i> (VAO) id
   */
  private final int vaoId;


  /**
   * Creates a new GlMesh from a mesh.
   * <ol>
   *   <li>Bind Vertex Array Object</li>
   *   <li>Generate, allocate and initialize Vertex (Positions) Buffer</li>
   *   <li>Generate, allocate and initialize Texture Coordinates Buffer</li>
   *   <li>Generate, allocate and initialize Index Buffer</li>
   *   <li>Unbind Vertex Array Object</li>
 *   </ol>
   * @param mesh The mesh that is wrapped by this gl mesh.
   */
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
      this.vboIdList.add(vboId);
      var idxBuffer = memoryStack.callocInt(this.mesh.getIdx().length);
      idxBuffer.put(0, this.mesh.getIdx());
      GL30.glBindBuffer(GL30.GL_ELEMENT_ARRAY_BUFFER, vboId);
      GL30.glBufferData(GL30.GL_ELEMENT_ARRAY_BUFFER, idxBuffer, GL30.GL_STATIC_DRAW);

      GL30.glBindBuffer(GL30.GL_ARRAY_BUFFER, 0);
      GL30.glBindVertexArray(0);
    }
  }

  /**
   * Cleans up the gl mesh and calls all underlying cleanup methods.
   * Removes only the references to VBOs and VAOs.
   * Removes the <i>Vertex Array Object</i> (VAO) and all <i>Vertex Buffer Object</i> (VBO) ids.
   */
  public void cleanup() {
    this.vboIdList.forEach(GL30::glDeleteBuffers);
    GL30.glDeleteVertexArrays(this.vaoId);
    this.vboIdList.clear();
    LOGGER.trace("Cleaned-up GlMesh");
  }

  /**
   * Returns the number of vertices of the wrapped generic mesh.
   * @return The number of vertices of the wrapped generic mesh.
   */
  public int getNumVertices() {
    return this.mesh.getNumVertices();
  }

  /**
   * Returns the <i>Vertex Array Object</i> (VAO) id.
   * @return The <i>Vertex Array Object</i> (VAO) id.
   */
  public final int getVaoId() {
    return this.vaoId;
  }

  /**
   * Returns the Id of the wrapped generic mesh.
   * @return The Id of the wrapped generic mesh.
   */
  public String getId() {
    return this.mesh.getId();
  }
}
