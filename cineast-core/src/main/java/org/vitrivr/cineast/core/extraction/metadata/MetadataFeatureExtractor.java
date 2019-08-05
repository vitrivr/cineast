package org.vitrivr.cineast.core.extraction.metadata;

import org.vitrivr.cineast.core.data.entities.MediaObjectMetadataDescriptor;

import java.nio.file.Path;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

/**
 * A {@link MetadataExtractor} that extracts only a specific feature.
 *
 * @param <T> type of the feature data
 */
public interface MetadataFeatureExtractor<T> extends MetadataExtractor {
  /**
   * Provides a default implementation by extracting the feature data and creating descriptors of
   * it.
   *
   * @param objectId ID of the multimedia object for which metadata will be generated.
   * @param path Path to the file for which metadata should be extracted.
   * @return list of descriptors describing the feature data, if found, otherwise an empty list.
   */
  @Override
  default List<MediaObjectMetadataDescriptor> extract(String objectId, Path path) {
    return this.extractFeature(objectId, path)
        .map(floatVector -> this.createDescriptors(objectId, floatVector))
        .orElse(Collections.emptyList());
  }

  /**
   * Returns an {@link Optional} containing the extracted feature data from the file, if found,
   * otherwise an empty {@code Optional}.
   */
  Optional<T> extractFeature(String objectId, Path path);

  /** Returns a list of descriptors of the given feature data. */
  List<MediaObjectMetadataDescriptor> createDescriptors(String objectId, T feature);
}
