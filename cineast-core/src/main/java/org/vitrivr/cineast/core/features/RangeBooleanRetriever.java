package org.vitrivr.cineast.core.features;

import org.vitrivr.cineast.core.data.providers.primitive.PrimitiveProviderComparator;
import org.vitrivr.cineast.core.data.providers.primitive.PrimitiveTypeProvider;
import org.vitrivr.cineast.core.db.RelationalOperator;
import org.vitrivr.cineast.core.features.abstracts.BooleanRetriever;

import java.util.*;

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

  private final HashMap<String, PrimitiveTypeProvider> minimumMap = new HashMap<>();
  private final HashMap<String, PrimitiveTypeProvider> maximumMap = new HashMap<>();

  protected RangeBooleanRetriever(String entity, Collection<String> attributes) {
    super(entity, attributes);
  }

  public RangeBooleanRetriever(LinkedHashMap<String, String> properties) {
    super(properties);
  }

  @Override
  protected Collection<RelationalOperator> getSupportedOperators() {
    return SUPPORTED_OPERATORS;
  }

  public PrimitiveTypeProvider getMinimum(String column){
    if (this.attributes.contains(column) && !this.minimumMap.containsKey(column)){
      populateExtremaMap();
    }
    return minimumMap.get(column);
  }

  public PrimitiveTypeProvider getMaximum(String column){
    if (this.attributes.contains(column) && !this.maximumMap.containsKey(column)){
      populateExtremaMap();
    }
    return maximumMap.get(column);
  }

  private void populateExtremaMap(){

    PrimitiveProviderComparator comparator = new PrimitiveProviderComparator();

    for(String column: this.attributes){
      List<PrimitiveTypeProvider> col = this.selector.getAll(column);
      if (col.isEmpty()){
        continue;
      }
      PrimitiveTypeProvider min = col.get(0), max = col.get(0);
      for (PrimitiveTypeProvider t : col){
        if (comparator.compare(t, min) < 0){
          min = t;
        }
        if(comparator.compare(t, max) > 0){
          max = t;
        }
      }
      this.minimumMap.put(column, min);
      this.maximumMap.put(column, max);
    }
  }


}
