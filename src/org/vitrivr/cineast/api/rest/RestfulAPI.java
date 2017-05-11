package org.vitrivr.cineast.api.rest;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.vitrivr.cineast.api.rest.handlers.actions.FindMetadatasByIdActionHandler;
import org.vitrivr.cineast.api.rest.handlers.actions.FindObjectAllActionHandler;
import org.vitrivr.cineast.api.rest.handlers.actions.FindObjectByActionHandler;
import org.vitrivr.cineast.api.rest.handlers.actions.FindObjectSimilarActionHandler;
import org.vitrivr.cineast.api.rest.handlers.actions.FindObjectsByIdActionHandler;
import org.vitrivr.cineast.api.rest.handlers.actions.FindSegmentAllByObjectIdActionHandler;
import org.vitrivr.cineast.api.rest.handlers.actions.FindSegmentsByIdActionHandler;
import org.vitrivr.cineast.api.rest.handlers.actions.StatusInvokationHandler;
import org.vitrivr.cineast.api.rest.handlers.actions.session.EndSessionHandler;
import org.vitrivr.cineast.api.rest.handlers.actions.session.StartSessionHandler;
import org.vitrivr.cineast.api.rest.handlers.actions.session.ValidateSessionHandler;

import spark.RouteGroup;
import spark.Service;
import spark.Spark;

/**
 * This class establishes a RESTful endpoint listening on the specified port. Incoming requests are
 * routed towards an ActionHandler based on the HTTP method and the URI, provided that such a
 * handler hasn been registered beforehand.
 *
 * @see org.vitrivr.cineast.api.rest.handlers.interfaces.ActionHandler
 *
 * @author rgasser
 * @version 1.0
 * @created 19.01.17
 */
public class RestfulAPI {

  private static final Logger LOGGER = LogManager.getLogger();

  /** Version of the protocol used by the RESTful endpoint. Will be appended to the endpoint URL. */
  private static final String VERSION = "v1";

  /** Named context of the RESTful endpoint. Will be appended to the endpoint URL. */
  private static final String CONTEXT = "api";

  /**
   * Starts the RESTful API.
   *
   * @param port
   *          Port on which the WebSocket endpoint should listen.
   * @param numberOfThreads
   *          Maximum number of threads that should be used to handle messages.
   */
  public static void start(int port, int numberOfThreads) {
    Service http = Service.ignite();
    
    if (port > 0 && port < 65535) {
      http.port(port);
    } else {
      LOGGER.warn("The specified port {} is not valid. Fallback to default port.", port);
    }
    http.threadPool(numberOfThreads, 2, 30000);
    //https.secure("keystore.jks", "password", null, null);

    /* Register routes! */
    http.get(makePath("status"), new StatusInvokationHandler());
    
    http.path(makePath("find"), () -> {
      http.get("/object/by/:attribute/:value", new FindObjectByActionHandler());
      http.get("/object/all/:type", new FindObjectAllActionHandler());
      
      http.get("/segment/all/object/:id", new FindSegmentAllByObjectIdActionHandler());
            
      http.post("/object/similar/", new FindObjectSimilarActionHandler());
      
      http.post("/segments/by/id", new FindSegmentsByIdActionHandler());
      http.post("/objects/by/id", new FindObjectsByIdActionHandler());
      http.post("/metas/by/id", new FindMetadatasByIdActionHandler());
    });

    
    http.path(makePath("session"), () -> { //TODO move to separate service
      http.post("/start", new StartSessionHandler());
      http.get("/end/:id", new EndSessionHandler());
      http.get("/validate/:id", new ValidateSessionHandler());
    });
    
    /*
     * Configure the result after processing was completed.
     */
    http.after((request, response) -> {
      response.type("application/json");
      response.header("Access-Control-Allow-Origin", "*");
    });

    /* TODO: Add fine grained exception handling. */
    http.exception(Exception.class, (exception, request, response) -> {
      LOGGER.log(Level.ERROR, exception);
    });

    http.awaitInitialization();
  }

  /**
   *
   * @param name
   * @return
   */
  private static String makePath(String name) {
    return String.format("/%s/%s/%s", CONTEXT, VERSION, name);
  }
}
