package org.vitrivr.cineast.core.run.filehandler;

import org.vitrivr.cineast.core.decode.general.Decoder;
import org.vitrivr.cineast.core.run.ExtractionCompleteListener;
import org.vitrivr.cineast.core.segmenter.general.Segmenter;

/**
 * ExtractionFileHandlers are the second step in a media-file extraction process. They orchestrate file
 * decoding, segmenting and hand the segments to the extraction-pipeline.
 *
 * The ExtractionFileHandlers are usually MediaType specific. However, most implementations can be derived from
 * the AbstractExtractionFileHandler class.
 *
 * @see AbstractExtractionFileHandler
 * @see org.vitrivr.cineast.core.run.ExtractionDispatcher
 *
 * @author rgasser
 * @version 1.0
 * @created 14.01.17
 */
public interface ExtractionFileHandler<T> extends Runnable {
    /**
     * Returns a new instance of Decoder<T> that should be used with a concrete implementation
     * of this interface.
     *
     * @return Decoder
     */
    Decoder<T> newDecoder();

    /**
     * Returns a new instance of Segmenter<T> that should be used with a concrete implementation
     * of this interface.
     *
     * @return Segmenter<T>
     */
    Segmenter<T> newSegmenter();

    /**
     * Adds a {@link ExtractionCompleteListener} to be notified about every object for which the extraction completes.
     *
     * @param listener
     */
    void addExtractionCompleteListener(ExtractionCompleteListener listener);
}
