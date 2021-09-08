package org.vitrivr.cineast.api.rest.handlers.actions.bool;

import io.javalin.http.Context;
import io.javalin.plugin.openapi.dsl.OpenApiBuilder;
import io.javalin.plugin.openapi.dsl.OpenApiDocumentation;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.vitrivr.cineast.api.messages.result.BooleanLookupResult;
import org.vitrivr.cineast.api.rest.handlers.interfaces.GetRestHandler;
import org.vitrivr.cineast.core.data.providers.primitive.PrimitiveTypeProvider;
import org.vitrivr.cineast.core.db.DBSelector;
import org.vitrivr.cineast.core.db.dao.reader.BooleanReader;
import org.vitrivr.cineast.standalone.config.Config;


/**
 * For boolean retrieval, returns all elements for a specific attribute
 */
public class FindAllElementsGetHandler implements GetRestHandler<BooleanLookupResult> {


    private static final Logger LOGGER = LogManager.getLogger();

    private static final DBSelector selector = Config.sharedConfig().getDatabase().getSelectorSupplier().get();

    private static final Map<String, List<String>> cache = new HashMap<String, List<String>>();

    public static final String TABLE_NAME = "table";
    public static final String TABLE_COLUMN = "column";

    public static final String ROUTE = "find/boolean/range/all/in/:" + TABLE_NAME + "/:" + TABLE_COLUMN;

    @Override
    public BooleanLookupResult doGet(Context ctx) {
        final Map<String, String> parameters = ctx.pathParamMap();
        final String table = parameters.get(TABLE_NAME);
        final String column = parameters.get(TABLE_COLUMN);
        final BooleanReader service = new BooleanReader(this.selector,
                table, column);
        List<String> content = service.getAllValues().stream().map(PrimitiveTypeProvider::getString).collect(Collectors.toList());;
        return new BooleanLookupResult("",0,content);
    }

    @Override
    public Class<BooleanLookupResult> outClass() {
        return BooleanLookupResult.class;
    }

    @Override
    public String route() {
        return ROUTE;
    }


    @Override
    public OpenApiDocumentation docs() {
        return OpenApiBuilder.document()
                .operation(op -> {
                    op.summary("Find all metadata for a specfic attribute");
                    op.description("Find all metadata for a specfic attribute");
                    op.operationId("findAllElements");
                    op.addTagsItem("Misc");
                })
                .pathParam(TABLE_NAME, String.class, p -> p.description("The table in which the data is stored"))
                .pathParam(TABLE_COLUMN, String.class, p -> p.description("The column with the data"))
                .json("200", outClass());
    }
}