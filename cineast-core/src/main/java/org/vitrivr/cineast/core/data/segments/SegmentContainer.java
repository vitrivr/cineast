package org.vitrivr.cineast.core.data.segments;

import org.vitrivr.cineast.core.data.providers.*;

public interface SegmentContainer
    extends IdProvider,
        AvgImgProvider,
        DurationProvider,
        MedianImgProvider,
        MostRepresentativeFrameProvider,
        SubtitleItemProvider,
        PathProvider,
        PoseProvider,
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
