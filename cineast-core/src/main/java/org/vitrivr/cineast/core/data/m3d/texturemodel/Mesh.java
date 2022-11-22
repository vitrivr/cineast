package org.vitrivr.cineast.core.data.m3d.texturemodel;

import org.joml.Vector3f;

public class Mesh {

  private Vector3f color;
  private final int numVertices;
  private String id;

  float[] positions;
  float[] textureCoords;
  int[] idx;

  float scalingfactorNorm = 1;


  public Mesh(float[] positions, float[] textureCoordinates, int[] idx) {
    this.positions = positions;
    var minX = 0f;
    var maxX = 0f;
    var minY = 0f;
    var maxY = 0f;
    var minZ = 0f;
    var maxZ = 0f;
    for (var ic = 0; ic < this.positions.length; ic += 3
    ) {
      minX = Math.min(minX, positions[ic]);
      maxX = Math.max(maxX, positions[ic]);
      minY = Math.min(minY, positions[ic]);
      maxY = Math.max(maxY, positions[ic]);
      minZ = Math.min(minZ, positions[ic]);
      maxZ = Math.max(maxZ, positions[ic]);
    }
    var dmax = Math.max(maxX - minX, Math.max(maxY - minY, maxZ - minZ));
    this.scalingfactorNorm = 1f/ dmax;

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

  public float getNormalizesScalingFactor(){
    return  this.scalingfactorNorm;
  }

  public String getId() {
    return this.id;
  }
}
