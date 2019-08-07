package org.vitrivr.cineast.standalone.run.filehandler;


import org.vitrivr.cineast.core.data.segments.ImageSegment;
import org.vitrivr.cineast.core.data.segments.SegmentContainer;
import org.vitrivr.cineast.core.extraction.ExtractionContextProvider;
import org.vitrivr.cineast.core.extraction.decode.general.Decoder;
import org.vitrivr.cineast.core.extraction.decode.image.DefaultImageDecoder;
import org.vitrivr.cineast.core.extraction.segmenter.general.PassthroughSegmenter;
import org.vitrivr.cineast.core.extraction.segmenter.general.Segmenter;
import org.vitrivr.cineast.standalone.config.Config;
import org.vitrivr.cineast.standalone.run.ExtractionContainerProvider;

import java.awt.image.BufferedImage;
import java.io.IOException;

/**
 * @author rgasser
 * @version 1.0
 * @created 14.01.17
 */
@Deprecated
public class ImageExtractionFileHandler extends AbstractExtractionFileHandler<BufferedImage> {

    /**
     * Default constructor.
     *
     * @param
     */
    public ImageExtractionFileHandler(ExtractionContainerProvider provider, ExtractionContextProvider context) throws IOException {
        super (provider,context);
    }

    /**
     * Returns a new instance of  Decoder<BufferedImage>
     *
     * @return Decoder
     */
    @Override
    public Decoder<BufferedImage> newDecoder() {
        return new DefaultImageDecoder();
    }

    /**
     * Returns a new instance of  Decoder<Segmenter>
     *
     * @return Decoder
     */
    @Override
    public Segmenter<BufferedImage> newSegmenter() {
        Segmenter<BufferedImage> segmenter = this.context.newSegmenter();
        if (segmenter == null) segmenter = new PassthroughSegmenter<BufferedImage>() {
            @Override
            protected SegmentContainer getSegmentFromContent(BufferedImage content) {
                return new ImageSegment(content, ImageExtractionFileHandler.this.cachedDataFactory);
            }
        };
        return segmenter;
    }
}
