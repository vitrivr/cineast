package org.vitrivr.cineast.core.data.m3d.texturemodel;

import java.util.ArrayList;
import java.util.List;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.joml.Vector3f;
import org.vitrivr.cineast.core.data.m3d.texturemodel.util.MinimalBoundingBox;

public class Mesh {

  private static final Logger LOGGER = LogManager.getLogger();
  private final int numVertices;
  private String id;

  private final float[] positions;
  //List<Vector3f> vertices;
  private final List<Vector3f> facenormals;
  //private final List<Vector3f> areas;
  private final float[] textureCoords;
  private final int[] idx;
  private final float[] normals;
  private final MinimalBoundingBox minimalBountingBox;

  public Mesh(float[] positions, float[] normals, float[] textureCoordinates, int[] idx) {
    this.positions = positions;
    this.idx = idx;
    //this.vertices = new ArrayList<>(positions.length / 3);
    this.numVertices = idx.length;
    this.normals = normals;
    this.facenormals = new ArrayList<>(this.numVertices / 3);
    //this.areas = new ArrayList<>(positions.length / 3);
    this.textureCoords = textureCoordinates;

    for (var ic = 0; ic < this.idx.length; ic += 3) {
      if (normals == null) {
        this.facenormals.add(new Vector3f(0f, 0f, 0f));
        //this.areas.add(new Vector3f(0f, 0f, 0f));
      } else {
        //get the face from idx list
        var v1 = new Vector3f(positions[idx[ic] * 3], positions[idx[ic] * 3 + 1], positions[idx[ic] * 3 + 2]);
        var v2 = new Vector3f(positions[idx[ic + 1] * 3], positions[idx[ic + 1] * 3 + 1], positions[idx[ic + 1] * 3 + 2]);
        var v3 = new Vector3f(positions[idx[ic + 2] * 3], positions[idx[ic + 2] * 3 + 1], positions[idx[ic + 2] * 3 + 2]);
        var vn1 = new Vector3f(normals[idx[ic] * 3], normals[idx[ic] * 3 + 1], normals[idx[ic] * 3 + 2]);
        var vn2 = new Vector3f(normals[idx[ic + 1] * 3], normals[idx[ic + 1] * 3 + 1], normals[idx[ic + 1] * 3 + 2]);
        var vn3 = new Vector3f(normals[idx[ic + 2] * 3], normals[idx[ic + 2] * 3 + 1], normals[idx[ic + 2] * 3 + 2]);
        var fn = new Vector3f(0, 0, 0);
        fn.add(vn1).add(vn2).add(vn3).div(3).normalize();
        var fa = new Vector3f(0, 0, 0);
        v2.sub(v1).cross(v3.sub(v1),fa);
        fa.div(2);
        this.facenormals.add(fn.mul(fa.length()));
      }
    }

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
    return this.facenormals;
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
    this.facenormals.clear();
    this.minimalBountingBox.close();
    //this.vertices.clear();
    //this.color = null;
    //this.positionsNorm = null;
    this.id = null;
    LOGGER.trace("Closing Mesh");
  }
}
