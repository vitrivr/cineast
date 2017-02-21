package org.vitrivr.cineast.core.decode.m3d;

import org.vitrivr.cineast.core.data.m3d.Mesh;

/**
 * Interface used by all mesh decoders. Those decoders translate arbitrary mesh formats, be they in memory
 * or on disk, into a Mesh object that can be used by Cineast.
 *
 * @author rgasser
 * @version 1.0
 * @created 28.12.16
 */
public interface MeshDecoder {
    /**
     * Decodes an arbitrary mesh representation (i.e. a 3D model file) into a Mesh object.
     *
     * @see Mesh
     *
     * @return Mesh representing the decoded mesh.
     * @throws MeshDecoderException If an error occurred during decoding.
     */
    Mesh getMesh() throws MeshDecoderException;
}
