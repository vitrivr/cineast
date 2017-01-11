package org.vitrivr.cineast.api.rest;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.vitrivr.cineast.api.rest.handlers.FindObjectSimilarActionHandler;
import org.vitrivr.cineast.api.rest.handlers.FindObjectAllActionHandler;
import org.vitrivr.cineast.api.rest.handlers.StatusInvokationHandler;
import org.vitrivr.cineast.api.rest.handlers.FindObjectByActionHandler;

import static spark.Spark.*;

/**
 * @author rgasser
 * @version 1.0
 * @created 09.01.17
 */
public class RestfulAPI {

    private static Logger LOGGER = LogManager.getLogger();

    /**
     *
     */
    private static final String VERSION = "v1";

    /**
     *
     */
    private static final String CONTEXT = "api/";

    /**
     *
     * @param port
     */
    public RestfulAPI(int port) {
        if (port > 0 && port <= 65535) port(port);
        threadPool(8);
    }

    /**
     *
     * @param port
     * @param maxthread
     */
    public RestfulAPI(int port, short maxthread) {
        if (port > 0 && port <= 65535) port(port);
        threadPool(maxthread);
    }


    /**
     *
     */
    public void start() {
        /*
         * Register different routes vor Vitrivr RESTful API.
         */
        get(path("status"), new StatusInvokationHandler());
        get(path("find/object/by/:attribute/:value"), new FindObjectByActionHandler());
        get(path("find/object/all/:type"), new FindObjectAllActionHandler());
        get(path("find/segment/all/:attribute/:value"), (request, response) -> {
            /* TODO: Implement! */
            return null;
        });
        get(path("find/segment/all/:type"), (request, response) -> {
            /* TODO: Implement! */
            return null;
        });
        post(path("find/object/similar"), new FindObjectSimilarActionHandler());

        /*
         * Configure the response after processing was completed.
         */
        after((request, response) -> {
            response.type("application/json");
            response.header("Access-Control-Allow-Origin", "*");
        });

        /* TODO: Add fine grained exception handling. */
        exception(Exception.class, (exception, request, response) -> {
            LOGGER.log(Level.ERROR, exception);
        });
    }

    /**
     *
     * @param name
     * @return
     */
    private String path(String name) {
        return String.format("/%s/%s/%s", CONTEXT, VERSION, name);
    }
}
