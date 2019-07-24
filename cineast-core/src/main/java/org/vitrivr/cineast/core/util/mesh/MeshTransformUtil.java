package org.vitrivr.cineast.core.util.mesh;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import org.ejml.data.DMatrixRMaj;
import org.ejml.dense.row.factory.DecompositionFactory_DDRM;
import org.ejml.interfaces.decomposition.EigenDecomposition_F64;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.joml.Vector3fc;
import org.vitrivr.cineast.core.data.Pair;
import org.vitrivr.cineast.core.data.m3d.Mesh;
import org.vitrivr.cineast.core.data.m3d.ReadableMesh;
import org.vitrivr.cineast.core.data.m3d.WritableMesh;

/**
 * A collection of utilities surrounding transformation of Meshes. Includes methods to scale, move, center or apply the
 * Karhunen–Loève (KHL) Transformation [1] for pose normalisation.
 *
 * [1] Vranic, D. V., Saupe, D., & Richter, J. (2001). Tools for 3D-object retrieval: Karhunen-Loeve transform and\nspherical harmonics.
 *  2001 IEEE Fourth Workshop on Multimedia Signal Processing (Cat. No.01TH8564), 0–5. http://doi.org/10.1109/MMSP.2001.962749
 *
 * @author rgasser
 * @version 1.0
 * @created 08.01.17
 */
public final class MeshTransformUtil {
    /**
     * Private constructor; cannot be instantiated.
     */
    private MeshTransformUtil() {}

    /**
     * Performs a Karhunen–Loève (KHL) Transformation on the provided Mesh by executing the following steps
     * according to [1]:
     *
     * <ol>
     *     <li>Move the Mesh's barycenter to the origin. </li>
     *     <li>Rotate the Mesh's so that its PCA axis becomes aligned with the coordinate-system's axis. </li>
     *     <li>Scale the mesh (usually to unit-size).</li>
     * </ol>
     *
     * This transformation is done on a copy of the Mesh, which is returned. The original Mesh is not affected by the operation.
     *
     * The KHL Transform is a default tool for pose estimation.
     *
     * @param mesh Mesh that should be transformed.
     * @param size Length of the longest edge of the mesh's bounding box (for scaling).
     * @return KHL transformed copy of the original Mesh
     */
    public static Mesh khlTransform(Mesh mesh, float size) {
        Mesh copy = new Mesh(mesh);
        khlTransformInPlace(copy, size);
        return copy;
    }

    /**
     * Performs a Karhunen–Loève (KHL) Transformation on the provided Mesh by executing the following steps
     * according to [1]:
     *
     * <ol>
     *     <li>Move the Mesh's barycenter to the origin. </li>
     *     <li>Rotate the Mesh's so that its PCA axis becomes aligned with the coordinate-system's axis. </li>
     *     <li>Scale the mesh (usually to unit-size).</li>
     * </ol>
     *
     * The KHL Transform is a default tool for pose estimation.
     *
     * <strong>Important:</strong> This transformation is done in place and affects the original Mesh.
     *
     * @param mesh Mesh that should be transformed.
     * @param size Length of the longest edge of the mesh's bounding box (for scaling).
     */
    public static void khlTransformInPlace(WritableMesh mesh, float size) {
        /* Check if Mesh is empty. */
        if (mesh.isEmpty()) {
          return;
        }

         /* 1) Scale the mesh. */
        MeshTransformUtil.scaleInPlace(mesh, size);

        /* 2) Rotate the mesh along its PCA axis. */
        Vector3fc barycenter = mesh.barycenter();

        /* Prepare an empty covariance matrix. */
        DMatrixRMaj covariance = new DMatrixRMaj(3,3);

        List<Mesh.Face> faces = mesh.getFaces();
        long vertices = 0;
        for (Mesh.Face face : faces) {
            for (Mesh.Vertex v : face.getVertices()) {
                Vector3f vm = (new Vector3f(v.getPosition())).sub(barycenter);
                covariance.add(0,0, vm.x * vm.x);
                covariance.add(0,1, vm.y * vm.x);
                covariance.add(0,2, vm.z * vm.x);
                covariance.add(1,0, vm.y * vm.x);
                covariance.add(1,1, vm.y * vm.y);
                covariance.add(1,2, vm.y * vm.z);
                covariance.add(2,0, vm.z * vm.x);
                covariance.add(2,1, vm.z * vm.y);
                covariance.add(2,2, vm.z * vm.z);
                vertices++;
            }
        }

        /* Normalizes the matrix. */
        for (int i=0; i<covariance.data.length; i++) {
            covariance.data[i] /= vertices;
        }

        /* 2a) List of eigenvectors sorted by eigenvalue in descending order. */
        List<Pair<Float,Vector3f>> eigenvectors = MeshTransformUtil.getEigenvectors(covariance);

        Matrix4f rotation = new Matrix4f();

        /* 2b) Apply first rotation: Largest spread should occur along the x-axis. */
        Vector3f xaxis = new Vector3f(1.0f,0.0f,0.0f);
        Vector3f yaxis = new Vector3f(0.0f,1.0f,0.0f);
        float angleX = xaxis.angle(eigenvectors.get(2).second);
        rotation.rotate(angleX, xaxis.cross(eigenvectors.get(2).second));

        /* 2c) Apply second rotation: Second largest spread should occur along the y-axis. */
        float angleY = yaxis.angle(eigenvectors.get(1).second);
        rotation.rotate(angleY, yaxis.cross(eigenvectors.get(1).second));
        mesh.transform(rotation);

        /* 3) Center the mesh. */
        MeshTransformUtil.centerInPlace(mesh);
    }

