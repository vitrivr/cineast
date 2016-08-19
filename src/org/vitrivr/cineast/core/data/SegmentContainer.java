package org.vitrivr.cineast.core.data;

import java.util.List;

import org.vitrivr.cineast.core.data.providers.AvgImgProvider;
import org.vitrivr.cineast.core.data.providers.DurationProvider;
import org.vitrivr.cineast.core.data.providers.MedianImgProvider;
import org.vitrivr.cineast.core.data.providers.MostRepresentativeFrameProvider;
import org.vitrivr.cineast.core.data.providers.PathProvider;
import org.vitrivr.cineast.core.data.providers.SubtitleItemProvider;
import org.vitrivr.cineast.core.data.providers.TagProvider;

public interface SegmentContainer extends AvgImgProvider, DurationProvider, MedianImgProvider, MostRepresentativeFrameProvider, SubtitleItemProvider, PathProvider, TagProvider{

	List<Frame> getFrames();
	
	/**
	 * @return a unique id of this 
	 */
	String getId();
	
	String getSuperId();
}
