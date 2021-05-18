package org.vitrivr.cineast.api.rest.resolvers;

import java.io.File;
import org.vitrivr.cineast.core.data.entities.MediaObjectDescriptor;

/**
 * Functional interface to map {@link MediaObjectDescriptor} to a filename.
 */
@FunctionalInterface
public interface ObjectToFileResolver {

  File resolve(File baseDir, MediaObjectDescriptor object);
}
