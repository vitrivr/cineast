package org.vitrivr.cineast.api.messages.interfaces;

import org.vitrivr.cineast.api.messages.general.Ping;
import org.vitrivr.cineast.api.messages.lookup.MetadataLookup;
import org.vitrivr.cineast.api.messages.query.MoreLikeThisQuery;
import org.vitrivr.cineast.api.messages.query.NeighboringSegmentQuery;
import org.vitrivr.cineast.api.messages.query.SegmentQuery;
import org.vitrivr.cineast.api.messages.query.SimilarityQuery;
import org.vitrivr.cineast.api.messages.query.TemporalQuery;
import org.vitrivr.cineast.api.messages.result.MediaObjectMetadataQueryResult;
import org.vitrivr.cineast.api.messages.result.MediaObjectQueryResult;
import org.vitrivr.cineast.api.messages.result.MediaSegmentQueryResult;
import org.vitrivr.cineast.api.messages.result.QueryEnd;
import org.vitrivr.cineast.api.messages.result.QueryError;
import org.vitrivr.cineast.api.messages.result.QueryStart;
import org.vitrivr.cineast.api.messages.result.SimilarityQueryResult;
import org.vitrivr.cineast.api.messages.session.StartSessionMessage;

/**
 * Defines the different MessageTypes used by the WebSocket and JSON API.
 */
public enum MessageType {
  /* Messages related to status updates. */
  PING(Ping.class),

  /* Query  message types. */
  Q_SIM(SimilarityQuery.class),
  Q_MLT(MoreLikeThisQuery.class),
  Q_NESEG(NeighboringSegmentQuery.class),
  Q_SEG(SegmentQuery.class),
  M_LOOKUP(MetadataLookup.class),
  Q_TEMPORAL(TemporalQuery.class),


  /* Session */
  SESSION_START(StartSessionMessage.class),

  /* Query results. */
  QR_START(QueryStart.class),
  QR_END(QueryEnd.class),
  QR_ERROR(QueryError.class),
  QR_OBJECT(MediaObjectQueryResult.class),
  QR_METADATA_O(MediaObjectMetadataQueryResult.class),
  QR_METADATA_S(MediaObjectMetadataQueryResult.class),
  QR_SEGMENT(MediaSegmentQueryResult.class),
  QR_SIMILARITY(SimilarityQueryResult.class);

  private final Class<? extends Message> c;

  MessageType(Class<? extends Message> c) {
    this.c = c;
  }

  public Class<? extends Message> getMessageClass() {
    return c;
  }
}
