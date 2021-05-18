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
import org.vitrivr.cineast.api.messages.result.DistinctElementsResult;
import org.vitrivr.cineast.api.rest.handlers.interfaces.ParsingPostRestHandler;
import org.vitrivr.cineast.core.data.providers.primitive.PrimitiveTypeProvider;
import org.vitrivr.cineast.core.db.DBSelector;
import org.vitrivr.cineast.standalone.config.Config;

/**
 * For boolean retrieval, it is useful to know all available options for a certain column.
 * <p>
 * For example, it would be useful to know all available timezones for the column $table.timezone
 */
public class FindDistinctElementsByColumnPostHandler implements ParsingPostRestHandler<ColumnSpecification, DistinctElementsResult> {

  public static final String ROUTE = "find/boolean/column/distinct";

  private static final Logger LOGGER = LogManager.getLogger();

  private static final DBSelector selector = Config.sharedConfig().getDatabase().getSelectorSupplier().get();

  private static final Map<String, List<String>> cache = new HashMap<String, List<String>>();


  @Override
  public DistinctElementsResult performPost(ColumnSpecification specification, Context ctx) {
    List<String> distinct = new ArrayList<>();
    if (specification == null || specification.getTable().isEmpty() || specification.getColumn().isEmpty()) {
      LOGGER.warn("No column specified, returning empty list: {}", specification);
      return new DistinctElementsResult("", distinct);
    }
    if (cache.containsKey(specification.getTable() + specification.getColumn())) {
      LOGGER.trace("Cache-hit for distinct lookup {}", specification);
      return new DistinctElementsResult("", cache.get(specification.getTable() + specification.getColumn()));
    }
    StopWatch watch = StopWatch.createStarted();
    selector.open(specification.getTable());
    distinct = selector.getUniqueValues(specification.getColumn()).stream().map(PrimitiveTypeProvider::getString).collect(Collectors.toList());
    cache.put(specification.getTable() + specification.getColumn(), distinct);
    LOGGER.trace("Retrieved unique values for {} in {} ms", specification.getTable() + "." + specification.getColumn(), watch.getTime(TimeUnit.MILLISECONDS));
    return new DistinctElementsResult("", distinct);
  }

  @Override
  public Class<ColumnSpecification> inClass() {
    return ColumnSpecification.class;
  }


  @Override
  public Class<DistinctElementsResult> outClass() {
    return DistinctElementsResult.class;
  }

  @Override
  public String route() {
    return ROUTE;
  }


  @Override
  public OpenApiDocumentation docs() {
    return OpenApiBuilder.document()
        .operation(op -> {
          op.summary("Find all distinct elements of a given column");
          op.description("Find all distinct elements of a given column. Please note that this operation does cache results.");
          op.operationId("FindDistinctElementsByColumn");
          op.addTagsItem("Misc");
        })
        .body(inClass())
        .json("200", outClass());
  }
}
