package org.vitrivr.cineast.core.db;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import org.apache.commons.lang3.builder.ReflectionToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.vitrivr.cineast.core.data.providers.primitive.PrimitiveTypeProvider;

public class BooleanExpression {

  private final String attribute;
  private final RelationalOperator operator;
  private final List<PrimitiveTypeProvider> values = new ArrayList<>();

  public BooleanExpression(String attribute, RelationalOperator operator, List<PrimitiveTypeProvider> values) {
    this.attribute = attribute;
    this.operator = operator;
    this.values.addAll(values);
  }

  public String getAttribute() {
    return this.attribute;
  }

  public RelationalOperator getOperator() {
    return this.operator;
  }

  public List<PrimitiveTypeProvider> getValues() {
    return Collections.unmodifiableList(this.values);
  }

  @Override
  public String toString() {
    return ReflectionToStringBuilder.toString(this, ToStringStyle.SHORT_PREFIX_STYLE);
  }

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
