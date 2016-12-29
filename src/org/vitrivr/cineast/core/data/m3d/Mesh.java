package org.vitrivr.cineast.core.data.m3d;

import com.jogamp.opengl.GL2;
import org.joml.Vector3f;
import org.joml.Vector3i;
import org.joml.Vector4i;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static com.jogamp.opengl.GL.GL_TRIANGLES;
import static com.jogamp.opengl.GL2.GL_COMPILE;
import static com.jogamp.opengl.GL2ES3.GL_QUADS;

/**
 * @author rgasser
 * @version 1.0
 * @created 29.12.16
 */
public class Mesh {
    /**
     * A face defined by the normal and the vertex-indices.
     */
    public class Face {
        private FaceType type;
        private Vector4i vertexIndices;
        private Vector4i normalIndices;

        public final FaceType getType() {
            return type;
        }
        public final Vector4i getVertexIndices() {
            return vertexIndices;
        }
        public final Vector4i getNormalIndices() {
            return normalIndices;
        }
    }

    /**
     * Enumeration used to distinguish between triangle and quadratic faces.
     */
    public enum FaceType {
        TRI,QUAD
    }

    private List<Vector3f> vertices = new ArrayList<>();
    private List<Vector3f> normals = new ArrayList<>();
    private List<Face> faces = new ArrayList<>();

    /**
     * Adds an vector defining a vertex to the Mesh.
     *
     * @param vertex
     */
    public void addVertex(Vector3f vertex) {
        this.vertices.add(vertex);
    }

    /**
     * Adds a vector defining a vertex normal to the Mesh.
     *
     * @param normal Vector defining the vertex-normal.
     */
    public void addNormal(Vector3f normal) {
        this.normals.add(normal);
    }

    /**
     * Adds a new triangular face to the Mesh. Faces index vertices and
     * vertex normals.
     *
     * @param vertices Vector3i containing the indices of the vertices.
     * @param normals Vector3i containing the indices of the normal vectors. May be null.
     */
    public void addFace(Vector3i vertices, Vector3i normals) {
        Mesh.Face face = new Face();
        face.type = FaceType.TRI;
        face.vertexIndices = new Vector4i(vertices, 0);
        if (normals!= null) face.normalIndices = new Vector4i(normals, 0);
        this.faces.add(face);
    }

    /**
     * Adds a new quadratic face to the Mesh.  Faces index vertices and
     * vertex normals.
     *
     * @param vertices Vector4i containing the indices of the vertices.
     * @param normals Vector4i containing the indices of the normal vectors. May be null.
     */
    public void addFace(Vector4i vertices, Vector4i normals) {
        Mesh.Face face = new Face();
        face.type = FaceType.QUAD;
        face.vertexIndices = vertices;
        face.normalIndices = normals;
        this.faces.add(face);
    }

    /**
     * Returns the list of vertices. The returned collection is unmodifiable.
     *
     * @return Unmodifiable list of vertices.
     */
    public List<Vector3f> getVertices() {
        return Collections.unmodifiableList(vertices);
    }

    /**
     * Returns the list of vertex-normals. The returned collection is unmodifiable.
     *
     * @return Unmodifiable list of vertex normals.
     */
    public List<Vector3f> getNormals() {
        return Collections.unmodifiableList(normals);
    }

    /**
     * Returns the list of vertex-normals. The returned collection is unmodifiable.
     *
     * @return Unmodifiable list of faces.
     */
    public List<Face> getFaces() {
        return Collections.unmodifiableList(faces);
    }

    /**
     * Assembles a mesh into a new glDisplayList. The method returns a handle for this
     * newly created glDisplayList. To actually render the list - by executing the commands it contains -
     * the glCallList function must be called!
     *
     * IMPORTANT: The glDisplayList bust be deleted once its not used anymore by calling glDeleteLists

     * @return Handle for the newly created glDisplayList.
     */
    public int assemble(GL2 gl) {
        int meshList = gl.glGenLists(1);
        gl.glNewList(meshList, GL_COMPILE);
        {
            for (Face face : this.faces) {

                if (face.type == FaceType.TRI) {
                    gl.glBegin(GL_TRIANGLES);
                    {
                        if (face.normalIndices != null) {
                            Vector3f n1 = this.normals.get(face.normalIndices.x - 1);
                            gl.glNormal3f(n1.x, n1.y, n1.z);
                        }
                        Vector3f v1 = this.vertices.get(face.vertexIndices.x - 1);
                        gl.glVertex3f(v1.x, v1.y, v1.z);

                        if (face.normalIndices != null) {
                            Vector3f n2 = this.normals.get(face.normalIndices.y - 1);
                            gl.glNormal3f(n2.x, n2.y, n2.z);
                        }
                        Vector3f v2 = this.vertices.get(face.vertexIndices.y - 1);
                        gl.glVertex3f(v2.x, v2.y, v2.z);

                        if (face.normalIndices != null) {
                            Vector3f n3 = this.normals.get(face.normalIndices.z - 1);
                            gl.glNormal3f(n3.x, n3.y, n3.z);
                        }
                        Vector3f v3 = this.vertices.get(face.vertexIndices.z - 1);
                        gl.glVertex3f(v3.x, v3.y, v3.z);
                    }
                    gl.glEnd();
                } else if (face.type == FaceType.QUAD) {
                    gl.glBegin(GL_QUADS);
                    {
                        if (face.normalIndices != null) {
                            Vector3f n1 = this.normals.get(face.normalIndices.x - 1);
                            gl.glNormal3f(n1.x, n1.y, n1.z);
                        }
                        Vector3f v1 = this.vertices.get(face.vertexIndices.x - 1);
                        gl.glVertex3f(v1.x, v1.y, v1.z);

                        if (face.normalIndices != null) {
                            Vector3f n2 = this.normals.get(face.normalIndices.y - 1);
                            gl.glNormal3f(n2.x, n2.y, n2.z);
                        }
                        Vector3f v2 = this.vertices.get(face.vertexIndices.y - 1);
                        gl.glVertex3f(v2.x, v2.y, v2.z);

                        if (face.normalIndices != null) {
                            Vector3f n3 = this.normals.get(face.normalIndices.z - 1);
                            gl.glNormal3f(n3.x, n3.y, n3.z);
                        }
                        Vector3f v3 = this.vertices.get(face.vertexIndices.z - 1);
                        gl.glVertex3f(v3.x, v3.y, v3.z);

                        if (face.normalIndices != null) {
                            Vector3f n4 = this.normals.get(face.normalIndices.w - 1);
                            gl.glNormal3f(n4.x, n4.y, n4.z);
                        }
                        Vector3f v4 = this.vertices.get(face.vertexIndices.w - 1);
                        gl.glVertex3f(v4.x, v4.y, v4.z);
                    }
                    gl.glEnd();
                }
            }
        }
        gl.glEndList();
        return meshList;
    }
}
