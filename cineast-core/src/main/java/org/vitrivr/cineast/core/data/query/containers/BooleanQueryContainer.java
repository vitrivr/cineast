package org.vitrivr.cineast.core.data.query.containers;

import com.fasterxml.jackson.databind.JsonNode;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.vitrivr.cineast.core.data.providers.primitive.PrimitiveTypeProvider;
import org.vitrivr.cineast.core.db.BooleanExpression;
import org.vitrivr.cineast.core.db.RelationalOperator;
import org.vitrivr.cineast.core.util.web.DataURLParser;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

public class BooleanQueryContainer extends QueryContainer {

  private static final String ATTRIBUTE_FIELD_NAME = "attribute";
  private static final String OPERATOR_FIELD_NAME = "operator";
  private static final String VALUES_FIELD_NAME = "values";

  private static final Logger LOGGER = LogManager.getLogger();

  private ArrayList<BooleanExpression> expressions = new ArrayList<>();

  public BooleanQueryContainer(Collection<BooleanExpression> expressions) {
    this.expressions.addAll(expressions);
  }

  public BooleanQueryContainer(String data) {
    this(DataURLParser.dataURLtoJsonNode(data).orElseThrow(() -> new IllegalArgumentException("Failed to parse the provided Boolean expression data.")));
  }

  public BooleanQueryContainer(JsonNode json) {
    if (!json.isArray()) {
      throw new IllegalArgumentException("Boolean expression data is not a list");
    }

    Iterator<JsonNode> iter = json.elements();
    while (iter.hasNext()) {
      JsonNode element = iter.next();

      if (!element.has(ATTRIBUTE_FIELD_NAME)) {
        LOGGER.error(
            "No '{}' field present in Boolean expression '{}', skipping",
            ATTRIBUTE_FIELD_NAME,
            element.toString());
        continue;
      }

      if (!element.has(OPERATOR_FIELD_NAME)) {
        LOGGER.error(
            "No '{}' field present in Boolean expression '{}', skipping",
            OPERATOR_FIELD_NAME,
            element.toString());
        continue;
      }

      String attribute = element.get(ATTRIBUTE_FIELD_NAME).asText();
      RelationalOperator operator =
          RelationalOperator.valueOf(element.get(OPERATOR_FIELD_NAME).asText());
      List<PrimitiveTypeProvider> values = new ArrayList<>();

      if (element.has(VALUES_FIELD_NAME)) {
        if (element.get(VALUES_FIELD_NAME).isArray()) {
          Iterator<JsonNode> elementIter = element.get(VALUES_FIELD_NAME).elements();
          while(elementIter.hasNext()){
            values.add(PrimitiveTypeProvider.fromJSON(elementIter.next()));
          }
        } else {
          values.add(PrimitiveTypeProvider.fromJSON(element.get(VALUES_FIELD_NAME)));
        }
      }

      this.expressions.add(new BooleanExpression(attribute, operator, values));

    }
  }

  @Override
  public List<BooleanExpression> getBooleanExpressions() {
    return this.expressions;
  }
}
