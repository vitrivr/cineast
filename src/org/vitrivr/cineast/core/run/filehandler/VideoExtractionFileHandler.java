package org.vitrivr.cineast.core.run.filehandler;

import org.vitrivr.cineast.core.data.frames.VideoFrame;
import org.vitrivr.cineast.core.decode.general.Decoder;
import org.vitrivr.cineast.core.decode.video.FFMpegVideoDecoder;
import org.vitrivr.cineast.core.run.ExtractionContextProvider;

import org.vitrivr.cineast.core.segmenter.general.Segmenter;
import org.vitrivr.cineast.core.segmenter.video.VideoHistogramSegmenter;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

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
    public VideoExtractionFileHandler(List<Path> files, ExtractionContextProvider context) throws IOException {
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
        return new VideoHistogramSegmenter();
    }
}
