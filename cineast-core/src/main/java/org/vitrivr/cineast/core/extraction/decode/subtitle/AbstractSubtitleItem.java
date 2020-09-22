package org.vitrivr.cineast.core.extraction.decode.subtitle;



public abstract class AbstractSubtitleItem implements SubtitleItem {

	protected final int id;
	protected final long start, end;
	protected final String text;
	
	protected AbstractSubtitleItem(int id, long start, long end, String text){
		this.id = id;
		this.start = start;
		this.end = end;
		this.text = text;
	}
	
	/* (non-Javadoc)
	 * @see subsync.SubItem#getLength()
	 */
	@Override
	public int getLength(){
		return (int) (end - start);
	}


	@Override
	public long getStartTimestamp() {
		return this.start;
	}

	@Override
	public long getEndTimestamp() {
		return this.end;
	}
	
	@Override
	public String toString() {
		return "id: " + id + "\n" + start + " ---> " + end + "\n" + text;
	}

}
