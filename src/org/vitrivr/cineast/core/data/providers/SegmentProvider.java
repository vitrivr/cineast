package org.vitrivr.cineast.core.data.providers;

import org.vitrivr.cineast.core.data.segments.SegmentContainer;

public interface SegmentProvider extends AutoCloseable {

  /**
   * 
   * @return the next {@link SegmentContainer} from the source or <code>null</code> if there are no more segments.
   */
	public SegmentContainer getNextSegment();

	@Override
  public void close();
	
}
