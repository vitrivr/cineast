package org.vitrivr.cineast.core.iiif.imageapi;

import java.io.IOException;
import org.apache.commons.io.FilenameUtils;
import org.vitrivr.cineast.core.iiif.IIIFItem;
import org.vitrivr.cineast.core.iiif.UnsupportedIIIFAPIException;

public class ImageFetcher {

  public static void fetch(IIIFItem item, String directory) throws IOException, UnsupportedIIIFAPIException {
    var imageRequest = prepareImageRequest(item);
    fetch(imageRequest, directory);
  }

  public static void fetch(ImageRequest imageRequest, String directory) throws IOException {
    imageRequest.downloadImage(directory, FilenameUtils.getBaseName(imageRequest.getBaseUrl()));
  }


  public static ImageRequest prepareImageRequest(IIIFItem item) throws IOException, UnsupportedIIIFAPIException {
    var imageRequest = ImageRequest.fromBaseUrl(item.identifier);
    return imageRequest
        .setRegion(item.region == null ? imageRequest.getRegion() : item.region)
        .setSize(item.size == null ? imageRequest.getSize() : item.size)
        .setRotation(item.rotation == null ? imageRequest.getRotation() : item.rotation.toString())
        .setQuality(item.quality == null ? imageRequest.getQuality() : item.quality)
        .setExtension(item.format == null ? imageRequest.getExtension() : item.format);
  }
}
