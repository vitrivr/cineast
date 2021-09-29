package org.vitrivr.cineast.core.iiif.presentationapi.v2;

import java.io.IOException;
import java.util.List;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.vitrivr.cineast.core.iiif.imageapi.ImageFactory;
import org.vitrivr.cineast.core.iiif.imageapi.ImageMetadata;
import org.vitrivr.cineast.core.iiif.presentationapi.v2.models.Canvas;
import org.vitrivr.cineast.core.iiif.presentationapi.v2.models.Manifest;
import org.vitrivr.cineast.core.iiif.presentationapi.v2.models.Sequence;

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
   * @param filename The name of the file with extension
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
  public void saveAllCanvasImages(String jobDirectoryString, String filenamePrefix) {
    List<Sequence> sequences = manifest.getSequences();
    if (sequences != null && sequences.size() != 0) {
      // Setting global metadata values that are common for every image in this sequence
      ImageMetadata globalMetadata = new ImageMetadata()
          .setDescription(manifest.getDescription())
          .setLinkingUrl(manifest.getAtId())
          .setAttribution(manifest.getAttribution());
      for (Sequence sequence : sequences) {
        List<Canvas> canvases = sequence.getCanvases();
        if (canvases != null && canvases.size() != 0) {
          for (final Canvas canvas : canvases) {
            ImageFactory imageFactory = new ImageFactory(canvas, globalMetadata);
            imageFactory.fetchImages(jobDirectoryString, filenamePrefix);
          }
        }
      }
    }
  }
}
