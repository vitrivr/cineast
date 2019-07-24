package org.vitrivr.cineast.core.util;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.vitrivr.cineast.core.data.frames.VideoFrame;
import org.vitrivr.cineast.core.data.segments.VideoSegment;

import javax.imageio.ImageIO;
import java.io.File;
import java.io.IOException;

public class ShotHelper {

	private static final Logger LOGGER = LogManager.getLogger();
	
	private ShotHelper(){}
	
	public static void shotToFolder(VideoSegment videoSegment, File folder){
		if(!folder.exists()){
			if(!folder.mkdirs()){
				LOGGER.warn("cannot create folder {}", folder.getPath());
				return;
			}
		}else if(!folder.isDirectory()){
			LOGGER.warn("cannot write shot because {} is no folder", folder.getAbsolutePath());
			return;
		}
		
		for(VideoFrame f : videoSegment.getVideoFrames()){
			try {
				ImageIO.write(f.getImage().getBufferedImage(), "jpg", new File(folder, f.getId() + ".jpg"));
			} catch (IOException e) {
				LOGGER.warn("error while writing image");
				LOGGER.warn(LogHelper.getStackTrace(e));
			}
		}
		
	}
	
	public static void shotToThubnailSequence(VideoSegment videoSegment, File folder, int shotId){
		if(!folder.exists()){
			if(!folder.mkdirs()){
				LOGGER.warn("cannot create folder {}", folder.getPath());
				return;
			}
		}else if(!folder.isDirectory()){
			LOGGER.warn("cannot write shot because {} is no folder", folder.getAbsolutePath());
			return;
		}
		
		for(VideoFrame f : videoSegment.getVideoFrames()){
			try {
				ImageIO.write(f.getImage().getThumbnailImage(), "jpg", new File(folder, String.format("%06d",f.getId()) + "(" + shotId + ").jpg"));
			} catch (IOException e) {
				LOGGER.warn("error while writing image");
				LOGGER.warn(LogHelper.getStackTrace(e));
			}
		}
	}
	
}
