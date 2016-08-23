package org.vitrivr.cineast.core.decode.video;

import org.vitrivr.cineast.core.data.Frame;

public interface VideoDecoder {

	void seekToFrame(int frameNumber);

	int getFrameNumber();

	Frame getFrame();
	
	int getTotalFrameCount();
	
	double getFPS();

	void close();
	
	/**
	 * width of the input video
	 * @return
	 */
	int getOriginalWidth();
	
	/**
	 * height of the input video
	 * @return
	 */
	int getOriginalHeight();
	
	/**
	 * width of a frame after scaling
	 * @return
	 */
	int getWidth();
	
	/**
	 * height of a frame after scaling
	 * @return
	 */
	int getHeight();

}