package org.vitrivr.cineast.api;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.vitrivr.cineast.api.rest.handlers.actions.*;
import org.vitrivr.cineast.api.rest.handlers.actions.session.*;
import org.vitrivr.cineast.api.rest.handlers.interfaces.ActionHandler;
import org.vitrivr.cineast.api.rest.resolvers.FileSystemObjectResolver;
import org.vitrivr.cineast.api.rest.resolvers.FileSystemThumbnailResolver;
import org.vitrivr.cineast.api.rest.routes.ResolvedContentRoute;
import org.vitrivr.cineast.api.websocket.WebsocketAPI;
import org.vitrivr.cineast.core.config.APIConfig;
import org.vitrivr.cineast.core.config.Config;
import spark.Service;

import java.io.File;

/**
 * This class establishes a HTTP API endpoint listening on the specified port(s). The HTTP handling is
 * facilitated by the Spark framework (http://sparkjava.com/).
 *
 * The {@link APIEndpoint} class supports setup for both the WebSocket and RestFul API endpoints, depending
 * on the configuration.
 *
 * Incoming requests are routed towards an {@link ActionHandler} based on the HTTP method and the URL,
 * provided that such a handler hasn been registered beforehand.
 *
 * WebSocket communication is forwarded to the {@link WebsocketAPI} class, which handles incoming packets.
 *
 * @author rgasser
 * @version 1.1
 * @created 07.12.17
 *
 * @see ActionHandler
 * @see WebsocketAPI
 */
public class APIEndpoint {
    private static final Logger LOGGER = LogManager.getLogger();

    /** Version of the protocol used by the RESTful endpoint. Will be appended to the endpoint URL.*/
    private static final String VERSION = "v1";

    /** Named context of the RESTful endpoint. Will be appended to the endpoint URL. */
    private static final String CONTEXT = "api";

    /** References to the HTTP and HTTPS service. */
    private static Service http, https;

    /**
     * Dispatches a new Jetty {@link Service} (HTTP endpoint). The method takes care of all the necessary setup.
     *
     * @param secure If true, the new Service will be setup as secure with TLS enabled.
     * @return {@link Service}
     */
    private static Service dispatchService(boolean secure) {
        final APIConfig config = Config.sharedConfig().getApi();
        final Service service = Service.ignite();

        /* Registers a exception handler in case the initialization fails. */
        service.initExceptionHandler((exception) -> {
           LOGGER.log(Level.FATAL,"Failed to start HTTP endpoint due to an exception. Cineast will shut down now!", exception);
           System.exit(100);
        });

        int port = secure ? config.getHttpsPort() : config.getHttpPort();

        /* Make basic setup (port and thread pool). */
        if (port > 0 && port < 65535) {
            service.port(port);
        } else {
            LOGGER.warn("The specified port {} is not valid. Fallback to default port.", port);
        }
        service.threadPool(config.getThreadPoolSize(), 2, 30000);

        /* Setup TLS if secure flag was set. */
        if (secure) {
            service.secure(config.getKeystore(), config.getKeystorePassword(), null, null);
        }

        /* Enable WebSocket (if configured). */
        if (config.getEnableWebsocket()) {
            service.webSocket(String.format("/%s/%s/websocket", CONTEXT, VERSION), WebsocketAPI.class);
        }


        if(config.getServeUI()){
          service.staticFiles.externalLocation(config.getUiLocation());
            service.redirect.any("/gallery", "/");
            service.redirect.any("/list", "/");
            service.redirect.any("/mini-gallery", "/");
            service.redirect.any("/mediaobject", "/");
            service.redirect.any("/mediaobject/:objectId", "/");

        }

        /* Setup HTTP/RESTful connection (if configured). */
        if (config.getEnableRest()) {
            registerRoutes(service);
        }

        /* Register a general exception handler. TODO: Add fine grained exception handling. */
        service.exception(Exception.class, (exception, request, response) -> {
            exception.printStackTrace();
            LOGGER.error(exception);
        });

        /* Configure the result after processing was completed. */
        service.after((request, response) -> {
            response.header("Access-Control-Allow-Origin", "*");
        });

        return service;
    }

    /**
     * Starts the RESTful / WebSocket API.
     */
    public static void start() {
        /* Start insecure HTTP connection. */
        if (Config.sharedConfig().getApi().getEnableRest() || Config.sharedConfig().getApi().getEnableWebsocket()) {
            http = dispatchService(false);
            http.init();
            http.awaitInitialization();
        }

        if (Config.sharedConfig().getApi().getEnableRestSecure() || Config.sharedConfig().getApi().getEnableWebsocketSecure()) {
            https = dispatchService(true);
            https.init();
            https.awaitInitialization();
        }
    }

    /**
     * Registers the routes for the provided service.
     *
     * @param service Service for which routes should be registered.
     */
    private static void registerRoutes(Service service) {
        service.get(makePath("status"), new StatusInvokationHandler());
        service.path(makePath("find"), () -> {
            service.get("/object/by/:attribute/:value", new FindObjectByActionHandler());
            service.get("/metadata/by/id/:id", new FindMetadataByObjectIdActionHandler());
            service.get("/metadata/in/:domain/by/id/:id", new FindMetadataInDomainByObjectIdActionHandler());
            service.get("/metadata/of/:id/in/:domain/with/:key", new FindMetadataByDomainWithKeyByObjectIdActionHandler());
            service.get("/tags/by/:attribute/:value", new FindTagsByActionHandler());
            service.get("/objects/all/:type", new FindObjectAllActionHandler());
            service.get("/segments/all/object/:id", new FindSegmentsByObjectIdActionHandler());
            service.get("/tags/all", new FindTagsActionHandler());
            service.post("/segments/similar", new FindSegmentSimilarActionHandler());
            service.post("/segments/by/id", new FindSegmentsByIdActionHandler());
            service.post("/objects/by/id", new FindObjectByActionHandler());
            service.post("/metadata/by/id", new FindMetadataByObjectIdActionHandler());
            service.post("/metadata/in/:domain", new FindMetadataInDomainByObjectIdActionHandler());
            service.post("/metadata/with/:key", new FindMetadataByKeyByObjectIdActionHandler());
            service.post("/tags/by/id", new FindTagsByActionHandler());
        });
        service.path(makePath("session"), () -> {
            service.post("/start", new StartSessionHandler());
            service.get("/end/:id", new EndSessionHandler());
            service.get("/validate/:id", new ValidateSessionHandler());
            service.post("/extract/new", new ExtractItemHandler());
            service.post("/extract/end", new EndExtractionHandler());
            service.post("/extract/start", new StartExtractionHandler());
        });
        if (Config.sharedConfig().getApi().getServeContent()) {
          service.path(makePath("get"), () -> {
            service.get("/thumbnails/:id", new ResolvedContentRoute(
                new FileSystemThumbnailResolver(
                    new File(Config.sharedConfig().getApi().getThumbnailLocation()))));
            service.get("/objects/:id", new ResolvedContentRoute(
                new FileSystemObjectResolver(
                    new File(Config.sharedConfig().getApi().getObjectLocation()))));
          });
        }

    }

    /**
     * Concatenates the provided service name into a full URL path.
     *
     * @param name Name of the service.
     * @return Full path to the service.
     */
    private static String makePath(String name) {
        return String.format("/%s/%s/%s", CONTEXT, VERSION, name);
    }
}
