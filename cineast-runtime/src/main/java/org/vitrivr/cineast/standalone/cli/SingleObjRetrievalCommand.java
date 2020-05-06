package org.vitrivr.cineast.standalone.cli;

import com.github.rvesse.airline.annotations.Command;
import com.github.rvesse.airline.annotations.Option;
import org.vitrivr.cineast.core.db.DBSelector;
import org.vitrivr.cineast.standalone.config.Config;

@Command(name = "retrieve-single", description = "Retrieves all information from the database for a given segment / object.")
public class SingleObjRetrievalCommand implements Runnable {

  @Option(name = {"--segmentid"}, title = "Segment ID", description = "The ID of the segment for which to retrieve detailed information.")
  private String segmentId;

  @Option(name = {"--lb"}, title = "Lower Bound Segment ID", description = "The lower bound of the segmentids for which to retrieve detailed information.")
  private String lower;

  @Option(name = {"--ub"}, title = "Upper Bound Segment ID", description = "The upper bound of the segmentids for which to retrieve detailed information.")
  private String upper;

  @Option(name = {"--objectid"}, title = "Object ID", description = "The ID of the object for which to retrieve detailed information.")
  private String objectId;

  @Option(name = {"-c", "--category"}, title = "Category", description = "Name of the feature category to retrieve. By default, all categories are retrieved for a segment.")
  private String category;

  public void run() {
    DBSelector selector = Config.sharedConfig().getDatabase().getSelectorSupplier().get();
    if (segmentId != null) {
      CliUtils.printInfoForSegment(segmentId, selector, category, true);
    }
    if (lower != null && upper != null) {
      int lb = Integer.parseInt(lower.split("_")[2]);
      int ub = Integer.parseInt(upper.split("_")[2]);
      for (int i = lb; i <= ub; i++) {
        CliUtils.printInfoForSegment(lower.replace(String.valueOf(lb), String.valueOf(i)), selector, category, false);
      }
    }
    if (objectId != null) {
      CliUtils.printInfoForObject(objectId, selector);
    }
  }

}
