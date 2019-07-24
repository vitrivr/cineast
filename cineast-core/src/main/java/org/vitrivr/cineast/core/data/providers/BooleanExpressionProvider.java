package org.vitrivr.cineast.core.data.providers;

import java.util.Collections;
import java.util.List;
import org.vitrivr.cineast.core.db.BooleanExpression;

public interface BooleanExpressionProvider {

  default List<BooleanExpression> getBooleanExpressions(){
    return Collections.emptyList();
  }

}
