package org.vitrivr.cineast.core.data.providers;

public interface DurationProvider {

	public default int getStart(){
	  return 0;
	}
	
	public default int getEnd(){
	  return 0;
	}
	
	public default float getRelativeStart(){
	  return 0f;
	}
	
	public default float getRelativeEnd(){
	  return 0f;
	}

	/**
	 * Returns the absolute start in seconds.
	 *
	 * @return
	 */
	public default float getAbsoluteStart(){
		return 0f;
	}

	/**
	 * Returns the absolute end in seconds.
	 *
	 * @return
	 */
	public default float getAbsoluteEnd(){
		return 0f;
	}
}
