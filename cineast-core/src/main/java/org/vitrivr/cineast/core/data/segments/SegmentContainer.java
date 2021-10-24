package org.vitrivr.cineast.core.data.segments;

import org.vitrivr.cineast.core.data.providers.*;
import org.vitrivr.cineast.core.features.extractor.Extractor;
import org.vitrivr.cineast.core.features.retriever.Retriever;

/**
 * A {@link SegmentContainer} mainly serves two purposes:
 *
 * During the offline phase, it is passed to an {@link Extractor}, and during the online phase, it is passed to a {@link Retriever}.
 */
public interface SegmentContainer
    extends IdProvider,
        AvgImgProvider,
        DurationProvider,
        MedianImgProvider,
        MostRepresentativeFrameProvider,
        SubtitleItemProvider,
        PathProvider,
        TagProvider,
        FrameListProvider,
        AudioFrameProvider,
        AudioSTFTProvider,
        MeshProvider,
        VoxelGridProvider,
        LocationProvider,
        InstantProvider,
        TextProvider,
        SemanticMapProvider,
        BooleanExpressionProvider {}
