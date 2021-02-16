package org.vitrivr.cineast.api;

import io.javalin.Javalin;
import io.javalin.http.staticfiles.Location;
import io.javalin.plugin.openapi.OpenApiPlugin;
import io.javalin.plugin.openapi.dsl.OpenApiBuilder;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.eclipse.jetty.server.Connector;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;
import org.eclipse.jetty.util.ssl.SslContextFactory;
import org.eclipse.jetty.util.thread.QueuedThreadPool;
import org.vitrivr.cineast.api.rest.OpenApiCompatHelper;
import org.vitrivr.cineast.api.rest.handlers.actions.StatusInvocationHandler;
import org.vitrivr.cineast.api.rest.handlers.actions.bool.FindDistinctElementsByColumnPostHandler;
import org.vitrivr.cineast.api.rest.handlers.actions.feature.FindSegmentFeaturesGetHandler;
import org.vitrivr.cineast.api.rest.handlers.actions.feature.FindSegmentTextGetHandler;
import org.vitrivr.cineast.api.rest.handlers.actions.feature.FindTagsForElementGetHandler;
import org.vitrivr.cineast.api.rest.handlers.actions.mediaobject.FindObjectAllGetHandler;
import org.vitrivr.cineast.api.rest.handlers.actions.mediaobject.FindObjectByIdPostHandler;
import org.vitrivr.cineast.api.rest.handlers.actions.mediaobject.FindObjectGetHandler;
import org.vitrivr.cineast.api.rest.handlers.actions.metadata.FindObjectMetadataByDomainGetHandler;
import org.vitrivr.cineast.api.rest.handlers.actions.metadata.FindObjectMetadataByDomainPostHandler;
import org.vitrivr.cineast.api.rest.handlers.actions.metadata.FindObjectMetadataByKeyGetHandler;
import org.vitrivr.cineast.api.rest.handlers.actions.metadata.FindObjectMetadataByKeyPostHandler;
import org.vitrivr.cineast.api.rest.handlers.actions.metadata.FindObjectMetadataFullyQualifiedGetHandler;
import org.vitrivr.cineast.api.rest.handlers.actions.metadata.FindObjectMetadataGetHandler;
import org.vitrivr.cineast.api.rest.handlers.actions.metadata.FindObjectMetadataPostHandler;
import org.vitrivr.cineast.api.rest.handlers.actions.metadata.FindSegmentMetadataGetHandler;
import org.vitrivr.cineast.api.rest.handlers.actions.segment.FindSegmentByIdPostHandler;
import org.vitrivr.cineast.api.rest.handlers.actions.segment.FindSegmentSimilarPostHandler;
import org.vitrivr.cineast.api.rest.handlers.actions.segment.FindSegmentsByIdGetHandler;
import org.vitrivr.cineast.api.rest.handlers.actions.segment.FindSegmentsByObjectIdGetHandler;
import org.vitrivr.cineast.api.rest.handlers.actions.session.EndExtractionHandler;
import org.vitrivr.cineast.api.rest.handlers.actions.session.EndSessionHandler;
import org.vitrivr.cineast.api.rest.handlers.actions.session.ExtractItemHandler;
import org.vitrivr.cineast.api.rest.handlers.actions.session.StartExtractionHandler;
import org.vitrivr.cineast.api.rest.handlers.actions.session.StartSessionHandler;
import org.vitrivr.cineast.api.rest.handlers.actions.session.ValidateSessionHandler;
import org.vitrivr.cineast.api.rest.handlers.actions.tag.FindTagsAllGetHandler;
import org.vitrivr.cineast.api.rest.handlers.actions.tag.FindTagsByIdsPostHandler;
import org.vitrivr.cineast.api.rest.handlers.actions.tag.FindTagsGetHandler;
import org.vitrivr.cineast.api.rest.handlers.interfaces.DeleteRestHandler;
import org.vitrivr.cineast.api.rest.handlers.interfaces.DocumentedRestHandler;
import org.vitrivr.cineast.api.rest.handlers.interfaces.GetRestHandler;
import org.vitrivr.cineast.api.rest.handlers.interfaces.PostRestHandler;
import org.vitrivr.cineast.api.rest.handlers.interfaces.PutRestHandler;
import org.vitrivr.cineast.api.rest.resolvers.FileSystemObjectResolver;
import org.vitrivr.cineast.api.rest.resolvers.FileSystemThumbnailResolver;
import org.vitrivr.cineast.api.rest.routes.ResolvedContentRoute;
import org.vitrivr.cineast.api.websocket.WebsocketAPI;
import org.vitrivr.cineast.core.db.dao.reader.MediaObjectReader;
import org.vitrivr.cineast.standalone.config.APIConfig;
import org.vitrivr.cineast.standalone.config.Config;
import org.vitrivr.cineast.standalone.util.ContinuousRetrievalLogic;

