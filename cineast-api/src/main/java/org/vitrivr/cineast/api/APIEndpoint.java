package org.vitrivr.cineast.api;

import static io.github.manusant.ss.descriptor.EndpointDescriptor.endpointPath;
import static io.github.manusant.ss.descriptor.MethodDescriptor.path;

import com.beerboy.spark.typify.spec.IgnoreSpec;
import com.fasterxml.jackson.annotation.JsonIgnore;
import io.github.manusant.ss.ApiEndpoint;
import io.github.manusant.ss.SparkSwagger;
import io.github.manusant.ss.descriptor.MethodDescriptor;
import io.github.manusant.ss.rest.Endpoint;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.vitrivr.cineast.api.rest.handlers.actions.*;
import org.vitrivr.cineast.api.rest.handlers.actions.session.*;
import org.vitrivr.cineast.api.rest.handlers.interfaces.ActionHandler;
import org.vitrivr.cineast.api.rest.handlers.interfaces.DocumentedRestOperation;
import org.vitrivr.cineast.api.rest.resolvers.FileSystemObjectResolver;
import org.vitrivr.cineast.api.rest.resolvers.FileSystemThumbnailResolver;
import org.vitrivr.cineast.api.rest.routes.ResolvedContentRoute;
import org.vitrivr.cineast.api.websocket.WebsocketAPI;

import org.vitrivr.cineast.core.db.dao.reader.MediaObjectReader;
import org.vitrivr.cineast.standalone.config.APIConfig;
import org.vitrivr.cineast.standalone.config.Config;
import org.vitrivr.cineast.standalone.util.ContinuousRetrievalLogic;
import spark.Service;

