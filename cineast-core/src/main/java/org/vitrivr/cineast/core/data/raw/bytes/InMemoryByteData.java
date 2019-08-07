package org.vitrivr.cineast.core.data.raw.bytes;

import org.vitrivr.cineast.core.data.raw.CacheableData;
import org.vitrivr.cineast.core.data.raw.images.CachedMultiImage;
import org.vitrivr.cineast.core.data.raw.CachedDataFactory;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

/**
 * The {@link InMemoryByteData} object is an immutable {@link ByteData} object that holds all its data in-memory. The memory
 * will be occupied until the {@link InMemoryByteData} is garbage collected.
 *
 * @author Ralph Gasser
 * @version 1.0
 *
 * @see ByteData
 * @see CacheableData
 * @see CachedDataFactory
 */
public class InMemoryByteData implements ByteData {

    /** Reference to the {@link CachedDataFactory} that created this {@link CachedMultiImage}. */
    private final CachedDataFactory factory;

    /** ByteBuffer holding the raw data. */
    private final ByteBuffer data;

    /**
     * Constructor for {@link InMemoryByteData} object.
     *
     * @param data The byte data with which to initialize the {@link InMemoryByteData} object
     */
    public InMemoryByteData(byte[] data, CachedDataFactory factory) {
        this.data = ByteBuffer.wrap(data).order(ByteOrder.LITTLE_ENDIAN);
        this.factory = factory;
    }

    /**
     * Returns the size in bytes of this {@link InMemoryByteData} packet.
     *
     * @return Size in bytes.
     */
    @Override
    public int size() {
        return this.data.capacity();
    }

    /**
     * Returns the {@link ByteBuffer} that backs this {@link InMemoryByteData} object.
     *
     * @return {@link ByteBuffer} object.
     */
    @Override
    public ByteBuffer buffer() {
        final ByteBuffer returnBuffer = this.data.asReadOnlyBuffer().order(ByteOrder.LITTLE_ENDIAN);
        returnBuffer.position(0);
        return returnBuffer;
    }

    /**
     * Returns the data in this {@link InMemoryByteData} object as byte array. Directly accesses the
     * underlying byte buffer to do so.
     *
     * @return ByteData of this {@link InMemoryByteData} object.
     */
    @Override
    public byte[] array() {
        return this.data.array();
    }

    /**
     * Exposes the {@link CachedDataFactory} that created this instance of {@link CachedByteData}.
     *
     * @return {@link CachedDataFactory} reference.
     */
    @Override
    public CachedDataFactory factory() {
        return this.factory;
    }
}