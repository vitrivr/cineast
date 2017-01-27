package org.vitrivr.cineast.core.data.messages.query;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.vitrivr.cineast.api.WebUtils;
import org.vitrivr.cineast.core.data.MultiImageFactory;
import org.vitrivr.cineast.core.data.QueryContainer;

import java.awt.image.BufferedImage;
import java.util.Arrays;
import java.util.List;

/**
 * @author rgasser
 * @version 1.0
 * @created 11.01.17
 */
public class QueryTerm {
    /**
     * List of categories defined as part of the query-term. This ultimately selects the feature-vectors
     * used for retrieval.
     */
    private final String[] categories;

    /**
     *
     */
    private final QueryTermType type;

    /**
     *
     */
    private final String data;

    /**
     *
     * @param categories
     */
    @JsonCreator
    public QueryTerm(@JsonProperty("type") QueryTermType type, @JsonProperty("data") String data, @JsonProperty("categories") String[] categories) {
        this.type = type;
        this.categories = categories;
        this.data = data;
    }

    /**
     *
     * @return
     */
    public List<String> getCategories() {
        return Arrays.asList(this.categories);
    }

    /**
     *
     * @return
     */
    public QueryTermType getType() {
        return type;
    }

    /**
     *
     * @return
     */
    public QueryContainer toContainer() {
        switch (this.type) {
            case IMAGE:
                BufferedImage image = WebUtils.dataURLtoBufferedImage(this.data);
                return new QueryContainer(MultiImageFactory.newInMemoryMultiImage(image));
            default:
                return null;
        }
    }
}
