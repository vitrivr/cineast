package org.vitrivr.cineast.core.data.providers;

import org.vitrivr.cineast.core.db.BooleanExpression;

import java.util.Collections;
import java.util.List;

public interface BooleanExpressionProvider {

  default List<BooleanExpression> getBooleanExpressions(){
    return Collections.emptyList();
  }
  default Double getContainerWeight() {
    return 1.0;
  }

}
