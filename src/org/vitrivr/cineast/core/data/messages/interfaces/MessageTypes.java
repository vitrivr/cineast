package org.vitrivr.cineast.core.data.messages.interfaces;

import org.vitrivr.cineast.core.data.messages.general.Ping;
import org.vitrivr.cineast.core.data.messages.lookup.MetadataLookup;
import org.vitrivr.cineast.core.data.messages.query.Query;
import org.vitrivr.cineast.core.data.messages.result.*;

/**
 * Defines the different MessageTypes used by the WebSocket and JSON API.
 *
 * @author rgasser
 * @version 1.0
 * @created 12.01.17
 */
public enum MessageTypes {

    /* Messages related to status updates. */
    PING(Ping.class),

    /* Query  message types. */
    Q_QUERY(Query.class), M_LOOKUP(MetadataLookup.class),

    /* Query results. */
    QR_START(QueryStart.class), QR_END(QueryEnd.class), QR_OBJECT(ObjectQueryResult.class),  QR_METADATA(MetadataQueryResult.class), QR_SEGMENT(SegmentQueryResult.class), QR_SIMILARITY(SimilarityQueryResult.class);

    private Class<? extends Message> c;

    MessageTypes(Class<? extends Message> c) {
        this.c = c;
    }

    public Class<? extends Message> getMessageClass() {
        return c;
    }
}
