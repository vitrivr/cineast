package org.vitrivr.cineast.core.features;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.vitrivr.cineast.core.data.providers.primitive.PrimitiveProviderComparator;
import org.vitrivr.cineast.core.data.providers.primitive.PrimitiveTypeProvider;
import org.vitrivr.cineast.core.db.RelationalOperator;
import org.vitrivr.cineast.core.features.abstracts.BooleanRetriever;

public class RangeBooleanRetriever extends BooleanRetriever {

  private static final List<RelationalOperator> SUPPORTED_OPERATORS =
      Collections.unmodifiableList(
          Arrays.asList(
              RelationalOperator.BETWEEN,
              RelationalOperator.EQ,
              RelationalOperator.NEQ,
              RelationalOperator.GEQ,
              RelationalOperator.GREATER,
              RelationalOperator.LEQ,
              RelationalOperator.LESS,
              RelationalOperator.IN));

  protected RangeBooleanRetriever(String entity, Collection<String> attributes) {
    super(entity, attributes);
  }

  public RangeBooleanRetriever(Map<String, String> properties) {
    super(properties);
  }

  @Override
  protected Collection<RelationalOperator> getSupportedOperators() {
    return SUPPORTED_OPERATORS;
  }

}
