package org.vitrivr.cineast.standalone.cli;

import com.github.rvesse.airline.annotations.Command;
import com.github.rvesse.airline.annotations.Option;
import java.util.ArrayList;
import java.util.List;
import org.vitrivr.cineast.core.data.query.containers.TextQueryTermContainer;
import org.vitrivr.cineast.core.features.AudioTranscriptionSearch;
import org.vitrivr.cineast.core.features.OCRSearch;
import org.vitrivr.cineast.core.features.SubtitleFulltextSearch;
import org.vitrivr.cineast.core.features.retriever.Retriever;
import org.vitrivr.cineast.standalone.config.Config;
import org.vitrivr.cineast.standalone.util.ContinuousRetrievalLogic;

@Command(name = "retrieve-text", description = "Retrieves objects from the database using text as query input.")
public class TextRetrievalCommand extends AbstractCineastCommand {

  @Option(name = {"--text"}, title = "text input", description = "query to be used for retrieval.")
  private String text;

  @Option(name = {"--limit"}, title = "limit results", description = "Only show the first n results.")
  private Integer limit = 1;

  @Option(name = {"--detail"}, title = "detailed results", description = "also list detailed results for retrieved segments.")
  private Boolean printDetail = false;

  @Override
  public void execute() {
    final ContinuousRetrievalLogic retrieval = new ContinuousRetrievalLogic(Config.sharedConfig().getDatabase());
    System.out.println("Querying for text " + text);
    TextQueryTermContainer qc = new TextQueryTermContainer(text);
    List<Retriever> retrievers = new ArrayList<>();
    retrievers.add(new SubtitleFulltextSearch());
    retrievers.add(new OCRSearch());
    retrievers.add(new AudioTranscriptionSearch());
    CliUtils.retrieveAndLog(retrievers, retrieval, limit, printDetail, qc);
    System.out.println("Done");
  }
}
