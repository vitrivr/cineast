package org.vitrivr.cineast.api.rest.handlers.actions.bool;

import io.javalin.http.Context;
import io.javalin.plugin.openapi.dsl.OpenApiBuilder;
import io.javalin.plugin.openapi.dsl.OpenApiDocumentation;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import org.apache.commons.lang3.time.StopWatch;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.vitrivr.cineast.api.messages.lookup.ColumnSpecification;
import org.vitrivr.cineast.api.messages.lookup.ColumnsSpecification;
import org.vitrivr.cineast.api.messages.result.DistinctElementsMultipleColumnsResult;
import org.vitrivr.cineast.api.messages.result.DistinctElementsResult;
import org.vitrivr.cineast.api.rest.handlers.interfaces.ParsingPostRestHandler;
import org.vitrivr.cineast.core.data.providers.primitive.PrimitiveTypeProvider;
import org.vitrivr.cineast.core.data.providers.primitive.ProviderDataType;
import org.vitrivr.cineast.core.db.DBSelector;
import org.vitrivr.cineast.standalone.config.Config;

/**
 * For boolean retrieval, it is useful to know all available options for a certain column.
 * <p>
 * For example, it would be useful to know all available timezones for the column $table.timezone
 */
public class FindDistinctElementsByColumnsPostHandler implements ParsingPostRestHandler<ColumnsSpecification, DistinctElementsMultipleColumnsResult> {

  public static final String ROUTE = "find/boolean/columns/distinct";

  private static final Logger LOGGER = LogManager.getLogger();

  private static final DBSelector selector = Config.sharedConfig().getDatabase().getSelectorSupplier().get();

  private static final Map<String, List<List<String>>> cache = new HashMap<>();


  @Override
  public DistinctElementsMultipleColumnsResult performPost(ColumnsSpecification specification, Context ctx) {
    List<List<String>> distinct = new ArrayList<>();
    if (specification == null || specification.table().isEmpty() || specification.columns().isEmpty()) {
      LOGGER.warn("No columns specified, returning empty list: {}", specification);
      return new DistinctElementsMultipleColumnsResult("", distinct);
    }
    if (cache.containsKey(specification.table() + specification.columns())) {
      LOGGER.trace("Cache-hit for distinct lookup {}", specification);
      return new DistinctElementsMultipleColumnsResult("", cache.get(specification.table() + specification.columns()));
    }
    StopWatch watch = StopWatch.createStarted();
    selector.open(specification.table());
    distinct = selector.getUniqueValues(specification.columns()).stream().filter(p -> p.stream().allMatch(tp -> tp.getType() != ProviderDataType.UNKNOWN)).map(values -> values.stream().map(PrimitiveTypeProvider::getString).collect(Collectors.toList())).collect(Collectors.toList());
    cache.put(specification.table() + specification.columns(), distinct);
    LOGGER.trace("Retrieved unique values for {} in {} ms", specification.table() + "." + specification.columns(), watch.getTime(TimeUnit.MILLISECONDS));
    return new DistinctElementsMultipleColumnsResult("", distinct);
  }

  @Override
  public Class<ColumnsSpecification> inClass() {
    return ColumnsSpecification.class;
  }


  @Override
  public Class<DistinctElementsMultipleColumnsResult> outClass() {
    return DistinctElementsMultipleColumnsResult.class;
  }

  @Override
  public String route() {
    return ROUTE;
  }


  @Override
  public OpenApiDocumentation docs() {
    return OpenApiBuilder.document()
        .operation(op -> {
          op.summary("Find all distinct elements of a given list of columns");
          op.description("Find all distinct elements of a given list of columns. Please note that this operation does cache results.");
          op.operationId("FindDistinctElementsByColumns");
          op.addTagsItem("Misc");
        })
        .body(inClass())
        .json("200", outClass());
  }
}
