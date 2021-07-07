package org.vitrivr.cineast.core.iiif.imageapi;

import static org.vitrivr.cineast.core.iiif.imageapi.BaseImageRequestBuilder.EXTENSION_JPG;
import static org.vitrivr.cineast.core.iiif.imageapi.BaseImageRequestBuilder.QUALITY_DEFAULT;
import static org.vitrivr.cineast.core.iiif.imageapi.BaseImageRequestBuilder.REGION_FULL;
import static org.vitrivr.cineast.core.iiif.imageapi.BaseImageRequestBuilder.REGION_SQUARE;
import static org.vitrivr.cineast.core.iiif.imageapi.BaseImageRequestBuilder.SIZE_MAX;
import static org.vitrivr.cineast.core.iiif.imageapi.v2.ImageRequestBuilder_v2.SIZE_FULL;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import javax.naming.OperationNotSupportedException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.Nullable;
import org.vitrivr.cineast.core.iiif.IIIFConfig;
import org.vitrivr.cineast.core.iiif.IIIFItem;
import org.vitrivr.cineast.core.iiif.imageapi.ImageApiVersion.IMAGE_API_VERSION;
import org.vitrivr.cineast.core.iiif.imageapi.v2.ImageInformationRequest_v2;
import org.vitrivr.cineast.core.iiif.imageapi.v2.ImageInformation_v2;
import org.vitrivr.cineast.core.iiif.imageapi.v2.ImageRequestBuilder_v2;
import org.vitrivr.cineast.core.iiif.imageapi.v3.ImageInformationRequest_v3;
import org.vitrivr.cineast.core.iiif.imageapi.v3.ImageInformation_v3;
import org.vitrivr.cineast.core.iiif.imageapi.v3.ImageRequestBuilder_v3;
import org.vitrivr.cineast.core.iiif.presentationapi.v2.models.Canvas;
import org.vitrivr.cineast.core.iiif.presentationapi.v2.models.Image;

/**
 * Accepts an {@link IIIFConfig} and downloads the image file & image information JSON of all {@link IIIFItem} specified in the config file
 */
public class ImageRequestFactory {

  private static final Logger LOGGER = LogManager.getLogger();
  private final IIIFConfig iiifConfig;
  private final Canvas canvas;
  private ImageApiVersion imageApiVersion;
  private ImageMetadata globalMetadata;

  public ImageRequestFactory(IIIFConfig iiifConfig) {
    this.iiifConfig = iiifConfig;
    try {
      imageApiVersion = iiifConfig.getImageApiVersion();
    } catch (IllegalArgumentException e) {
      LOGGER.debug("No valid Image API version specified in the config file");
      imageApiVersion = null;
    }
    this.canvas = null;
  }

  public ImageRequestFactory(Canvas canvas, ImageMetadata globalMetadata) {
    this.canvas = canvas;
    this.globalMetadata = globalMetadata;
    this.iiifConfig = null;
  }

  private static boolean isParamStringValid(String configRegion) {
    return configRegion != null && configRegion.length() != 0;
  }

