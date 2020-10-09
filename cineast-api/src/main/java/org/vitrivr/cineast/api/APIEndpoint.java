package org.vitrivr.cineast.api;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.javalin.Javalin;
import io.javalin.plugin.openapi.OpenApiOptions;
import io.javalin.plugin.openapi.OpenApiPlugin;
import io.javalin.plugin.openapi.dsl.OpenApiBuilder;
import io.javalin.plugin.openapi.ui.ReDocOptions;
import io.javalin.plugin.openapi.ui.SwaggerOptions;
import io.swagger.v3.core.jackson.ModelResolver;
import io.swagger.v3.core.util.Json;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.tags.Tag;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.eclipse.jetty.server.Connector;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;
import org.eclipse.jetty.util.ssl.SslContextFactory;
import org.eclipse.jetty.util.thread.QueuedThreadPool;
import org.vitrivr.cineast.api.rest.handlers.actions.StatusInvocationHandler;
import org.vitrivr.cineast.api.rest.handlers.actions.bool.FindDistinctElementsByColumnPostHandler;
import org.vitrivr.cineast.api.rest.handlers.actions.mediaobject.FindObjectAllGetHandler;
import org.vitrivr.cineast.api.rest.handlers.actions.mediaobject.FindObjectByIdPostHandler;
import org.vitrivr.cineast.api.rest.handlers.actions.mediaobject.FindObjectGetHandler;
import org.vitrivr.cineast.api.rest.handlers.actions.metadata.*;
import org.vitrivr.cineast.api.rest.handlers.actions.segment.FindSegmentByIdPostHandler;
import org.vitrivr.cineast.api.rest.handlers.actions.segment.FindSegmentSimilarPostHandler;
import org.vitrivr.cineast.api.rest.handlers.actions.segment.FindSegmentsByIdGetHandler;
import org.vitrivr.cineast.api.rest.handlers.actions.segment.FindSegmentsByObjectIdGetHandler;
import org.vitrivr.cineast.api.rest.handlers.actions.session.*;
import org.vitrivr.cineast.api.rest.handlers.actions.tag.FindTagsAllGetHandler;
import org.vitrivr.cineast.api.rest.handlers.actions.tag.FindTagsByIdsPostHandler;
import org.vitrivr.cineast.api.rest.handlers.actions.tag.FindTagsGetHandler;
import org.vitrivr.cineast.api.rest.handlers.interfaces.*;
import org.vitrivr.cineast.api.rest.resolvers.FileSystemObjectResolver;
import org.vitrivr.cineast.api.rest.resolvers.FileSystemThumbnailResolver;
import org.vitrivr.cineast.api.rest.routes.ResolvedContentRoute;
import org.vitrivr.cineast.api.websocket.WebsocketAPI;
import org.vitrivr.cineast.core.db.dao.reader.MediaObjectReader;
import org.vitrivr.cineast.standalone.config.APIConfig;
import org.vitrivr.cineast.standalone.config.Config;
import org.vitrivr.cineast.standalone.util.ContinuousRetrievalLogic;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * This class establishes a HTTP API endpoint listening on the specified port(s). The HTTP handling is facilitated by
 * the Spark framework (http://sparkjava.com/).
 * <p>
 * The {@link APIEndpoint} class supports setup for both the WebSocket and RestFul API endpoints, depending on the
 * configuration.
 * <p>
 * Incoming requests are routed towards an {@link DocumentedRestHandler} based on the HTTP method and the URL, provided that
 * such a handler hasn been registered beforehand.
 * <p>
 * WebSocket communication is forwarded to the {@link WebsocketAPI} class, which handles incoming packets.
 *
 * @author rgasser
 * @version 1.1
 * @created 07.12.17
 * @see WebsocketAPI
 */
public class APIEndpoint {
  /**
   * OpenAPI Specification tag for metadata related routes
   */
  public static final String METADATA_OAS_TAG = "Metadata";
  
  private static final Logger LOGGER = LogManager.getLogger();
  
  /**
   * Version of the protocol used by the RESTful endpoint. Will be appended to the endpoint URL.
   */
  private static final String VERSION = "v1";
  
  /**
   * Named context of the RESTful endpoint. Will be appended to the endpoint URL.
   */
  private static final String CONTEXT = "api";
  public static ContinuousRetrievalLogic retrievalLogic = new ContinuousRetrievalLogic(Config.sharedConfig().getDatabase()); //TODO there is certainly a nicer way to do this...
  private static APIEndpoint instance = null;
  private WebsocketAPI webSocketApi = null;
  private final List<DocumentedRestHandler> restHandlers = new ArrayList<>();
  /**
   * References to the HTTP and HTTPS service.
   */
  private Javalin http, https;
  /**
   * The Javalin OpenAPI plugin that generates the specification and serves the Swagger-UI
   */
  private OpenApiPlugin openApi;
  
  private APIEndpoint() {
    registerRestOperations();
  }
  
  public static APIEndpoint getInstance() {
    if (instance == null) {
      instance = new APIEndpoint();
    }
    return instance;
  }
  
  /**
   * Stops the RESTful / WebSocket API.
   */
  public static void stop() {
    if (instance != null) {
      instance.shutdown();
    }
    retrievalLogic.shutdown();
  }
  
  /**
   * Concatenates the provided service name into a full URL path.
   *
   * @param name Name of the service.
   * @return Full path to the service.
   */
  private static String makePath(String name) {
//        return String.format("%s/%s", "", name);
    return String.format("%s/%s", namespace(), name);
    //        return String.format("/%s/%s/%s", CONTEXT, VERSION, name);
  }
  
  private static String namespace() {
    return String.format("/%s/%s", CONTEXT, VERSION);
  }
  
  /**
   * Starts the RESTful / WebSocket API.
   */
  public void start() {
    /* Start insecure HTTP connection. */
    if (Config.sharedConfig().getApi().getEnableRest() || Config.sharedConfig().getApi().getEnableWebsocket()) {
      http = dispatchService(false);
    }
    
    if (Config.sharedConfig().getApi().getEnableRestSecure() || Config.sharedConfig().getApi().getEnableWebsocketSecure()) {
      https = dispatchService(true);
    }
  }
  
  public void shutdown() {
    if (Config.sharedConfig().getApi().getEnableRest() || Config.sharedConfig().getApi()
        .getEnableWebsocket()) {
      http.stop();
    }
    if (Config.sharedConfig().getApi().getEnableRestSecure() || Config.sharedConfig().getApi()
        .getEnableWebsocketSecure()) {
      https.stop();
    }
    if (Config.sharedConfig().getApi().getEnableWebsocket()) {
      webSocketApi.shutdown();
    }
  }
  
  public void writeOpenApiDocPersistently(final String path) throws IOException {
    try {
      http = dispatchService(false);
      if (openApi != null) {
        String schema = Json.pretty(openApi.getOpenApiHandler().createOpenAPISchema());
        File file = new File(path);
        File folder = file.getParentFile();
        if (folder != null) {
          folder.mkdirs();
        }
        if (file.exists()) {
          file.delete();
        }
        try (FileOutputStream stream = new FileOutputStream(file); PrintWriter writer = new PrintWriter(stream)) {
          writer.print(schema);
          writer.flush();
        }
        LOGGER.info("Successfully stored openapi spec at {}", path);
      }
    } finally {
      stop();
    }
  }
  
  /**
   * Dispatches a new Jetty {@link Javalin} (HTTP endpoint). The method takes care of all the necessary setup.
   *
   * @param secure If true, the new Service will be setup as secure with TLS enabled.
   * @return {@link Javalin}
   */
  private Javalin dispatchService(boolean secure) {
    final APIConfig config = Config.sharedConfig().getApi();
    
    final int port = this.validateAndNormalizePort(secure, config);
    
    final Javalin service = Javalin.create(serviceConfig -> {
      /* Configure server (TLS, thread pool, etc.) */
      serviceConfig.enableCorsForAllOrigins();
      serviceConfig.server(() -> {
        QueuedThreadPool threadPool = new QueuedThreadPool(config.getThreadPoolSize(), 2, 30000);
        
        Server server = new Server(threadPool);
        
        ServerConnector connector;
        if (secure) {
          /* Setup TLS if secure flag was set. */
          SslContextFactory sslContextFactory = new SslContextFactory.Server();
          sslContextFactory.setKeyStorePath(config.getKeystore());
          sslContextFactory.setKeyStorePassword(config.getKeystorePassword());
          connector = new ServerConnector(server, sslContextFactory);
        } else {
          connector = new ServerConnector(server);
        }
        
        if (port > 0) {
          connector.setPort(port);
        }
        
        server.setConnectors(new Connector[]{connector});
        
        return server;
      });
      
      /* Configure OpenAPI/Swagger doc */
      if (config.getEnableLiveDoc()) {
        this.openApi = new OpenApiPlugin(this.getJavalinOpenApiOptions(config));
        serviceConfig.registerPlugin(this.openApi);
        
        /* Enable webjars to serve Swagger-UI */
        serviceConfig.enableWebjars();
      }
    });
    
    /* Enable WebSocket (if configured). */
    if (config.getEnableWebsocket()) {
      this.webSocketApi = new WebsocketAPI();
      
      service.ws(String.format("%s/websocket", namespace()), handler -> {
        handler.onConnect(ctx -> {
          webSocketApi.connected(ctx.session);
        });
        
        handler.onClose(ctx -> {
          webSocketApi.closed(ctx.session, ctx.status(), ctx.reason());
        });
        
        handler.onError(ctx -> {
          webSocketApi.onWebSocketException(ctx.session, ctx.error());
        });
        
        handler.onMessage(ctx -> {
          webSocketApi.message(ctx.session, ctx.message());
        });
      });
    }

        /*
        // TODO re-implement this functionality
        if(config.getServeUI()){
          service.staticFiles.externalLocation(config.getUiLocation());
            service.redirect.any("/gallery", "/");
            service.redirect.any("/list", "/");
            service.redirect.any("/mini-gallery", "/");
            service.redirect.any("/mediaobject", "/");
            service.redirect.any("/mediaobject/:objectId", "/");

        }*/
    
    /* Setup HTTP/RESTful connection (if configured). */
    if (config.getEnableRest() || config.getEnableRestSecure()) {
      this.restHandlers.forEach(handler -> registerRestHandler(service, handler, config));
      this.registerServingRoutes(service, config);
    }
    
    /* Register a general exception handler. TODO: Add fine grained exception handling. */
    service.exception(Exception.class, (ex, ctx) -> {
      ex.printStackTrace();
      LOGGER.error(ex);
    });
    
    /* Some reasonable settings */
    service.config.defaultContentType = "application/json";
    service.config.prefer405over404 = true;
    
    /* Start javalin */
    try {
      if (port > 0) {
        service.start(port);
      } else {
        service.start();
      }
    } catch (Exception ex) {
      LOGGER.log(Level.FATAL, "Failed to start HTTP endpoint due to an exception. Cineast will shut down now!", ex);
      System.exit(100);
    }
    
    return service;
  }
  
  private int validateAndNormalizePort(boolean secure, APIConfig config) {
    int port = secure ? config.getHttpsPort() : config.getHttpPort();
    if (port <= 0 || port >= 65535) {
      APIConfig defaultConfig = new APIConfig();
      int _port = secure ? defaultConfig.getHttpsPort() : defaultConfig.getHttpPort();
      LOGGER.warn("The specified port {} is not valid. Fallback to default port {}.", port, _port);
      return _port;
    }
    return port;
  }
  
  private void registerRestHandler(final Javalin javalin, final DocumentedRestHandler handler, final APIConfig config) {
    if (handler instanceof GetRestHandler<?>) {
      javalin.get(makePath(handler.route()), OpenApiBuilder.documented(handler.docs(), ((GetRestHandler<?>) handler)::get));
    } else if (handler instanceof PostRestHandler<?>) {
      javalin.post(makePath(handler.route()), OpenApiBuilder.documented(handler.docs(), ((PostRestHandler<?>) handler)::post));
    } else if (handler instanceof DeleteRestHandler<?>) {
      javalin.delete(makePath(handler.route()), OpenApiBuilder.documented(handler.docs(), ((DeleteRestHandler<?>) handler)::delete));
    } else if (handler instanceof PutRestHandler<?>) {
      javalin.put(makePath(handler.route()), OpenApiBuilder.documented(handler.docs(), ((PutRestHandler<?>) handler)::put));
    } else {
      throw new IllegalArgumentException("The given handler of type " + handler.getClass() + " has no specified method");
    }
    /* One would implement the remaining HTTP methods here */
  }
  
  private void registerRestOperations() {
    restHandlers.addAll(Arrays.asList(
        /* Metadata */
        new FindObjectMetadataFullyQualifiedGetHandler(),
        new FindObjectMetadataGetHandler(),
        new FindObjectMetadataPostHandler(),
        new FindObjectMetadataByDomainGetHandler(),
        new FindObjectMetadataByDomainPostHandler(),
        new FindObjectMetadataByKeyGetHandler(),
        new FindObjectMetadataByKeyPostHandler(),
        /* Media Object */
        new FindObjectAllGetHandler(),
        new FindObjectByIdPostHandler(),
        new FindObjectGetHandler(),
        /* Segments */
        new FindSegmentByIdPostHandler(),
        new FindSegmentsByIdGetHandler(),
        new FindSegmentsByObjectIdGetHandler(),
        new FindSegmentSimilarPostHandler(retrievalLogic),
        new FindSegmentFeaturesGetHandler(),
        new FindSegmentTagsGetHandler(),
        new FindSegmentCaptionsGetHandler(),
        new FindSegmentOCRGetHandler(),
        new FindSegmentASRGetHandler(),
        /* Tags */
        new FindTagsAllGetHandler(),
        new FindTagsByIdsPostHandler(),
        new FindTagsGetHandler(),
        /* Session */
        new StartSessionHandler(),
        new StartExtractionHandler(),
        new ExtractItemHandler(),
        new ValidateSessionHandler(),
        new EndExtractionHandler(),
        new EndSessionHandler(),
        /* Boolean */
        new FindDistinctElementsByColumnPostHandler(),
        /* Status */
        new StatusInvocationHandler()
    ));
  }
  
  private void registerServingRoutes(final Javalin service, final APIConfig config) {
    // TODO Register these special cases as well with the new model
    if (config.getServeContent()) {
      service.get("/thumbnails/:id", new ResolvedContentRoute(
          new FileSystemThumbnailResolver(
              new File(Config.sharedConfig().getApi().getThumbnailLocation()))));
      
      service.get("/objects/:id", new ResolvedContentRoute(
          new FileSystemObjectResolver(
              new File(Config.sharedConfig().getApi().getObjectLocation()),
              new MediaObjectReader(Config.sharedConfig().getDatabase().getSelectorSupplier().get()))));
    }
  }
  
  /**
   * Creates the Javalin options used to create an OpenAPI specification.
   *
   * @param config
   * @return
   */
  private OpenApiOptions getJavalinOpenApiOptions(APIConfig config) {
    //Default Javalin JSON mapper includes all null values and breaks spec json
    ObjectMapper mapper = new ObjectMapper();
    mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
    
    return new OpenApiOptions(() -> this.getOpenApi(config))
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

//        return new OpenApiOptions(() -> this.getOpenApi(config))
//                .activateAnnotationScanningFor("org.vitrivr.cineast.api.rest")
//                .modelConverterFactory(() -> new ModelResolver(mapper) /*Default JavalinModelResolver breaks with Jackson JSON objects*/)
//                .jacksonMapper(mapper)
//                .toJsonMapper(o -> {
//                    try {
//                        return mapper.writeValueAsString(o);
//                    } catch (JsonProcessingException e) {
//                        throw new RuntimeException(e);
//                    }
//                })
//                .path("/openapi-specs")
//                .swagger(
//                        new SwaggerOptions("/swagger-ui")
//                        .title("Cineast Swagger Documentation")
//                        );
  }
  
  /**
   * Creates the base {@link OpenAPI} specification.
   *
   * @param config
   * @return
   */
  private OpenAPI getOpenApi(APIConfig config) {
    OpenAPI api = new OpenAPI();
    
    api.info(
        new Info()
            .title("Cineast RESTful API")
            .description("Cineast is vitrivr's content-based multimedia retrieval engine. This is it's RESTful API.")
            .version(VERSION)
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
            .name(namespace())
            .description("Cineast Default")
    );
    
    api.addTagsItem(new Tag().name(METADATA_OAS_TAG).description("Metadata related operations"));
    
    api.addServersItem(
        new io.swagger.v3.oas.models.servers.Server()
            .description("Cineast API Address")
            .url(config.getApiAddress())
    );
    
    return api;
  }
}
