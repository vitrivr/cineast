package org.vitrivr.cineast.core.data;


public class Frame {

  public static final Frame EMPTY_FRAME = new Frame(0, MultiImage.EMPTY_MULTIIMAGE);
  
	private final int id;
	private MultiImage img;
	
	public Frame(int id, MultiImage image){
		this.id = id;
		this.img = image;
	}
	
	public int getId(){
		return this.id;
	}
	
	public MultiImage getImage(){
		return this.img;
	}
	
	public void clear(){
		this.img.clear();
		this.img = null;
	}
}
