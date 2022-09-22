package org.vitrivr.cineast.core.iiif.presentationapi;

import java.util.List;

public interface Manifest {

  /**
   * @return a list containing the Image API URL IDs of the image resources contained within this manifest.
   */
  List<String> getImageUrls();

  String getSummary();

  String getId();

  String getRequiredStatement();

  List<Object> getMetadata();
}
