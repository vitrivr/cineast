package org.vitrivr.cineast.core.iiif.discoveryapi.v1;

import java.io.IOException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.vitrivr.cineast.core.iiif.discoveryapi.v1.models.OrderedCollection;
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
  public void saveAllCreatedImages(String jobDirectoryString, String filenamePrefix){
    //TODO
    LOGGER.info("Ordered collection: "  + collection);
  }
}