/**
 * This class establishes a HTTP API endpoint listening on the specified port(s). The HTTP handling is facilitated by the Javalin framework (https://javalin.io/).
 * <p>
 * The {@link APIEndpoint} class supports setup for both the WebSocket and RestFul API endpoints, depending on the configuration.
 * <p>
 * Incoming requests are routed towards a {@link DocumentedRestHandler} based on the HTTP method and the URL, provided that such a handler hasn't been registered beforehand.
 * <p>
 * WebSocket communication is forwarded to the {@link WebsocketAPI} class, which handles incoming packets.
 *
 * @see WebsocketAPI
 */
public class APIEndpoint {

  /**
   * Version of the protocol used by the RESTful endpoint. Will be appended to the endpoint URL.
   */
  public static final String VERSION = "v1";

  /**
   * The Logger used by the api to log general things. It is recommended that {@link org.vitrivr.cineast.api.rest.handlers.interfaces.RestHandler}s provide own loggers for more logging control.
   */
  private static final Logger LOGGER = LogManager.getLogger();
  /**
   * Named context of the RESTful endpoint. Will be appended to the endpoint URL.
   */
  private static final String CONTEXT = "api";

  /**
   * The retrieval logic used to retrieve
   */
  public static ContinuousRetrievalLogic retrievalLogic = new ContinuousRetrievalLogic(
      Config.sharedConfig().getDatabase());

