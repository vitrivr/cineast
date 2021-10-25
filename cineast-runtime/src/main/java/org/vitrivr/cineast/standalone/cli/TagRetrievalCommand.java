package org.vitrivr.cineast.standalone.cli;

import com.github.rvesse.airline.annotations.Command;
import com.github.rvesse.airline.annotations.Option;
import java.util.ArrayList;
import java.util.List;
import org.vitrivr.cineast.core.data.query.containers.TagQueryTermContainer;
import org.vitrivr.cineast.core.features.SegmentTags;
import org.vitrivr.cineast.core.features.retriever.Retriever;
import org.vitrivr.cineast.standalone.config.Config;
import org.vitrivr.cineast.standalone.util.ContinuousRetrievalLogic;

@Command(name = "retrieve-tags", description = "Retrieves objects from the database using text as query input.")
public class TagRetrievalCommand implements Runnable {

  private String data = "data:application/json;base64,W3siaWQiOiJRODUwMiIsIm5hbWUiOiJtb3VudGFpbiIsImRlc2NyaXB0aW9uIjoiIn0seyJpZCI6IlE2OTMwMTcwNSIsIm5hbWUiOiJzaGVlcCIsImRlc2NyaXB0aW9uIjoiIn0seyJpZCI6IlE3MzY4IiwibmFtZSI6InNoZWVwIiwiZGVzY3JpcHRpb24iOiIifV0=";

  @Option(name = {"--limit"}, title = "limit results", description = "Only show the first n results.")
  private Integer limit = 1;

  @Option(name = {"--detail"}, title = "detailed results", description = "also list detailed results for retrieved segments.")
  private Boolean printDetail = false;

  public void run() {
    final ContinuousRetrievalLogic retrieval = new ContinuousRetrievalLogic(Config.sharedConfig().getDatabase());
    TagQueryTermContainer qc = new TagQueryTermContainer(data);
    List<Retriever> retrievers = new ArrayList<>();
    retrievers.add(new SegmentTags());

    CliUtils.retrieveAndLog(retrievers, retrieval, limit, printDetail, qc);
    System.out.println("Done");
  }
}
