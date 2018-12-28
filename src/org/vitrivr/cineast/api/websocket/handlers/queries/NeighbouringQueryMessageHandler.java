package org.vitrivr.cineast.api.websocket.handlers.queries;

import org.eclipse.jetty.websocket.api.Session;
import org.vitrivr.cineast.core.config.Config;
import org.vitrivr.cineast.core.config.QueryConfig;
import org.vitrivr.cineast.core.data.entities.MediaObjectMetadataDescriptor;
import org.vitrivr.cineast.core.data.entities.MediaSegmentDescriptor;
import org.vitrivr.cineast.core.data.entities.MediaSegmentMetadataDescriptor;
import org.vitrivr.cineast.core.data.messages.query.MoreLikeThisQuery;
import org.vitrivr.cineast.core.data.messages.query.NeighboringSegmentQuery;
import org.vitrivr.cineast.core.data.messages.result.*;

import java.util.ArrayList;
import java.util.List;

public class NeighbouringQueryMessageHandler extends AbstractQueryMessageHandler<NeighboringSegmentQuery> {
    /**
     * Executes a {@link NeighboringSegmentQuery} message. Performs a lookup for the {@link MediaSegmentDescriptor}s
     * that are temporal neigbours of the provided segment ID.
     *
     * @param session WebSocket session the invocation is associated with.
     * @param qconf The {@link QueryConfig} that contains additional specifications.
     * @param message Instance of {@link NeighboringSegmentQuery}
     */
    @Override
    public void execute(Session session, QueryConfig qconf, NeighboringSegmentQuery message) throws Exception {
        /* Prepare QueryConfig (so as to obtain a QueryId). */
        final String uuid = qconf.getQueryId().toString();

        /* Retrieve segments. If empty, abort query. */
        final List<String> segmentIds = message.getSegmentIds();
        final List<MediaSegmentDescriptor> segment = this.loadSegments(segmentIds);
        if (segmentIds.isEmpty()) return;

        /* Write segments to stream. */
        this.write(session, new MediaSegmentQueryResult(uuid, segment));

        /* Load and transmit segment metadata. */
        this.loadAndWriteSegmentMetadata(session, uuid, segmentIds);
    }
}