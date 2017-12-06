package org.vitrivr.cineast.api.rest;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.vitrivr.cineast.api.rest.handlers.actions.FindMetadatasByIdActionHandler;
import org.vitrivr.cineast.api.rest.handlers.actions.FindObjectAllActionHandler;
import org.vitrivr.cineast.api.rest.handlers.actions.FindObjectByActionHandler;
import org.vitrivr.cineast.api.rest.handlers.actions.FindObjectsByIdActionHandler;
import org.vitrivr.cineast.api.rest.handlers.actions.FindSegmentAllByObjectIdActionHandler;
import org.vitrivr.cineast.api.rest.handlers.actions.FindSegmentSimilarActionHandler;
import org.vitrivr.cineast.api.rest.handlers.actions.FindSegmentsByIdActionHandler;
import org.vitrivr.cineast.api.rest.handlers.actions.FindTagsActionHandler;
import org.vitrivr.cineast.api.rest.handlers.actions.FindTagsByActionHandler;
import org.vitrivr.cineast.api.rest.handlers.actions.StatusInvokationHandler;
import org.vitrivr.cineast.api.rest.handlers.actions.session.EndSessionHandler;
import org.vitrivr.cineast.api.rest.handlers.actions.session.StartSessionHandler;
import org.vitrivr.cineast.api.rest.handlers.actions.session.ValidateSessionHandler;
import org.vitrivr.cineast.api.websocket.WebsocketAPI;
import org.vitrivr.cineast.core.config.APIConfig;
import org.vitrivr.cineast.core.config.Config;

import spark.Service;

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
  
  
  private static Service http, https;
  
  public static Service getHttp(){
    if(http == null){
      int port = Config.sharedConfig().getApi().getHttpPort();
      int threadPoolSize = Config.sharedConfig().getApi().getThreadPoolSize();
      http = Service.ignite();
      if (port > 0 && port < 65535) {
        http.port(port);
      } else {
        LOGGER.warn("The specified port {} is not valid. Fallback to default port.", port);
      }
      http.threadPool(threadPoolSize, 2, 30000);
      
    }
    return http;
  }
  
  public static Service getHttps(){
    if(https == null){
      
      APIConfig config = Config.sharedConfig().getApi();
      
      int port = config.getHttpsPort();
      https = Service.ignite();
      if (port > 0 && port < 65535) {
        https.port(port);
      } else {
        LOGGER.warn("The specified port {} is not valid. Fallback to default port.", port);
      }
      https.threadPool(config.getThreadPoolSize(), 2, 30000);
      https.secure(config.getKeystore(), config.getKeystorePassword(), null, null);
     
      
    }
    return https;
  }

  /**
   * Starts the RESTful / WebSocket API.
   *
   * @param port
   *          Port on which the WebSocket endpoint should listen.
   * @param numberOfThreads
   *          Maximum number of threads that should be used to handle messages.
   */
  public static void start() { //TODO check if already running
    
    if(Config.sharedConfig().getApi().getEnableWebsocket()){
      Service http = RestfulAPI.getHttp();
      http.webSocket(String.format("/%s/%s", CONTEXT, VERSION), WebsocketAPI.class);
    }
    
    if(Config.sharedConfig().getApi().getEnableRest()){
      Service http = getHttp();
      
      /* Register routes! */
      http.get(makePath("status"), new StatusInvokationHandler());
      
      http.path(makePath("find"), () -> {
        http.get("/object/by/:attribute/:value", new FindObjectByActionHandler());
        http.get("/objects/all/:type", new FindObjectAllActionHandler());
        
        http.get("/segments/all/object/:id", new FindSegmentAllByObjectIdActionHandler());
        
        http.get("/tags/by/:attribute/:value", new FindTagsByActionHandler(false));
        http.get("/tags/all", new FindTagsActionHandler());
              
        http.post("/segments/similar", new FindSegmentSimilarActionHandler());
        
        http.post("/segments/by/id", new FindSegmentsByIdActionHandler());
        http.post("/objects/by/id", new FindObjectsByIdActionHandler());
        http.post("/metas/by/id", new FindMetadatasByIdActionHandler());
        http.post("/tags/by/id", new FindTagsByActionHandler(true));
      });
      
    }
    
    
    if (Config.sharedConfig().getApi().getEnableRest()
        || Config.sharedConfig().getApi().getEnableWebsocket()) {
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

      http.init();
      http.awaitInitialization();
    }
    
    if(Config.sharedConfig().getApi().getEnableWebsocketSecure()){
      Service http = RestfulAPI.getHttps();
      http.webSocket(String.format("/%s/%s", CONTEXT, VERSION), WebsocketAPI.class);

    }
    
    if (Config.sharedConfig().getApi().getEnableRestSecure()) {
      Service http = getHttps();

      /* Register routes! */
      http.get(makePath("status"), new StatusInvokationHandler());
      
      http.path(makePath("find"), () -> {
        http.get("/object/by/:attribute/:value", new FindObjectByActionHandler());
        http.get("/objects/all/:type", new FindObjectAllActionHandler());
        
        http.get("/segments/all/object/:id", new FindSegmentAllByObjectIdActionHandler());
        
        http.get("/tags/by/:attribute/:value", new FindTagsByActionHandler(false));
        http.get("/tags/all", new FindTagsActionHandler());
              
        http.post("/segments/similar", new FindSegmentSimilarActionHandler());
        
        http.post("/segments/by/id", new FindSegmentsByIdActionHandler());
        http.post("/objects/by/id", new FindObjectsByIdActionHandler());
        http.post("/metas/by/id", new FindMetadatasByIdActionHandler());
        http.post("/tags/by/id", new FindTagsByActionHandler(true));
      });
      
      http.path(makePath("session"), () -> {
        http.post("/start", new StartSessionHandler());
        http.get("/end/:id", new EndSessionHandler());
        http.get("/validate/:id", new ValidateSessionHandler());
      });

      
    }
    
    if (Config.sharedConfig().getApi().getEnableRestSecure()
        || Config.sharedConfig().getApi().getEnableWebsocketSecure()) {
      /*
       * Configure the result after processing was completed.
       */
      https.after((request, response) -> {
        response.type("application/json");
        response.header("Access-Control-Allow-Origin", "*");
      });

      /* TODO: Add fine grained exception handling. */
      https.exception(Exception.class, (exception, request, response) -> {
        LOGGER.log(Level.ERROR, exception);
      });

      https.init();
      https.awaitInitialization();
    }
    
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
