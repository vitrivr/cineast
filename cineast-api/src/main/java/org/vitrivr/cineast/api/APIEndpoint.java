package org.vitrivr.cineast.api;

import static io.github.manusant.ss.descriptor.EndpointDescriptor.endpointPath;
import static io.github.manusant.ss.descriptor.MethodDescriptor.path;

import com.beerboy.spark.typify.spec.IgnoreSpec;
import com.fasterxml.jackson.annotation.JsonIgnore;
import io.github.manusant.ss.SparkSwagger;
import io.github.manusant.ss.rest.Endpoint;
import java.io.IOException;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.vitrivr.cineast.api.messages.general.AnyMessage;
import org.vitrivr.cineast.api.messages.general.Ping;
import org.vitrivr.cineast.api.messages.lookup.IdList;
import org.vitrivr.cineast.api.messages.lookup.OptionallyFilteredIdList;
import org.vitrivr.cineast.api.messages.query.SimilarityQuery;
import org.vitrivr.cineast.api.messages.result.MediaObjectMetadataQueryResult;
import org.vitrivr.cineast.api.messages.result.MediaObjectQueryResult;
import org.vitrivr.cineast.api.messages.result.MediaSegmentQueryResult;
import org.vitrivr.cineast.api.messages.result.SimilarityQueryResultBatch;
import org.vitrivr.cineast.api.messages.session.ExtractionContainerMessage;
import org.vitrivr.cineast.api.messages.session.SessionState;
import org.vitrivr.cineast.api.messages.session.StartSessionMessage;
import org.vitrivr.cineast.api.rest.handlers.actions.*;
import org.vitrivr.cineast.api.rest.handlers.actions.session.*;
import org.vitrivr.cineast.api.rest.handlers.interfaces.ActionHandler;
import org.vitrivr.cineast.api.rest.resolvers.FileSystemObjectResolver;
import org.vitrivr.cineast.api.rest.resolvers.FileSystemThumbnailResolver;
import org.vitrivr.cineast.api.rest.routes.ResolvedContentRoute;
import org.vitrivr.cineast.api.websocket.WebsocketAPI;

import org.vitrivr.cineast.core.data.entities.MediaObjectDescriptor;
import org.vitrivr.cineast.core.data.entities.MediaSegmentDescriptor;
import org.vitrivr.cineast.core.data.tag.Tag;
import org.vitrivr.cineast.core.db.dao.reader.MediaObjectReader;
import org.vitrivr.cineast.standalone.config.APIConfig;
import org.vitrivr.cineast.standalone.config.Config;
import org.vitrivr.cineast.standalone.util.ContinuousRetrievalLogic;
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
public class APIEndpoint implements Endpoint {
    private static final Logger LOGGER = LogManager.getLogger();

    /** Version of the protocol used by the RESTful endpoint. Will be appended to the endpoint URL.*/
    private static final String VERSION = "v1";

    /** Named context of the RESTful endpoint. Will be appended to the endpoint URL. */
    private static final String CONTEXT = "api";

    /** References to the HTTP and HTTPS service. */
    private static Service http, https;

    private static SparkSwagger swagger;

    public static ContinuousRetrievalLogic retrievalLogic = new ContinuousRetrievalLogic(Config.sharedConfig().getDatabase()); //TODO there is certainly a nicer way to do this...

    /**
     * Dispatches a new Jetty {@link Service} (HTTP endpoint). The method takes care of all the necessary setup.
     *
     * @param secure If true, the new Service will be setup as secure with TLS enabled.
     * @return {@link Service}
     */
    @SuppressWarnings("unchecked") // SparkSwagger.ignores() results in warning...
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

        /* Init Swagger */
        swagger = SparkSwagger.of(service).ignores(IgnoreSpec.newBuilder().withIgnoreAnnotated(
            JsonIgnore.class)::build).endpoint(new APIEndpoint());

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
            response.header("Access-Control-Allow-Headers", "*");
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


