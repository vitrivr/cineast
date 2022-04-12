package org.vitrivr.cineast.api.rest.handlers.actions.bool;

import static org.vitrivr.cineast.api.util.APIConstants.TABLE_NAME;

import io.javalin.http.Context;
import io.javalin.plugin.openapi.dsl.OpenApiBuilder;
import io.javalin.plugin.openapi.dsl.OpenApiDocumentation;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.vitrivr.cineast.api.messages.general.IntegerMessage;
import org.vitrivr.cineast.api.rest.handlers.interfaces.GetRestHandler;
import org.vitrivr.cineast.standalone.config.Config;

public class CountRowsGetHandler implements GetRestHandler<IntegerMessage> {

  public static final String ROUTE = "count/table/{" + TABLE_NAME + "}";

  private static final Logger LOGGER = LogManager.getLogger(CountRowsGetHandler.class);

  @Override
  public IntegerMessage doGet(Context ctx) {
    try (final var selector = Config.sharedConfig().getDatabase().getSelectorSupplier().get()) {
      var tableName = ctx.pathParam(TABLE_NAME);
      selector.open(tableName);
      var count = selector.rowCount();
      LOGGER.trace("counted {} objects in table {}", count, tableName);
      return new IntegerMessage(count);
    }
  }

  @Override
  public Class<IntegerMessage> outClass() {
    return IntegerMessage.class;
  }

  @Override
  public String route() {
    return ROUTE;
  }

  @Override
  public OpenApiDocumentation docs() {
    return OpenApiBuilder.document()
        .operation(op -> {
          op.summary("Count objects");
          op.description("Equivalent to calling SELECT count(*) FROM table. Used to determined #pages for pagination in a frontend or statistical purposes");
          op.operationId("countRows");
          op.addTagsItem("Misc");
        })
        .json("200", outClass());
  }
}
