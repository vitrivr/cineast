package org.vitrivr.cineast.core.extraction.decode.image;

import java.awt.image.BufferedImage;
import java.util.LinkedList;

/**
 *
 */
public final class ImageSequence extends LinkedList<BufferedImage> {
    /** Name of this {@link ImageSequence}. */
    private final String name;

    /**
     * Default constructor for {@link ImageSequence}.
     *
     * @param name  Name of this {@link ImageSequence}.
     */
    public ImageSequence(String name) {
        this.name = name;
    }
}
