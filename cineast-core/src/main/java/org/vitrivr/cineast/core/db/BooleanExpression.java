package org.vitrivr.cineast.core.db;

import org.apache.commons.lang3.builder.ReflectionToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.vitrivr.cineast.core.data.providers.primitive.PrimitiveTypeProvider;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class BooleanExpression {

  private final String attribute;
  private final RelationalOperator operator;
  private final List<PrimitiveTypeProvider> values = new ArrayList<>();
  private final Boolean relevant;

  public BooleanExpression(String attribute, RelationalOperator operator, List<PrimitiveTypeProvider> values) {
    this.attribute = attribute;
    this.operator = operator;
    this.values.addAll(values);
    this.relevant = true;
  }

  public BooleanExpression(String attribute, RelationalOperator operator, List<PrimitiveTypeProvider> values, Boolean relevant) {
    this.attribute = attribute;
    this.operator = operator;
    this.values.addAll(values);
    this.relevant = relevant;
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

  public Boolean getRelevant() { return this.relevant; }

  @Override
  public String toString() {
    return ReflectionToStringBuilder.toString(this, ToStringStyle.SHORT_PREFIX_STYLE);
  }
}
