package org.vitrivr.cineast.api.websocket.handlers.queries;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;
import org.eclipse.jetty.websocket.api.Session;
import org.vitrivr.cineast.api.messages.query.NeighboringSegmentQuery;
import org.vitrivr.cineast.api.messages.result.MediaSegmentQueryResult;
import org.vitrivr.cineast.core.config.QueryConfig;
import org.vitrivr.cineast.core.data.entities.MediaSegmentDescriptor;

/**
 * This class extends the {@link AbstractQueryMessageHandler} abstract class and handles messages of type {@link NeighboringSegmentQuery}.
 */
public class NeighbouringQueryMessageHandler extends AbstractQueryMessageHandler<NeighboringSegmentQuery> {

  /**
   * Executes a {@link NeighboringSegmentQuery} message. Performs a lookup for the {@link MediaSegmentDescriptor}s that are temporal neigbours of the provided segment ID.
   *
   * @param session                             WebSocket session the invocation is associated with.
   * @param qconf                               The {@link QueryConfig} that contains additional specifications.
   * @param message                             Instance of {@link NeighboringSegmentQuery}
   * @param segmentIdsForWhichMetadataIsFetched Segment IDs for which metadata is fetched
   * @param objectIdsForWhichMetadataIsFetched  Object IDs for which metadata is fetched
   */
  @Override
  public void execute(Session session, QueryConfig qconf, NeighboringSegmentQuery message, Set<String> segmentIdsForWhichMetadataIsFetched, Set<String> objectIdsForWhichMetadataIsFetched) throws Exception {
    /* Prepare QueryConfig (so as to obtain a QueryId). */
    final String uuid = qconf.getQueryId().toString();

    /* Retrieve segments. If empty, abort query. */
    final String segmentId = message.getSegmentId();

    if (segmentId == null || segmentId.isEmpty()) {
      return;
    }

    Optional<MediaSegmentDescriptor> segmentOption = this.mediaSegmentReader.lookUpSegment(segmentId);

    if (!segmentOption.isPresent()) {
      return;
    }

    MediaSegmentDescriptor segment = segmentOption.get();

    final List<MediaSegmentDescriptor> segments = this.mediaSegmentReader.lookUpSegmentsByNumberRange(segment.getObjectId(), segment.getSequenceNumber() - message.getCount(), segment.getSequenceNumber() + message.getCount());

    /* Write segments to stream. */
    CompletableFuture<Void> future = this.write(session, new MediaSegmentQueryResult(uuid, segments));

    /* Load and transmit segment metadata. */
    List<Thread> threads = this.loadAndWriteSegmentMetadata(session, uuid, segments.stream().map(MediaSegmentDescriptor::getSegmentId).collect(Collectors.toList()), segmentIdsForWhichMetadataIsFetched);
    for (Thread thread : threads) {
      thread.join();
    }
    future.join();
  }
}
