package org.vitrivr.cineast.core.descriptor;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.vitrivr.cineast.core.color.ReadableRGBContainer;
import org.vitrivr.cineast.core.data.MultiImage;
import org.vitrivr.cineast.core.data.MultiImageFactory;
import org.vitrivr.cineast.core.data.frames.VideoFrame;
import org.vitrivr.cineast.core.util.DecodingError;
import org.vitrivr.cineast.core.util.TimeHelper;

import java.util.List;

public class AvgImg {

	private static final Logger LOGGER = LogManager.getLogger();
	
	private AvgImg(){}
	
	public static MultiImage getAvg(List<VideoFrame> videoFrames){
		TimeHelper.tic();
		LOGGER.traceEntry();
		MultiImage first = videoFrames.get(0).getImage();
		int width = first.getWidth(), height = first.getHeight();
		double[] buffer = new double[width * height * 3];
		int[] colors;
		try{
			for(VideoFrame videoFrame : videoFrames){
				colors = videoFrame.getImage().getColors();
				if((colors.length * 3) != buffer.length){
					throw new DecodingError();
				}
				for(int i = 0; i < colors.length; ++i){
					int col = colors[i];
					buffer[3*i]     += ReadableRGBContainer.getRed(col);
					buffer[3*i + 1] += ReadableRGBContainer.getGreen(col);
					buffer[3*i + 2] += ReadableRGBContainer.getBlue(col);
				}
			}
		}catch(Exception e){
			throw new DecodingError();
		}
		
		
		int size = videoFrames.size();
		
		//BufferedImage _return = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
		
		colors = new int[width * height];
		
		for(int i = 0; i < colors.length; ++i){
			colors[i] = ReadableRGBContainer.toIntColor(
					(int)Math.round(buffer[3*i] / size),
					(int)Math.round(buffer[3*i + 1] / size),
					(int)Math.round(buffer[3*i + 2] / size));
		}
		
		//_return.setRGB(0, 0, width, height, colors, 0, width);
		
		//colors = null;
		buffer = null;
		
		System.gc();
		LOGGER.debug("AvgImg.getAvg() done in {}", TimeHelper.toc());
		LOGGER.traceExit();
		return MultiImageFactory.newMultiImage(width, height, colors);
	}
	
}
