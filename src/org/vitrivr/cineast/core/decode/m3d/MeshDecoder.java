package org.vitrivr.cineast.core.decode.m3d;

import org.vitrivr.cineast.core.data.m3d.Mesh;

/**
 * @author rgasser
 * @version 1.0
 * @created 28.12.16
 */
public interface MeshDecoder {
    Mesh getMesh() throws MeshDecoderException;
}
