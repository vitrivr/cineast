package org.vitrivr.cineast.core.data.providers;

import java.util.Optional;
import org.vitrivr.cineast.core.data.frames.VideoFrame;
import org.vitrivr.cineast.core.pose.SkelProcessor;

public interface PoseProvider extends MostRepresentativeFrameProvider {

  /**
   * 
   * @return the poses in segment.
   */

	default float[][][] getPose() {
		VideoFrame frame = this.getMostRepresentativeFrame();
		if (frame == VideoFrame.EMPTY_VIDEO_FRAME) {
			return new float[][][]{};
		} else {
			SkelProcessor skelProcessor = SkelProcessor.getInstance();
			return skelProcessor.getPoses(frame.getImage());
		}
	}

	default Optional<String> getPoseModel() {
		return Optional.empty();
	}

	default boolean[] getOrientations() {
		return new boolean[] { false, false };
	}
}
