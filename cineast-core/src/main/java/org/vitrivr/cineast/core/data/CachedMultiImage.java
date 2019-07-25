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

	private static final Logger LOGGER = LogManager.getLogger();

	private BufferedImage thumb;
	private final int width, height, id;
	private final File file;
	private final MultiImageFactory factory;
	
	protected CachedMultiImage(int width, int height, MultiImageFactory factory){
		this.id = getId();
		this.width = width;
		this.height = height;
		this.factory = factory;
		this.file = new File(this.factory.getCacheLocation(), Integer.toString(this.id));
	}
	
	CachedMultiImage(BufferedImage img, MultiImageFactory factory){
		this(img, null, factory);
	}
	
	CachedMultiImage(BufferedImage img, BufferedImage thumb, MultiImageFactory factory){
		this(img.getWidth(), img.getHeight(), factory);
		this.thumb = thumb;
		if(this.thumb == null){
			generateThumb(img);
		}
		try {
			int[] colors = img.getRGB(0, 0, width, height, null, 0, width);
			ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(file));
			oos.writeObject(colors);
			oos.flush();
			oos.close();
		} catch (IOException e) {
			LOGGER.fatal("Could not write MultiImage to file cache.");
			LOGGER.fatal(LogHelper.getStackTrace(e));
		}
	}
	
	CachedMultiImage(int width, int height, int[] colors, MultiImageFactory factory){
		this(width, MultiImageFactory.checkHeight(width, height, colors), factory);
		BufferedImage bimg = new BufferedImage(this.width, this.height, BufferedImage.TYPE_INT_ARGB);
		bimg.setRGB(0, 0, this.width, this.height, colors, 0, this.width);
		generateThumb(bimg);
		try {
			ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(file));
			oos.writeObject(colors);
			oos.flush();
			oos.close();
		} catch (IOException e) {
			LOGGER.fatal("Could not write MultiImage to file cache");
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
	public MultiImageFactory factory() {
		return this.factory;
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
	
	private void generateThumb(BufferedImage img){
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
