package org.vitrivr.cineast.standalone.cli;

import com.github.rvesse.airline.annotations.Command;
import com.github.rvesse.airline.annotations.Option;
import java.util.Optional;
import org.vitrivr.cineast.core.data.entities.MediaSegmentDescriptor;
import org.vitrivr.cineast.core.db.DBSelector;
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

  @Option(name = {"-s", "--segmentid"}, title = "Segment ID", description = "The ID of the segment to use an example for retrieval.")
  private String segmentId;

  public void run() {
    DBSelector selector = Config.sharedConfig().getDatabase().getSelectorSupplier().get();

    System.out.println("= Retrieving segment information =");
    MediaSegmentReader segmentReader = new MediaSegmentReader(selector);
    Optional<MediaSegmentDescriptor> segmentDescriptor = segmentReader.lookUpSegment(segmentId);
    segmentDescriptor.ifPresent(System.out::println);

    System.out.println("= Retrievin corresponding object information if it exists");
    MediaObjectReader objectReader = new MediaObjectReader(selector);
    segmentDescriptor.ifPresent(descriptor -> System.out.println(objectReader.lookUpObjectById(descriptor.getObjectId())));

    System.out.println("= Retrieving metadata =");
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
