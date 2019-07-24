package org.vitrivr.cineast.standalone.run.filehandler;

import org.vitrivr.cineast.core.data.frames.VideoFrame;
import org.vitrivr.cineast.core.extraction.ExtractionContextProvider;
import org.vitrivr.cineast.core.extraction.decode.general.Decoder;
import org.vitrivr.cineast.core.extraction.decode.video.FFMpegVideoDecoder;
import org.vitrivr.cineast.core.extraction.segmenter.general.Segmenter;
import org.vitrivr.cineast.core.extraction.segmenter.video.VideoHistogramSegmenter;
import org.vitrivr.cineast.standalone.run.ExtractionContainerProvider;

import java.io.IOException;

/**
 * @author rgasser
 * @version 1.0
 * @created 17.01.17
 */
public class VideoExtractionFileHandler extends AbstractExtractionFileHandler<VideoFrame> {
    /**
     * @param files
     * @param context
     */
    public VideoExtractionFileHandler(ExtractionContainerProvider files, ExtractionContextProvider context) throws IOException {
        super(files, context);
    }

    /**
     * Returns a new instance of Decoder<T> that should be used with a concrete implementation
     * of this class.
     *
     * @return Decoder
     */
    @Override
    public Decoder<VideoFrame> newDecoder() {
        return new FFMpegVideoDecoder();
    }

    /**
     * Returns a new instance of Segmenter<T> that should be used with a concrete implementation
     * of this class.
     *
     * @return
     */
    @Override
    public Segmenter<VideoFrame> newSegmenter() {
        Segmenter<VideoFrame> segmenter = this.context.newSegmenter();
        if (segmenter == null) segmenter = new VideoHistogramSegmenter(this.context);
        return segmenter;
    }
}
