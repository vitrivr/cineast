package ch.unibas.cs.dbis.cineast.core.data;

import java.util.List;

import ch.unibas.cs.dbis.cineast.core.data.providers.AvgImgProvider;
import ch.unibas.cs.dbis.cineast.core.data.providers.DurationProvider;
import ch.unibas.cs.dbis.cineast.core.data.providers.MedianImgProvider;
import ch.unibas.cs.dbis.cineast.core.data.providers.MostRepresentativeFrameProvider;
import ch.unibas.cs.dbis.cineast.core.data.providers.PathProvider;
import ch.unibas.cs.dbis.cineast.core.data.providers.SubtitleItemProvider;
import ch.unibas.cs.dbis.cineast.core.data.providers.TagProvider;

public interface SegmentContainer extends AvgImgProvider, DurationProvider, MedianImgProvider, MostRepresentativeFrameProvider, SubtitleItemProvider, PathProvider, TagProvider{

	List<Frame> getFrames();
	
	/**
	 * @return a unique id of this 
	 */
	String getId();
	
	String getSuperId();
}
