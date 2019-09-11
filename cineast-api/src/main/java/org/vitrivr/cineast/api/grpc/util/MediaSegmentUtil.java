package org.vitrivr.cineast.api.grpc.util;

import org.vitrivr.cineast.api.grpc.CineastGrpc;
import org.vitrivr.cineast.core.data.entities.MediaSegmentDescriptor;

public class MediaSegmentUtil {

    public static CineastGrpc.MediaSegment fromMediaSegmentDescriptor(MediaSegmentDescriptor descriptor){

        CineastGrpc.MediaSegment.Builder builder = CineastGrpc.MediaSegment.newBuilder();

        builder.setObjectId(MediaObjectUtil.mediaObjectId(descriptor.getObjectId()));
        builder.setSegmentId(mediaSegmentId(descriptor.getSegmentId()));
        builder.setStart(descriptor.getStart());
        builder.setEnd(descriptor.getEnd());
        builder.setStartAbs(descriptor.getStartabs());
        builder.setEndAbs(descriptor.getEndabs());
        builder.setNumber(descriptor.getSequenceNumber());

        return builder.build();

    }

    public static CineastGrpc.MediaSegmentId mediaSegmentId(String id) {
        return CineastGrpc.MediaSegmentId.newBuilder().setId(id).build();
    }

}
