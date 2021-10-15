package org.vitrivr.cineast.core.extraction.decode.audio;

import org.vitrivr.cineast.core.data.frames.AudioFrame;
import org.vitrivr.cineast.core.extraction.decode.general.Decoder;


public interface AudioDecoder extends Decoder<AudioFrame> {

    void seekToFrame(int frameNumber);

    int getFrameNumber();
}