        try {
            swagger.generateDoc();
        } catch (IOException e) {
            LOGGER.warn("OpenAPI serving failed due to exception during generateDoc", e);
        }
    }

    /**
     * Stops the RESTful / WebSocket API.
     */
    public static void stop() {
        if (Config.sharedConfig().getApi().getEnableRest() || Config.sharedConfig().getApi().getEnableWebsocket()) {
            http.stop();
        }
        if (Config.sharedConfig().getApi().getEnableRestSecure() || Config.sharedConfig().getApi().getEnableWebsocketSecure()) {
            https.stop();
        }
    }

    @Override
    public void bind(final SparkSwagger restApi){
        // TODO replace parameters with constants and the routes (minus common makePath result) as route names. Might also be worth to document the routes by themselves
        restApi.endpoint(
            endpointPath(namespace()) // TODO See whether that's a good idea or the prefix of makePath should be used here
                .withDescription(""), (q,a) -> {})
        .get(path(makePath("status")).withDescription("Ping the server").withResponseType(Ping.class), new StatusInvokationHandler())
        .get(path(makePath("find/object/by/:attribute/:value"))
            .withDescription("Find object by attribute and value")
            .withResponseType(MediaObjectQueryResult.class)
            , new FindObjectByActionHandler())
        .get(path(makePath("find/metadata/by/id/:id"))
            .withDescription("Find meta data by object id")
            .withResponseType(MediaObjectMetadataQueryResult.class)
            , new FindMetadataByObjectIdActionHandler())
        .get(path(makePath("find/metadata/in/:domain/by/id/:id"))
            .withDescription("Find meta data in specific domain by object id")
            .withResponseType(MediaObjectMetadataQueryResult.class)
            , new FindMetadataInDomainByObjectIdActionHandler())
        .get(path(makePath("find/metadata/of/:id/in/:domain/with/:key"))
            .withDescription("Find meta data for a specific object id in given domain and key")
            .withResponseType(MediaObjectMetadataQueryResult.class)
            , new FindMetadataByDomainWithKeyByObjectIdActionHandler())
        .get(path(makePath("find/metadata/with/:key"))
            .withDescription("Find Meta data for a specific key")
            .withResponseType(MediaObjectMetadataQueryResult.class)
            , new FindMetadataByKeyByObjectIdActionHandler())
        .get(path(makePath("find/tags/by/:attribute/:value"))
            .withDescription("Find tags by attribute")
            .withResponseAsCollection(Tag.class)// TODO un-collection-ify this
            , new FindTagsByActionHandler())
        .get(path(makePath("find/objects/all/:type"))
            .withDescription("Find all objects by type")
            .withResponseAsCollection(MediaObjectDescriptor.class) // Todo un-collection-ify this
            , new FindObjectAllActionHandler())
        .get(path(makePath("find/segments/all/object/:id"))
            .withDescription("Find all segments by object id")
            .withResponseAsCollection(MediaSegmentDescriptor.class) // TODO un-collection-ify this
            , new FindSegmentsByObjectIdActionHandler())
        .get(path(makePath("find/tags/all"))
            .withDescription("Find all tags")
            .withResponseAsCollection(Tag.class) // TODO un-collection-ify this
            , new FindTagsActionHandler())
        .post(path(makePath("find/segments/similar"))
            .withDescription("Find similar segments. Request contains similarity query")
            .withRequestType(SimilarityQuery.class)
            .withResponseType(SimilarityQueryResultBatch.class)
            , new FindSegmentSimilarActionHandler(retrievalLogic))
        .post(path(makePath("find/segments/by/id"))
            .withDescription("Finds segments by segment id.  Request contains list of ids for which request was issued")
            .withRequestType(IdList.class)
            .withResponseType(MediaSegmentQueryResult.class)
            , new FindSegmentsByIdActionHandler())
        .post(path(makePath("find/objects/by/id"))
            .withDescription("Find objects by object id.  Request contains list of ids for which request was issued")
            .withRequestType(IdList.class)
            .withResponseType(MediaObjectQueryResult.class)
            , new FindObjectByActionHandler())
        .post(path(makePath("find/metadata/by/id"))
            .withDescription("Find meta data by object id.  Request contains list of ids for which request was issued")
            .withRequestType(OptionallyFilteredIdList.class)
            .withResponseType(MediaObjectMetadataQueryResult.class)
            , new FindMetadataByObjectIdActionHandler())
        .post(path(makePath("find/metadata/in/:domain"))
            .withDescription("Find meta data in domain.  Request contains list of ids for which request was issued")
            .withRequestType(IdList.class)
            .withResponseType(MediaObjectMetadataQueryResult.class)
            , new FindMetadataInDomainByObjectIdActionHandler())
        .post(path(makePath("find/metadata/with/:key"))
            .withDescription("Find meta data for a given key. Request contains list of ids for which request was issued")
            .withRequestType(IdList.class)
            .withResponseType(MediaObjectMetadataQueryResult.class)
            , new FindMetadataByKeyByObjectIdActionHandler())
        .post(path(makePath("find/tags/by/id"))
            .withDescription("Find tags by id")
            .withRequestType(IdList.class)
            .withResponseAsCollection(Tag.class) // TODO un-collection-ify this
            , new FindTagsByActionHandler())
        .post(path(makePath("session/start"))
            .withDescription("Starts a new session")
            .withRequestType(StartSessionMessage.class)
            .withResponseType(SessionState.class)
            , new StartSessionHandler())
        .get(path(makePath("session/end/:id"))
            .withDescription("Ends the given session")
            .withResponseType(SessionState.class)
            , new EndSessionHandler())
        .get(path(makePath("session/validate:id"))
            .withDescription("Validates the specified session")
            .withResponseType(SessionState.class)
            , new ValidateSessionHandler())
        .post(path(makePath("session/extract/new"))
            .withDescription("Extract new object")
            .withRequestType(ExtractionContainerMessage.class)
            .withResponseType(SessionState.class)
            , new ExtractItemHandler())
        .post(path(makePath("session/extract/end"))
            .withDescription("End the current extraction session")
            .withResponseType(SessionState.class)
            .withRequestType(AnyMessage.class)
            , new EndExtractionHandler())
        .post(path(makePath("session/extract/start"))
            .withDescription("Start the extraction session")
            .withRequestType(AnyMessage.class)
            .withResponseType(SessionState.class)
            , new StartExtractionHandler())
        ;
    }

    /**
     * Registers the routes for the provided service.
     *
     * @param service Service for which routes should be registered.
     */
    private static void registerRoutes(Service service) {
        // TODO is being replaced by singleton-swaggerised version of APIEndpoint, this is the todo-list
        /*service.get(makePath("status"), new StatusInvokationHandler());
        service.path(makePath("find"), () -> {
            service.get("/object/by/:attribute/:value", new FindObjectByActionHandler());
            service.get("/metadata/by/id/:id", new FindMetadataByObjectIdActionHandler());
            service.get("/metadata/in/:domain/by/id/:id", new FindMetadataInDomainByObjectIdActionHandler());
            service.get("/metadata/of/:id/in/:domain/with/:key", new FindMetadataByDomainWithKeyByObjectIdActionHandler());
            service.get("/metadata/with/:key", new FindMetadataByKeyByObjectIdActionHandler());
            service.get("/tags/by/:attribute/:value", new FindTagsByActionHandler());
            service.get("/objects/all/:type", new FindObjectAllActionHandler());
            service.get("/segments/all/object/:id", new FindSegmentsByObjectIdActionHandler());
            service.get("/tags/all", new FindTagsActionHandler());

            service.post("/segments/similar", new FindSegmentSimilarActionHandler(retrievalLogic));
            service.post("/segments/by/id", new FindSegmentsByIdActionHandler());
            service.post("/objects/by/id", new FindObjectByActionHandler());
            service.post("/metadata/by/id", new FindMetadataByObjectIdActionHandler());
            service.post("/metadata/in/:domain", new FindMetadataInDomainByObjectIdActionHandler());
            service.post("/metadata/with/:key", new FindMetadataByKeyByObjectIdActionHandler());
            service.post("/tags/by/id", new FindTagsByActionHandler());
        });*/
        /*service.path(makePath("session"), () -> {
            service.post("/start", new StartSessionHandler());
            service.get("/end/:id", new EndSessionHandler());
            service.get("/validate/:id", new ValidateSessionHandler());
            service.post("/extract/new", new ExtractItemHandler());
            service.post("/extract/end", new EndExtractionHandler());
            service.post("/extract/start", new StartExtractionHandler());
        });*/
        if (Config.sharedConfig().getApi().getServeContent()) {
          service.path(makePath("get"), () -> {
            service.get("/thumbnails/:id", new ResolvedContentRoute(
                new FileSystemThumbnailResolver(
                    new File(Config.sharedConfig().getApi().getThumbnailLocation()))));
            service.get("/objects/:id", new ResolvedContentRoute(
                new FileSystemObjectResolver(
                    new File(Config.sharedConfig().getApi().getObjectLocation()),
                        new MediaObjectReader(Config.sharedConfig().getDatabase().getSelectorSupplier().get()))));
          });
        }

    }

    public static void writeOpenApiDocPersistently(final String path) throws IOException {
        swagger.generateAndStoreDoc(path);
        LOGGER.info("Successfully stored openapi spec at "+path);
    }

    /**
     * Concatenates the provided service name into a full URL path.
     *
     * @param name Name of the service.
     * @return Full path to the service.
     */
    private static String makePath(String name) {
        return String.format("%s/%s", "", name);
//        return String.format("%s/%s", namespace(), name);
//        return String.format("/%s/%s/%s", CONTEXT, VERSION, name);
    }

    private static String namespace(){
        return String.format("/%s/%s", CONTEXT, VERSION);
    }
}
