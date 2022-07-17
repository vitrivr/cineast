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
import org.vitrivr.cineast.api.messages.lookup.SelectSpecification;
import org.vitrivr.cineast.api.messages.result.SelectResult;
import org.vitrivr.cineast.api.rest.handlers.interfaces.ParsingPostRestHandler;
import org.vitrivr.cineast.core.data.providers.primitive.PrimitiveTypeProvider;
import org.vitrivr.cineast.standalone.config.Config;

public class SelectFromTablePostHandler implements ParsingPostRestHandler<SelectSpecification, SelectResult> {

  public static final String ROUTE = "find/boolean/table/select";

  private static final Logger LOGGER = LogManager.getLogger();

  @Override
  public SelectResult performPost(SelectSpecification input, Context ctx) {
    if (input == null || input.table().isEmpty() || input.columns().isEmpty()) {
      LOGGER.warn("returning empty list, invalid input {}", input);
      return new SelectResult(new ArrayList<>());
    }
    StopWatch watch = StopWatch.createStarted();
    try (var selector = Config.sharedConfig().getDatabase().getSelectorSupplier().get()) {

      selector.open(input.table());
      var _result = selector.getAll(input.columns(), input.limit());
      var stringified = _result.stream().map(el -> {
        Map<String, String> m = new HashMap<>();
        el.forEach((k, v) -> m.put(k, v.getString()));
        return m;
      }).collect(Collectors.toList());

      watch.stop();
      LOGGER.trace("Performed select on {}.{} in {} ms", input.table(), input.columns(), watch.getTime(TimeUnit.MILLISECONDS));
      return new SelectResult(stringified);
    }
  }

  @Override
  public Class<SelectSpecification> inClass() {
    return SelectSpecification.class;
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
          op.summary("Find all elements of given columns");
          op.description("Find all elements of given columns");
          op.operationId("SelectFromTable");
          op.addTagsItem("Misc");
        })
        .body(inClass())
        .json("200", outClass());
  }
}
