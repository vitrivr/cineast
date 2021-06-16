package org.vitrivr.cineast.core.iiif.imageapi;

import static org.vitrivr.cineast.core.iiif.imageapi.BaseImageRequestBuilder.EXTENSION_JPG;
import static org.vitrivr.cineast.core.iiif.imageapi.BaseImageRequestBuilder.QUALITY_DEFAULT;
import static org.vitrivr.cineast.core.iiif.imageapi.BaseImageRequestBuilder.REGION_FULL;
import static org.vitrivr.cineast.core.iiif.imageapi.BaseImageRequestBuilder.REGION_SQUARE;
import static org.vitrivr.cineast.core.iiif.imageapi.BaseImageRequestBuilder.SIZE_MAX;
import static org.vitrivr.cineast.core.iiif.imageapi.v2.ImageRequestBuilder_v2.SIZE_FULL;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import javax.naming.OperationNotSupportedException;
import org.vitrivr.cineast.core.iiif.IIIFConfig;
import org.vitrivr.cineast.core.iiif.IIIFConfig.IIIFItem;
import org.vitrivr.cineast.core.iiif.imageapi.ImageApiVersion.IMAGE_API_VERSION;
import org.vitrivr.cineast.core.iiif.imageapi.v2.ImageInformationRequest_v2;
import org.vitrivr.cineast.core.iiif.imageapi.v2.ImageInformation_v2;
import org.vitrivr.cineast.core.iiif.imageapi.v2.ImageRequestBuilder_v2;
import org.vitrivr.cineast.core.iiif.imageapi.v3.ImageInformationRequest_v3;
import org.vitrivr.cineast.core.iiif.imageapi.v3.ImageInformation_v3;
import org.vitrivr.cineast.core.iiif.imageapi.v3.ImageRequestBuilder_v3;

/**
 * @author singaltanmay
 * @version 1.0
 * @created 10.06.21
 */
public class ImageRequestFactory {

  private final ImageApiVersion imageApiVersion;
  private final IIIFConfig iiifConfig;

  public ImageRequestFactory(IIIFConfig iiifConfig) {
    this.iiifConfig = iiifConfig;
    imageApiVersion = iiifConfig.getImageApiVersion();
  }

  public List<ImageRequest> createImageRequests(String jobDirectoryString, String itemPrefixString) {
    if (imageApiVersion.equals(new ImageApiVersion(IMAGE_API_VERSION.TWO_POINT_ONE_POINT_ONE))) {
      return new ApiJob_v2().run(jobDirectoryString, itemPrefixString);
    } else if (imageApiVersion.equals(new ImageApiVersion(IMAGE_API_VERSION.THREE_POINT_ZERO))) {
      runImageApi_v3_0_job(jobDirectoryString, itemPrefixString);
    }
    return new LinkedList<>();
  }


  private boolean isParamStringValid(String configRegion) {
    return configRegion != null && configRegion.length() != 0;
  }

  private List<ImageRequest> runImageApi_v3_0_job(String jobDirectoryString, String itemPrefixString) {
    List<ImageRequest> imageRequests = new LinkedList<>();
    List<IIIFItem> iiifItems = iiifConfig.getIiifItems();
    if (iiifItems == null) {
      return imageRequests;
    }
    for (final IIIFItem iiifItem : iiifItems) {
      String identifier = iiifItem.getIdentifier();
      final String imageName = itemPrefixString + identifier;

      ImageInformation_v3 imageInformation = null;
      try {
        final ImageInformationRequest_v3 informationRequest = new ImageInformationRequest_v3(iiifConfig.getBaseUrl() + "/" + identifier);
        informationRequest.saveToFile(jobDirectoryString, imageName);
        imageInformation = informationRequest.parseImageInformation(null);
      } catch (IOException e) {
        e.printStackTrace();
      }

      ImageRequestBuilder_v3 builder;
      if (imageInformation != null) {
        builder = new ImageRequestBuilder_v3(imageInformation);
      } else {
        builder = new ImageRequestBuilder_v3(iiifConfig.getBaseUrl());
      }

      float rotationDegree;
      if (iiifItem.getRotation() != null) {
        try {
          rotationDegree = iiifItem.getRotation();
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
            .setSizeMaxNotUpscaled()
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

  private class ApiJob_v2 {

    private List<ImageRequest> run(String jobDirectoryString, String itemPrefixString) {
      // Set default values for global parameters with missing values
      String configRegion = iiifConfig.getRegion();
      if (!isParamStringValid(configRegion)) {
        configRegion = REGION_FULL;
        iiifConfig.setRegion(configRegion);
      }
      String configSize = iiifConfig.getSize();
      if (!isParamStringValid(configSize)) {
        configSize = SIZE_FULL;
        iiifConfig.setSize(configSize);
      }
      Float configRotation = iiifConfig.getRotation();
      if (configRotation == null) {
        configRotation = 0f;
        iiifConfig.setRotation(configRotation);
      }
      String configQuality = iiifConfig.getQuality();
      if (!isParamStringValid(configQuality)) {
        configQuality = QUALITY_DEFAULT;
        iiifConfig.setQuality(configQuality);
      }
      String configFormat = iiifConfig.getFormat();
      if (!isParamStringValid(configFormat)) {
        configFormat = EXTENSION_JPG;
        iiifConfig.setFormat(configFormat);
      }

      List<ImageRequest> imageRequests = new LinkedList<>();
      List<IIIFItem> iiifItems = iiifConfig.getIiifItems();
      if (iiifItems == null) {
        return imageRequests;
      }

      for (final IIIFItem iiifItem : iiifItems) {
        String identifier = iiifItem.getIdentifier();
        final String imageName = itemPrefixString + identifier;

        ImageInformation_v2 imageInformation = null;
        try {
          final ImageInformationRequest_v2 informationRequest = new ImageInformationRequest_v2(iiifConfig.getBaseUrl() + "/" + identifier);
          informationRequest.saveToFile(jobDirectoryString, imageName);
          imageInformation = informationRequest.parseImageInformation(null);
        } catch (IOException e) {
          e.printStackTrace();
        }

        ImageRequestBuilder_v2 builder;
        if (imageInformation != null) {
          builder = new ImageRequestBuilder_v2(imageInformation);
        } else {
          builder = new ImageRequestBuilder_v2(iiifConfig.getBaseUrl());
        }

        ImageRequest imageRequest = null;
        try {
          String region;
          if (isParamStringValid(iiifItem.getRegion())) {
            region = iiifItem.getRegion();
          } else {
            region = configRegion;
          }

          switch (region) {
            case REGION_FULL:
              builder.setRegionFull();
              break;
            case REGION_SQUARE:
              builder.setRegionSquare();
              break;
          }

          String size;
          if (isParamStringValid(iiifItem.getSize())) {
            size = iiifItem.getSize();
          } else {
            size = configSize;
          }

          switch (size) {
            case SIZE_FULL:
              builder.setSizeFull();
              break;
            case SIZE_MAX:
              builder.setSizeMax();
              break;
          }

          Float rotation;
          if (iiifItem.getRotation() != null) {
            rotation = iiifItem.getRotation();
          } else {
            rotation = configRotation;
          }
          builder.setRotation(rotation, false);

          String quality;
          if (isParamStringValid(iiifItem.getQuality())) {
            quality = iiifItem.getQuality();
          } else {
            quality = configQuality;
          }
          builder.setQuality(quality);

          String format;
          if (isParamStringValid(iiifItem.getFormat())) {
            format = iiifItem.getFormat();
          } else {
            format = configFormat;
          }
          builder.setFormat(format);

          imageRequest = builder.build();
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
}
