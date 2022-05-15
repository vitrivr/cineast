package org.vitrivr.cineast.standalone.cli;

import com.github.rvesse.airline.annotations.Command;
import com.github.rvesse.airline.annotations.Option;
import java.util.ArrayList;
import java.util.List;
import org.vitrivr.cineast.core.config.QueryConfig;
import org.vitrivr.cineast.core.data.score.SegmentScoreElement;
import org.vitrivr.cineast.standalone.config.Config;
import org.vitrivr.cineast.standalone.config.ConstrainedQueryConfig;
import org.vitrivr.cineast.standalone.util.ContinuousRetrievalLogic;


@Command(name = "retrieve-mlt", description = "Retrieves objects from the database using an example segment. Equivalent to an MLT lookup.")
public class RetrieveCommand extends AbstractCineastCommand {

  @Option(name = {"-s", "--segmentid"}, title = "Segment ID", description = "The ID of the segment to use an example for retrieval.")
  private String segmentId;

  @Option(name = {"-c", "--category"}, title = "Category", description = "Name of the feature category to retrieve. By default, all categories are searched")
  private String category;

  @Option(name = {"-r", "--relevantSegments"}, title = "Relevant segments", description = "Comma separated list of segment IDs to which the query is to be limited.")
  private String relevantSegments;

  @Override
  public void execute() {
    final ContinuousRetrievalLogic retrieval = new ContinuousRetrievalLogic(Config.sharedConfig().getDatabase());

    QueryConfig qc = QueryConfig.newQueryConfigFromOther(new ConstrainedQueryConfig("cli-query", new ArrayList<>()));

    if (relevantSegments != null && !relevantSegments.isEmpty()) {
      String[] segments = relevantSegments.split(",");
      for (String segment : segments) {
        qc.addRelevantSegmentIds(segment);
      }
    }

    qc.setMaxResults(Config.sharedConfig().getRetriever().getMaxResults());
    qc.setResultsPerModule(Config.sharedConfig().getRetriever().getMaxResultsPerModule());

    final List<SegmentScoreElement> results = retrieval.retrieve(this.segmentId, this.category, qc);
    System.out.println("results:");
    results.forEach(System.out::println);
    System.out.println();
  }
}
