package org.vitrivr.cineast.api.messages.query;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.vitrivr.cineast.api.messages.interfaces.MessageType;
import org.vitrivr.cineast.core.config.QueryConfig;

import java.util.List;

public class StagedSimilarityQuery extends Query {

    private List<SimilarityQuery> stages;

    public StagedSimilarityQuery(@JsonProperty(value = "stages", required = true) List<SimilarityQuery> stages,
                                 @JsonProperty(value = "config", required = false) QueryConfig config) {
        super(config != null ?
                config :
                (!stages.isEmpty() ? stages.get(stages.size() - 1).config : null)
        );

        this.stages = stages;
    }

    public List<SimilarityQuery> getStages() {
        return stages;
    }

    @Override
    public MessageType getMessageType() {
        return MessageType.Q_SSIM;
    }
}
