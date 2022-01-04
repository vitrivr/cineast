package org.vitrivr.cineast.core.features.retriever;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import org.vitrivr.cineast.core.db.RelationalOperator;
import org.vitrivr.cineast.core.features.abstracts.BooleanRetriever;

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

  protected CollectionBooleanRetriever(Map<String, String> properties) {
    super(properties);
  }

  @Override
  protected Collection<RelationalOperator> getSupportedOperators() {
    return SUPPORTED_OPERATORS;
  }
}
