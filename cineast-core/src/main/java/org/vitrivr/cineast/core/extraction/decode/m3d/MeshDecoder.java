package org.vitrivr.cineast.core.extraction.decode.m3d;

import org.vitrivr.cineast.core.data.m3d.Mesh;
import org.vitrivr.cineast.core.extraction.decode.general.Decoder;

/**
 * Interface used by all mesh decoders. Those decoders translate arbitrary mesh formats, be they in memory or on disk, into a Mesh object that can be used by Cineast.
 */
public interface MeshDecoder extends Decoder<Mesh> {

}
