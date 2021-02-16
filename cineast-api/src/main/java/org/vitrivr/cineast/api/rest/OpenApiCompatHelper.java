package org.vitrivr.cineast.api.rest;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.javalin.plugin.openapi.OpenApiOptions;
import io.javalin.plugin.openapi.ui.ReDocOptions;
import io.javalin.plugin.openapi.ui.SwaggerOptions;
import io.swagger.v3.core.jackson.ModelResolver;
import io.swagger.v3.core.util.Json;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.tags.Tag;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.vitrivr.cineast.api.APIEndpoint;
import org.vitrivr.cineast.standalone.config.APIConfig;

import java.util.Arrays;
import java.util.List;

/**
 * Helper class for openapi compatibility.
 *
 * @author Loris Sauter
 * @version 1.0
 */
public class OpenApiCompatHelper {

    /**
     * OpenAPI Specification tag for metadata related routes
     */
    public static final String METADATA_OAS_TAG = "Metadata";
    public static final String SEGMENT_OAS_TAG = "Segment";


    public static final List<Tag> OAS_TAGS = Arrays.asList(
            new Tag().name(METADATA_OAS_TAG).description("Metadata related operations")
    );
    private static final Logger LOGGER = LogManager.getLogger();

    private OpenApiCompatHelper() {
        /* A static helper class. */
    }

    /**
     * Creates the Javalin options used to create an OpenAPI specification.
     *
     * @param config
     * @return
     */
    public static OpenApiOptions getJavalinOpenApiOptions(APIConfig config) {
        //Default Javalin JSON mapper includes all null values and breaks spec json
        ObjectMapper mapper = new ObjectMapper();
        mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);

        return new OpenApiOptions(() -> getOpenApi(config))
                .path("/openapi-specs")
                .activateAnnotationScanningFor("org.vitrivr.cineast.api")
                //        .toJsonMapper(new JacksonToJsonMapper())
                //        .modelConverterFactory(new JacksonModelConverterFactory())
                .toJsonMapper(o -> {
                    try {
                        return mapper.writeValueAsString(o);
                    } catch (JsonProcessingException e) {
                        throw new RuntimeException("Couldn't serialise due to ", e);
                    }
                })
                .modelConverterFactory(() -> new ModelResolver(mapper))
                .swagger(new SwaggerOptions("/swagger-ui").title("Swagger UI for Cineast Documentation"))
                .reDoc(new ReDocOptions("/redoc").title("ReDoc for Cineast Documentation"));
    }

    /**
     * Creates the base {@link OpenAPI} specification.
     *
     * @param config
     * @return
     */
    public static OpenAPI getOpenApi(APIConfig config) {
        OpenAPI api = new OpenAPI();

        api.info(
                new Info()
                        .title("Cineast RESTful API")
                        .description("Cineast is vitrivr's content-based multimedia retrieval engine. This is it's RESTful API.")
                        .version(APIEndpoint.VERSION)
                        .license(
                                new License()
                                        .name("Apache 2.0")
                                        .url("http://www.apache.org/licenses/LICENSE-2.0.html")
                        )
                        .contact(
                                new Contact()
                                        .name("Cineast Team")
                                        .url("https://vitrivr.org")
                                        .email("contact@vitrivr.org")
                        )
        );

        api.addTagsItem(
                new Tag()
                        .name(APIEndpoint.namespace())
                        .description("Cineast Default")
        );

        /* Add the OAS tags */
        OAS_TAGS.forEach(api::addTagsItem);

        api.addServersItem(
                new io.swagger.v3.oas.models.servers.Server()
                        .description("Cineast API Address")
                        .url(config.getApiAddress())
        );

        return api;
    }

    public static void writeOpenApiDocPersistently(APIEndpoint apiEndpoint, final String path) throws IOException {
      try {
        apiEndpoint.setHttp(apiEndpoint.dispatchService(false));
        if (apiEndpoint.getOpenApi() != null) {
          String schema = Json.pretty(apiEndpoint.getOpenApi().getOpenApiHandler().createOpenAPISchema());
          File file = new File(path);
          File folder = file.getParentFile();
          if (folder != null) {
            folder.mkdirs();
          }
          if (file.exists()) {
            file.delete();
          }
          try (FileOutputStream stream = new FileOutputStream(
              file); PrintWriter writer = new PrintWriter(stream)) {
            writer.print(schema);
            writer.flush();
          }
          LOGGER.info("Successfully stored openapi spec at {}", path);
        }
      } finally {
        APIEndpoint.stop();
      }
    }
}
