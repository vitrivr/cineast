package org.vitrivr.cineast.core.data.raw.images;

import java.awt.image.BufferedImage;
import java.lang.ref.SoftReference;

import org.vitrivr.cineast.core.data.raw.CacheableData;
import org.vitrivr.cineast.core.data.raw.CachedDataFactory;

/**
 * The {@link InMemoryMultiImage} object is an immutable representation of a {@link BufferedImage} that holds all its data in-memory.
 * The memory will be occupied until the {@link InMemoryMultiImage} is garbage collected.
 *
  * @version 1.0
 *
 * @see MultiImage
 * @see CacheableData
 * @see CachedDataFactory
 */
public class InMemoryMultiImage implements MultiImage {

    /** Thumbnail image. This reference will remain in memory as long as {@link InMemoryMultiImage} does. */
    private SoftReference<BufferedImage> thumb;

    /** Reference to the colors array of the image. */
    private int[] colors;

    /** The width of the cached {@link MultiImage}. */
    private final int width;

    /** The height of the cached {@link MultiImage}. */
    private final int height;

    /** The height of the cached {@link MultiImage}. */
    private final int type;

    /** Reference to the {@link CachedDataFactory} that created this {@link InMemoryMultiImage}. */
    private final CachedDataFactory factory;

    /**
     * Constructor for {@link InMemoryMultiImage}.
     *
     * @param img {@link BufferedImage} to create a {@link InMemoryMultiImage} from.
     */
    public InMemoryMultiImage(BufferedImage img, CachedDataFactory factory){
        this(img, null, factory);
    }

    /**
     * Constructor for {@link InMemoryMultiImage}.
     *
     * @param img {@link BufferedImage} to create a {@link InMemoryMultiImage} from.
     * @param thumb {@link BufferedImage} holding the thumbnail image.
     */
    public InMemoryMultiImage(BufferedImage img, BufferedImage thumb, CachedDataFactory factory) {
        this.colors = img.getRGB(0, 0, img.getWidth(), img.getHeight(), null, 0, img.getWidth());
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
     * Getter for the {@link BufferedImage} held by this {@link InMemoryMultiImage}. The image is reconstructed from the the
     * color array. See {@link InMemoryMultiImage#getColors()}
     *
     * @return The image held by this  {@link InMemoryMultiImage}
     */
    @Override
    public BufferedImage getBufferedImage() {
        final BufferedImage image = new BufferedImage(this.width, this.height, this.type);
        image.setRGB(0, 0, this.width, this.height, this.colors, 0, this.width);
        return image;
    }

    /**
     * Getter for the thumbnail image of this {@link InMemoryMultiImage}. If the thumbnail image reference does not
     * exist anymore, a new thumbnail image will be created from the original image.
     *
     * @return The thumbnail image for this {@link InMemoryMultiImage}
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
     * Getter for the colors array representing this {@link InMemoryMultiImage}.
     *
     * @return The thumbnail image for this {@link InMemoryMultiImage}
     */
    @Override
    public int[] getColors() {
        return this.colors;
    }

    /**
     * Getter for the colors array representing the thumbnail of this {@link InMemoryMultiImage}.
     *
     * @return Color array of the thumbnail image.
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
     * @return Width of the {@link MultiImage}
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

    @Override
    public void clear() {}
}
