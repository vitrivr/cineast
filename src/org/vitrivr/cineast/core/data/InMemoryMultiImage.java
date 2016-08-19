package org.vitrivr.cineast.core.data;

import java.awt.image.BufferedImage;
import java.io.IOException;

import net.coobird.thumbnailator.Thumbnails;


public class InMemoryMultiImage implements MultiImage {

	private BufferedImage bimg, thumb;
	private int[] colors, thumbColors;
	
	InMemoryMultiImage(BufferedImage bimg){
		this(bimg, null);
	}
	
	InMemoryMultiImage(BufferedImage bimg, BufferedImage thumb){
		this.bimg = bimg;
		this.thumb = thumb;
		if(this.thumb == null){
			gernerateThumb(bimg);
		}
		this.colors = this.bimg.getRGB(0, 0, this.bimg.getWidth(), this.bimg.getHeight(), null, 0, this.bimg.getWidth());
		this.thumbColors = this.thumb.getRGB(0, 0, this.thumb.getWidth(), this.thumb.getHeight(), null, 0, this.thumb.getWidth());
	}
	
	@Override
	public BufferedImage getBufferedImage() {
		return this.bimg;
	}

	@Override
	public BufferedImage getThumbnailImage() {
		return this.thumb;
	}

	@Override
	public int[] getColors() {
		return this.colors;
	}

	@Override
	public int[] getThumbnailColors() {
		return this.thumbColors;
	}

	@Override
	public int getWidth() {
		return this.bimg.getWidth();
	}

	@Override
	public int getHeight() {
		return this.bimg.getHeight();
	}

	@Override
	public void clear(){}
	
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

}
