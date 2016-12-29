package org.vitrivr.cineast.core.data.m3d;

import com.jogamp.opengl.GL2;
import org.joml.Vector3f;

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
    private List<Vector3f> vertices = new ArrayList<>();
    private List<Vector3f> normals = new ArrayList<>();
    private List<Face> faces = new ArrayList<>();

    public void addVertex(Vector3f vertex) {
        this.vertices.add(vertex);
    }

    public void addNormal(Vector3f vertex) {
        this.normals.add(vertex);
    }

    public void addFace(Face face) {
        this.faces.add(face);
    }

    public List<Vector3f> getVertices() {
        return Collections.unmodifiableList(vertices);
    }

    public List<Vector3f> getNormals() {
        return Collections.unmodifiableList(normals);
    }

    public List<Face> getFaces() {
        return Collections.unmodifiableList(faces);
    }

    /**
     *
     * @return
     */
    public int render(GL2 gl) {
        int meshList = gl.glGenLists(1);
        gl.glNewList(meshList, GL_COMPILE);
        {
            for (Face face : this.faces) {

                if (face.getType() == FaceType.TRI) {
                    gl.glBegin(GL_TRIANGLES);
                    {
                        if (face.getNormalIndices() != null) {
                            Vector3f n1 = this.normals.get(face.getNormalIndices().x - 1);
                            gl.glNormal3f(n1.x, n1.y, n1.z);
                        }
                        Vector3f v1 = this.vertices.get(face.getVertexIndices().x - 1);
                        gl.glVertex3f(v1.x, v1.y, v1.z);

                        if (face.getNormalIndices() != null) {
                            Vector3f n2 = this.normals.get(face.getNormalIndices().y - 1);
                            gl.glNormal3f(n2.x, n2.y, n2.z);
                        }
                        Vector3f v2 = this.vertices.get(face.getVertexIndices().y - 1);
                        gl.glVertex3f(v2.x, v2.y, v2.z);

                        if (face.getNormalIndices() != null) {
                            Vector3f n3 = this.normals.get(face.getNormalIndices().z - 1);
                            gl.glNormal3f(n3.x, n3.y, n3.z);
                        }
                        Vector3f v3 = this.vertices.get(face.getVertexIndices().z - 1);
                        gl.glVertex3f(v3.x, v3.y, v3.z);
                    }
                    gl.glEnd();
                } else if (face.getType() == FaceType.QUAD) {
                    gl.glBegin(GL_QUADS);
                    {
                        if (face.getNormalIndices() != null) {
                            Vector3f n1 = this.normals.get(face.getNormalIndices().x - 1);
                            gl.glNormal3f(n1.x, n1.y, n1.z);
                        }
                        Vector3f v1 = this.vertices.get(face.getVertexIndices().x - 1);
                        gl.glVertex3f(v1.x, v1.y, v1.z);

                        if (face.getNormalIndices() != null) {
                            Vector3f n2 = this.normals.get(face.getNormalIndices().y - 1);
                            gl.glNormal3f(n2.x, n2.y, n2.z);
                        }
                        Vector3f v2 = this.vertices.get(face.getVertexIndices().y - 1);
                        gl.glVertex3f(v2.x, v2.y, v2.z);

                        if (face.getNormalIndices() != null) {
                            Vector3f n3 = this.normals.get(face.getNormalIndices().z - 1);
                            gl.glNormal3f(n3.x, n3.y, n3.z);
                        }
                        Vector3f v3 = this.vertices.get(face.getVertexIndices().z - 1);
                        gl.glVertex3f(v3.x, v3.y, v3.z);

                        if (face.getNormalIndices() != null) {
                            Vector3f n4 = this.normals.get(face.getNormalIndices().w - 1);
                            gl.glNormal3f(n4.x, n4.y, n4.z);
                        }
                        Vector3f v4 = this.vertices.get(face.getVertexIndices().w - 1);
                        gl.glVertex3f(v4.x, v3.y, v3.z);
                    }
                    gl.glEnd();
                }
            }
        }
        gl.glEndList();
        return meshList;
    }
}
