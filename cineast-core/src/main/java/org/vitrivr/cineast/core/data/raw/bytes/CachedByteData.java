package org.vitrivr.cineast.core.data.raw.bytes;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import org.vitrivr.cineast.core.data.raw.CacheableData;
import org.vitrivr.cineast.core.data.raw.images.CachedMultiImage;
import org.vitrivr.cineast.core.data.raw.CachedDataFactory;

import java.io.IOException;
import java.io.OutputStream;

import java.lang.ref.SoftReference;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;

/**
 * The {@link CachedByteData} object is an immutable {@link ByteData} object backed by a file cache. The data held by the {@link ByteData}
 * object may be garbage collected if memory pressure builds up and must be re-created from the cache when accessed.
 *
 * A temporary cache file is created upon constructing the {@link CachedByteData} object and holds its content in case the in-memory
 * representation gets garbage collected.
 *
 * @author Ralph Gasser
 * @version 1.1
 *
 * @see ByteData
 * @see CacheableData
 * @see CachedDataFactory
 */
public class CachedByteData implements ByteData {
    /** Logger instance used to log errors. */
    private static final Logger LOGGER = LogManager.getLogger();

    /** Reference to the {@link CachedDataFactory} that created this {@link CachedMultiImage}. */
    private final CachedDataFactory factory;

    /** The file that backs this {@link CachedByteData} object. */
    protected final Path path;

    /** Size of the {@link CachedByteData}. Because the reference to the underlying data is volatile, this value is stored separately. */
    protected final int size;

    /** ByteBuffer holding the raw data. */
    protected SoftReference<ByteBuffer> data;

    /**
     * Constructor for {@link CachedByteData} object.
     *
     * @param data The byte data with which to initialize the {@link CachedByteData} object
     * @param file The path to the file that should is supposed to hold the
     *
     * @throws IOException If unable to create a cache file.
     */
    public CachedByteData(byte[] data, Path file, CachedDataFactory factory) throws IOException {
        /* Write data to cache file. */
        try (final OutputStream stream = Files.newOutputStream(file,StandardOpenOption.CREATE, StandardOpenOption.WRITE, StandardOpenOption.TRUNCATE_EXISTING)){
            stream.write(data);
            this.path = file;
            this.size = data.length;
            this.data = new SoftReference<>(ByteBuffer.wrap(data).order(ByteOrder.LITTLE_ENDIAN));
            this.factory = factory;
        } catch (IOException e) {
            LOGGER.error("Failed to write data to cache at {}", file);
            LOGGER.error(e);
            throw e;
        }
    }

    /**
     * Returns the size in bytes of this {@link InMemoryByteData} packet.
     *
     * @return Size in bytes.
     */
    @Override
    public synchronized int size() {
        return this.size;
    }

    /**
     * Getter for {@link Path} to cache file.
     *
     * @return {@link Path} to cache file.
     */
    public Path getPath() {
        return this.path;
    }

    /**
     * Returns the {@link ByteBuffer} that backs this {@link InMemoryByteData} object. The returned buffer is
     * readonly and cannot be changed.
     *
     * @return {@link ByteBuffer} object.
     */
    @Override
    public ByteBuffer buffer() {
        ByteBuffer buffer = this.data.get();
        if (buffer == null) {
            buffer = this.resurrect();
        }
        if (buffer == null) {
            buffer = ByteBuffer.wrap(new byte[1]);
        }
        return buffer.asReadOnlyBuffer().order(ByteOrder.LITTLE_ENDIAN);
    }

    /**
     * Returns the data in this {@link InMemoryByteData} object as byte array. Directly accesses the
     * underlying byte buffer to do so.
     *
     * @return ByteData of this {@link InMemoryByteData} object.
     */
    @Override
    public byte[] array() {
        ByteBuffer buffer = this.data.get();
        if (buffer == null) {
            buffer = this.resurrect();
        }
        if (buffer == null) {
            return new byte[0];
        }
        return buffer.array();
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

    /**
     * Reads the content of this {@link CachedByteData} object from the cache file. If the cache file exists and is readable,
     * this method guarantees to return the {@link ByteBuffer} object that contains the data in the cache.
     *
     * When invoking this  method, the local soft reference to that data is also refreshed. However, there is no guarantee
     * that when invoking any of the other methods defined in the {@link ByteData} interface, that this reference is still around.
     *
     * @return {@link ByteBuffer} loaded from cache or null, if reading into the {@link ByteBuffer} failed.
     */
    protected ByteBuffer resurrect() {
        try {
            /* Allocate a new Buffer according to the size of the CachedByteData object. */
            final byte[] data = Files.readAllBytes(this.path);
            final ByteBuffer buffer = ByteBuffer.wrap(data).order(ByteOrder.LITTLE_ENDIAN);
            this.data = new SoftReference<>(buffer);

            /* Return true.*/
            return buffer;
        } catch (IOException e) {
            LOGGER.error("Failed to read data from cache at {} due to an exception. The data contained in {} is lost!", this.path, this.toString());
            LOGGER.error(e);
            return ByteBuffer.wrap(new byte[0]).order(ByteOrder.LITTLE_ENDIAN);
        }
    }
}
