package org.vitrivr.cineast.core.data.providers;

public interface DurationProvider {
	/**
	 * Returns the start in some arbitrary unit, e.g. in samples or frames depending
	 * on the implementation.
	 *
	 * @return
	 */
	default int getStart(){
	  return 0;
	}

	/**
	 * Returns the end in some arbitrary unit, e.g. in samples or frames depending
	 * on the implementation.
	 *
	 * @return
	 */
	default int getEnd(){
	  return 0;
	}

	/**
	 * Returns the relative start in percent (value between 0.0 and 1.0)
	 *
	 * @return
	 */
	default float getRelativeStart(){
	  return 0f;
	}

	/**
	 * Returns the relative end in percent (value between 0.0 and 1.0)
	 *
	 * @return
	 */
	default float getRelativeEnd(){
	  return 0f;
	}

	/**
	 * Returns the absolute start in seconds.
	 *
	 * @return
	 */
	default float getAbsoluteStart(){
		return 0f;
	}

	/**
	 * Returns the absolute end in seconds.
	 *
	 * @return
	 */
	default float getAbsoluteEnd(){
		return 0f;
	}
}
