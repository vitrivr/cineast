package org.vitrivr.cineast.core.metadata;

import org.vitrivr.cineast.core.data.entities.MediaObjectMetadataDescriptor;

import java.nio.file.Path;
import java.util.List;

/**
 * @author rgasser
 * @version 1.0
 * @created 19.01.17
 */
public interface MetadataExtractor {
  /**
   * Initializes the extractor. The default implementation does nothing.
   */
  default void init() {
    // As a default do nothing
  }

  /**
   * Extracts the metadata from the specified path and returns a List of
   * MediaObjectMetadataDescriptor objects (one for each metadata entry).
   *
   * @param objectId ID of the multimedia object for which metadata will be generated.
   * @param path Path to the file for which metadata should be extracted.
   * @return List of MultimediaMetadataDescriptors. The list may be empty but must always be
   *         returned!
   */
  List<MediaObjectMetadataDescriptor> extract(String objectId, Path path);

  /**
   * Closes and cleans up the extractor. The default implementation does nothing.
   */
  default void finish() {
    // As a default do nothing
  }

  /**
   * Returns a name that helps to identify the metadata domain. E.g. EXIF for EXIF
   * metadata or DC for Dublin Core.
   *
   * @return Name of the metadata domain for which this extractor returns metadata.
   */
  String domain();
}
