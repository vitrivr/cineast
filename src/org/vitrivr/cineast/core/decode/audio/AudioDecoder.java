package org.vitrivr.cineast.core.decode.audio;

import org.vitrivr.cineast.core.data.audio.AudioFrame;

/**
 * @author rgasser
 * @version 1.0
 * @created 30.11.16
 */
public interface AudioDecoder {

    void seekToFrame(int frameNumber);

    int getFrameNumber();

    AudioFrame getFrame();

    int getTotalFrameCount();

    void close();
}
