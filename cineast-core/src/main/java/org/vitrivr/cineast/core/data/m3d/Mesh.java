package org.vitrivr.cineast.core.data.m3d;

import org.apache.commons.math3.util.FastMath;
import org.joml.*;
import org.vitrivr.cineast.core.util.mesh.MeshMathUtil;

import java.awt.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * @author rgasser
 * @version 1.0
 * @created 29.12.16
 */
public class Mesh implements WritableMesh {

    /** The default, empty mesh. */
    public static final Mesh EMPTY = new Mesh(1,1);

    /**
     * A single vertex, which contains all the information about position, normal and color.
     */
    public class Vertex {
        /** Position of the vertex in 3D space. */
        private final Vector3f position;

        /** The vertex-normal. */
        private final Vector3f normal;

        /** Color of the vertex. */
        private final Vector3f color;

        /** List of faces the current vertex participates in. */
        private final List<Face> faces = new ArrayList<>(4);

        /**
         *
         * @param position
         */
        public Vertex(Vector3f position) {
            this(position, new Vector3f(0.0f, 0.0f, 0.0f), new Vector3f(0.0f, 0.0f, 0.0f));
        }

        /**
         *
         * @param position
         * @param normal
         * @param color
         */
        public Vertex(Vector3f position,Vector3f color, Vector3f normal) {
            this.position = position;
            this.normal = normal;
            this.color = color;
        }

        /**
         * Returns the number of faces this vertex participates in.
         *
         * @return Number of faces.
         */
        public int numberOfFaces() {
            return this.faces.size();
        }


        /**
         * Getter to the position vector.
         *
         * @return Immutable version of the position vector.
         */
        public Vector3fc getPosition() {
            return this.position;
        }

        /**
         *  Getter to the vertex normal.
         *
         * @return Immutable version of the vertex normal.
         */
        public Vector3fc getNormal() {
            return this.normal;
        }

        /**
         * Getter to the vertex color.
         *
         * @return Immutable version of the vertex normal.
         */
        public Vector3fc getColor() {
            return this.color;
        }

        /**
         * Attaches the vertex to a face, which causes the vertex-normal
         * to be re-calculated.
         *
         * @param face Face to which the vertex should be attached.
         */
        private void attachToFace(Face face) {
            if (!this.faces.contains(face)) {
                this.faces.add(face);
                this.rebuild();
            }
        }

        /**
         * Detaches the vertex from a face, which causes the vertex-normal
         * to be re-calculated.
         *
         * @param face Face to which the vertex should be attached.
         */
        private void detachFromFace(Face face) {
            if (!this.faces.contains(face)) {
                this.faces.remove(face);
                this.rebuild();
            }
        }

        /**
         * Re-calculates the vertex-normal by calculating the weighted mean of all
         * face-normals this vertex participates in.
         */
        private void rebuild() {
            this.normal.x = 0.0f;
            this.normal.y = 0.0f;
            this.normal.z = 0.0f;

            for (Face face : this.faces) {
                Vector3f fn = face.normal();
                if (!Float.isNaN(fn.x) && !Float.isNaN(fn.y) && !Float.isNaN(fn.z)) {
                    this.normal.x += fn.x / this.numberOfFaces();
                    this.normal.y += fn.y / this.numberOfFaces();
                    this.normal.z += fn.z / this.numberOfFaces();
                }
            }
        }
    }

    /**
     * A face defined that is made up by either three or four vertices.
     */
    public class Face {
        /** Type of face, defaults to TRI (= three vertices). */
        private final FaceType type;

        /** List of vertices in this face. */
        private final Vertex[] vertices;

        /** Vertex indices. */
        private final int[] vertexIndices;

        /**
         * Getter for the face's type.
         *
         * @return Type of the face.
         */
        public final FaceType getType() {
            return type;
        }

