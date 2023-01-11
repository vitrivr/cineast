package org.vitrivr.cineast.core.data.m3d.texturemodel;

import java.util.ArrayList;
import java.util.List;
import org.joml.Vector3f;

public class Mesh {

  private Vector3f color;
  private final int numVertices;
  private String id;

  float[] positions;
  List<Vector3f> vertices;
  float[] textureCoords;
  int[] idx;

  float scalingfactorNorm = 1;
  Vector3f positionsNorm;


  public Mesh(float[] positions, float[] textureCoordinates, int[] idx) {
    this.positions = positions;

    this.vertices = new ArrayList<>();
    var MAX = Float.MAX_VALUE;
    var MIN = -1f * Float.MAX_VALUE;
    var vPosX = new Vector3f(MIN, MIN, MIN);
    var vNegX = new Vector3f(MAX, MAX, MAX);
    var vPosY = new Vector3f(MIN, MIN, MIN);
    var vNegY = new Vector3f(MAX, MAX, MAX);
    var vPosZ = new Vector3f(MIN, MIN, MIN);
    var vNegZ = new Vector3f(MAX, MAX, MAX);
    for (var ic = 0; ic < this.positions.length; ic += 3) {
      var vec = new Vector3f(this.positions[ic], this.positions[ic + 1], this.positions[ic + 2]);
      this.vertices.add(vec);
      if (vec.x > vPosX.x) {
        vPosX = new Vector3f(vec);
      }
      if (vec.x < vNegX.x) {
        vNegX = new Vector3f(vec);
      }
      if (vec.y > vPosY.y) {
        vPosY = new Vector3f(vec);
      }
      if (vec.y < vNegY.y) {
        vNegY = new Vector3f(vec);
      }
      if (vec.z > vPosZ.z) {
        vPosZ = new Vector3f(vec);
      }
      if (vec.z < vNegZ.z) {
        vNegZ = new Vector3f(vec);
      }
    }

    var absPosition = new Vector3f((vPosX.x + vNegX.x) / 2f, (vPosY.y + vNegY.y) / 2f, (vPosZ.z + vNegZ.z) / 2f);
    var longest = new Vector3f(0, 0, 0);
    for (var vec : this.vertices) {
      var vector = new Vector3f(vec).sub(absPosition);
      if (vector.length() > longest.length()) {
        longest = vector;
      }
    }

    var dmax = longest.length()*2;
    this.scalingfactorNorm = 1f / dmax;
    this.positionsNorm = new Vector3f(absPosition.mul(this.scalingfactorNorm));

    this.textureCoords = textureCoordinates;
    this.idx = idx;
    this.numVertices = idx.length;
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

  public void setId(int id) {
    this.id = Integer.toString(id);
  }

  public float getNormalizedScalingFactor() {
    return this.scalingfactorNorm;
  }

  public Vector3f getNormalizedPosition() {
    return this.positionsNorm;
  }

  public String getId() {
    return this.id;
  }
}
