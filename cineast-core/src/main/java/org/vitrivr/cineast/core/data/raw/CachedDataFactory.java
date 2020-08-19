package org.vitrivr.cineast.core.data.raw;

import static java.lang.Thread.MIN_PRIORITY;

import java.lang.ref.PhantomReference;
import java.lang.ref.Reference;
import java.lang.ref.ReferenceQueue;
import java.util.HashSet;
import java.util.concurrent.locks.ReentrantLock;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import org.vitrivr.cineast.core.config.CacheConfig;
import org.vitrivr.cineast.core.data.raw.bytes.ByteData;
import org.vitrivr.cineast.core.data.raw.bytes.CachedByteData;
import org.vitrivr.cineast.core.data.raw.bytes.InMemoryByteData;
import org.vitrivr.cineast.core.data.raw.images.CachedMultiImage;
import org.vitrivr.cineast.core.data.raw.images.InMemoryMultiImage;
import org.vitrivr.cineast.core.data.raw.images.MultiImage;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;


/**
 * This factory class generates {@link ByteData} objects either in memory or backed by a file cache based on a heuristic involving
 * the size of the allocated data chunks.
 *
 * @author Ralph Gasser
 * @version 1.1
 *
 * @see ByteData
 * @see InMemoryByteData
 * @see CachedByteData
 * @see InMemoryMultiImage
 * @see CachedMultiImage
 */
public class CachedDataFactory {
    private static CachedDataFactory defaultInstance = new CachedDataFactory(new CacheConfig());

    /** Default instance of {@link CachedDataFactory}. */
    public static CachedDataFactory getDefault() {
      return defaultInstance;
    }

    public static void configureDefault(CacheConfig cacheConfig) {
      defaultInstance = new CachedDataFactory(cacheConfig);
    }

    /** A {@link ReentrantLock} to mediate access to CACHED_REFS. */
    private static final ReentrantLock CACHED_REFS_LOCK = new ReentrantLock();

    /** Internal set that keeps track of {@link CachedByteData} objects; prevents garbage collection of those references. */
    private static final HashSet<Reference<CachedByteData>> CACHED_REFS = new HashSet<>();

    /** {@link ReferenceQueue} for {@link CachedByteData} objects. */
    private static final ReferenceQueue<CachedByteData> CACHED_REF_QUEUE = new ReferenceQueue<>();

    /** Logger instance used to log errors. */
    private static final Logger LOGGER = LogManager.getLogger();

    /** Thread that cleans the cached byte data files. */
    private static final Thread CLEANER_THREAD = new Thread(() -> {
        while (true) {
            try {
                final Reference<?> ref = CACHED_REF_QUEUE.remove();

                /* Synchronization. */
                CACHED_REFS_LOCK.lock();
                CACHED_REFS.remove(ref);
                CACHED_REFS_LOCK.unlock();

                if (ref instanceof CachedByteDataReference) {
                    final Path path = ((CachedByteDataReference) ref).path;
                    try {
                        Files.deleteIfExists(path);
                        LOGGER.trace("Temporary cache file was purged: {}", path.toString());
                    } catch (IOException e) {
                        LOGGER.fatal("Failed to delete temporary cache file: {}", path.toString());
                    }
                }
            } catch (InterruptedException e) {
                LOGGER.fatal("Cleaner thread was interrupted. Cached objects backed by disk will no longer be purged!");
            }
        }
    });

    static {
        CLEANER_THREAD.setName("Cache-File-Cleaner");
        CLEANER_THREAD.setPriority(MIN_PRIORITY);
        CLEANER_THREAD.setDaemon(true);
        CLEANER_THREAD.start();
    }

    /** Reference to {@link CacheConfig} used to setup this {@link CachedDataFactory}. */
    private final CacheConfig config;

    /** Location where this instance of {@link CachedDataFactory} will store its cached images. */
    private final Path cacheLocation;

    /** Keeps track of whether the cache directory has been created yet */
    private boolean cacheDirectoryCreated = false;

    /**
     * Inner {@link PhantomReference} implementations that keeps track of the cache path for every {@link CachedByteData}.
     */
    private static class CachedByteDataReference extends PhantomReference<CachedByteData> {
        private final Path path;
        private CachedByteDataReference(CachedByteData data) {
            super(data, CachedDataFactory.CACHED_REF_QUEUE);
            CACHED_REFS_LOCK.lock();
            CACHED_REFS.add(this);
            CACHED_REFS_LOCK.unlock();
            this.path = data.getPath();
        }
        public Path getPath() {
            return path;
        }
    }

