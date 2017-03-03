package org.vitrivr.cineast.core.data.providers;

import org.vitrivr.cineast.core.data.segments.SegmentContainer;

public interface SegmentProvider {

  /**
   * 
   * @return the next {@link SegmentContainer} from the source or <code>null</code> if there are no more segments.
   */
	public SegmentContainer getNextSegment();

	public void close();
	
}
