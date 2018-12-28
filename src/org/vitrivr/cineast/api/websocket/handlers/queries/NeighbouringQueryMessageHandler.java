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
     * Handles a {@link NeighboringSegmentQuery} message. Makes a lookup for the {@link MediaSegmentDescriptor}s that are
     * neigbours of the provided segment ID.
     *
     * @param session WebSocket session the invokation is associated with.
     * @param message Instance of {@link MoreLikeThisQuery}
     */
    @Override
    public void handle(Session session, NeighboringSegmentQuery message) {
        /* Prepare QueryConfig (so as to obtain a QueryId). */
        final QueryConfig qconf = (message.getQueryConfig() == null) ? QueryConfig.newQueryConfigFromOther(Config.sharedConfig().getQuery()) : message.getQueryConfig();
        final String uuid = qconf.getQueryId().toString();

        /* Begin of Query: Send QueryStart Message to Client. */
        this.write(session, new QueryStart(uuid));

        /* Retrieve segments. If empty, abort query. */
        final List<String> segmentIds = message.getSegmentIds();
        final List<MediaSegmentDescriptor> segment = this.loadSegments(segmentIds);
        if (segmentIds.size() == 0) {
            this.write(session, new QueryEnd(uuid));
            return;
        }

        /* Write segments to stream. */
        this.write(session, new MediaSegmentQueryResult(uuid, segment));

        /* Load and transmit segment metadata. */
        this.loadAndWriteSegmentMetadata(session, uuid, segmentIds);

        /* End of Query: Send QueryEnd Message to Client. */
        this.write(session, new QueryEnd(uuid));
    }
}