package org.vitrivr.cineast.core.util;

import org.vitrivr.cineast.core.color.ReadableRGBContainer;
import org.vitrivr.cineast.core.data.MultiImage;
import org.vitrivr.cineast.core.data.frames.VideoFrame;
import org.vitrivr.cineast.core.data.segments.SegmentContainer;

import java.util.ArrayList;

public class MotionHistoryImage {

	private MotionHistoryImage(int width, int height){
		this.width = width;
		this.height = height;
		this.intensities = new byte[width * height];
	}
	
	private final int width, height;
	private final byte[] intensities;
	
	public int getWidth(){
		return this.width;
	}
	
	public int getHeight(){
		return this.height;
	}
	
	public byte[] getIntensities(){
		return this.intensities;
	}
	
	public static MotionHistoryImage motionHistoryImage(SegmentContainer container, int lifeTime, int threshold){
		return motionHistoryImage(container, lifeTime, threshold, true);
	}
	/**
	 * 
	 * @param container
	 * @param lifeTime number of frames to consider for image
	 * @param threshold threshold distance [0, 255]
	 * @param useThumbnails produce image based on thumbnails to entire frame
	 * @return
	 */
	public static MotionHistoryImage motionHistoryImage(SegmentContainer container, int lifeTime, int threshold, boolean useThumbnails){
		if(container.getVideoFrames().isEmpty()){
			return null;
		}
		
		ArrayList<int[]> images = new ArrayList<int[]>(container.getVideoFrames().size());
		for(VideoFrame f : container.getVideoFrames()){
			if(useThumbnails){
				images.add(f.getImage().getThumbnailColors());
			}else{
				images.add(f.getImage().getColors());
			}
		}
		
		MultiImage first = container.getVideoFrames().get(0).getImage();
		
		MotionHistoryImage _return = new MotionHistoryImage(
				useThumbnails ? first.getThumbnailImage().getWidth() : first.getWidth(),
				useThumbnails ? first.getThumbnailImage().getHeight() : first.getHeight());
		
		if(container.getVideoFrames().size() == 1){
			return _return;
		}
		
		float sub = 1f / lifeTime;
		
		float[] tmp = new float[images.get(0).length];
		for(int i = 1; i < images.size(); ++i){
			int[] current = images.get(i);
			int[] last = images.get(i - 1);
			
			for(int j = 0; j < current.length; ++j){
				int dist = dist(last[j], current[j]);
				if(dist > threshold){
					tmp[j] = 1f;
				}else{
					tmp[j] = Math.max(0f, tmp[j] - sub);
				}
			}
			
		}
		
		for(int i = 0; i < tmp.length; ++i){
			_return.intensities[i] = (byte)Math.round(127f * tmp[i]);
		}
		
		return _return;
	}
	
	private static int dist(int color1, int color2){
		
		float l1 = 0.2126f * ReadableRGBContainer.getRed(color1) + 0.7152f * ReadableRGBContainer.getGreen(color1) + 0.0722f * ReadableRGBContainer.getBlue(color1);
		float l2 = 0.2126f * ReadableRGBContainer.getRed(color2) + 0.7152f * ReadableRGBContainer.getGreen(color2) + 0.0722f * ReadableRGBContainer.getBlue(color2);
		
		return Math.round(Math.abs(l1 - l2));
	}
	
}
