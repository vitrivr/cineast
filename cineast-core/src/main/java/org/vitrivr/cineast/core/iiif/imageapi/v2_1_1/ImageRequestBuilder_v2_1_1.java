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

  /**
   * Returns an image scaled to the exact dimensions given in the parameters. If only height or width are provided then image is scaled to that dimension while maintaining the aspect ratio. If both height and width are given then image is scaled to those dimensions by ignoring the aspect ratio.
   *
   * @return this {@link BaseImageRequestBuilder}
   * @throws IllegalArgumentException If both height and width are undefined then an IllegalArgumentException is thrown
   */
  @Override
  BaseImageRequestBuilder setSizeScaledExact(Float width, Float height) throws IllegalArgumentException;

  /**
   * The image content is scaled for the best fit such that the resulting width and height are less than or equal to the requested width and height. The exact scaling may be determined by the service provider, based on characteristics including image quality and system performance. The dimensions of the returned image content are calculated to maintain the aspect ratio of the extracted region.
   *
   * @return this {@link ImageRequestBuilder_v2_1_1_Impl}
   * @throws IllegalArgumentException Behaviour of server when both width and height are overridable is undefined
   */
  @Override
  BaseImageRequestBuilder setSizeScaledBestFit(float width, float height, boolean isWidthOverridable, boolean isHeightOverridable) throws IllegalArgumentException;
}
