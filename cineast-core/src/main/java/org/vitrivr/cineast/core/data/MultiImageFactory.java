package org.vitrivr.cineast.core.data;

import java.io.File;

import java.util.UUID;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import org.vitrivr.cineast.core.config.ImageCacheConfig;
import org.vitrivr.cineast.core.config.ImageCacheConfig.Policy;

import java.awt.image.BufferedImage;

public class MultiImageFactory {

	private static final Logger LOGGER = LogManager.getLogger();

	/** Reference to {@link ImageCacheConfig} used to setup this {@link MultiImageFactory}. */
	private final ImageCacheConfig config;

	/** Location where this instance of {@link MultiImageFactory} will store its cached images. */
	private final File cacheLocation;

	/**
	 * Default constructor.
	 *
	 * @param config {@link ImageCacheConfig} reference.
	 */
	public MultiImageFactory(ImageCacheConfig config){
		this.config = config;
		this.cacheLocation = new File(this.config.getCacheLocation(), "framecache_" + config.getUUID());
		if (this.cacheLocation.mkdirs()) {
			this.cacheLocation.deleteOnExit();
		}
	}
	
	public MultiImage newMultiImage(BufferedImage bimg){
		return newMultiImage(bimg, null);
	}
	
	public MultiImage newMultiImage(BufferedImage bimg, BufferedImage thumb){
		if (keepInMemory()) {
			return new InMemoryMultiImage(bimg, thumb, this);
		} else {
			return new CachedMultiImage(bimg, thumb, this);
		}
	}
	
	public MultiImage newMultiImage(int width, int height, int[] colors){
		if(keepInMemory()){
			height = MultiImageFactory.checkHeight(width, height, colors);
			BufferedImage bimg = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
			bimg.setRGB(0, 0, width, height, colors, 0, width);
			return new InMemoryMultiImage(bimg, this);
		}else{
			return new CachedMultiImage(width, height, colors, this);
		}
	}
	
	public MultiImage newInMemoryMultiImage(BufferedImage bimg){
		if(this.config.getCachingPolicy() == Policy.FORCE_DISK_CACHE){
			LOGGER.warn("Creating CachedMultiImage instead of InMemoryMultiImage because of policy.");
			return new CachedMultiImage(bimg, this);
		}
		return new InMemoryMultiImage(bimg, this);
	}

	/**
	 * Getter for cache location.
	 *
	 * @return Cache location for this {@link MultiImageFactory}.
	 */
	public File getCacheLocation() {
		return this.cacheLocation;
	}

	/**
	 * Determines whether or not an image should be held in memory or cached to disk.
	 *
	 * @return True if image should be kept in memory.
	 */
	private boolean keepInMemory(){
		long freeMemory = Runtime.getRuntime().freeMemory();

		Policy cachePolicy = this.config.getCachingPolicy();
		long hardMinMemory = this.config.getHardMinMemory();
		long softMinMemory = this.config.getSoftMinMemory();
		
		if(cachePolicy == Policy.AVOID_CACHE){
			if(freeMemory > hardMinMemory){
				return true;
			}else{
				System.gc();
				return false;
			}
		}
		
		if(cachePolicy == Policy.FORCE_DISK_CACHE || cachePolicy == Policy.DISK_CACHE){
			return false;
		}
		
		//check if access comes from preferred source
		boolean isVideoDecoder = false;
		
		StackTraceElement[] stackTraceElements = Thread.currentThread().getStackTrace();
		for(StackTraceElement element : stackTraceElements){
			if(element.getClassName().toLowerCase().contains("decoder")){
				isVideoDecoder = true;
				break;
			}
		}
		
		if(freeMemory > hardMinMemory){
			if(!isVideoDecoder){
				return true;
			}
			return freeMemory > softMinMemory;
		}else{
			System.gc();
			return false;
		}
		
	}
	
	static int checkHeight(int width, int height, int[] colors){
		if(colors.length / width != height){
			LOGGER.debug("Dimension mismatch in MultiImage, setting height from {} to {}", height, (height = colors.length / width));
		}
		return height;
	}
}
