package org.vitrivr.cineast.api.rest.resolvers;

import org.vitrivr.cineast.core.data.entities.MediaObjectDescriptor;

import java.io.File;

/**
 * Functional interface to map {@link MediaObjectDescriptor} to a filename.
 */
@FunctionalInterface
public interface ObjectToFileResolver {

    File resolve(File baseDir, MediaObjectDescriptor object);
}
