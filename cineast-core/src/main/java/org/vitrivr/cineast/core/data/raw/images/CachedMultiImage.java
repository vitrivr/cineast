package org.vitrivr.cineast.core.data.raw.images;

import org.vitrivr.cineast.core.data.raw.CacheableData;
import org.vitrivr.cineast.core.data.raw.bytes.ByteData;
import org.vitrivr.cineast.core.data.raw.bytes.CachedByteData;
import org.vitrivr.cineast.core.data.raw.CachedDataFactory;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.lang.ref.SoftReference;
import java.nio.ByteBuffer;
import java.nio.file.Path;

/**
 * The {@link CachedMultiImage} object is an immutable  representation of a {@link BufferedImage} backed by a file cache. The data held
 * by the {@link CachedMultiImage} object may be garbage collected if memory pressure builds up and must be re-created from the cache when accessed.
 *
 * A temporary cache file is created upon constructing the {@link CachedMultiImage} object and holds its content in case the in-memory
 * representation gets garbage collected.
 *
 * @author Ralph Gasser
 * @version 1.0
 *
 * @see MultiImage
 * @see CacheableData
 * @see CachedDataFactory
 */
public class CachedMultiImage extends CachedByteData implements MultiImage {

    /** The width of the cached {@link MultiImage}. */
    private final int width;

    /** The height of the cached {@link MultiImage}. */
    private final int height;

    /** The height of the cached {@link MultiImage}. */
    private final int type;

    /** Soft reference to the thumbnail image. May be garbage collected under memory pressure. */
    private SoftReference<BufferedImage> thumb;

    /** Reference to the {@link CachedDataFactory} that created this {@link CachedMultiImage}. */
    private final CachedDataFactory factory;

    /**
     * Constructor for {@link CachedMultiImage}.
     *
     * @param img {@link BufferedImage} to create a {@link CachedMultiImage} from.
     * @param cacheFile The cache file in which to store {@link CachedMultiImage}.
     * @throws IOException If creation of the cache file failed.
     */
    public CachedMultiImage(BufferedImage img, Path cacheFile, CachedDataFactory factory) throws IOException {
        this(img, null, cacheFile, factory);
    }

    /**
     * Constructor for {@link CachedMultiImage}.
     *
     * @param img {@link BufferedImage} to create a {@link CachedMultiImage} from.
     * @param thumb {@link BufferedImage} holding the thumbnail image.
     * @param cacheFile The cache file in which to store {@link CachedMultiImage}.
     * @throws IOException If creation of the cache file failed.
     */
    public CachedMultiImage(BufferedImage img, BufferedImage thumb, Path cacheFile, CachedDataFactory factory) throws IOException {
        super(toBytes(img), cacheFile, factory);
        this.width = img.getWidth();
        this.height = img.getHeight();
        this.type = img.getType();
        this.factory = factory;
        if (thumb != null) {
            this.thumb = new SoftReference<>(thumb);
        } else {
            this.thumb = new SoftReference<>(MultiImage.generateThumb(img));
        }
    }

    /**
     * Constructor for {@link CachedMultiImage}.
     *
     * @param colors The array holding the colors of the original, {@link BufferedImage}.
     * @param width Width of the image.
     * @param height Height of the image.
     *  @throws IOException If creation of the cache file failed.
     */
    public CachedMultiImage(int[] colors, int width, int height, Path cacheFile, CachedDataFactory factory) throws IOException {
        super(toBytes(colors, width, height), cacheFile, factory);

        this.width = width;
        this.height = height;
        this.type = BufferedImage.TYPE_INT_RGB;
        this.factory = factory;

        final BufferedImage bimg = new BufferedImage(this.width, this.height, this.type);
        bimg.setRGB(0, 0, this.width, this.height, colors, 0, this.width);
        this.thumb = new SoftReference<>(MultiImage.generateThumb(bimg));
    }

    /**
     * Getter for the thumbnail image of this {@link CachedMultiImage}. If the thumbnail image reference does not
     * exist anymore, a new thumbnail image will be created from the raw data.
     *
     * Calling this method will cause the soft reference {@link CachedMultiImage#thumb} to be refreshed! However, there is
     * no guarantee that the reference will still be around when invoking this or any other accessor the next time.
     *
     * @return The thumbnail image for this {@link CachedMultiImage}
     */
    @Override
    public BufferedImage getThumbnailImage() {
        BufferedImage thumbnail = this.thumb.get();
        if (thumbnail == null) {
            thumbnail = MultiImage.generateThumb(this.getBufferedImage());
        }
        this.thumb = new SoftReference<>(thumbnail);
        return thumbnail;
    }

    /**
     * Getter for the colors array representing this {@link CachedMultiImage}.
     *
     * @return The thumbnail image for this {@link CachedMultiImage}
     */
    @Override
    public int[] getColors() {
        final ByteBuffer buffer = this.buffer();
        final int[] colors = new int[this.width * this.height];
        for (int i=0; i<colors.length; i++) {
            colors[i] = buffer.getInt();
        }
        return colors;
    }

    /**
     * Getter for the {@link BufferedImage} held by this {@link CachedMultiImage}. The image is reconstructed from the the
     * color array. See {@link CachedMultiImage#getColors()}
     *
     * @return The image held by this  {@link CachedMultiImage}
     */
    @Override
    public BufferedImage getBufferedImage() {
        int[] colors = getColors();
        final BufferedImage image = new BufferedImage(this.width, this.height, this.type);
        image.setRGB(0, 0, this.width, this.height, colors, 0, this.width);
        return image;
    }

    /**
     * Getter for the colors array representing the thumbnail of this {@link CachedMultiImage}.
     *
     * @return Color array
     */
    @Override
    public int[] getThumbnailColors() {
        final BufferedImage thumb = this.getThumbnailImage();
        return this.getThumbnailImage().getRGB(0, 0, thumb.getWidth(), thumb.getHeight(), null, 0, thumb.getWidth());
    }

    /**
     * Getter for width value.
     *
     * @return Width of the {@link MultiImage}
     */
    @Override
    public int getWidth() {
        return this.width;
    }

    /**
     * Getter for height value.
     *
     * @return Height of the {@link MultiImage}
     */
    @Override
    public int getHeight() {
        return this.height;
    }

    /**
     * Getter for this {@link CachedDataFactory}.
     *
     * @return Factory that created this {@link InMemoryMultiImage}
     */
    @Override
    public CachedDataFactory factory() {
        return this.factory;
    }

    /**
     * Force clears all the {@link SoftReference}s associated with this {@link CachedMultiImage} object.
     */
    @Override
    public void clear() {
        this.data.clear();
        this.thumb.clear();
    }

    /**
     * Converts the {@link BufferedImage} to a byte array representation.
     *
     * @param img The {@link BufferedImage} that should be converted.
     * @return The byte array representing the {@link BufferedImage}
     */
    private static byte[] toBytes(BufferedImage img) {
        int[] colors = img.getRGB(0, 0, img.getWidth(), img.getHeight(), null, 0, img.getWidth());
        return toBytes(colors, img.getWidth(),img.getHeight());
    }

    /**
     * Converts the int array holding the colors of a {@link BufferedImage} to a byte array representation.
     *
     * @param colors The int array holding the color values.
     * @param width Width of the image.
     * @param height Height of the image.
     * @return The byte array representing the {@link BufferedImage}
     */
    private static byte[] toBytes(int[] colors, int width, int height) {
        final ByteBuffer data = ByteBuffer.allocate(width * height * 4);
        for (int c : colors) {
            data.putInt(c);
        }
        return data.array();
    }
}