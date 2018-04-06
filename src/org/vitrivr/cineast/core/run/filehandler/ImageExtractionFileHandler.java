package org.vitrivr.cineast.core.run.filehandler;


import java.awt.image.BufferedImage;
import java.io.IOException;
import org.vitrivr.cineast.core.data.segments.ImageSegment;
import org.vitrivr.cineast.core.data.segments.SegmentContainer;
import org.vitrivr.cineast.core.decode.general.Decoder;
import org.vitrivr.cineast.core.decode.image.DefaultImageDecoder;
import org.vitrivr.cineast.core.run.ExtractionContextProvider;
import org.vitrivr.cineast.core.run.ExtractionPathProvider;
import org.vitrivr.cineast.core.segmenter.general.PassthroughSegmenter;
import org.vitrivr.cineast.core.segmenter.general.Segmenter;

/**
 * @author rgasser
 * @version 1.0
 * @created 14.01.17
 */
public class ImageExtractionFileHandler extends AbstractExtractionFileHandler<BufferedImage> {
    /**
     * Default constructor.
     *
     * @param
     */
    public ImageExtractionFileHandler(ExtractionPathProvider provider, ExtractionContextProvider context) throws IOException {
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
                return new ImageSegment(content);
            }
        };
        return segmenter;
    }
}
