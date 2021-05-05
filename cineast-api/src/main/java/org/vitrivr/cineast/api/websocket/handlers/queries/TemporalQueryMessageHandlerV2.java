package org.vitrivr.cineast.api.websocket.handlers.queries;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.eclipse.jetty.websocket.api.Session;
import org.vitrivr.cineast.api.messages.query.TemporalQueryV2;
import org.vitrivr.cineast.core.config.QueryConfig;
import org.vitrivr.cineast.standalone.config.Config;
import org.vitrivr.cineast.standalone.util.ContinuousRetrievalLogic;

public class TemporalQueryMessageHandlerV2 extends AbstractQueryMessageHandler<TemporalQueryV2> {

  private static final Logger LOGGER = LogManager.getLogger();

  private final ContinuousRetrievalLogic continuousRetrievalLogic;

  public TemporalQueryMessageHandlerV2(ContinuousRetrievalLogic retrievalLogic) {
    this.continuousRetrievalLogic = retrievalLogic;
  }

  @Override
  public void execute(Session session, QueryConfig qconf, TemporalQueryV2 message, Set<String> segmentIdsForWhichMetadataIsFetched, Set<String> objectIdsForWhichMetadataIsFetched) throws Exception {

    final String uuid = qconf.getQueryId().toString();
    final int max = qconf.getMaxResults().orElse(Config.sharedConfig().getRetriever().getMaxResults());
    qconf.setMaxResults(max);
    final int resultsPerModule = qconf.getRawResultsPerModule() == -1 ? Config.sharedConfig().getRetriever().getMaxResultsPerModule() : qconf.getResultsPerModule();
    qconf.setResultsPerModule(resultsPerModule);

    List<Thread> metadataRetrievalThreads = new ArrayList<>();

    // TODO

  }

}
