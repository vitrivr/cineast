package org.vitrivr.cineast.standalone.cli;

import com.github.rvesse.airline.annotations.Command;
import com.github.rvesse.airline.annotations.Option;
import java.util.List;
import java.util.Optional;
import org.vitrivr.cineast.core.data.entities.MediaObjectMetadataDescriptor;
import org.vitrivr.cineast.core.data.entities.MediaSegmentDescriptor;
import org.vitrivr.cineast.core.db.DBSelector;
import org.vitrivr.cineast.core.db.dao.reader.MediaObjectMetadataReader;
import org.vitrivr.cineast.core.db.dao.reader.MediaObjectReader;
import org.vitrivr.cineast.core.db.dao.reader.MediaSegmentMetadataReader;
import org.vitrivr.cineast.core.db.dao.reader.MediaSegmentReader;
import org.vitrivr.cineast.standalone.config.Config;
import org.vitrivr.cineast.standalone.config.RetrievalRuntimeConfig;


/**
 * @author Silvan Heller
 * @version 1.0
 */
@Command(name = "retrieve-single", description = "Retrieves all information from the database for a given segment / object.")
public class SingleObjRetrievalCommand implements Runnable {

  @Option(name = {"--segmentid"}, title = "Segment ID", description = "The ID of the segment for which to retrieve detailed information.")
  private String segmentId;

  @Option(name = {"--objectid"}, title = "Object ID", description = "The ID of the object for which to retrieve detailed information.")
  private String objectId;

  public void run() {
    DBSelector selector = Config.sharedConfig().getDatabase().getSelectorSupplier().get();
    if (segmentId != null) {
      printInfoForSegment(segmentId, selector);
    }
    if (objectId != null) {
      printInfoForObject(objectId, selector);
    }
  }

  public static void printInfoForObject(String objectId, DBSelector selector) {
    System.out.println("= Retrieving object information for " + objectId + " =");
    MediaObjectReader objectReader = new MediaObjectReader(selector);
    System.out.println(objectReader.lookUpObjectById(objectId));

    System.out.println("= Retrieving object metadata for =");
    MediaObjectMetadataReader reader = new MediaObjectMetadataReader(selector);
    List<MediaObjectMetadataDescriptor> metadataDescriptors = reader.lookupMultimediaMetadata(objectId);
    metadataDescriptors.forEach(System.out::println);
  }

  public static void printInfoForSegment(String segmentId, DBSelector selector) {

    System.out.println("= Retrieving segment information for " + segmentId + "=");
    MediaSegmentReader segmentReader = new MediaSegmentReader(selector);
    Optional<MediaSegmentDescriptor> segmentDescriptor = segmentReader.lookUpSegment(segmentId);
    segmentDescriptor.ifPresent(System.out::println);

    segmentDescriptor.ifPresent(descriptor -> printInfoForObject(descriptor.getObjectId(), selector));

    System.out.println("= Retrieving segment metadata =");
    MediaSegmentMetadataReader reader = new MediaSegmentMetadataReader(selector);
    reader.lookupMultimediaMetadata(segmentId).forEach(System.out::println);

    System.out.println("Retrieving all columns for segment " + segmentId);
    RetrievalRuntimeConfig retrievalRuntimeConfig = Config.sharedConfig().getRetriever();

    retrievalRuntimeConfig.getRetrieverCategories().forEach(cat -> retrievalRuntimeConfig.getRetrieversByCategory(cat).forEachEntry((retriever, weight) -> {
      System.out.println("= Retrieving for feature: " + retriever.getClass().getSimpleName() + " =");
      retriever.getTableNames().forEach(tableName -> {
        selector.open(tableName);
        selector.getRows("id", segmentId).forEach(row -> {
          System.out.println("== New row == ");
          row.forEach((key, value) -> System.out.println(tableName + "." + key + " - " + value));
        });
      });
      return true;
    }));

    System.out.println("Done");
  }
}
