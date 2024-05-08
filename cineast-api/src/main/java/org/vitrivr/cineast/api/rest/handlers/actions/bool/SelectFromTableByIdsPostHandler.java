package org.vitrivr.cineast.api.rest.handlers.actions.bool;

import io.javalin.http.Context;
import io.javalin.plugin.openapi.dsl.OpenApiBuilder;
import io.javalin.plugin.openapi.dsl.OpenApiDocumentation;
import org.apache.commons.lang3.time.StopWatch;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.vitrivr.cineast.api.messages.lookup.SelectByIdsSpecification;
import org.vitrivr.cineast.api.messages.result.SelectResult;
import org.vitrivr.cineast.api.rest.handlers.interfaces.ParsingPostRestHandler;
import org.vitrivr.cineast.core.data.providers.primitive.NothingProvider;
import org.vitrivr.cineast.standalone.config.Config;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

public class SelectFromTableByIdsPostHandler implements ParsingPostRestHandler<SelectByIdsSpecification, SelectResult> {

  public static final String ROUTE = "find/boolean/table/select/ids";

  private static final Logger LOGGER = LogManager.getLogger();

  @Override
  public SelectResult performPost(SelectByIdsSpecification input, Context ctx) {
    if (input == null || input.table().isEmpty() || input.columns().isEmpty() || input.ids().isEmpty()) {
      LOGGER.warn("returning empty list, invalid input {}", input);
      return new SelectResult(new ArrayList<>());
    }
    StopWatch watch = StopWatch.createStarted();
    try (var selector = Config.sharedConfig().getDatabase().getSelectorSupplier().get()) {

      selector.open(input.table());
      var _result = selector.getRows(input.idColumn(), input.ids());
      var stringified = _result.stream().map(el -> {
        Map<String, String> m = new HashMap<>();
        input.columns().forEach(col -> {
          if (el.containsKey(col) && el.get(col).getClass() != NothingProvider.class) {
            m.put(col, el.get(col).getString());
          }
        });
        return m;
      }).collect(Collectors.toList());

      watch.stop();
      LOGGER.trace("Performed select on {}.{} in {} ms", input.table(), input.columns(), watch.getTime(TimeUnit.MILLISECONDS));
      return new SelectResult(stringified);
    }
  }

  @Override
  public Class<SelectByIdsSpecification> inClass() {
    return SelectByIdsSpecification.class;
  }


  @Override
  public Class<SelectResult> outClass() {
    return SelectResult.class;
  }

  @Override
  public String route() {
    return ROUTE;
  }

  @Override
  public OpenApiDocumentation docs() {
    return OpenApiBuilder.document()
        .operation(op -> {
          op.summary("Find all elements of given columns with given ids");
          op.description("Find all elements of given columns with given ids");
          op.operationId("SelectFromTableByIds");
          op.addTagsItem("Misc");
        })
        .body(inClass())
        .json("200", outClass());
  }
}
