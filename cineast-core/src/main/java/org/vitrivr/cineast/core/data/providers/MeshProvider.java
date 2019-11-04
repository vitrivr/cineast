package org.vitrivr.cineast.core.data.providers;

import org.vitrivr.cineast.core.data.m3d.Mesh;
import org.vitrivr.cineast.core.data.m3d.ReadableMesh;
import org.vitrivr.cineast.core.data.m3d.WritableMesh;

/**
 * @author rgasser
 * @version 1.0
 * @created 11.03.17
 */
public interface MeshProvider {
    /**
     * Returns the original 3D Mesh. Defaults to the empty mesh, if the method
     * hasn't been implemented.
     *
     * <strong>Important: </strong> The instance of the Mesh returned by this method is
     * potentially shared by different feature modules and Threads. Keep that in mind when
     * applying in-place transformations on the Mesh!
     *
     * @return Original mesh.
     */
    default ReadableMesh getMesh() {
        return Mesh.EMPTY;
    }

    /**
     * Returns a copy of the original 3D Mesh.
     *
     * @return Copy of the original 3D mesh.
     */
    default WritableMesh copyMesh() {
        return new Mesh(this.getMesh());
    }

    /**
     * Returns a version of the 3D Mesh that has been KHL-transformed. Defaults to the empty mesh,
     * if the method hasn't been implemented.
     *
     * <strong>Important: </strong> The instance of the Mesh returned by this method is potentially
     * shared by different feature modules and Threads. Keep that in mind when applying in-place
     * transformations on the Mesh!
     *
     * @return KHL transformed mesh.
     */
    default ReadableMesh getNormalizedMesh() {
        return Mesh.EMPTY;
    }

    /**
     * Returns a copy of the KHL transformed 3D Mesh.
     *
     * @return Copy of the KHL transformed 3D mesh.
     */
    default WritableMesh copyNormalizedMesh() {
        return new Mesh(this.getNormalizedMesh());
    }
}
