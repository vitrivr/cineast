package org.vitrivr.cineast.core.iiif.presentationapi;

import java.io.IOException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.vitrivr.cineast.core.iiif.imageapi.ImageFetcher;
import org.vitrivr.cineast.core.iiif.imageapi.ImageRequest;

/**
 * Takes a URL String to a manifest in the constructor and downloads all the images, image information and simplified metadata to the filesystem.
 */
public class ManifestFactory {

  private static final Logger LOGGER = LogManager.getLogger();
  private final Manifest manifest;

  public ManifestFactory(String manifestUrl) throws Exception {
    ManifestRequest manifestRequest = new ManifestRequest(manifestUrl);
    this.manifest = manifestRequest.parseManifest();
    if (manifest == null) {
      throw new Exception("Error occurred in parsing the manifest!");
    }
  }

  /**
   * Writes the {@link MetadataJson} generated from this {@link Manifest} to the filesystem
   *
   * @param jobDirectoryString The directory where the file has to be written to
   * @param filename           The name of the file with extension
   */
  public void saveMetadataJson(String jobDirectoryString, String filename) {
    MetadataJson metadataJson = new MetadataJson(manifest);
    try {
      metadataJson.saveToFile(jobDirectoryString, filename);
    } catch (IOException e) {
      LOGGER.error("Failed to save manifest metadata JSON to filesystem");
      e.printStackTrace();
    }
  }

  /**
   * Save all images in the canvasses along with their respective {@link MetadataJson} metadata.iiif files
   */
  public void saveAllCanvasImages(String jobDirectoryString) throws IOException {
    for (var imageUrl : manifest.getImageUrls()) {
      var imageRequest = ImageRequest.fromUrl(imageUrl);
      ImageFetcher.fetch(imageRequest, jobDirectoryString);
    }
  }
}
