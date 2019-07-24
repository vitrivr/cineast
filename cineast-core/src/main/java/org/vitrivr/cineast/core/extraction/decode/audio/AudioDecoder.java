package org.vitrivr.cineast.core.extraction.decode.audio;

import org.vitrivr.cineast.core.data.frames.AudioFrame;
import org.vitrivr.cineast.core.extraction.decode.general.Decoder;

/**
 * @author rgasser
 * @version 1.0
 * @created 30.11.16
 */
public interface AudioDecoder extends Decoder<AudioFrame> {

    void seekToFrame(int frameNumber);

    int getFrameNumber();
}