import java.io.File;
import spark.Spark;
import spark.route.HttpMethod;

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
    private Service http, https;

    private SparkSwagger swagger;

    public static ContinuousRetrievalLogic retrievalLogic = new ContinuousRetrievalLogic(Config.sharedConfig().getDatabase()); //TODO there is certainly a nicer way to do this...

    private static APIEndpoint instance = null;

    public static APIEndpoint getInstance(){
        if(instance == null){
            instance = new APIEndpoint();
        }
        return instance;
    }

    private APIEndpoint(){
        registerOperations();
    }

    private List<DocumentedRestOperation> registeredOperations = new ArrayList<>();

    /**
     * Dispatches a new Jetty {@link Service} (HTTP endpoint). The method takes care of all the necessary setup.
     *
     * @param secure If true, the new Service will be setup as secure with TLS enabled.
     * @return {@link Service}
     */
    @SuppressWarnings("unchecked") // SparkSwagger.ignores() results in warning...
    private Service dispatchService(boolean secure) {
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
        if(config.getEnableLiveDoc() && swagger == null){
            swagger = SparkSwagger.of(service).ignores(IgnoreSpec.newBuilder().withIgnoreAnnotated(
                JsonIgnore.class)::build).endpoint(instance);
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
    public void start() {
        /* Start insecure HTTP connection. */
        if (Config.sharedConfig().getApi().getEnableRest() || Config.sharedConfig().getApi().getEnableWebsocket()) {
            http = dispatchService(false);
            if(!Config.sharedConfig().getApi().getEnableLiveDoc()){
                registerRoutesOnService(http);
            }
            http.init();
            http.awaitInitialization();
        }

        if (Config.sharedConfig().getApi().getEnableRestSecure() || Config.sharedConfig().getApi().getEnableWebsocketSecure()) {
            https = dispatchService(true);
            registerRoutesOnService(https);
            https.init();
            https.awaitInitialization();
        }


        try {
            if(Config.sharedConfig().getApi().getEnableLiveDoc() && swagger != null){
                swagger.generateDoc();
            }
        } catch (IOException e) {
            LOGGER.warn("OpenAPI serving failed due to exception during generateDoc", e);
        }
    }

    /**
     * Stops the RESTful / WebSocket API.
     */
    public static void stop() {
        if(instance != null){
            instance.shutdown();
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
    }

    private ApiEndpoint registerSparkSwaggerEndpoint(final SparkSwagger restApi){
        return restApi.endpoint(endpointPath(namespace()).withDescription("namespace description or what?"), (q,a) -> {/* no default handler TODO endpoint descriptor? */});
    }


    private void registerRoutesOnEndpoint(final ApiEndpoint apiEndpoint){
        registeredOperations.forEach(r -> registerRoute(r,apiEndpoint));
    }

    private void registerRoutesOnService(final Service service){
        registeredOperations.forEach(r -> registerRoute(r, service));
    }

    @SuppressWarnings("unchecked")
    private void registerRoute(final DocumentedRestOperation route, final ApiEndpoint apiEndpoint){
        route.supportedMethods().forEach(m -> {
            HttpMethod method = (HttpMethod)m;
            MethodDescriptor.Builder builder;
            switch(method){
                case get:
                    apiEndpoint.get(path(makePath(route.routeForGet())).withResponseType(route.outClass()).withDescription(route.descriptionForGet()), route);
                    break;
                case post:
                    builder = path(makePath(route.routeForPost()));
                    if(route.isResponseCollection()){
                        builder.withResponseAsCollection(route.outClass());
                    }else{
                        builder.withResponseType(route.outClass());
                    }
                    builder.withRequestType(route.inClass()).withDescription(route.descriptionForPost());
                    apiEndpoint.post(builder, route);
                    break;
                case put:
                    builder = path(makePath(route.routeForPost()));
                    if(route.isResponseCollection()){
                        builder.withResponseAsCollection(route.outClass());
                    }else{
                        builder.withResponseType(route.outClass());
                    }
                    builder.withRequestType(route.inClass()).withDescription(route.descriptionForPost());
                    apiEndpoint.put(builder, route);
                    break;
                case delete:
                    builder = path(makePath(route.routeForPost()));
                    if(route.isResponseCollection()){
                        builder.withResponseAsCollection(route.outClass());
                    }else{
                        builder.withResponseType(route.outClass());
                    }
                    builder.withRequestType(route.inClass()).withDescription(route.descriptionForPost());
                    apiEndpoint.delete(builder, route);
                    break;
                default:
                    LOGGER.warn("Route {} supports unsupported http method {}",route, method);
            }
        });
    }

    @SuppressWarnings("unchecked")
    private void registerRoute(final DocumentedRestOperation route, final Service service){
        route.supportedMethods().forEach(m -> {
            HttpMethod method = (HttpMethod)m;
            MethodDescriptor.Builder builder;
            switch(method){
                case get:
                    service.get(makePath(route.routeForGet()), route);
                    break;
                case post:
                    service.post(makePath(route.routeForPost()), route);
                    break;
                case put:
                    service.put(makePath(route.routeForPut()), route);
                    break;
                case delete:
                    service.delete(makePath(route.routeForDelete()), route);
                    break;
                default:
                    LOGGER.warn("Route {} supports unsupported http method {}",route, method);
            }
        });
    }

    @Override
    public void bind(final SparkSwagger restApi){
        ApiEndpoint apiEndpoint = registerSparkSwaggerEndpoint(restApi);
        registerRoutesOnEndpoint(apiEndpoint);
    }

    private void registerOperations(){
        /* Add your operations here */
        registeredOperations.addAll(
            Arrays.asList(
                new FindMetadataByDomainWithKeyByObjectIdActionHandler(),
                new FindMetadataByKeyByObjectIdActionHandler(),
                new FindMetadataByObjectIdActionHandler(),
                new FindMetadataInDomainByObjectIdActionHandler(),
                new FindObjectAllActionHandler(),
                new FindObjectByActionHandler(),
                new FindSegmentsByIdActionHandler(),
                new FindSegmentsByObjectIdActionHandler(),
                new FindSegmentSimilarActionHandler(retrievalLogic),
                new FindTagsActionHandler(),
                new FindTagsByActionHandler(),
                new StatusInvokationHandler(),
                new EndExtractionHandler(),
                new EndSessionHandler(),
                new ExtractItemHandler(),
                new StartExtractionHandler(),
                new StartSessionHandler(),
                new ValidateSessionHandler()
            )
        );
    }

    /**
     * Registers the routes for the provided service.
     *
     * @param service Service for which routes should be registered.
     */
    private void registerRoutes(Service service) {
        // TODO Register these special cases as well with the new model
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

    public void writeOpenApiDocPersistently(final String path) throws IOException {
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