  public List<ImageRequest> createImageRequests(String jobDirectoryString, String itemPrefixString) {
    // If Image API version is not specified then auto-detect the highest API version supported by the server
    if (imageApiVersion == null) {
      LOGGER.debug("Image API version not specified. Starting detection of highest Image API version supported by the server.");
      String url = null;
      if (iiifConfig != null) {
        List<IIIFItem> iiifItems = iiifConfig.getIiifItems();
        if (iiifItems != null) {
          Optional<IIIFItem> iiifItem = iiifItems.stream().findAny();
          if (iiifItem.isPresent()) {
            String identifier = iiifItem.get().getIdentifier();
            url = iiifConfig.getBaseUrl() + "/" + identifier;
          }
        }
      } else if (canvas != null) {
        Optional<Image> image = canvas.getImages().stream().findAny();
        if (image.isPresent()) {
          Image it = image.get();
          String imageApiUrl = it.getResource().getAtId();
          ImageRequest imageRequest = ImageRequest.fromUrl(imageApiUrl);
          String baseUrl = imageRequest.getBaseUrl();
          if (!baseUrl.endsWith("/")) {
            baseUrl += "/";
          }
          url = baseUrl + "info.json";
        }
      }
      ImageApiVersion highestSupportedApiVersion = determineHighestSupportedApiVersion(url);
      if (highestSupportedApiVersion == null) {
        LOGGER.error("The Image API version could not be auto detected and thus Image Requests could not be created.");
        return Collections.emptyList();
      } else {
        this.imageApiVersion = highestSupportedApiVersion;
        LOGGER.debug("The highest supported Image API version was autodetected to version " + this.imageApiVersion.toNumericString());
      }
    }
    if (iiifConfig != null) {
      if (imageApiVersion.equals(new ImageApiVersion(IMAGE_API_VERSION.TWO_POINT_ONE_POINT_ONE))) {
        return new ApiJob_v2(iiifConfig).run(jobDirectoryString, itemPrefixString);
      } else if (imageApiVersion.equals(new ImageApiVersion(IMAGE_API_VERSION.THREE_POINT_ZERO))) {
        return new ApiJob_v3(iiifConfig).run(jobDirectoryString, itemPrefixString);
      }
    } else if (canvas != null) {
      runCanvasJob(jobDirectoryString, itemPrefixString);
    }
    return new LinkedList<>();
  }

  private List<ImageRequest> runCanvasJob(String jobDirectoryString, String itemPrefixString) {
    List<ImageRequest> imageRequests = new LinkedList<>();
    List<Image> images = canvas.getImages();
    if (images != null && images.size() != 0) {
      // Download all images in the canvas
      for (final Image image : images) {
        String imageApiUrl = image.getResource().getAtId();
        // Make image request to remote server
        ImageRequest imageRequest = ImageRequest.fromUrl(imageApiUrl);
        imageRequests.add(imageRequest);
        ImageMetadata imageMetadata = ImageMetadata.from(globalMetadata);
        imageMetadata.setResourceUrl(imageApiUrl);
        if (imageApiVersion.getVersion().equals(IMAGE_API_VERSION.TWO_POINT_ONE_POINT_ONE)) {
          try {
            String baseUrl = imageRequest.getBaseUrl();
            if (!baseUrl.endsWith("/")) {
              baseUrl += "/";
            }
            String imageInformationUrl = baseUrl + "info.json";
            final ImageInformationRequest_v2 informationRequest = new ImageInformationRequest_v2(imageInformationUrl);
            ImageInformation_v2 imageInformation_v2 = informationRequest.parseImageInformation();
            if (imageInformation_v2 != null) {
              imageMetadata.setHeight(imageInformation_v2.getHeight());
              imageMetadata.setWidth(imageInformation_v2.getWidth());
            }
          } catch (IOException e) {
            e.printStackTrace();
          }
        }
        // Write the downloaded image to the filesystem
        LOGGER.info("Downloading and saving image to the filesystem: " + image);
        String imageFilename = itemPrefixString + canvas.getLabel();
        try {
          imageRequest.saveToFile(jobDirectoryString, imageFilename, imageApiUrl);
        } catch (IOException e) {
          LOGGER.error("Failed to save image to file system: " + image);
          e.printStackTrace();
        }
        imageMetadata.setQuality(imageRequest.getQuality());
        try {
          imageMetadata.saveToFile(jobDirectoryString, imageFilename);
        } catch (IOException e) {
          e.printStackTrace();
        }
      }
    }
    return imageRequests;
  }

