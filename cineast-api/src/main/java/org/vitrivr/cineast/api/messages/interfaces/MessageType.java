package org.vitrivr.cineast.api.messages.interfaces;

import org.vitrivr.cineast.api.messages.general.Ping;
import org.vitrivr.cineast.api.messages.lookup.MetadataLookup;
import org.vitrivr.cineast.api.messages.query.*;
import org.vitrivr.cineast.api.messages.result.*;
import org.vitrivr.cineast.api.messages.session.StartSessionMessage;

/**
 * Defines the different MessageTypes used by the WebSocket and JSON API.
 *
 * @author rgasser
 * @version 1.0
 * @created 12.01.17
 */
public enum MessageType {
    /* Messages related to status updates. */
    PING(Ping.class),

    /* Query  message types. */
    Q_SIM(SimilarityQuery.class), Q_SSIM(StagedSimilarityQuery.class), Q_MLT(MoreLikeThisQuery.class), Q_NESEG(NeighboringSegmentQuery.class), Q_SEG(SegmentQuery.class), M_LOOKUP(MetadataLookup.class),

    /* Session */
    SESSION_START(StartSessionMessage.class),

    /* Query results. */
    QR_START(QueryStart.class), QR_END(QueryEnd.class), QR_ERROR(QueryError.class), QR_OBJECT(MediaObjectQueryResult.class),  QR_METADATA_O(MediaObjectMetadataQueryResult.class), QR_METADATA_S(MediaObjectMetadataQueryResult.class), QR_SEGMENT(MediaSegmentQueryResult.class), QR_SIMILARITY(SimilarityQueryResult.class);

    private Class<? extends Message> c;

    MessageType(Class<? extends Message> c) {
        this.c = c;
    }

    public Class<? extends Message> getMessageClass() {
        return c;
    }
}
