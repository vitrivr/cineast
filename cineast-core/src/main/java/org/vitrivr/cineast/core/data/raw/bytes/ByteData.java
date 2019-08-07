package org.vitrivr.cineast.core.data.raw.bytes;

import org.vitrivr.cineast.core.data.raw.CacheableData;

import java.nio.ByteBuffer;


/**
 * This interface represents an object that holds arbitrary, immutable data that can be stored in a ByteBuffer.
 * The kind of data is implementation specific as are the pattern used to access the data.
 *
 * @author Ralph Gasser
 * @version 1.0
 */
public interface ByteData extends CacheableData {
    /**
     * Returns the size in bytes of this {@link ByteData} packet.
     *
     * @return Size in bytes.
     */
    int size();

    /**
     * Returns a {@link ByteBuffer} that backs this {@link ByteData} object. The {@link ByteBuffer} is supposed to be
     * read-only and it's position is supposed to be 0.
     *
     * @return Read-only {@link ByteBuffer}.
     */
    ByteBuffer buffer();

    /**
     * Returns the data in this {@link ByteData} object as byte array.
     *
     * @return Data of this {@link ByteData} object.
     */
    byte[] array();
}


