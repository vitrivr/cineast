package org.vitrivr.cineast.core.data.providers;

import org.vitrivr.cineast.core.data.m3d.Mesh;

/**
 * @author rgasser
 * @version 1.0
 * @created 11.03.17
 */
public interface MeshProvider {

    /**
     * Returns a 3D Mesh. Defaults to the empty mesh, if not implemented.
     *
     * @return Mesh
     */
    default Mesh getMesh() {
        return Mesh.EMPTY;
    }
}
