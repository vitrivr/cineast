package org.vitrivr.cineast.core.extraction.segmenter.image;

import org.vitrivr.cineast.core.data.raw.CachedDataFactory;
import org.vitrivr.cineast.core.data.segments.ImageSegment;
import org.vitrivr.cineast.core.data.segments.SegmentContainer;
import org.vitrivr.cineast.core.extraction.ExtractionContextProvider;
import org.vitrivr.cineast.core.extraction.segmenter.general.PassthroughSegmenter;
import org.vitrivr.cineast.core.extraction.segmenter.general.Segmenter;
import org.vitrivr.cineast.core.util.ReflectionHelper;

import java.awt.image.BufferedImage;
import java.util.Map;

/**
 * A {@link Segmenter} that converts a {@link BufferedImage} into a {@link SegmentContainer}.
 *
 * @see Segmenter
 * @see PassthroughSegmenter
 *
 * @author rgasser
 * @version 1.0
 */
public class ImageSegmenter extends PassthroughSegmenter<BufferedImage> {
    /** THe {@link CachedDataFactory} that is used to create {@link org.vitrivr.cineast.core.data.raw.images.MultiImage}s. */
    private final CachedDataFactory factory;

    /**
     * Constructor for {@link ImageSegmenter required for instantiation through {@link ReflectionHelper }.
     *
     * @param context The {@link ExtractionContextProvider } for the extraction context this {@link ImageSegmenter} is created in.
     */
    public ImageSegmenter(ExtractionContextProvider context) {
        super();
        this.factory = context.cacheConfig().sharedCachedDataFactory();
    }

    /**
     * Constructor for {@link ImageSegmenter required for instantiation through {@link ReflectionHelper }.
     *
     * @param context The {@link ExtractionContextProvider} for the extraction context this {@link ImageSegmenter} is created in.
     * @param properties A HashMap containing the configuration properties for {@link ImageSegmenter}
     */
    public ImageSegmenter(ExtractionContextProvider context, Map<String,String> properties) {
        super();
        this.factory = context.cacheConfig().sharedCachedDataFactory();
    }

    /**
     * Creates a new {@link SegmentContainer} from a {@link BufferedImage}.
     *
     * @param content The {@link BufferedImage} to extract a {@link SegmentContainer} from.
     * @return {@link SegmentContainer}
     */
    @Override
    protected SegmentContainer getSegmentFromContent(BufferedImage content) {
        return new ImageSegment(content, this.factory);
    }
}
