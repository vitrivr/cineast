package org.vitrivr.cineast.core.data.segments;

import org.vitrivr.cineast.core.data.providers.AudioFrameProvider;
import org.vitrivr.cineast.core.data.providers.AudioSTFTProvider;
import org.vitrivr.cineast.core.data.providers.AvgImgProvider;
import org.vitrivr.cineast.core.data.providers.BooleanExpressionProvider;
import org.vitrivr.cineast.core.data.providers.DurationProvider;
import org.vitrivr.cineast.core.data.providers.FrameListProvider;
import org.vitrivr.cineast.core.data.providers.IdProvider;
import org.vitrivr.cineast.core.data.providers.InstantProvider;
import org.vitrivr.cineast.core.data.providers.LocationProvider;
import org.vitrivr.cineast.core.data.providers.MedianImgProvider;
import org.vitrivr.cineast.core.data.providers.MeshProvider;
import org.vitrivr.cineast.core.data.providers.MostRepresentativeFrameProvider;
import org.vitrivr.cineast.core.data.providers.PathProvider;
import org.vitrivr.cineast.core.data.providers.SemanticMapProvider;
import org.vitrivr.cineast.core.data.providers.SubtitleItemProvider;
import org.vitrivr.cineast.core.data.providers.TagProvider;
import org.vitrivr.cineast.core.data.providers.TextProvider;
import org.vitrivr.cineast.core.data.providers.VoxelGridProvider;

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