  /**
   * Determines the highest version of the Image API supported by the server by making an Image Information request and trying to parse as different versions of the {@link ImageInformation} starting with the highest version {@link ImageInformation_v3} and moving downwards
   */
  @Nullable
  public ImageApiVersion determineHighestSupportedApiVersion(String url) {
    String response = null;
    try {
      response = ImageInformationRequest.fetchImageInformation(url);
    } catch (IOException e) {
      LOGGER.error("An error occurred while making an Image Information request with the url: " + url);
      e.printStackTrace();
    }
    if (response != null) {
      try {
        new ObjectMapper().readValue(response, ImageInformation_v3.class);
        return new ImageApiVersion(IMAGE_API_VERSION.THREE_POINT_ZERO);
      } catch (JsonProcessingException e) {
        LOGGER.info("Server does not support Image API version " + ImageApiVersion.IMAGE_API_VERSION_3_0_NUMERIC);
      }
      try {
        new ObjectMapper().readValue(response, ImageInformation_v2.class);
        return new ImageApiVersion(IMAGE_API_VERSION.TWO_POINT_ONE_POINT_ONE);
      } catch (JsonProcessingException e) {
        LOGGER.info("Server does not support Image API version " + ImageApiVersion.IMAGE_API_VERSION_2_1_1_NUMERIC);
      }
    }
    return null;
  }

  private static class ApiJob_v2 {

    private static final Logger LOGGER = LogManager.getLogger();

    private final IIIFConfig iiifConfig;

    private ApiJob_v2(IIIFConfig iiifConfig) {
      this.iiifConfig = iiifConfig;
    }

    public ImageInformation_v2 parseImageInformation(String url) {
      ImageInformation_v2 imageInformation = null;
      try {
        final ImageInformationRequest_v2 informationRequest = new ImageInformationRequest_v2(url);
        imageInformation = informationRequest.parseImageInformation();
      } catch (IOException e) {
        e.printStackTrace();
      }
      return imageInformation;
    }

    private List<ImageRequest> run(String jobDirectoryString, String itemPrefixString) {
      // Set default values for global parameters with missing values
      initGlobalParameters();

      List<ImageRequest> imageRequests = new LinkedList<>();
      List<IIIFItem> iiifItems = iiifConfig.getIiifItems();
      if (iiifItems == null) {
        return imageRequests;
      }

      for (final IIIFItem iiifItem : iiifItems) {
        String identifier = iiifItem.getIdentifier();
        final String imageName = itemPrefixString + identifier;

        ImageInformation_v2 imageInformation = parseImageInformation(iiifConfig.getBaseUrl() + "/" + identifier);

        ImageRequestBuilder_v2 builder;
        if (imageInformation != null) {
          builder = new ImageRequestBuilder_v2(imageInformation);
        } else {
          builder = new ImageRequestBuilder_v2(iiifConfig.getBaseUrl());
        }

        ImageRequest imageRequest;
        try {
          setRequestParameters(iiifItem, builder);
          imageRequest = builder.build();
        } catch (OperationNotSupportedException e) {
          LOGGER.debug("Failed to make image request for IIIFConfig item: " + iiifItem);
          e.printStackTrace();
          continue;
        }

        try {
          imageRequest.saveToFile(jobDirectoryString, imageName);
        } catch (IOException e) {
          LOGGER.debug("Failed to save image: " + imageName);
          e.printStackTrace();
        }

        ImageMetadata imageMetadata = new ImageMetadata();
        imageMetadata.setQuality(imageRequest.getQuality())
            .setResourceUrl(imageRequest.getBaseUrl());
        try {
          imageMetadata.saveToFile(jobDirectoryString, imageName);
        } catch (IOException e) {
          e.printStackTrace();
        }

        imageRequests.add(imageRequest);
      }
      return imageRequests;
    }

    private void initGlobalParameters() {
      if (!isParamStringValid(iiifConfig.getRegion())) {
        iiifConfig.setRegion(REGION_FULL);
      }
      String configSize = iiifConfig.getSize();
      if (!isParamStringValid(configSize)) {
        iiifConfig.setSize(SIZE_FULL);
      }
      Float configRotation = iiifConfig.getRotation();
      if (configRotation == null) {
        iiifConfig.setRotation(0f);
      }
      String configQuality = iiifConfig.getQuality();
      if (!isParamStringValid(configQuality)) {
        iiifConfig.setQuality(QUALITY_DEFAULT);
      }
      String configFormat = iiifConfig.getFormat();
      if (!isParamStringValid(configFormat)) {
        iiifConfig.setFormat(EXTENSION_JPG);
      }
    }

