package org.vitrivr.cineast.core.data;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.vitrivr.cineast.core.util.LogHelper;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;

public class FileCachedImageHistogram {
	
	public static final int RED = 0, GREEN = 1, BLUE = 2;

	private static final File DEFAULT_FOLDER = new File(".");
	
	private static final Logger LOGGER = LogManager.getLogger();
	
	private final int width, height;
	private final File file;
	private RandomAccessFile raf;
	
	public FileCachedImageHistogram(int width, int height, File folder){
		this.width = width;
		this.height = height;
		folder.mkdirs();
		this.file = new File(folder, ((Object)this).toString() + ".fcih");
		try {
			this.raf = new RandomAccessFile(file, "rwd");
			this.raf.setLength(width * height * 3l * 256l * 4l);
		} catch (Exception e) {
			LOGGER.fatal("could not create file cache for FileCachedImageHistogram");
			LOGGER.fatal(LogHelper.getStackTrace(e));
		}
	}
	
	public FileCachedImageHistogram(int width, int height){
		this(width, height, DEFAULT_FOLDER);
	}
	
	public int getWidth(){
		return this.width;
	}
	
	public int getHeight(){
		return this.height;
	}
	
	protected long getFilePosition(int x, int y, int color, int bucket){
		if(color > 2 || color < 0 || bucket > 255 || bucket < 0 || x >= width || x < 0 || y >= height || y < 0){
			throw new IllegalArgumentException();
		}
		
		return (((((y * width + x) * 3L) + color) * 256L) + bucket) * 4L; 
	}
	
	protected void writeInt(long pos, int val){
		try {
			this.raf.seek(pos);
			this.raf.writeInt(val);
		} catch (IOException e) {
			LOGGER.fatal("could not write to file cache");
			LOGGER.fatal(LogHelper.getStackTrace(e));
		}
	}
	
	protected int readInt(long pos){
		try {
			this.raf.seek(pos);
			return this.raf.readInt();
		} catch (Exception e) {
			LOGGER.fatal("could not read from file cache");
			LOGGER.fatal(LogHelper.getStackTrace(e));
			return 0;
		}
	}
	
	public int getBucketValue(int x, int y, int color, int bucket){
		return readInt(getFilePosition(x, y, color, bucket));
	}
	
	public void updateBucket(int x, int y, int color, int bucket){
		long pos = getFilePosition(x, y, color, bucket);
		writeInt(pos, readInt(pos) + 1);
	}
	
	public int[] getBucket(int x, int y, int color){
		int[] _return = new int[256];
		long pos = getFilePosition(x, y, color, 0);
		for(int i = 0; i < 256; ++i){
			_return[i] = readInt(pos);
			pos += 4;
		}
		return _return;
	}
	
	public void delete(){
		this.file.delete();
	}

	@Override
	protected void finalize() throws Throwable {
		delete();
		super.finalize();
	}
	
}
