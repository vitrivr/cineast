package org.vitrivr.cineast.api.websocket.handlers.queries;
import org.eclipse.jetty.websocket.api.Session;
import org.vitrivr.cineast.core.config.QueryConfig;
import org.vitrivr.cineast.core.data.entities.MediaObjectDescriptor;
import org.vitrivr.cineast.core.data.entities.MediaSegmentDescriptor;
import org.vitrivr.cineast.core.data.messages.query.SegmentQuery;
import org.vitrivr.cineast.core.data.messages.result.*;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class SegmentQueryMessageHandler extends AbstractQueryMessageHandler<SegmentQuery> {
    /**
     * Executes a {@link SegmentQuery} message. Performs a lookup for the segment ID specified in the {@link SegmentQuery} object.
     *
     * @param session WebSocket session the invocation is associated with.
     * @param qconf The {@link QueryConfig} that contains additional specifications.
     * @param message Instance of {@link SegmentQuery}
     */
    @Override
    public void execute(Session session, QueryConfig qconf, SegmentQuery message) throws Exception {
        /* Prepare QueryConfig (so as to obtain a QueryId). */
        final String uuid = qconf.getQueryId().toString();

        /* Retrieve segments; if empty, abort query. */
        final List<String> segmentId = new ArrayList<>(0);
        segmentId.add(message.getSegmentId());
        final List<MediaSegmentDescriptor> segment = this.loadSegments(segmentId);
        if (segment.isEmpty()) return;

        /* Retrieve media objects; if empty, abort query. */
        final List<String> objectId = segment.stream().map(MediaSegmentDescriptor::getObjectId).collect(Collectors.toList());
        final List<MediaObjectDescriptor> object = this.loadObjects(objectId);
        if (object.isEmpty()) return;

        /* Write segments and objects to results stream. */
        this.write(session, new MediaSegmentQueryResult(uuid, segment));
        this.write(session, new MediaObjectQueryResult(uuid, object));

        /* Load and transmit segment & object metadata. */
        this.loadAndWriteSegmentMetadata(session, uuid, segmentId);
        this.loadAndWriteObjectMetadata(session, uuid, objectId);
    }
}