    /**
     * Scales the provided Mesh so that the length of the largest edge of its bounding-box
     * equals to the provided size. This transformation is done on a copy of the Mesh, which is
     * returned. The original Mesh is not affected by the operation.
     *
     * @param mesh Mesh that should be scaled.
     * @param size Length of the longest edge of the mesh's bounding box.
     * @return Scaled copy of the original Mesh
     */
    public static Mesh scale(ReadableMesh mesh, float size) {
        Mesh copy = new Mesh(mesh);
        scaleInPlace(copy, size);
        return copy;
    }

    /**
     * Scales the provided Mesh so that the length of the largest edge of its bounding-box
     * equals to the provided size.
     *
     *  <strong>Important:</strong> This transformation is done in place and affects the original Mesh.
     *
     * @param mesh Mesh that should be scaled.
     * @param size Length of the longest edge of the mesh's bounding box.
     */
    public static void scaleInPlace(WritableMesh mesh, float size) {
        if (mesh.isEmpty()) {
          return;
        }
        float[] bounds = MeshMathUtil.bounds(mesh);
        float max = Math.max(bounds[0]-bounds[1], Math.max(bounds[2]-bounds[3], bounds[4]-bounds[5]));
        mesh.scale(size/max);
    }

    /**
     * Moves the provided Mesh so that its barycenter is positioned at the origin [0,0,0]. This
     * transformation is done on a copy of the Mesh, which is returned. The original Mesh is not
     * affected by the operation.
     *
     * @param mesh Mesh that should be centered.
     * @return Centered copy of the original Mesh
     */
    public static Mesh center(ReadableMesh mesh) {
        Mesh copy = new Mesh(mesh);
        centerInPlace(copy);
        return copy;
    }

    /**
     * Moves the provided Mesh so that its barycenter is positioned at the origin [0,0,0].
     *
     *<strong>Important:</strong> This transformation is done in place and affects the original Mesh.
     *
     * @param mesh Centered Mesh.
     */
    public static void centerInPlace(WritableMesh mesh) {
        if (mesh.isEmpty()) {
          return;
        }
        Vector3f center = new Vector3f(mesh.barycenter());
        mesh.move(center.negate());
    }

    /**
     * Calculates a List of eigenvalues and eigenvectors from the provided 3x3 (covariance) matrix.
     *
     * @param matrix 3x3 Matrix to derive the eigenvalues and eigenvectors from.
     * @return List of pairs containing both the eigenvalues and the eigenvectors. The entries are
     * sorted in ascending order of the eigenvalue.
     */
    private static List<Pair<Float,Vector3f>> getEigenvectors(DMatrixRMaj matrix) {
        List<Pair<Float,Vector3f>> eigenvectors = new ArrayList<>();

        EigenDecomposition_F64<DMatrixRMaj> eig = DecompositionFactory_DDRM.eig(3, true);
        eig.decompose(matrix);

        int eigValNum = eig.getNumberOfEigenvalues();
        for(int i = 0; i < eigValNum; i++){
            DMatrixRMaj eigMat = eig.getEigenVector(i);
            if(eigMat != null){
                eigenvectors.add(new Pair<>((float)eig.getEigenvalue(i).getReal(), new Vector3f((float)eigMat.get(0,0), (float)eigMat.get(1,0), (float)eigMat.get(2,0))));
            }
        }

        eigenvectors.sort(Comparator.comparing(e -> e.first));

        return eigenvectors;
    }
}
