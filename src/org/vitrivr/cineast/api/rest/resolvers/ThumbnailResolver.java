package org.vitrivr.cineast.api.rest.resolvers;

import java.io.InputStream;

public interface ThumbnailResolver {

  InputStream resolve(String segmentId);

}
