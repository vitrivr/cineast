package org.vitrivr.cineast.core.descriptor;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.vitrivr.cineast.core.data.raw.images.MultiImage;
import org.vitrivr.cineast.core.data.frames.VideoFrame;
import org.vitrivr.cineast.core.data.segments.VideoSegment;

public class MostRepresentative {

	private static final Logger LOGGER = LogManager.getLogger();
	
	private MostRepresentative(){}
	
	public static VideoFrame getMostRepresentative(VideoSegment videoSegment){
		LOGGER.traceEntry();
		MultiImage reference = videoSegment.getAvgImg();
		VideoFrame _return = null;
		double minDist = Double.POSITIVE_INFINITY;
		for(VideoFrame f : videoSegment.getVideoFrames()){
			double dist = ImageDistance.colorDistance(reference, f.getImage());
			if(dist < minDist){
				minDist = dist;
				_return = f;
			}
		}
		return LOGGER.traceExit(_return);
	}
	
}
