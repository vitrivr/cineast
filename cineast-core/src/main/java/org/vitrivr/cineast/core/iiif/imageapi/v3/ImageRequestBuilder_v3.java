package org.vitrivr.cineast.core.iiif.imageapi.v3;

import javax.naming.OperationNotSupportedException;
import org.vitrivr.cineast.core.iiif.imageapi.ImageRequest;

/**
 * @author singaltanmay
 * @version 1.0
 * @created 10.06.21
 */
public interface ImageRequestBuilder_v3 {

  String SIZE_MAX_NOT_UPSCALED = "max";
  String SIZE_MAX_UPSCALED = "^max";
  String SIZE_PERCENTAGE_NOT_UPSCALED = "pct:";
  String SIZE_PERCENTAGE_UPSCALED = "^pct:";

  /**
   * The full image is returned, without any cropping.
   *
   * @return this {@link ImageRequestBuilder_v3}
   */
  ImageRequestBuilder_v3 setRegionFull();

  /**
   * The region is defined as an area where the width and height are both equal to the length of the shorter dimension of the full image. The region may be positioned anywhere in the longer dimension of the full image at the server’s discretion, and centered is often a reasonable default.
   *
   * @return this {@link ImageRequestBuilder_v3}
   */
  ImageRequestBuilder_v3 setRegionSquare() throws OperationNotSupportedException;

  /**
   * The region of the full image to be returned is specified in terms of absolute pixel values.
   *
   * @param x Represents the number of pixels from the 0 position on the horizontal axis
   * @param y Represents the number of pixels from the 0 position on the vertical axis
   * @param w Represents the width of the region
   * @param h Represents the height of the region in pixels
   * @return this {@link ImageRequestBuilder_v3}
   */
  ImageRequestBuilder_v3 setRegionAbsolute(float x, float y, float w, float h) throws OperationNotSupportedException;

  /**
   * The region to be returned is specified as a sequence of percentages of the full image’s dimensions, as reported in the image information document.
   *
   * @param x Represents the number of pixels from the 0 position on the horizontal axis, calculated as a percentage of the reported width
   * @param y Represents the number of pixels from the 0 position on the vertical axis, calculated as a percentage of the reported height
   * @param w Represents the width of the region, calculated as a percentage of the reported width
   * @param h Represents the height of the region, calculated as a percentage of the reported height
   * @return this {@link ImageRequestBuilder_v3}
   */
  ImageRequestBuilder_v3 setRegionPercentage(float x, float y, float w, float h) throws OperationNotSupportedException;

  /**
   * The extracted region is scaled to the maximum size permitted by maxWidth, maxHeight, or maxArea. If the resulting dimensions are greater than the pixel width and height of the extracted region, the extracted region is upscaled.
   *
   * @return this {@link ImageRequestBuilder_v3}
   */
  ImageRequestBuilder_v3 setSizeMaxUpscaled();

  /**
   * The extracted region is returned at the maximum size available, but will not be upscaled. The resulting image will have the pixel dimensions of the extracted region, unless it is constrained to a smaller size by maxWidth, maxHeight, or maxArea
   *
   * @return this {@link ImageRequestBuilder_v3}
   */
  ImageRequestBuilder_v3 setSizeMaxNotUpscaled();

  /**
   * Returns an image scaled to the exact dimensions given in the parameters. If only height or width are provided then image is scaled to that dimension while maintaining the aspect ratio. If both height and width are given then image is scaled to those dimensions by ignoring the aspect ratio.
   *
   * @param w The extracted region should be scaled so that the width of the returned image is exactly equal to w. If w is greater than the pixel width of the extracted region, the extracted region is upscaled.
   * @param h The extracted region should be scaled so that the height of the returned image is exactly equal to h. If h is greater than the pixel height of the extracted region, the extracted region is upscaled.
   * @return this {@link ImageRequestBuilder_v3}
   * @throws IllegalArgumentException If both height and width are undefined then an IllegalArgumentException is thrown
   */
  ImageRequestBuilder_v3 setSizeScaledExactUpscaled(Float w, Float h) throws IllegalArgumentException;

  /**
   * Returns an image scaled to the exact dimensions given in the parameters. If only height or width are provided then image is scaled to that dimension while maintaining the aspect ratio. If both height and width are given then image is scaled to those dimensions by ignoring the aspect ratio.
   *
   * @param w The extracted region should be scaled so that the width of the returned image is exactly equal to w. The value of w must not be greater than the width of the extracted region.
   * @param h The extracted region should be scaled so that the height of the returned image is exactly equal to h. The value of h must not be greater than the height of the extracted region.
   * @return this {@link ImageRequestBuilder_v3}
   * @throws IllegalArgumentException If both height and width are undefined or greater than maxHeight or maxWidth then an IllegalArgumentException is thrown
   */
  ImageRequestBuilder_v3 setSizeScaledExactNotUpscaled(Float w, Float h) throws IllegalArgumentException;

  /**
   * The extracted region is scaled so that the width and height of the returned image are not greater than w and h, while maintaining the aspect ratio. The returned image must be as large as possible but not larger than the extracted region, w or h, or server-imposed limits.
   *
   * @return this {@link ImageRequestBuilder_v3}
   * @throws IllegalArgumentException Behaviour of server when both width and height are overridable is undefined
   */
  ImageRequestBuilder_v3 setSizeScaledBestFitNotUpscaled(float w, float h, boolean isWidthOverridable, boolean isHeightOverridable) throws IllegalArgumentException;

  /**
   * The extracted region is scaled so that the width and height of the returned image are not greater than w and h, while maintaining the aspect ratio. The returned image must be as large as possible but not larger than w, h, or server-imposed limits.
   *
   * @return this {@link ImageRequestBuilder_v3}
   * @throws IllegalArgumentException Behaviour of server when both width and height are overridable is undefined
   */
  ImageRequestBuilder_v3 setSizeScaledBestFitUpscaled(float w, float h, boolean isWidthOverridable, boolean isHeightOverridable) throws IllegalArgumentException;

  /**
   * The width and height of the returned image is scaled to n percent of the width and height of the extracted region. The value of n must not be greater than 100.
   *
   * @return this {@link ImageRequestBuilder_v3}
   */
  ImageRequestBuilder_v3 setSizePercentageNotUpscaled(float n);

  /**
   * The width and height of the returned image is scaled to n percent of the width and height of the extracted region. For values of n greater than 100, the extracted region is upscaled.
   *
   * @return this {@link ImageRequestBuilder_v3}
   */
  ImageRequestBuilder_v3 setSizePercentageUpscaled(float n);

  /**
   * This method is used to specify the mirroring and rotation applied to the image.
   *
   * @param degree Represents the number of degrees of clockwise rotation, and may be any floating point number from 0 to 360.
   * @param mirror Indicates if that the image should be mirrored by reflection on the vertical axis before any rotation is applied.
   * @return this {@link ImageRequestBuilder_v3}
   */
  ImageRequestBuilder_v3 setRotation(float degree, boolean mirror) throws OperationNotSupportedException;

  /**
   * This method is used to specify the quality of the image.
   *
   * @param quality The quality of the image
   * @return this {@link ImageRequestBuilder_v3}
   */
  ImageRequestBuilder_v3 setQuality(String quality) throws OperationNotSupportedException;

  /**
   * This method is used to specify the file extension of the image.
   *
   * @param extension The file extension of the image
   * @return this {@link ImageRequestBuilder_v3}
   */
  ImageRequestBuilder_v3 setFormat(String extension) throws OperationNotSupportedException;

  /** This method builds a new ImageRequest with the parameters set using the dedicated setter methods */
  ImageRequest build();

}
