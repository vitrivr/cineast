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
}
