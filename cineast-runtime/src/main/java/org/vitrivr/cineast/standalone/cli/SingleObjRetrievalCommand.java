package org.vitrivr.cineast.standalone.cli;

import com.github.rvesse.airline.annotations.Command;
import com.github.rvesse.airline.annotations.Option;
import org.vitrivr.cineast.core.db.DBSelector;
import org.vitrivr.cineast.standalone.config.Config;

@Command(name = "retrieve-single", description = "Retrieves all information from the database for a given segment / object.")
public class SingleObjRetrievalCommand implements Runnable {

  @Option(name = {"--segmentid"}, title = "Segment ID", description = "The ID of the segment for which to retrieve detailed information.")
  private String segmentId;

  @Option(name = {"--objectid"}, title = "Object ID", description = "The ID of the object for which to retrieve detailed information.")
  private String objectId;

  @Option(name = {"-c", "--category"}, title = "Category", description = "Name of the feature category to retrieve. By default, all categories are retrieved for a segment.")
  private String category;

  public void run() {
    DBSelector selector = Config.sharedConfig().getDatabase().getSelectorSupplier().get();
    if (segmentId != null) {
      CliUtils.printInfoForSegment(segmentId, selector, category);
    }
    if (objectId != null) {
      CliUtils.printInfoForObject(objectId, selector);
    }
  }

}
