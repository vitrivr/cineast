package org.vitrivr.cineast.core.run.filehandler;

import java.io.IOException;
import org.vitrivr.cineast.core.data.frames.AudioFrame;
import org.vitrivr.cineast.core.decode.audio.FFMpegAudioDecoder;
import org.vitrivr.cineast.core.decode.general.Decoder;
import org.vitrivr.cineast.core.run.ExtractionContextProvider;
import org.vitrivr.cineast.core.run.ExtractionContainerProvider;
import org.vitrivr.cineast.core.segmenter.audio.ConstantLengthAudioSegmenter;
import org.vitrivr.cineast.core.segmenter.general.Segmenter;

/**
 *
 * @author rgasser
 * @version 1.0
 * @created 31.01.17
 */
public class AudioExtractionFileHandler extends AbstractExtractionFileHandler<AudioFrame> {

    /**
     * Default constructor used to initialize the class.
     *
     * @param files   List of files that should be extracted.
     * @param context ExtractionContextProvider that holds extraction specific configurations.
     */
    public AudioExtractionFileHandler(ExtractionContainerProvider files, ExtractionContextProvider context) throws IOException {
        super(files, context);
    }

    /**
     * Returns a new instance of  Decoder<T> that should be used with a concrete implementation
     * of this interface.
     *
     * @return Decoder
     */
    @Override
    public Decoder<AudioFrame> newDecoder() {
        return new FFMpegAudioDecoder();
    }

    /**
     * Returns a new instance of Segmenter<T> that should be used with a concrete implementation
     * of this interface.
     *
     * @return Segmenter<T>
     */
    @Override
    public Segmenter<AudioFrame> newSegmenter() {
        Segmenter<AudioFrame> segmenter = this.context.newSegmenter();
        if (segmenter == null) segmenter = new ConstantLengthAudioSegmenter(this.context);
        return segmenter;
    }
}
