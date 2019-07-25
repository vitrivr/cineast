package org.vitrivr.cineast.core.data;

import net.coobird.thumbnailator.Thumbnails;

import java.awt.image.BufferedImage;
import java.io.IOException;

public class InMemoryMultiImage implements MultiImage {

	private final BufferedImage bimg;
	private final int[] colors, thumbColors;
	private final MultiImageFactory factory;
	private BufferedImage thumb;

	InMemoryMultiImage(BufferedImage bimg, MultiImageFactory factory){
		this(bimg, null, factory);
	}
	
	InMemoryMultiImage(BufferedImage bimg, BufferedImage thumb, MultiImageFactory factory){
		this.bimg = bimg;
		this.thumb = thumb;
		this.factory = factory;
		if(this.thumb == null){
			generateThumb(bimg);
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
	public MultiImageFactory factory() {
		return this.factory;
	}

	@Override
	public void clear(){}
	
	private void generateThumb(BufferedImage img){
		double scale = MAX_THUMB_SIZE / Math.max(img.getWidth(), img.getHeight());
		if(scale >= 1 || scale <= 0){
			this.thumb = img;
		} else {
			try {
				this.thumb = Thumbnails.of(img).scale(scale).asBufferedImage();
			} catch (IOException e) {
				this.thumb = img;
			}
		}
	}
}
