package org.vitrivr.cineast.core.data.m3d.texturemodel;

import java.util.ArrayList;
import java.util.List;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.joml.Vector3f;
import org.vitrivr.cineast.core.data.m3d.texturemodel.util.MinimalBoundingBox;

/**
 * The Mesh is the geometric representation of a model.
 * It contains the vertices, faces, normals and texture coordinates.
 * It also constructs the face normals and the minimal bounding box.
 */
public class Mesh {

  private static final Logger LOGGER = LogManager.getLogger();

  /**
   * Number of all vertices in the mesh
   */
  private final int numVertices;

  /**
   * Id of the mesh
   */
  private String id;

  /**
   * List of all vertices in the mesh
   * The positions are flatterned vectors
   * positions[0] = x
   * positions[1] = y
   * positions[2] = z
   * positions[3] = x
   * ...
   */
  private final float[] positions;

  /**
   * List of all face normals in the mesh
   * The length of the normals describes the area of the face
   * The direction of the normals describes the direction of the face and points outwards
   */
  private final List<Vector3f> facenormals;

  /**
   * List of all texture coordinates in the mesh
   */
  private final float[] textureCoords;

  /**
   * Flattered list of all vertices ids.
   * A three tuple describes a face.
   * e.g  0, 1, 3, 3, 1, 2,
   * face1 = (0, 1, 3)
   * face2 = (3, 1, 2)
   */
  private final int[] idx;

  /**
   * List of all vertices normals in the mesh
   * TODO: not used yet, will be used for vertex shading
   */
  @SuppressWarnings("FieldCanBeLocal")
  private final float[] normals;

  /**
   * MinimalBoundingBox that encloses the mesh
   */
  private final MinimalBoundingBox minimalBountingBox;

  /**
   * Constructor for Mesh.
   * Arrays are flattened vectors.
   * e.g. positions[0] = x
   *     positions[1] = y
   *     positions[2] = z
   *     positions[3] = x
   *     ...
   *
   * @param positions List of all vertices in the mesh
   * @param normals List of all vertices normals in the mesh
   * @param textureCoordinates List of all texture coordinates in the mesh
   * @param idx List of all vertices ids.
   */
  public Mesh(float[] positions, float[] normals, float[] textureCoordinates, int[] idx) {
    //Stors all the data
    this.positions = positions;
    this.idx = idx;
    this.numVertices = idx.length;
    this.normals = normals;
    // List to store results of face normals calculation
    this.facenormals = new ArrayList<>(this.numVertices / 3);
    //this.areas = new ArrayList<>(positions.length / 3);
    this.textureCoords = textureCoordinates;

    // Calculate face normals
    // ic incerements by 3 because a face is defined by 3 vertices
    for (var ic = 0; ic < this.idx.length; ic += 3) {
      if (normals == null) {
        // Add zero vector if there are no vertex normals
        this.facenormals.add(new Vector3f(0f, 0f, 0f));
      } else {
        // Get the three vertices of the face
        var v1 = new Vector3f(positions[idx[ic] * 3], positions[idx[ic] * 3 + 1], positions[idx[ic] * 3 + 2]);
        var v2 = new Vector3f(positions[idx[ic + 1] * 3], positions[idx[ic + 1] * 3 + 1], positions[idx[ic + 1] * 3 + 2]);
        var v3 = new Vector3f(positions[idx[ic + 2] * 3], positions[idx[ic + 2] * 3 + 1], positions[idx[ic + 2] * 3 + 2]);
        // Get the three vertices normals of the face
        var vn1 = new Vector3f(normals[idx[ic] * 3], normals[idx[ic] * 3 + 1], normals[idx[ic] * 3 + 2]);
        var vn2 = new Vector3f(normals[idx[ic + 1] * 3], normals[idx[ic + 1] * 3 + 1], normals[idx[ic + 1] * 3 + 2]);
        var vn3 = new Vector3f(normals[idx[ic + 2] * 3], normals[idx[ic + 2] * 3 + 1], normals[idx[ic + 2] * 3 + 2]);
        // Instanciate the face normal
        var fn = new Vector3f(0, 0, 0);
        // Calculate the direction of the face normal by averaging the three vertex normals
        fn.add(vn1).add(vn2).add(vn3).div(3).normalize();
        // Instanciate the face area
        var fa = new Vector3f(0, 0, 0);
        // Calculate the area of the face by calculating the cross product of the two edges and dividing by 2
        v2.sub(v1).cross(v3.sub(v1),fa);
        fa.div(2);
        // Add the face normal to the list of face normals
        this.facenormals.add(fn.mul(fa.length()));
      }
    }
    // Calculate the minimal bounding box
    this.minimalBountingBox = new MinimalBoundingBox(this.positions);
  }

  /**
   *
   * @return returns the number of vertices in the mesh
   */
  public int getNumVertices() {
    return this.numVertices;
  }

/**
   * @return the flattened array of all positions
   */
  public float[] getPositions() {
    return this.positions;
  }

  /**
   * @return the flattened array of all texture coordinates
   */
  public float[] getTextureCoords() {
    return this.textureCoords;
  }

  /**
   * @return the flattened array of all vertices ids
   * A three tuple describes a face.
   * e.g  0, 1, 3, 3, 1, 2,
   * face1 = (0, 1, 3)
   * face2 = (3, 1, 2)
   */
  public int[] getIdx() {
    return this.idx;
  }

  /**
   * @return list containing all face normals
   */
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

  /**
   * @return the id of the mesh
   */
  public String getId() {
    return this.id;
  }

  /**
   * @param id sets the id of the mesh
   */
  public void setId(int id) {
    this.id = Integer.toString(id);
  }


  /**
   * closes the mesh
   * releases all resources
   */
  public void close() {
    this.facenormals.clear();
    this.minimalBountingBox.close();
    this.id = null;
    LOGGER.trace("Closing Mesh");
  }
}
