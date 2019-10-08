package org.vitrivr.cineast.api.grpc.util;

import org.vitrivr.cineast.api.grpc.CineastGrpc;
import org.vitrivr.cineast.core.data.MediaType;
import org.vitrivr.cineast.core.data.entities.MediaObjectDescriptor;

public class MediaObjectUtil {

    public static CineastGrpc.MediaObject fromMediaObjectDescriptor(MediaObjectDescriptor descriptor) {
        if (descriptor == null) {
            return null;
        }

        CineastGrpc.MediaObject.Builder builder = CineastGrpc.MediaObject.newBuilder();

        builder.setObjectId(mediaObjectId(descriptor.getObjectId()));
        builder.setName(descriptor.getName());
        builder.setPath(descriptor.getPath());
        builder.setType(mediaType(descriptor.getMediatype()));
        builder.setContentUrl(descriptor.getContentURL());

        return builder.build();
    }

    public static CineastGrpc.MediaObjectId mediaObjectId(String id){
        return CineastGrpc.MediaObjectId.newBuilder().setId(id).build();
    }

    public static CineastGrpc.MediaObject.MediaType mediaType(MediaType type){
        return CineastGrpc.MediaObject.MediaType.forNumber(type.getId());
    }

}
