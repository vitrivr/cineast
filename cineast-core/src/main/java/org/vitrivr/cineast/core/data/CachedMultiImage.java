package org.vitrivr.cineast.core.data;

import net.coobird.thumbnailator.Thumbnails;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.vitrivr.cineast.core.util.LogHelper;

import java.awt.image.BufferedImage;
import java.io.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class CachedMultiImage implements MultiImage {
	
	private static final File FRAME_CACHE = new File(Config.sharedConfig().getImagecache().getCacheLocation(), "framecache_" + Config.UNIQUE_ID.toString());
	
	
	private static final Logger LOGGER = LogManager.getLogger();
	
	static{
		if(!FRAME_CACHE.exists()){
			FRAME_CACHE.mkdirs();
		}
		for(File f : FRAME_CACHE.listFiles()){
			f.delete();
		}
		FRAME_CACHE.deleteOnExit();
	}

	private BufferedImage thumb;
	private final int width, height, id;
	private final File file;
	
	protected CachedMultiImage(int width, int height){
		this.id = getId();
		this.width = width;
		this.height = height;
		this.file = new File(FRAME_CACHE, Integer.toString(this.id));
		this.file.deleteOnExit();
	}
	
	CachedMultiImage(BufferedImage img){
		this(img, null);
	}
	
	CachedMultiImage(BufferedImage img, BufferedImage thumb){
		this(img.getWidth(), img.getHeight());
		this.thumb = thumb;
		if(this.thumb == null){
			gernerateThumb(img);
		}
		try {
			int[] colors = img.getRGB(0, 0, width, height, null, 0, width);
			ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(file));
			oos.writeObject(colors);
			oos.flush();
			oos.close();
		} catch (IOException e) {
			LOGGER.fatal("could not write MultiImage to filecache");
			LOGGER.fatal(LogHelper.getStackTrace(e));
		}
	}
	
	CachedMultiImage(int width, int height, int[] colors){
		this(width, MultiImageFactory.checkHeight(width, height, colors));
		BufferedImage bimg = new BufferedImage(this.width, this.height, BufferedImage.TYPE_INT_ARGB);
		bimg.setRGB(0, 0, this.width, this.height, colors, 0, this.width);
		gernerateThumb(bimg);
		try {
			ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(file));
			oos.writeObject(colors);
			oos.flush();
			oos.close();
		} catch (IOException e) {
			LOGGER.fatal("could not write MultiImage to filecache");
			LOGGER.fatal(LogHelper.getStackTrace(e));
		}
	}
	
	private Lock getBufferedImageLock = new ReentrantLock();
	/* (non-Javadoc)
	 * @see cineast.core.data._MultiImage#getBufferedImage()
	 */
	@Override
	public BufferedImage getBufferedImage(){
		getBufferedImageLock.lock();
		int[] colors = getColors();
		
		if(colors == null){
			return null;
		}
		
		BufferedImage _return = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
		_return.setRGB(0, 0, width, height, colors, 0, width);
		try{
			return _return;
		}finally{
			getBufferedImageLock.unlock();
		}		
	}

	
	/* (non-Javadoc)
	 * @see cineast.core.data._MultiImage#getThumbnailImage()
	 */
	@Override
	public BufferedImage getThumbnailImage(){
		return this.thumb;
	}
	
	private ReentrantLock getColorsLock = new ReentrantLock();
	/* (non-Javadoc)
	 * @see cineast.core.data._MultiImage#getColors()
	 */
	@Override
	public synchronized int[] getColors(){
		getColorsLock.lock();
		LOGGER.traceEntry();
		int[] _return = null;
		
		try {
			ObjectInputStream oin = new ObjectInputStream(new FileInputStream(file));
			_return = (int[]) oin.readObject();
			oin.close();
		} catch (Exception e) {
			LOGGER.fatal("could not read MultiImage from filecache");
			LOGGER.fatal(LogHelper.getStackTrace(e));
		}
		
		try{
			LOGGER.traceExit();
			return _return;
		}finally{
			getColorsLock.unlock();
		}
	}
	
	/* (non-Javadoc)
	 * @see cineast.core.data._MultiImage#getThumbnailColors()
	 */
	@Override
	public int[] getThumbnailColors(){
		int[] _return = this.thumb.getRGB(0, 0, this.thumb.getWidth(), this.thumb.getHeight(), null, 0, this.thumb.getWidth());
		return _return;
	}
	
	/* (non-Javadoc)
	 * @see cineast.core.data._MultiImage#getWidth()
	 */
	@Override
	public int getWidth(){
		return this.width;
	}
	
	/* (non-Javadoc)
	 * @see cineast.core.data._MultiImage#getHeight()
	 */
	@Override
	public int getHeight(){
		return this.height;
	}
	
	@Override
  public synchronized void clear(){
		this.thumb = null;
		this.file.delete();
	}

	private static AtomicInteger counter = new AtomicInteger();
	private static int getId(){
		return counter.getAndIncrement();
	}
	
	private void gernerateThumb(BufferedImage img){
		double scale = MAX_THUMB_SIZE / Math.max(img.getWidth(), img.getHeight());
		if(scale >= 1 || scale <= 0){
			this.thumb = img;
		}else{
			try {
				this.thumb = Thumbnails.of(img).scale(scale).asBufferedImage();
			} catch (IOException e) {
				this.thumb = img;
			}
		}
	}

	@Override
	public String toString() {
		return "CachedMultiImage id: " + this.id + " (" + this.width + "x" + this.height + ") @ " + this.file.getAbsolutePath();
	}
	
}
