package org.vitrivr.cineast.core.iiif.imageapi;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Image Metadata file containing technical and non-technical information about the image. This file must be convertible to a flat JSON and can only contain key-value pairs.
 */

public class ImageMetadata {

  private static final Logger LOGGER = LogManager.getLogger();
  @JsonInclude(Include.NON_NULL)
  private Long width;
  @JsonInclude(Include.NON_NULL)
  private Long height;
  @JsonInclude(Include.NON_NULL)
  private String region = null;
  @JsonInclude(Include.NON_NULL)
  private String size = null;
  @JsonInclude(Include.NON_NULL)
  private String rotation = null;
  @JsonInclude(Include.NON_NULL)
  private String quality;
  @JsonInclude(Include.NON_NULL)
  private String extension = null;
  @JsonInclude(Include.NON_NULL)
  private String description;
  @JsonInclude(Include.NON_NULL)
  private String attribution;
  /**
   * Link to the original IIIF resource from where this image was downloaded. This URL should not contain any configuration parameters such as region, size, rotation etc and show end at the identifier without the trailing '/'
   */
  @JsonInclude(Include.NON_NULL)
  private String resourceUrl;
  /**
   * URL of a manifest or canvas that this image is a part of
   */
  @JsonInclude(Include.NON_NULL)
  private String linkingUrl;
  /**
   * Label such as page number or chapter name
   */
  @JsonInclude(Include.NON_NULL)
  private String label;

  /**
   * Constructor function to initialize values of this object using an existing object
   */
  public static ImageMetadata from(ImageMetadata obj) {
    ImageMetadata imageMetadata = new ImageMetadata();
    if (obj != null) {
      imageMetadata
          .setHeight(obj.getHeight())
          .setWidth(obj.getWidth())
          .setRegion(obj.getRegion())
          .setSize(obj.getSize())
          .setRotation(obj.getRotation())
          .setQuality(obj.getQuality())
          .setExtension(obj.getExtension())
          .setDescription(obj.getDescription())
          .setAttribution(obj.getAttribution())
          .setResourceUrl(obj.getResourceUrl())
          .setLinkingUrl(obj.getLinkingUrl())
          .setLabel(obj.getLabel());
    }
    return imageMetadata;
  }

  /**
   * Sets the region, size, rotation, quality and extension parameters from the IIIF request
   */
  public void setIIIFParameters(ImageRequest imageRequest){
    this.setRegion(imageRequest.getRegion());
    this.setSize(imageRequest.getSize());
    this.setRotation(imageRequest.getRotation());
    this.setQuality(imageRequest.getQuality());
    this.setExtension(imageRequest.getExtension());
  }

  /**
   * Convert this object into a JSON string
   */
  public String toJsonString() throws JsonProcessingException {
    ObjectMapper objectMapper = new ObjectMapper();
    return objectMapper.writeValueAsString(this);
  }

  /**
   * Saves the metadata file to the filesystem as a JSON file with the extension .iiif
   */
  public void saveToFile(String filePath, String fileName) throws IOException {
    File file = new File(filePath + "/" + fileName + ".iiif");
    FileOutputStream fileOutputStream = new FileOutputStream(file);
    BufferedOutputStream bufferedOutputStream = new BufferedOutputStream(fileOutputStream);
    //write byte array to file
    bufferedOutputStream.write(this.toJsonString().getBytes());
    bufferedOutputStream.close();
    fileOutputStream.close();
    LOGGER.debug("Metadata associated with this image written to file successfully");
  }

  public Long getWidth() {
    return width;
  }

  public ImageMetadata setWidth(Long width) {
    this.width = width;
    return this;
  }

  public Long getHeight() {
    return height;
  }

  public ImageMetadata setHeight(Long height) {
    this.height = height;
    return this;
  }

  public String getQuality() {
    return quality;
  }

  public ImageMetadata setQuality(String quality) {
    this.quality = quality;
    return this;
  }

  public String getDescription() {
    return description;
  }

  public ImageMetadata setDescription(String description) {
    this.description = description;
    return this;
  }

  public String getAttribution() {
    return attribution;
  }

  public ImageMetadata setAttribution(String attribution) {
    this.attribution = attribution;
    return this;
  }

  public String getResourceUrl() {
    return resourceUrl;
  }

  public ImageMetadata setResourceUrl(String resourceUrl) {
    this.resourceUrl = resourceUrl;
    return this;
  }

  public String getLinkingUrl() {
    return linkingUrl;
  }

  public ImageMetadata setLinkingUrl(String linkingUrl) {
    this.linkingUrl = linkingUrl;
    return this;
  }

  public String getLabel() {
    return label;
  }

  public ImageMetadata setLabel(String label) {
    this.label = label;
    return this;
  }

  public String getRegion() {
    return region;
  }

  public ImageMetadata setRegion(String region) {
    this.region = region;
    return this;
  }

  public String getSize() {
    return size;
  }

  public ImageMetadata setSize(String size) {
    this.size = size;
    return this;
  }

  public String getRotation() {
    return rotation;
  }

  public ImageMetadata setRotation(String rotation) {
    this.rotation = rotation;
    return this;
  }

  public String getExtension() {
    return extension;
  }

  public ImageMetadata setExtension(String extension) {
    this.extension = extension;
    return this;
  }
}
