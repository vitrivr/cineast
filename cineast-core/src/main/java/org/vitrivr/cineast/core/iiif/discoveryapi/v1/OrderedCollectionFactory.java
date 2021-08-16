package org.vitrivr.cineast.core.iiif.discoveryapi.v1;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.UUID;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.vitrivr.cineast.core.iiif.discoveryapi.v1.models.IdTypeObject;
import org.vitrivr.cineast.core.iiif.discoveryapi.v1.models.OrderedCollection;
import org.vitrivr.cineast.core.iiif.discoveryapi.v1.models.OrderedCollectionPage;
import org.vitrivr.cineast.core.iiif.discoveryapi.v1.models.OrderedItem;
import org.vitrivr.cineast.core.iiif.presentationapi.v2.ManifestFactory;
import org.vitrivr.cineast.core.iiif.presentationapi.v2.MetadataJson;

public class OrderedCollectionFactory {

  private static final Logger LOGGER = LogManager.getLogger();
  private final OrderedCollection collection;

  public OrderedCollectionFactory(String collectionUrl) throws Exception {
    OrderedCollectionRequest collectionRequest = new OrderedCollectionRequest(collectionUrl);
    this.collection = collectionRequest.parseOrderedCollection();
    if (collection == null) {
      throw new Exception("Error occurred in parsing the manifest!");
    }
  }

  /**
   * Save all the newly created images in the Ordered Collection Pages' manifests along with their respective {@link MetadataJson} metadata.iiif files
   */
  public void saveAllCreatedImages(String jobDirectoryString, String filenamePrefix) throws Exception {
    LOGGER.info("Ordered collection: " + collection);
    IdTypeObject nextPage = collection.getFirst();
    while (nextPage != null && nextPage.getId() != null) {
      OrderedCollectionPage orderedCollectionPage = OrderedCollectionPage.fromUrl(nextPage.getId());
      List<OrderedItem> orderedItems = orderedCollectionPage.getOrderedItems();
      for (OrderedItem orderedItem : orderedItems) {
        /* Only download images whose type is Create **/
        if (orderedItem.getType().equals(OrderedCollection.TYPE_CREATE)) {
          ManifestFactory manifestFactory = null;
          try {
            manifestFactory = new ManifestFactory(orderedItem.getObject().getId());
          } catch (Exception e) {
            e.printStackTrace();
          }
          if (manifestFactory != null) {
            String jobIdentifier = UUID.randomUUID().toString();
            String manifestJobDirectoryString = jobDirectoryString + "/manifest_job_" + jobIdentifier;
            Path manifestJobDirectory = Paths.get(manifestJobDirectoryString);
            if (!Files.exists(manifestJobDirectory)) {
              try {
                Files.createDirectories(manifestJobDirectory);
              } catch (IOException e) {
                e.printStackTrace();
              }
            }
            manifestFactory.saveMetadataJson(manifestJobDirectoryString, "metadata_" + jobIdentifier);
            manifestFactory.saveAllCanvasImages(manifestJobDirectoryString, "image_" + jobIdentifier + "_");
          }
        }
      }
      nextPage = orderedCollectionPage.getNext();
    }
  }
}
