package org.vitrivr.cineast.core.iiif.imageapi.v2_1_1;

import org.vitrivr.cineast.core.iiif.imageapi.BaseImageRequestBuilder;

/**
 * @author singaltanmay
 * @version 1.0
 * @created 09.06.21
 */
public interface ImageRequestBuilder_v2_1_1 extends BaseImageRequestBuilder {

  String SIZE_FULL = "full";

  /** The image or region is not scaled, and is returned at its full size. */
  ImageRequestBuilder_v2_1_1 setSizeFull();

}