    /**
     * Default constructor.
     *
     * @param config {@link CacheConfig} reference.
     */
    public CachedDataFactory(CacheConfig config){
        this.config = config;
        this.cacheLocation = this.config.getCacheLocation().resolve("cineast_cache_" + config.getUUID());
    }

    /** Method to lazily create the cache directory exists */
    private void ensureDirectory() {
        if (this.cacheDirectoryCreated) {
            return;
        }
        this.cacheDirectoryCreated = true;
        try {
            Files.createDirectories(this.cacheLocation);
            Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                try {
                    Files.walk(CachedDataFactory.this.cacheLocation).sorted(Comparator.reverseOrder()).map(Path::toFile).forEach(File::delete);
                } catch (IOException e) {
                    LOGGER.fatal("Failed to sweep the cache location under {}", this.cacheLocation.toAbsolutePath().toString());
                }
            })) ;
        } catch (IOException e) {
            LOGGER.fatal("Failed to create the cache location under {}", this.cacheLocation.toAbsolutePath().toString());
        }
    }

    /**
     * Wraps the provided byte array in a {@link ByteData} object and returns it. This method determines whether to use a
     * {@link InMemoryByteData} or a {@link CachedByteData} object based on the size of the data object and global
     * application settings.
     *
     * @param data The data that should be wrapped.
     * @return {@link ByteData} object.
     */
    public ByteData newByteData(byte[] data) {
        if (this.config.keepInMemory(data.length)) {
            return newInMemoryData(data);
        } else {
            return newCachedData(data, "default");
        }
    }

    /**
     * Wraps the provided byte array in a {@link ByteData} object and returns it. This method determines whether to use a
     * {@link InMemoryByteData} or a {@link CachedByteData} object based on the size of the data object and global
     * application settings.
     *
     * @param data Size of the data object.
     * @param prefix The string prefix used to denote the cache file.
     * @return {@link ByteData} object.
     */
    public ByteData newByteData(byte[] data, String prefix) {
        if (this.config.keepInMemory(data.length)) {
            return newInMemoryData(data);
        } else {
            return newCachedData(data, prefix);
        }
    }

    /**
     * Wraps the provided byte array in a {@link InMemoryByteData} object and returns it.
     *
     * @param data The data that should be wrapped.
     * @return {@link ByteData} object.
     */
    public ByteData newInMemoryData(byte[] data) {
        return new InMemoryByteData(data, this);
    }

    /**
     * Wraps the provided byte array in a {@link CachedByteData} object and returns it. If for some reason, allocation
     * of the {@link CachedByteData} fails, an {@link InMemoryByteData} will be returned instead.
     *
     * @param data The data that should be wrapped.
     * @return {@link ByteData} object.
     * @throws UncheckedIOException If the allocation of the {@link CachedByteData} fails and FORCE_DISK_CACHE cache policy is used.
     */
    public ByteData newCachedData(byte[] data, String prefix) {
        ensureDirectory();
        final CacheConfig.Policy cachePolicy = this.config.getCachingPolicy();
        final Path cacheLocation = this.config.getCacheLocation();
        try {
            return new CachedByteData(data, Files.createTempFile(cacheLocation, prefix, ".tmp"), this);
        } catch (IOException e) {
            LOGGER.warn("Failed to instantiate an object of type CachedByteDate. Fallback to InMemoryByteData instead.");
            return new InMemoryByteData(data, this);
        }
    }

    /**
     * Creates a new {@link MultiImage} from a {@link BufferedImage}.
     *
     * @param bimg The {@link BufferedImage} to created the {@link MultiImage} from.
     * @return {@link CachedMultiImage} or {@link InMemoryMultiImage}, depending on cache settings and memory utilisation.
     */
    public MultiImage newMultiImage(BufferedImage bimg) {
        return newMultiImage(bimg, null);
    }

    /**
     * Creates a new {@link MultiImage} from a {@link BufferedImage} and its thumbnail.
     *
     * @param bimg The {@link BufferedImage} to created the {@link MultiImage} from.
     * @param thumb The {@link BufferedImage} representing the thumbnail.
     * @return {@link CachedMultiImage} or {@link InMemoryMultiImage}, depending on cache settings and memory utilisation.
     */
    public MultiImage newMultiImage(BufferedImage bimg, BufferedImage thumb) {
        if (this.config.keepInMemory(bimg.getWidth() * bimg.getHeight() * 3 * 8)) {
            return new InMemoryMultiImage(bimg, thumb, this);
        } else {
            return newCachedMultiImage(bimg, thumb, "img");
        }
    }

    /**
     * Creates a new {@link MultiImage} from raw color data.
     *
     * @param width Width of the image.
     * @param height Height of the image.
     * @param colors Array of color values.
     * @return {@link CachedMultiImage} or {@link InMemoryMultiImage}, depending on cache settings and memory utilisation.
     */
    public MultiImage newMultiImage(int width, int height, int[] colors) {
        height = MultiImage.checkHeight(width, height, colors);
        final BufferedImage bimg = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        bimg.setRGB(0, 0, width, height, colors, 0, width);
        if (this.config.keepInMemory(colors.length * 8)) {
            return newInMemoryMultiImage(bimg);
        } else {
            return newCachedMultiImage(bimg, "img");
        }
    }


    /**
     * Creates a new {@link InMemoryMultiImage} from raw color data.
     *
     * @param width Width of the image.
     * @param height Height of the image.
     * @param colors Array of color values.
     * @return {@link InMemoryMultiImage}
     */
    public MultiImage newInMemoryMultiImage(int width, int height, int[] colors) {
        height = MultiImage.checkHeight(width, height, colors);
        final BufferedImage bimg = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        bimg.setRGB(0, 0, width, height, colors, 0, width);
        return newInMemoryMultiImage(bimg);
    }

    /**
     * Wraps the provided  {@link BufferedImage} in a {@link InMemoryMultiImage} object and returns it.
     *
     * @param bimg The {@link BufferedImage} that should be wrapped.
     * @return {@link InMemoryMultiImage}
     */
    public MultiImage newInMemoryMultiImage(BufferedImage bimg) {
        return new InMemoryMultiImage(bimg, this);
    }

    /**
     * Creates and returns a new {@link CachedMultiImage} from the provided image using the provided cache prefix. If the
     * instantiation of the {@link CachedMultiImage} fails, the method is allowed to fallback to a {@link InMemoryMultiImage}
     *
     * @param image The image from which to create a {@link CachedMultiImage}.
     * @param prefix The cache prefix used to name the files.
     * @return {@link CachedMultiImage} or {@link InMemoryMultiImage}, if former could not be created.
     */
    public MultiImage newCachedMultiImage(BufferedImage image, String prefix) {
        ensureDirectory();
        try {
            final CachedMultiImage cimg = new CachedMultiImage(image, Files.createTempFile(this.cacheLocation, prefix, ".tmp"), this);
            new CachedByteDataReference(cimg); /* Enqueue phantom reference for garbage collection. */
            return cimg;
        } catch (IOException e) {
            LOGGER.warn("Failed to instantiate an object of type CachedMultiImage. Fallback to InMemoryMultiImage instead.");
            return new InMemoryMultiImage(image, this);
        }
    }

    /**
     * Creates and returns a new {@link CachedMultiImage} from the provided image using the provided cache prefix. If the
     * instantiation of the {@link CachedMultiImage} fails, the method is allowed to fallback to a {@link InMemoryMultiImage}
     *
     * @param image The image from which to create a {@link CachedMultiImage}.
     * @param thumb Pre-computed thumbnail that should be used.
     * @param prefix The cache prefix used to name the files.
     * @return {@link CachedMultiImage} or  {@link InMemoryMultiImage}, if former could not be created.
     */
    public MultiImage newCachedMultiImage(BufferedImage image, BufferedImage thumb, String prefix) {
        ensureDirectory();
        try {
            final CachedMultiImage cimg = new CachedMultiImage(image, thumb, Files.createTempFile(this.cacheLocation, prefix, ".tmp"), this);
            new CachedByteDataReference(cimg); /* Enqueue phantom reference for garbage collection. */
            return cimg;
        } catch (IOException e) {
            LOGGER.warn("Failed to instantiate an object of type CachedMultiImage. Fallback to InMemoryMultiImage instead.");
            return new InMemoryMultiImage(image, thumb, this);
        }
    }
}
