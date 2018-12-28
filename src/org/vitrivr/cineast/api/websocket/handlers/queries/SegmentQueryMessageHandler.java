package org.vitrivr.cineast.api.websocket.handlers.queries;
import org.eclipse.jetty.websocket.api.Session;
import org.vitrivr.cineast.core.config.Config;
import org.vitrivr.cineast.core.config.QueryConfig;
import org.vitrivr.cineast.core.data.entities.MediaObjectDescriptor;
import org.vitrivr.cineast.core.data.entities.MediaSegmentDescriptor;
import org.vitrivr.cineast.core.data.messages.query.SegmentQuery;
import org.vitrivr.cineast.core.data.messages.result.*;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class SegmentQueryMessageHandler extends AbstractQueryMessageHandler<SegmentQuery> {
    @Override
    public void handle(Session session, SegmentQuery message) {
        /* Prepare QueryConfig (so as to obtain a QueryId). */
        final QueryConfig qconf = (message.getQueryConfig() == null) ? QueryConfig.newQueryConfigFromOther(Config.sharedConfig().getQuery()) : message.getQueryConfig();
        final String uuid = qconf.getQueryId().toString();

        /* Begin of Query: Send QueryStart Message to Client. */
        this.write(session, new QueryStart(uuid));

        /* Retrieve segments; if empty, abort query. */
        final List<String> segmentId = new ArrayList<>(0);
        segmentId.add(message.getSegmentId());
        final List<MediaSegmentDescriptor> segment = this.loadSegments(segmentId);
        if (segment.size() == 0) {
            this.write(session, new QueryEnd(uuid));
            return;
        }

        /* Retrieve media objects; if empty, abort query. */
        final List<String> objectId = segment.stream().map(MediaSegmentDescriptor::getObjectId).collect(Collectors.toList());
        final List<MediaObjectDescriptor> object = this.loadObjects(objectId);
        if (object.size() == 0) {
            this.write(session, new QueryEnd(uuid));
            return;
        }

        /* Write segments and objects to results stream. */
        this.write(session, new MediaSegmentQueryResult(uuid, segment));
        this.write(session, new MediaObjectQueryResult(uuid, object));

        /* Load and transmit segment & object metadata. */
        this.loadAndWriteSegmentMetadata(session, uuid, segmentId);
        this.loadAndWriteObjectMetadata(session, uuid, objectId);

        /* Finalize query. */
        this.write(session, new QueryEnd(uuid));
    }
}
