package org.vitrivr.cineast.core.db;

import java.util.List;
import java.util.Objects;
import org.vitrivr.cineast.core.data.providers.primitive.PrimitiveTypeProvider;

public record BooleanExpression(String attribute, RelationalOperator operator, List<PrimitiveTypeProvider> values) {
}
