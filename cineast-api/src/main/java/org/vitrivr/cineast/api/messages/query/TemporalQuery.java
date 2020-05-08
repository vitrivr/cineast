package org.vitrivr.cineast.api.messages.query;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import org.vitrivr.cineast.api.messages.interfaces.MessageType;
import org.vitrivr.cineast.core.config.QueryConfig;

public class TemporalQuery extends Query {

  public final List<StagedSimilarityQuery> queries;

  @JsonCreator
  public TemporalQuery(@JsonProperty(value = "queries", required = true) List<StagedSimilarityQuery> queries, @JsonProperty(value = "config", required = false) QueryConfig config) {
    super(config);
    this.queries = queries;
  }

  public MessageType getMessageType() {
    return MessageType.Q_TEMPORAL;
  }

}
