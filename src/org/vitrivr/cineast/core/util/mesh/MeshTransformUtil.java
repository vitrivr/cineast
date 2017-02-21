package org.vitrivr.cineast.core.util.mesh;

import org.ddogleg.struct.Tuple2;
import org.ejml.data.DenseMatrix64F;
import org.ejml.factory.DecompositionFactory;
import org.ejml.interfaces.decomposition.EigenDecomposition;
import org.joml.Matrix3f;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.vitrivr.cineast.core.data.m3d.Mesh;

import java.util.*;

/**
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
     *
     */
    public static void normalize(Mesh mesh, float size) {
        MeshTransformUtil.scale(mesh, size);
        Vector3f barycenter = MeshMathUtil.barycenter(mesh);
        Matrix3f cov = new Matrix3f();
        List<Vector3f> vertices = mesh.getVertices();
        int amount = vertices.size();
        for (Vector3f v : vertices) {
            Vector3f vm = (new Vector3f(v)).sub(barycenter).div(amount);
            cov.add(new Matrix3f(
                vm.x * vm.x, vm.y * vm.x, vm.z * vm.x,
                vm.y * vm.x, vm.y * vm.y, vm.y * vm.z,
                vm.z * vm.x, vm.z * vm.y, vm.z * vm.z
             ));
        }

        List<Tuple2<Float,Vector3f>> eigenvectors = MeshTransformUtil.getEigenvectors(cov);
        int i = 0;
        Matrix3f A = new Matrix3f();
        for (Tuple2<Float,Vector3f> tuple2 : eigenvectors) {
            A.setColumn(i, tuple2.data1.normalize());
            i++;
        }

        for (Vector3f vertex : vertices) {
            vertex.sub(barycenter).mul(A);
        }
    }


    /**
     *
     * @param mesh
     */
    public static void scale(Mesh mesh, float size) {
        float[] bounds = MeshMathUtil.bounds(mesh);
        float max = Math.max(bounds[0]-bounds[1], Math.max(bounds[2]-bounds[3], bounds[4]-bounds[5]));
        float factor = size/max;
        Matrix4f scaling = new Matrix4f().scale(factor);
        for (Vector3f vertex : mesh.getVertices()) {
            vertex.mulDirection(scaling);
        }
    }

    /**
     *
     * @param mesh
     */
    public static void center(Mesh mesh) {
        Vector3f center = MeshMathUtil.barycenter(mesh);
        Matrix4f translation = new Matrix4f().translation(center.negate());
        for (Vector3f vertex : mesh.getVertices()) {
            vertex.mulPosition(translation);
        }
    }

    /**
     *
     * @param matrix
     * @return
     */
    private static List<Tuple2<Float,Vector3f>> getEigenvectors(Matrix3f matrix) {
        List<Tuple2<Float,Vector3f>> eigenvectors = new ArrayList<>();
        double[][] covfloat = {
          {matrix.m00, matrix.m10, matrix.m20},
          {matrix.m01, matrix.m11, matrix.m21},
          {matrix.m02, matrix.m12, matrix.m22}
        };

        DenseMatrix64F covDenseMatrix = new DenseMatrix64F(covfloat);
        EigenDecomposition<DenseMatrix64F> eig = DecompositionFactory.eig(3, true);
        eig.decompose(covDenseMatrix);

        int eigValNum = eig.getNumberOfEigenvalues();
        for(int i = 0; i < eigValNum; i++){
            DenseMatrix64F eigMat = eig.getEigenVector(i);
            if(eigMat != null){
                eigenvectors.add(new Tuple2<>((float)eig.getEigenvalue(i).getReal(), new Vector3f((float)eigMat.get(0,0), (float)eigMat.get(1,0), (float)eigMat.get(2,0))));
            }
        }

        eigenvectors.sort(Comparator.comparing(e -> e.data0));

        return eigenvectors;
    }
}