        /**
         * Constructor for a face.
         *
         * @param indices
         */
        private Face(Vector4i indices) {
            /* If the w-index is greater than -1 a QUAD face is created. */
            if (indices.w > -1) {
                this.type = FaceType.QUAD;
                this.vertices = new Vertex[4];
                this.vertexIndices = new int[4];
            } else {
                this.type = FaceType.TRI;
                this.vertices = new Vertex[3];
                this.vertexIndices = new int[3];
            }

            /* Store vertex-indices. */
            this.vertexIndices[0] = indices.x;
            this.vertexIndices[1] = indices.y;
            this.vertexIndices[2] = indices.z;
            if (this.getType() == FaceType.QUAD) {
              this.vertexIndices[3] = indices.w;
            }

            /* Add vertices to face. */
            this.vertices[0] = Mesh.this.vertices.get(this.vertexIndices[0]);
            this.vertices[1] = Mesh.this.vertices.get(this.vertexIndices[1]);
            this.vertices[2] = Mesh.this.vertices.get(this.vertexIndices[2]);
            if (this.getType() == FaceType.QUAD) {
              this.vertices[3] = Mesh.this.vertices.get(this.vertexIndices[3]);
            }

            /* Attach face to vertices. */
            this.vertices[0].attachToFace(this);
            this.vertices[1].attachToFace(this);
            this.vertices[2].attachToFace(this);
            if (this.getType() == FaceType.QUAD) {
              this.vertices[3].attachToFace(this);
            }
        }

        /**
         * Returns the list of vertices that make up this face.
         *
         * @return Unmodifiable list of vertices.
         */
        public final List<Vertex> getVertices() {
            return Arrays.asList(this.vertices);
        }

        /**
         * Calculates and returns the area of a face.
         *
         * @return Area of the face.
         */
        public double area() {
            if (this.type == FaceType.TRI) {
                /* Extract vertices. */
                Vector3f v1 = this.vertices[0].position;
                Vector3f v2 = this.vertices[1].position;
                Vector3f v3 = this.vertices[2].position;

                /* Generate the edges and sort them in ascending order. */
                List<Vector3f> edges = new ArrayList<>();
                edges.add(new Vector3f(v1).sub(v2));
                edges.add(new Vector3f(v2).sub(v3));
                edges.add(new Vector3f(v3).sub(v1));

                edges.sort((o1, o2) -> {
                    float difference = o1.length() - o2.length();
                    if (difference < 0) {
                        return -1;
                    } else if (difference > 0) {
                        return 1;
                    } else {
                        return 0;
                    }
                });

                float a = edges.get(2).length();
                float b = edges.get(1).length();
                float c = edges.get(0).length();

                /* Returns the area of the triangle according to Heron's Formula. */
                double area = 0.25 * FastMath.sqrt((a+(b+c)) * (c-(a-b)) * (c+(a-b)) * (a+(b-c)));
                if (Double.isNaN(area)) {
                    return 0.0f;
                } else {
                    return area;
                }
            } else {
                /* Extract vertices. */
                Vector3f v1 = this.vertices[0].position;
                Vector3f v2 = this.vertices[1].position;
                Vector3f v3 = this.vertices[2].position;
                Vector3f v4 = this.vertices[3].position;

                /* Calculates the area of the face using Bretschneider's Formula. */
                Vector3f s1 = new Vector3f(v1).sub(v2);
                Vector3f s2 = new Vector3f(v2).sub(v3);
                Vector3f s3 = new Vector3f(v3).sub(v4);
                Vector3f s4 = new Vector3f(v4).sub(v1);

                Vector3f d1 = new Vector3f(v1).sub(v3);
                Vector3f d2 = new Vector3f(v2).sub(v4);
                return 0.25 * FastMath.sqrt(4*FastMath.pow(d1.length(),2)*FastMath.pow(d2.length(),2) - FastMath.pow((FastMath.pow(s2.length(),2) + FastMath.pow(s4.length(),2)-FastMath.pow(s1.length(),2)-FastMath.pow(s3.length(),2)),2));
            }
        }

        /**
         * Calculates and returns the face normal.
         *
         * @return
         */
        public Vector3f normal() {
            Vector3f e1 = new Vector3f(this.vertices[1].position).sub(this.vertices[0].position);
            Vector3f e2 = new Vector3f(this.vertices[2].position).sub(this.vertices[0].position);
            return e1.cross(e2).normalize();
        }

        /**
         * Calculates and returns the centroid of the face.
         *
         * @return Centroid of the face.
         */
        public Vector3f centroid() {
            Vector3f centroid = new Vector3f(0f,0f,0f);
            for (Vertex vertex : this.vertices) {
                centroid.add(vertex.position);
            }

            if (this.type == FaceType.TRI) {
                centroid.div(3.0f);
            } else {
                centroid.div(4.0f);
            }

            return centroid;
        }
    }