  /**
   * The single instance of this class
   */
  private static APIEndpoint instance = null;
  /**
   * A list of {@link DocumentedRestHandler}s to register in this API.
   */
  private final List<DocumentedRestHandler> restHandlers = new ArrayList<>();
  /**
   * Websocket API
   */
  private WebsocketAPI webSocketApi = null;
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
    return String.format("%s/%s", namespace(), name);
  }

  /**
   * Returns the namespace of the API. Concatenation of /{@link #CONTEXT}/{@link #VERSION}
   *
   * @return The REST API namespace
   */
  public static String namespace() {
    return String.format("/%s/%s", CONTEXT, VERSION);
  }

  /**
   * Returns the non-secure {@link Javalin} instance this API uses
   *
   * <p>
   * *   <b>Warning</b> This is only exposed for {@link OpenApiCompatHelper} and might be removed * in a future update * </p>
   *
   * @return The non-secure http instance this API uses
   */
  public Javalin getHttp() {
    return http;
  }

  /**
   * Sets the non-secure http {@link Javalin} instance this API uses.
   * <p>
   * <b>Warning</b> This is only exposed for {@link OpenApiCompatHelper} and might be removed
   * in a future update
   * </p>
   *
   * @param http The non-secure {@link Javalin} instance for this API.
   */
  public void setHttp(Javalin http) {
    this.http = http;
  }

  /**
   * Returns the {@link OpenApiPlugin} this API has configured
   *
   * @return The {@link OpenApiPlugin} this API has configured
   */
  public OpenApiPlugin getOpenApi() {
    return openApi;
  }

  /**
   * Starts the RESTful / WebSocket API.
   */
  public void start() {
    /* Start insecure HTTP connection. */
    if (Config.sharedConfig().getApi().getEnableRest() || Config.sharedConfig().getApi()
        .getEnableWebsocket()) {
      http = dispatchService(false);
    }
    /* Start secure HTTP connection */
    if (Config.sharedConfig().getApi().getEnableRestSecure() || Config.sharedConfig().getApi()
        .getEnableWebsocketSecure()) {
      https = dispatchService(true);
    }
  }

  /**
   * Shuts down this API
   */
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

  /**
   * Dispatches a new Jetty {@link Javalin} (HTTP endpoint). The method takes care of all the necessary setup.
   *
   * @param secure If true, the new Service will be setup as secure with TLS enabled.
   * @return {@link Javalin}
   */
  public Javalin dispatchService(boolean secure) {
    final APIConfig config = Config.sharedConfig().getApi();

    final int port = this.validateAndNormalizePort(secure, config);

    final Javalin service = Javalin.create(serviceConfig -> {
      /* Configure server (TLS, thread pool, etc.) */
      serviceConfig.enableCorsForAllOrigins();
      /* Configuration of the actual server */
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
        this.openApi = new OpenApiPlugin(OpenApiCompatHelper.getJavalinOpenApiOptions(config));
        serviceConfig.registerPlugin(this.openApi);

        /* Enable webjars to serve Swagger-UI */
        serviceConfig.enableWebjars();
      }
      /* Serve the UI if requested statically*/
      if (config.getServeUI()) {
        /* Add css, js and other static files */
        serviceConfig.addStaticFiles(config.getUiLocation(), Location.EXTERNAL);
        /* Add index.html - the ui's front page as default route. Anything reroutes to thre */
        serviceConfig
            .addSinglePageRoot("/", config.getUiLocation() + "/index.html", Location.EXTERNAL);
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



    /* Setup HTTP/RESTful connection (if configured). */
    if (config.getEnableRest() || config.getEnableRestSecure()) {
      this.restHandlers.forEach(handler -> registerRestHandler(service, handler));
      this.registerServingRoutes(service, config);
    }

    /* Register a general exception handler. TODO: Add fine grained exception handling. */
    service.exception(Exception.class, (ex, ctx) -> {
      ex.printStackTrace();
      LOGGER.error(ex);
    });

    /* General settings */
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
      LOGGER.log(Level.FATAL,
          "Failed to start HTTP endpoint due to an exception. Cineast will shut down now!", ex);
      System.exit(100);
    }

    return service;
  }

  /**
   * Makes sure the configured port(s) is (are) in valid range. Fallback to default otherwise
   *
   * @param secure If the validation is for the secure configuration
   * @param config The active config
   * @return The port, validated and otherwise default port fromt he default config.
   */
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

  /**
   * Registers a {@link DocumentedRestHandler} according its specialisation with the corresponding http method. Currently, there are four specialisations:
   * <ul>
   *   <li>{@link GetRestHandler} for HTTP method <code>GET</code></li>
   *   <li>{@link PostRestHandler} for HTTP method <code>POST</code></li>
   *   <li>{@link DeleteRestHandler} for HTTP method <code>DELETE</code></li>
   *   <li>{@link PutRestHandler} for HTTP method <code>PUT</code></li>
   * </ul>
   *
   * @param javalin The {@link Javalin} instance to register the handler at
   * @param handler The handler to register
   */
  private void registerRestHandler(final Javalin javalin, final DocumentedRestHandler handler) {
    if (handler instanceof GetRestHandler<?>) {
      javalin.get(makePath(handler.route()),
          OpenApiBuilder.documented(handler.docs(), ((GetRestHandler<?>) handler)::get));
    } else if (handler instanceof PostRestHandler<?>) {
      javalin.post(makePath(handler.route()),
          OpenApiBuilder.documented(handler.docs(), ((PostRestHandler<?>) handler)::post));
    } else if (handler instanceof DeleteRestHandler<?>) {
      javalin.delete(makePath(handler.route()),
          OpenApiBuilder.documented(handler.docs(), ((DeleteRestHandler<?>) handler)::delete));
    } else if (handler instanceof PutRestHandler<?>) {
      javalin.put(makePath(handler.route()),
          OpenApiBuilder.documented(handler.docs(), ((PutRestHandler<?>) handler)::put));
    } else {
      throw new IllegalArgumentException(
          "The given handler of type " + handler.getClass() + " has no specified method");
    }
    /* One would implement the remaining HTTP methods here */
  }

  /**
   * Registers all the rest handlers.
   * <b>Must be called before actually creating the server</b>
   */
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
        new FindSegmentMetadataGetHandler(),
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
        new FindSegmentTextGetHandler(),
        /* Tags */
        new FindTagsAllGetHandler(),
        new FindTagsByIdsPostHandler(),
        new FindTagsForElementGetHandler(),
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

  /**
   * If configured, this registers two special routes that serve the media objects as media content and additionally a thumbnails endpoint for them.
   *
   * @param service
   * @param config
   */
  private void registerServingRoutes(final Javalin service, final APIConfig config) {
    if (config.getServeContent()) {
      service.get("/thumbnails/:id", new ResolvedContentRoute(
          new FileSystemThumbnailResolver(
              new File(Config.sharedConfig().getApi().getThumbnailLocation()))));

      /* The VBS database in-use is broken, this is the hack to circumvent object paths being wrong */
      FileSystemObjectResolver fsor;
      if (config.isObjectsFilesAreIDed()) {
        fsor = new FileSystemObjectResolver(
            new File(Config.sharedConfig().getApi().getObjectLocation()),
            new MediaObjectReader(
                Config.sharedConfig().getDatabase().getSelectorSupplier().get()),
            ((baseDir, object) -> {
              String ext = object.getPath().substring(object.getPath().lastIndexOf('.'));
              return new File(baseDir, object.getObjectId() + ext);
            }));
      } else {
        fsor = new FileSystemObjectResolver(
            new File(Config.sharedConfig().getApi().getObjectLocation()),
            new MediaObjectReader(
                Config.sharedConfig().getDatabase().getSelectorSupplier().get()));
      }

      service.get("/objects/:id", new ResolvedContentRoute(fsor
      ));
    }
  }

}
