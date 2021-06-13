package org.vitrivr.cineast.core.iiif.imageapi;

import static org.vitrivr.cineast.core.iiif.imageapi.BaseImageRequestBuilder.EXTENSION_JPG;
import static org.vitrivr.cineast.core.iiif.imageapi.BaseImageRequestBuilder.QUALITY_DEFAULT;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import javax.naming.OperationNotSupportedException;
import org.vitrivr.cineast.core.iiif.IIIFConfig;
import org.vitrivr.cineast.core.iiif.IIIFConfig.IIIFItem;
import org.vitrivr.cineast.core.iiif.imageapi.ImageInformation.IMAGE_API_VERSION;
import org.vitrivr.cineast.core.iiif.imageapi.v2_1_1.ImageInformation_v2_1_1;
import org.vitrivr.cineast.core.iiif.imageapi.v2_1_1.ImageRequestBuilder_v2_1_1;
import org.vitrivr.cineast.core.iiif.imageapi.v2_1_1.ImageRequestBuilder_v2_1_1_Impl;

/**
 * @author singaltanmay
 * @version 1.0
 * @created 10.06.21
 */
public class ImageRequestFactory {

  private final IMAGE_API_VERSION imageApiVersion;
  private final IIIFConfig iiifConfig;

  public ImageRequestFactory(IIIFConfig iiifConfig) {
    this.iiifConfig = iiifConfig;
    imageApiVersion = ImageInformation_v2_1_1.getImageApiVersionNumeric(iiifConfig.getImageApiVersion());
  }

  public List<ImageRequest> createImageRequests(String jobDirectoryString, String itemPrefixString) {
    if (imageApiVersion == IMAGE_API_VERSION.TWO_POINT_ONE_POINT_ONE) {
      return runImageApi_v2_1_1_job(jobDirectoryString, itemPrefixString);
    }
    return new LinkedList<>();
  }

  private List<ImageRequest> runImageApi_v2_1_1_job(String jobDirectoryString, String itemPrefixString) {
    List<ImageRequest> imageRequests = new LinkedList<>();
    List<IIIFItem> iiifItems = iiifConfig.getIiifItems();
    if (iiifItems == null) {
      return imageRequests;
    }
    for (final IIIFItem iiifItem : iiifItems) {
      String identifier = iiifItem.getIdentifier();
      final String imageName = itemPrefixString + identifier;

      ImageInformation_v2_1_1 imageInformation = null;
      try {
        final ImageInformationRequest informationRequest = new ImageInformationRequest(iiifConfig.getBaseUrl() + "/" + identifier);
        informationRequest.saveToFile(jobDirectoryString, imageName);
        imageInformation = informationRequest.getImageInformation(null);
      } catch (IOException e) {
        e.printStackTrace();
      }

      ImageRequestBuilder_v2_1_1 builder;
      if (imageInformation != null) {
        builder = new ImageRequestBuilder_v2_1_1_Impl(imageInformation);
      } else {
        builder = new ImageRequestBuilder_v2_1_1_Impl(iiifConfig.getBaseUrl());
      }

      float rotationDegree;
      if (iiifItem.getRotation() != null) {
        try {
          rotationDegree = Float.parseFloat(iiifItem.getRotation());
        } catch (NumberFormatException e) {
          e.printStackTrace();
          continue;
        }
      } else {
        rotationDegree = 0;
      }

      ImageRequest imageRequest = null;
      try {
        imageRequest = builder
            .setRegionFull()
            .setSizeFull()
            .setRotation(rotationDegree, false)
            .setQuality(QUALITY_DEFAULT)
            .setFormat(EXTENSION_JPG)
            .build();
      } catch (OperationNotSupportedException e) {
        e.printStackTrace();
      }

      try {
        if (imageRequest != null) {
          imageRequest.saveToFile(jobDirectoryString, imageName);
        }
      } catch (IOException e) {
        e.printStackTrace();
      }
      imageRequests.add(imageRequest);
    }
    return imageRequests;
  }
}
