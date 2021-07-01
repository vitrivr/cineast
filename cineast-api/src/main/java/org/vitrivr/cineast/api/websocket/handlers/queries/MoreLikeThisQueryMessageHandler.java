package org.vitrivr.cineast.api.websocket.handlers.queries;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;
import org.eclipse.jetty.websocket.api.Session;
import org.vitrivr.cineast.api.messages.query.MoreLikeThisQuery;
import org.vitrivr.cineast.core.config.QueryConfig;
import org.vitrivr.cineast.core.data.StringDoublePair;
import org.vitrivr.cineast.standalone.config.Config;
import org.vitrivr.cineast.standalone.util.ContinuousRetrievalLogic;

/**
 * This class extends the {@link AbstractQueryMessageHandler} abstract class and handles messages of type {@link MoreLikeThisQuery}.
 */
public class MoreLikeThisQueryMessageHandler extends AbstractQueryMessageHandler<MoreLikeThisQuery> {

  private final ContinuousRetrievalLogic continuousRetrievalLogic;

  public MoreLikeThisQueryMessageHandler(ContinuousRetrievalLogic retrievalLogic) {
    this.continuousRetrievalLogic = retrievalLogic;
  }

  /**
   * Executes a {@link MoreLikeThisQuery} message. Performs a similarity query based on the segmentId specified the {@link MoreLikeThisQuery} object.
   *
   * @param session WebSocket session the invocation is associated with.
   * @param qconf   The {@link QueryConfig} that contains additional specifications.
   * @param message Instance of {@link MoreLikeThisQuery}
   */
  @Override
  public void execute(Session session, QueryConfig qconf, MoreLikeThisQuery message, Set<String> segmentIdsForWhichMetadataIsFetched, Set<String> objectIdsForWhichMetadataIsFetched) throws Exception {
    /* Extract categories from MoreLikeThisQuery. */
    final String queryId = qconf.getQueryId().toString();
    final HashSet<String> categoryMap = new HashSet<>(message.getCategories());

    List<Thread> threads = new ArrayList<>();
    List<CompletableFuture<Void>> futures = new ArrayList<>();
    /* Retrieve per-category results and return them. */
    for (String category : categoryMap) {
      final List<StringDoublePair> results = continuousRetrievalLogic.retrieve(message.getSegmentId(), category, qconf).stream()
          .map(score -> new StringDoublePair(score.getSegmentId(), score.getScore()))
          .sorted(StringDoublePair.COMPARATOR)
          .limit(Config.sharedConfig().getRetriever().getMaxResults())
          .collect(Collectors.toList());
      List<String> segmentIds = results.stream().map(el -> el.key).collect(Collectors.toList());
      List<String> objectIds = this.submitSegmentAndObjectInformation(session, queryId, segmentIds);

      /* Finalize and submit per-category results. */
      futures.addAll(this.finalizeAndSubmitResults(session, queryId, category, -1, results));

      List<Thread> _threads = this.submitMetadata(session, queryId, segmentIds, objectIds, segmentIdsForWhichMetadataIsFetched, objectIdsForWhichMetadataIsFetched);
      threads.addAll(_threads);
    }
    futures.forEach(CompletableFuture::join);
    for (Thread thread : threads) {
      thread.join();
    }
  }
}