    private void setRequestParameters(IIIFItem iiifItem, ImageRequestBuilder_v2 builder) throws OperationNotSupportedException {
      String region;
      if (isParamStringValid(iiifItem.getRegion())) {
        region = iiifItem.getRegion();
      } else {
        region = iiifConfig.getRegion();
      }
      iiifItem.setRegion(region);

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
        size = iiifConfig.getSize();
      }
      iiifItem.setSize(size);

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
        rotation = iiifConfig.getRotation();
      }
      iiifItem.setRotation(rotation);
      builder.setRotation(rotation, false);

      String quality;
      if (isParamStringValid(iiifItem.getQuality())) {
        quality = iiifItem.getQuality();
      } else {
        quality = iiifConfig.getQuality();
      }
      iiifItem.setQuality(quality);
      builder.setQuality(quality);

      String format;
      if (isParamStringValid(iiifItem.getFormat())) {
        format = iiifItem.getFormat();
      } else {
        format = iiifConfig.getFormat();
      }
      iiifItem.setFormat(format);
      builder.setFormat(format);
    }
  }

  private static class ApiJob_v3 {

    private static final Logger LOGGER = LogManager.getLogger();

    private final IIIFConfig iiifConfig;

    private ApiJob_v3(IIIFConfig iiifConfig) {
      this.iiifConfig = iiifConfig;
    }

    private List<ImageRequest> run(String jobDirectoryString, String itemPrefixString) {
      // Set default values for global parameters with missing values
      initGlobalParameters();

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

        ImageRequest imageRequest;
        try {
          setRequestParameters(iiifItem, builder);
          imageRequest = builder.build();
        } catch (OperationNotSupportedException e) {
          LOGGER.debug("Failed to make image request for IIIFConfig item: " + iiifItem);
          e.printStackTrace();
          continue;
        }

        try {
          imageRequest.saveToFile(jobDirectoryString, imageName);
        } catch (IOException e) {
          LOGGER.debug("Failed to save image: " + imageName);
          e.printStackTrace();
        }
        imageRequests.add(imageRequest);
      }
      return imageRequests;
    }

    private void initGlobalParameters() {
      if (!isParamStringValid(iiifConfig.getRegion())) {
        iiifConfig.setRegion(REGION_FULL);
      }
      String configSize = iiifConfig.getSize();
      if (!isParamStringValid(configSize)) {
        iiifConfig.setSize(SIZE_FULL);
      }
      Float configRotation = iiifConfig.getRotation();
      if (configRotation == null) {
        iiifConfig.setRotation(0f);
      }
      String configQuality = iiifConfig.getQuality();
      if (!isParamStringValid(configQuality)) {
        iiifConfig.setQuality(QUALITY_DEFAULT);
      }
      String configFormat = iiifConfig.getFormat();
      if (!isParamStringValid(configFormat)) {
        iiifConfig.setFormat(EXTENSION_JPG);
      }
    }

    private void setRequestParameters(IIIFItem iiifItem, ImageRequestBuilder_v3 builder) throws OperationNotSupportedException {
      String region;
      if (isParamStringValid(iiifItem.getRegion())) {
        region = iiifItem.getRegion();
      } else {
        region = iiifConfig.getRegion();
      }
      iiifItem.setRegion(region);

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
        size = iiifConfig.getSize();
      }
      iiifItem.setSize(size);

      if (SIZE_FULL.equals(size) || SIZE_MAX.equals(size)) {
        builder.setSizeMaxNotUpscaled();
      }

      Float rotation;
      if (iiifItem.getRotation() != null) {
        rotation = iiifItem.getRotation();
      } else {
        rotation = iiifConfig.getRotation();
      }
      iiifItem.setRotation(rotation);
      builder.setRotation(rotation, false);

      String quality;
      if (isParamStringValid(iiifItem.getQuality())) {
        quality = iiifItem.getQuality();
      } else {
        quality = iiifConfig.getQuality();
      }
      iiifItem.setQuality(quality);
      builder.setQuality(quality);

      String format;
      if (isParamStringValid(iiifItem.getFormat())) {
        format = iiifItem.getFormat();
      } else {
        format = iiifConfig.getFormat();
      }
      iiifItem.setFormat(format);
      builder.setFormat(format);
    }
  }
}
