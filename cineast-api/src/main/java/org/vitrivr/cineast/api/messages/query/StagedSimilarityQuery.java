package org.vitrivr.cineast.api.messages.query;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import org.vitrivr.cineast.api.messages.interfaces.MessageType;
import org.vitrivr.cineast.core.config.QueryConfig;

public class StagedSimilarityQuery extends SimilarityQuery {

  public StagedSimilarityQuery(@JsonProperty(value = "containers", required = true) List<QueryComponent> components,
      @JsonProperty(value = "config", required = false) QueryConfig config) {
    super(components, config);
  }

  @Override
  public MessageType getMessageType() {
    return MessageType.Q_SSIM;
  }
}
