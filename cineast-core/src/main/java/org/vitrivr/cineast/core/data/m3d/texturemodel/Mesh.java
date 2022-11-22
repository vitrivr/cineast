package org.vitrivr.cineast.core.data.m3d.texturemodel;

import org.joml.Vector3f;

public class Mesh {

  private Vector3f color;
  private final int numVertices;
  private String id;

  float[] positions;
  float[] textureCoords;
  int[] idx;



  public Mesh(float[] positions, float[] textureCoordinates,  int[] idx) {
    this.positions = positions;
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
  public String getId() {
    return this.id;
  }
}