    /** Enumeration used to distinguish between triangular and quadratic faces. */
    public enum FaceType {
        TRI,QUAD;
    }

    /** List of vertices in the Mesh. */
    private final List<Vertex> vertices;

    /** List of faces in the mesh. */
    private final List<Face> faces;

    /** The position of the Mesh's barycenter. Its value is lazily calculated during invocation of the @see barycenter() method. */
    private Vector3f barycenter;

    /** The surface-area of the mesh. Its value is lazily calculated during invocation of the @see surfaceArea() method. */
    private Double surfaceArea;

    /** The bounding box of the mesh. Its value is lazily calculated during invocation of the @see bounds() method. */
    private float[] boundingbox;

    /**
     * Copy constructor for Mesh.
     *
     * @param mesh Mesh that should be copied.
     */
    public Mesh(ReadableMesh mesh) {
        this(mesh.numberOfFaces(), mesh.numberOfVertices());

        for (Vertex vertex : mesh.getVertices()) {
            this.addVertex(new Vector3f(vertex.position), new Vector3f(vertex.color), new Vector3f(vertex.normal));
        }

        for (Face face : mesh.getFaces()) {
            if (face.getType() == FaceType.QUAD) {
                this.addFace(new Vector4i(face.vertexIndices[0], face.vertexIndices[1], face.vertexIndices[2], face.vertexIndices[3]));
            } else {
                this.addFace(new Vector4i(face.vertexIndices[0], face.vertexIndices[1], face.vertexIndices[2], -1));
            }
        }
    }

    /**
     * Default constructor.
     *
     * @param faces Expected number of faces (not a fixed limit).
     * @param vertices  Expected number of vertices (not a fixed limit).
     */
    public Mesh(int faces, int vertices) {
        this.faces = new ArrayList<>(faces);
        this.vertices = new ArrayList<>(vertices);
    }

    /**
     * Adds an vector defining a vertex to the Mesh.
     *
     * @param vertex
     */
    public synchronized void addVertex(Vector3f vertex) {
        this.addVertex(vertex, new Vector3f(1.0f, 1.0f, 1.0f));
    }

    /**
     * Adds an vector defining a vertex to the Mesh.
     *
     * @param vertex
     */
    public synchronized void addVertex(Vector3f vertex, Vector3f color) {
        this.addVertex(vertex, color, new Vector3f(0.0f, 0.0f, 0.0f));
    }

    /**
     * Adds an vector defining a vertex to the Mesh.
     *
     * @param vertex
     */
    public synchronized void addVertex(Vector3f vertex, Vector3f color, Vector3f normal) {
        this.vertices.add(new Vertex(vertex, color, normal));
    }

    /**
     * Adds a new triangular face to the Mesh. Faces index vertices and
     * vertex normals.
     *
     * @param vertices Vector3i containing the indices of the vertices.
     */
    @Override
    public synchronized boolean addFace(Vector3i vertices) {
        int limit = this.vertices.size();
        if (vertices.x < limit && vertices.y < limit && vertices.z < limit) {
            Mesh.Face face = new Face(new Vector4i(vertices, -1));
            this.faces.add(face);
            return true;
        } else {
            return false;
        }
    }

    /**
     * Adds a new quadratic face to the Mesh. Faces index vertices and
     * vertex normals.
     *
     * @param vertices Vector4i containing the indices of the vertices.
     */
    @Override
    public synchronized boolean addFace(Vector4i vertices) {
        int limit = this.vertices.size();
        if (vertices.x < limit && vertices.y < limit && vertices.z < limit && vertices.w < limit) {
            Mesh.Face face = new Face(vertices);
            this.faces.add(face);
            return true;
        } else {
            return false;
        }
    }

    /**
     * Accessor for an individual vertex.
     *
     * @param vertexIndex Index of the vertex that should be returned.
     * @return Vertex.
     */
    @Override
    public synchronized Vertex getVertex(int vertexIndex) {
        return this.vertices.get(vertexIndex);
    }

