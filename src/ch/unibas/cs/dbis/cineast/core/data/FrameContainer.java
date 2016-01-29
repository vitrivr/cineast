package ch.unibas.cs.dbis.cineast.core.data;

import java.util.List;

import ch.unibas.cs.dbis.cineast.core.data.providers.AvgImgProvider;
import ch.unibas.cs.dbis.cineast.core.data.providers.DurationProvider;
import ch.unibas.cs.dbis.cineast.core.data.providers.MedianImgProvider;
import ch.unibas.cs.dbis.cineast.core.data.providers.MostRepresentativeFrameProvider;
import ch.unibas.cs.dbis.cineast.core.data.providers.PathProvider;
import ch.unibas.cs.dbis.cineast.core.data.providers.SubtitleItemProvider;

public interface FrameContainer extends AvgImgProvider, DurationProvider, MedianImgProvider, MostRepresentativeFrameProvider, SubtitleItemProvider, PathProvider{

	List<Frame> getFrames();
	
	/**
	 * @return a unique id of this 
	 */
	long getId();
	
	long getSuperId();
}
