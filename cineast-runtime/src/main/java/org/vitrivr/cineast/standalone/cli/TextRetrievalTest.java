package org.vitrivr.cineast.standalone.cli;

import com.github.rvesse.airline.annotations.Command;
import com.github.rvesse.airline.annotations.Option;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import org.vitrivr.cineast.core.data.query.containers.TextQueryContainer;
import org.vitrivr.cineast.core.data.score.SegmentScoreElement;
import org.vitrivr.cineast.core.db.DBSelector;
import org.vitrivr.cineast.core.features.AudioTranscriptionSearch;
import org.vitrivr.cineast.core.features.DescriptionTextSearch;
import org.vitrivr.cineast.core.features.OCRSearch;
import org.vitrivr.cineast.core.features.SubtitleFulltextSearch;
import org.vitrivr.cineast.core.features.TagsFtSearch;
import org.vitrivr.cineast.core.features.retriever.Retriever;
import org.vitrivr.cineast.standalone.config.Config;
import org.vitrivr.cineast.standalone.config.ConstrainedQueryConfig;
import org.vitrivr.cineast.standalone.util.ContinuousRetrievalLogic;


/**
 * A CLI command that can be used to retrieve data from the database based on an example segment.
 *
 * @author Ralph Gasser
 * @version 1.0
 */
@Command(name = "retrieve-text", description = "Retrieves objects from the database using text as query input.")
public class TextRetrievalTest implements Runnable {

  @Option(name = {"--text"}, title = "text input", description = "query to be used for retrieval.")
  private String text;

  @Option(name = {"--limit"}, title = "limit results", description = "Only show the first n results.")
  private Integer limit = 1;

  @Option(name = {"--detail"}, title = "detailed results", description = "also list detailed results for retrieved segments.")
  private Boolean printDetail = false;

  public void run() {
    final ContinuousRetrievalLogic retrieval = new ContinuousRetrievalLogic(Config.sharedConfig().getDatabase());
    TextQueryContainer qc = new TextQueryContainer(text);
    List<Retriever> retrievers = new ArrayList<>();
    retrievers.add(new SubtitleFulltextSearch());
    retrievers.add(new OCRSearch());
    retrievers.add(new AudioTranscriptionSearch());
    retrievers.add(new DescriptionTextSearch());
    retrievers.add(new TagsFtSearch());

    System.out.println("Only printing the first " + limit + " results, change with --limit parameter");
    DBSelector selector = Config.sharedConfig().getDatabase().getSelectorSupplier().get();
    retrievers.forEach(retriever -> {
      AtomicBoolean entityExists = new AtomicBoolean(true);
      retriever.getTableNames().forEach(table -> {
        if (!selector.existsEntity(table)) {
          System.out.println("Entity " + table + " does not exist");
          entityExists.set(false);
        }
      });
      if (!entityExists.get()) {
        System.out.println("Not retrieving for " + retriever.getClass().getSimpleName() + " because entity does not exist");
        return;
      }
      System.out.println("Retrieving for " + retriever.getClass().getSimpleName());
      List<SegmentScoreElement> results = retrieval.retrieveByRetriever(qc, retriever, new ConstrainedQueryConfig());
      System.out.println("Results for " + retriever.getClass().getSimpleName() + ":");

      for (SegmentScoreElement e : results.subList(0, Math.min(limit, results.size()))) {
        System.out.print(e.getSegmentId());
        System.out.print(": ");
        System.out.println(e.getScore());
        if (printDetail) {
          SingleObjRetrievalCommand.printInfoForSegment(e.getSegmentId(), selector);
        }
      }
      System.out.println();
    });
    retrieval.shutdown();
    System.out.println("Done");
  }
}
