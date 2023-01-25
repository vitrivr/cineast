package org.vitrivr.cineast.core.data.m3d.texturemodel;

import java.util.ArrayList;
import java.util.List;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.joml.Vector3f;

public class Mesh {

  private static final Logger LOGGER = LogManager.getLogger();
  private Vector3f color;
  private final int numVertices;
  private String id;

  float[] positions;
  List<Vector3f> vertices;
  List<Vector3f> normals;
  float[] textureCoords;
  int[] idx;
  float[] norms;

  private float scalingfactorNorm = 1;
  private Vector3f positionsNorm;


  public Mesh(float[] positions, float[] normals, float[] textureCoordinates, int[] idx) {
    this.positions = positions;
    this.norms = normals;

    this.vertices = new ArrayList<>(positions.length / 3);

    this.normals = new ArrayList<>(positions.length / 3);
    for (var ic = 0; ic < this.positions.length; ic += 3) {
      if (normals == null) {
        this.normals.add(new Vector3f(0f, 0f, 0f));
      } else {
        this.normals.add(new Vector3f(normals[ic], normals[ic + 1], normals[ic + 2]));
      }
    }

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

    var dmax = longest.length() * 2;
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

  public List<Vector3f> getNormals() {
    return this.vertices;
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


  public void close() {
    this.vertices.clear();
    this.color = null;
    this.positionsNorm = null;
    this.id = null;
    LOGGER.trace("Closing Mesh");
  }
}
