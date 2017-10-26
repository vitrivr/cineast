package org.vitrivr.cineast.core.decode.video;

import org.vitrivr.cineast.core.data.frames.VideoFrame;

@Deprecated
public interface VideoDecoder extends AutoCloseable {

	void seekToFrame(int frameNumber);

	int getFrameNumber();

	VideoFrame getFrame();
	
	int getTotalFrameCount();
	
	double getFPS();

	@Override
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