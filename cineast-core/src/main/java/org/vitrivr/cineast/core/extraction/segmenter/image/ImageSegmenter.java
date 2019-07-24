package org.vitrivr.cineast.core.extraction.segmenter.image;

import java.awt.image.BufferedImage;
import java.util.Map;

import org.vitrivr.cineast.core.data.segments.ImageSegment;
import org.vitrivr.cineast.core.data.segments.SegmentContainer;
import org.vitrivr.cineast.core.extraction.ExtractionContextProvider;
import org.vitrivr.cineast.core.extraction.segmenter.general.PassthroughSegmenter;
import org.vitrivr.cineast.core.util.ReflectionHelper;

/**
 * @author rgasser
 * @version 1.0
 * @created 17.01.17
 */
public class ImageSegmenter extends PassthroughSegmenter<BufferedImage> {
    /**
     * Constructor for {@link ImageSegmenter required for instantiation through {@link ReflectionHelper }.
     *
     * @param context The {@link ExtractionContextProvider } for the extraction context this {@link ImageSegmenter} is created in.
     */
    public ImageSegmenter(ExtractionContextProvider context) {
        super();
    }

    /**
     * Constructor for {@link ImageSegmenter required for instantiation through {@link ReflectionHelper }.
     *
     * @param context The {@link ExtractionContextProvider} for the extraction context this {@link ImageSegmenter} is created in.
     * @param properties A HashMap containing the configuration properties for {@link ImageSegmenter}
     */
    public ImageSegmenter(ExtractionContextProvider context, Map<String,String> properties) {
        super();
    }

    /**
     * @param content
     * @return
     */
    @Override
    protected SegmentContainer getSegmentFromContent(BufferedImage content) {
        return new ImageSegment(content);
    }
}
