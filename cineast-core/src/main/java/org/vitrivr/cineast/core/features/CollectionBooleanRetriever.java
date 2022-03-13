package org.vitrivr.cineast.core.features;

import org.vitrivr.cineast.core.db.RelationalOperator;
import org.vitrivr.cineast.core.features.abstracts.BooleanRetriever;

import java.util.*;

public class CollectionBooleanRetriever extends BooleanRetriever {

    private static final List<RelationalOperator> SUPPORTED_OPERATORS =
            Collections.unmodifiableList(
                    Arrays.asList(
                            RelationalOperator.EQ,
                            RelationalOperator.NEQ,
                            RelationalOperator.IN,
                            RelationalOperator.LIKE));

    protected CollectionBooleanRetriever(String entity, Collection<String> attributes) {
        super(entity, attributes);
    }

    protected CollectionBooleanRetriever(LinkedHashMap<String, String> properties) {
        super(properties);
    }

    @Override
    protected Collection<RelationalOperator> getSupportedOperators() {
        return SUPPORTED_OPERATORS;
    }
}