    /**
     * Returns the list of vertices. The returned collection is unmodifiable.
     *
     * @return Unmodifiable list of vertices.
     */
    @Override
    public List<Vertex> getVertices() {
        return Collections.unmodifiableList(this.vertices);
    }

    /**
     * Returns the list of vertex-normals. The returned collection is unmodifiable.
     *
     * @return Unmodifiable list of faces.
     */
    @Override
    public List<Face> getFaces() {
        return Collections.unmodifiableList(faces);
    }

    /**
     * Returns the number of vertices in this Mesh.
     *
     * @return Number of vertices.
     */
    @Override
    public synchronized final int numberOfVertices() {
        return this.vertices.size();
    }

    /**
     * Returns the number of faces in this Mesh.
     *
     * @return Number of faces.
     */
    @Override
    public synchronized final int numberOfFaces() {
        return this.faces.size();
    }

    /**
     * Indicates, whether the mesh is an empty Mesh or not
     *
     * @return True if mesh is empty, false otherwise.
     */
    @Override
    public synchronized final boolean isEmpty() {
        return this.faces.isEmpty();
    }

    /**
     * Returns the total surface area of the Mesh.
     *
     * @return Surface area of the mesh.
     */
    @Override
    public synchronized final double surfaceArea() {
        if (this.surfaceArea == null) {
            this.surfaceArea = 0.0;
            for (Face face : this.faces) {
                this.surfaceArea += face.area();
            }
        }
        return this.surfaceArea;
    }

    /**
     * Calculates and returns the Mesh's bounding-box
     *
     * @return Bounding-box of the mesh.
     */
    @Override
    public synchronized float[] bounds() {
        if (this.boundingbox == null) {
            this.boundingbox = MeshMathUtil.bounds(this);
        }
        return Arrays.copyOf(this.boundingbox, 6);
    }

    /**
     * Calculates and returns the Mesh's barycenter.
     *
     * @return Barycenter of the Mesh.
     */
    @Override
    public synchronized Vector3fc barycenter() {
        if (this.barycenter == null) {
            this.barycenter = MeshMathUtil.barycenter(this);
        }
        return this.barycenter;
    }

    /**
     * Moves the Mesh in the direction of the provided vector.
     *
     * @param translation Vector describing the translation in the three directions.
     */
    @Override
    public synchronized final void move(Vector3f translation) {
        Matrix4f translationMatrix = new Matrix4f().translation(translation);
        for (Mesh.Vertex v : this.vertices) {
            v.position.mulPosition(translationMatrix);
        }
        if (this.barycenter != null) {
            this.barycenter.mulPosition(translationMatrix);
        }
    }

    /**
     * Scales the Mesh by the provided factor. This will reset the surfaceArea and bounding-box property.
     *
     * @param factor Factor by which the Mesh should be scaled. Values < 1.0 will cause the Mesh to shrink.
     */
    @Override
    public synchronized final void scale(float factor) {
        Matrix4f scaling = new Matrix4f().scale(factor);
        for (Mesh.Vertex v : this.vertices) {
            v.position.mulPosition(scaling);
        }

        /* Reset surface-area and bounding box, which are not invariant under scaling. */
        this.surfaceArea = null;
        this.boundingbox = null;
    }

    /**
     * Applies a transformation matrix on the Mesh by applying it to all its vertices.
     *
     * <strong>Important: </strong> Because transformation matrices may invalidate all derived properties
     * like barycenter or surfaceArea, all this fields are reset when invoking this method.
     *
     * @param transformation Transformation matrix that should be applied.
     */
    @Override
    public synchronized final void transform(Matrix4f transformation) {
        for (Mesh.Vertex v : this.vertices) {
            v.position.mulPosition(transformation);
        }

        /* Reset surface-area and bounding box, which are not invariant under scaling. */
        this.surfaceArea = null;
        this.boundingbox = null;
        this.barycenter = null;
    }

    /**
     * Updates the color of a vertex.
     *
     * @param vertexIndex Index of the vertex that should be upadated.
     * @param color New color of the vertex.
     */
    @Override
    public synchronized void updateColor(int vertexIndex, Color color) {
        Vertex vertex = this.vertices.get(vertexIndex);
        vertex.color.x = color.getRed()/255.0f;
        vertex.color.y = color.getBlue()/255.0f;
        vertex.color.z = color.getGreen()/255.0f;
    }
}
