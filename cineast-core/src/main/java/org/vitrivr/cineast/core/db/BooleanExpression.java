package org.vitrivr.cineast.core.db;

import java.util.List;
import java.util.Objects;
import org.vitrivr.cineast.core.data.providers.primitive.PrimitiveTypeProvider;

public record BooleanExpression(String attribute, RelationalOperator operator, List<PrimitiveTypeProvider> values) {

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    BooleanExpression that = (BooleanExpression) o;
    return Objects.equals(attribute, that.attribute) && operator == that.operator && Objects.equals(values, that.values);
  }

  @Override
  public int hashCode() {
    return Objects.hash(attribute, operator, values);
  }
}
