package org.vitrivr.cineast.core.run.filehandler;


import org.vitrivr.cineast.core.decode.general.Decoder;
import org.vitrivr.cineast.core.decode.image.DefaultImageDecoder;
import org.vitrivr.cineast.core.run.ExtractionContextProvider;
import org.vitrivr.cineast.core.segmenter.image.ImageSegmenter;
import org.vitrivr.cineast.core.segmenter.general.Segmenter;

import java.awt.image.BufferedImage;
import java.nio.file.Path;
import java.util.List;

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
    public ImageExtractionFileHandler(List<Path> files, ExtractionContextProvider context) {
        super (files,context);
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
        return new ImageSegmenter();
    }
}
