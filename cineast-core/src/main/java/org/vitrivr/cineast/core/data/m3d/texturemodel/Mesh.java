package org.vitrivr.cineast.core.data.m3d.texturemodel;

import java.util.ArrayList;
import java.util.List;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.joml.Vector3f;
import org.vitrivr.cineast.core.data.m3d.texturemodel.util.MinimalBoundingBox;

public class Mesh {

  private static final Logger LOGGER = LogManager.getLogger();
  private Vector3f color;
  private final int numVertices;
  private String id;

  float[] positions;
  //List<Vector3f> vertices;
  List<Vector3f> normals;
  float[] textureCoords;
  int[] idx;
  float[] norms;
  MinimalBoundingBox minimalBountingBox;

  //private float scalingfactorNorm = 1;
  private Vector3f positionsNorm;


  public Mesh(float[] positions, float[] normals, float[] textureCoordinates, int[] idx) {
    this.positions = positions;
    this.norms = normals;

    //this.vertices = new ArrayList<>(positions.length / 3);

    this.normals = new ArrayList<>(positions.length / 3);
    for (var ic = 0; ic < this.positions.length; ic += 3) {
      if (normals == null) {
        this.normals.add(new Vector3f(0f, 0f, 0f));
      } else {
        this.normals.add(new Vector3f(normals[ic], normals[ic + 1], normals[ic + 2]));
      }
    }

    this.textureCoords = textureCoordinates;
    this.idx = idx;
    this.numVertices = idx.length;

    this.minimalBountingBox = new MinimalBoundingBox(this.positions);
  }

  public int getNumVertices() {
    return this.numVertices;
  }

  public float[] getPositions() {
    return this.positions;
  }

  public float[] getTextureCoords() {
    return this.textureCoords;
  }

  public int[] getIdx() {
    return this.idx;
  }

  public List<Vector3f> getNormals() {
    return this.normals;
  }

  /**
   * @return the MinimalBoundingBox which contains the scaling factor to norm and the translation to origin (0,0,0)
   */
  public MinimalBoundingBox getMinimalBoundingBox() {
    return this.minimalBountingBox;
  }

  /**
   * @return the scaling factor to norm 1 size
   * @deprecated use {@link #getMinimalBoundingBox()} instead
   */
  @Deprecated
  public float getNormalizedScalingFactor() {
    return this.minimalBountingBox.getScalingFactorToNorm();
  }

  /**
   * @return the translation to origin (0,0,0)
   * @deprecated use {@link #getMinimalBoundingBox()} instead
   */
  @Deprecated
  public Vector3f getNormalizedPosition() {
    return this.minimalBountingBox.getTranslationToNorm();
  }

  public String getId() {
    return this.id;
  }

  public void setId(int id) {
    this.id = Integer.toString(id);
  }


  public void close() {
    //this.vertices.clear();
    this.color = null;
    this.positionsNorm = null;
    this.id = null;
    LOGGER.trace("Closing Mesh");
  }
}
