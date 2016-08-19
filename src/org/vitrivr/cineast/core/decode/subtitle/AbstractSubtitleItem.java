package org.vitrivr.cineast.core.decode.subtitle;



public abstract class AbstractSubtitleItem implements SubtitleItem {

	int id;
	long start, end;
	protected String text;
	protected SubTitle st;
	
	protected AbstractSubtitleItem(int id, long start, long end, String text, SubTitle st){
		this.id = id;
		this.start = start;
		this.end = end;
		this.text = text;
		this.st = st;
	}
	
	/* (non-Javadoc)
	 * @see subsync.SubItem#getLength()
	 */
	@Override
	public int getLength(){
		return (int) (end - start);
	}

	/* (non-Javadoc)
	 * @see subsync.SubItem#getRawText()
	 */
	@Override
	public String getRawText(){
		return this.text;
	}

	@Override
	public int getStartFrame() {
		return Math.round(this.start * this.st.getFrameRate() / 1000);
	}

	@Override
	public int getEndFrame() {
		return Math.round(this.end * this.st.getFrameRate() / 1000);
	}

	@Override
	public SubTitle getSubTitle() {
		return this.st;
	}
	
	@Override
	public String toString() {
		return "id: " + id + "\n" + start + " ---> " + end + "\n" + text;
	}

}
