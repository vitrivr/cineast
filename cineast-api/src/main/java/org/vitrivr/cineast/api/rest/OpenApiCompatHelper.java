package org.vitrivr.cineast.api.rest;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import io.javalin.plugin.openapi.OpenApiOptions;
import io.javalin.plugin.openapi.jackson.JacksonToJsonMapper;
import io.javalin.plugin.openapi.ui.ReDocOptions;
import io.javalin.plugin.openapi.ui.SwaggerOptions;
import io.swagger.v3.core.jackson.ModelResolver;
import io.swagger.v3.core.jackson.mixin.SchemaMixin;
import io.swagger.v3.core.util.Json;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.oas.models.tags.Tag;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Collections;
import java.util.List;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.vitrivr.cineast.api.APIEndpoint;
import org.vitrivr.cineast.standalone.config.APIConfig;

/**
 * Helper class for openapi compatibility.
 */
public class OpenApiCompatHelper {

  /**
   * OpenAPI Specification tag for metadata related routes
   */
  public static final String METADATA_OAS_TAG = "Metadata";
  public static final String SEGMENT_OAS_TAG = "Segment";


  public static final List<Tag> OAS_TAGS = Collections.singletonList(
      new Tag().name(METADATA_OAS_TAG).description("Metadata related operations")
  );
  private static final Logger LOGGER = LogManager.getLogger();

  private OpenApiCompatHelper() {
    /* A static helper class. */
  }

  /**
   * Creates the Javalin options used to create an OpenAPI specification.
   */
  public static OpenApiOptions getJavalinOpenApiOptions(APIConfig config) {
    //Default Javalin JSON mapper includes all null values, which breakes the openapi specs.
    ObjectMapper mapper = new ObjectMapper();
    mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
    mapper.enable(SerializationFeature.INDENT_OUTPUT);
    mapper.addMixIn(Schema.class,
        SchemaMixin.class); // Makes Schema.exampleFlagSet being ignored by jackson
    return new OpenApiOptions(() -> getOpenApi(config))
        .path("/openapi-specs")
        .activateAnnotationScanningFor("org.vitrivr.cineast.api")
        .toJsonMapper(new JacksonToJsonMapper(mapper))
        .modelConverterFactory(() -> new ModelResolver(mapper))
        .swagger(new SwaggerOptions("/swagger-ui").title("Swagger UI for Cineast Documentation"))
        .reDoc(new ReDocOptions("/redoc").title("ReDoc for Cineast Documentation"));
  }

  /**
   * Creates the base {@link OpenAPI} specification.
   */
  public static OpenAPI getOpenApi(APIConfig config) {
    OpenAPI api = new OpenAPI();

    api.info(
        new Info()
            .title("Cineast RESTful API")
            .description(
                "Cineast is vitrivr's content-based multimedia retrieval engine. This is it's RESTful API.")
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

    return api;
  }

  public static void writeOpenApiDocPersistently(APIEndpoint apiEndpoint, final String path)
      throws IOException {
    try {
      apiEndpoint.setHttp(apiEndpoint.dispatchService(false));
      if (apiEndpoint.getOpenApi() != null) {
        String schema = Json.pretty(
            apiEndpoint.getOpenApi().getOpenApiHandler().createOpenAPISchema());
        File file = new File(path);
        File folder = file.getParentFile();
        if (folder != null && !folder.exists() && !folder.mkdirs()) {
          LOGGER.warn("Could not create OpenAPI documentation path: {}", folder.getAbsolutePath());
        }
        if (file.exists() && !file.delete()) {
          LOGGER.warn("Could not delete existing OpenAPI documentation: {}", file.getAbsolutePath());
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
