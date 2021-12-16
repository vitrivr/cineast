package org.vitrivr.cineast.core.data.providers;

public interface DurationProvider {
	/**
	 * Returns the start in some arbitrary unit, e.g. in samples or frames depending
	 * on the implementation.
	 */
	default int getStart(){
	  return 0;
	}

	/**
	 * Returns the end in some arbitrary unit, e.g. in samples or frames depending
	 * on the implementation.
	 */
	default int getEnd(){
	  return 0;
	}

	/**
	 * Returns the relative start in percent (value between 0.0 and 1.0)
	 */
	default float getRelativeStart(){
	  return 0f;
	}

	/**
	 * Returns the relative end in percent (value between 0.0 and 1.0)
	 */
	default float getRelativeEnd(){
	  return 0f;
	}

	/**
	 * Returns the absolute start in seconds.
	 */
	default float getAbsoluteStart(){
		return 0f;
	}

	/**
	 * Returns the absolute end in seconds.
	 */
	default float getAbsoluteEnd(){
		return 0f;
	}
}
