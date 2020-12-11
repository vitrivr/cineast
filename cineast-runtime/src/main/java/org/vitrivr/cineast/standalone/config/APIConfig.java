package org.vitrivr.cineast.standalone.config;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.vitrivr.cineast.core.data.entities.MediaObjectDescriptor;

/**
 * The API configuration for cineast.
 * <p>
 * Settings regarding all aspects of the API are collected in this configuration.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public final class APIConfig {

  private boolean enableWebsocket = true;
  private boolean enableWebsocketSecure = true;
  private boolean enableExtractionServer = true;
  private boolean enableRest = false;
  private boolean enableRestSecure = false;

  private boolean enableGRPC = true;
  private String keystore;
  private String keystorePassword;

  private boolean enableRestLiveDoc = false; // Defaults to same result as enableRest
  private String apiAddress = "http://localhost:4567/";

  private int httpPort = 4567;
  private int httpsPort = 4568;


  private int grpcPort = 4570;
  private int maxMessageSize = 5120 * 1000; /* Maximum size of a single WebSocket message (binary or text). */

  private boolean allowExtraction = true;
  private boolean enableCLI = false;

  private int threadPoolSize = 8;

  private boolean serveContent = false;

  /**
   * A hack-flag to prevent the object serving using {@link MediaObjectDescriptor#getPath()} to find the actual media file. If this is true, the {@link MediaObjectDescriptor#getPath()}'s extension is appendet to {@link MediaObjectDescriptor#getObjectId()} and this is resolved to be directly under {@link #objectLocation}.
   */
  private boolean objectsFilesAreIDed = false;


  private boolean serveUI = false;
  private String thumbnailLocation = "";
  private String objectLocation = "";
  private String uiLocation = "";


  @JsonCreator
  public APIConfig() {
  }

  @JsonProperty
  public boolean getEnableWebsocket() {
    return this.enableWebsocket;
  }

  public void setEnableWebsocket(boolean enableWebsocket) {
    this.enableWebsocket = enableWebsocket;
  }

  @JsonProperty
  public boolean getEnableWebsocketSecure() {
    return this.enableWebsocketSecure;
  }

  public void setEnableWebsocketSecure(boolean enableWebsocket) {
    this.enableWebsocketSecure = enableWebsocket;
  }

  @JsonProperty
  public boolean getEnableRest() {
    return this.enableRest;
  }

  public void setEnableRest(boolean enableRest) {
    this.enableRest = enableRest;
  }

  @JsonProperty
  public boolean getEnableRestSecure() {
    return this.enableRestSecure;
  }

  public void setEnableRestSecure(boolean enableRest) {
    this.enableRestSecure = enableRest;
  }

  @JsonProperty
  public boolean getEnableLiveDoc() {
    return this.enableRestLiveDoc;
  }

  public void setEnableRestLiveDoc(boolean enableRestLiveDoc) {
    this.enableRestLiveDoc = enableRestLiveDoc;
  }

  @JsonProperty
  public String getKeystore() {
    return keystore;
  }

  public void setKeystore(String keystore) {
    this.keystore = keystore;
  }

  @JsonProperty
  public String getKeystorePassword() {
    return keystorePassword;
  }

  public void setKeystorePassword(String keystorePassword) {
    this.keystorePassword = keystorePassword;
  }

  @JsonProperty
  public String getApiAddress() {
    return apiAddress;
  }

  public void setApiAddress(String apiAddress) {
    this.apiAddress = apiAddress;
  }

  @JsonProperty
  public int getHttpPort() {
    return httpPort;
  }

  public void setHttpPort(int httpPort) {
    if (httpPort < 1) {
      throw new IllegalArgumentException("httpPort must be > 0");
    }
    this.httpPort = httpPort;
  }

  @JsonProperty
  public int getHttpsPort() {
    return httpsPort;
  }

  public void setHttpsPort(int httpsPort) {
    if (httpsPort < 1) {
      throw new IllegalArgumentException("httpPort must be > 0");
    }
    this.httpsPort = httpsPort;
  }

  @JsonProperty
  public int getMaxMessageSize() {
    return this.maxMessageSize;
  }

  public void setMaxMessageSize(int maxTextMessageSize) {
    this.maxMessageSize = maxTextMessageSize;
  }

  @JsonProperty
  public boolean getAllowExtraction() {
    return this.allowExtraction;
  }

  public void setAllowExtraction(boolean allowExtraction) {
    this.allowExtraction = allowExtraction;
  }

  @JsonProperty
  public boolean getEnableCli() {
    return this.enableCLI;
  }

  public void setEnableCLI(boolean enableCLI) {
    this.enableCLI = enableCLI;
  }

  @JsonProperty
  public int getThreadPoolSize() {
    return threadPoolSize;
  }

  public void setThreadPoolSize(int threadPoolSize) {
    this.threadPoolSize = threadPoolSize;
  }

  @JsonProperty
  public String getThumbnailLocation() {
    return thumbnailLocation;
  }

  public void setThumbnailLocation(String thumbnailLocation) {
    this.thumbnailLocation = thumbnailLocation;
  }

  @JsonProperty
  public String getObjectLocation() {
    return objectLocation;
  }

  public void setObjectLocation(String objectLocation) {
    this.objectLocation = objectLocation;
  }

  @JsonProperty
  public String getUiLocation() {
    return uiLocation;
  }

  public void setUiLocation(String uiLocation) {
    this.uiLocation = uiLocation;
  }

  @JsonProperty
  public boolean getServeContent() {
    return this.serveContent;
  }

  public void setServeContent(boolean serveContent) {
    this.serveContent = serveContent;
  }

  @JsonProperty
  public boolean getServeUI() {
    return this.serveUI;
  }

  public void setServeUI(boolean serveUI) {
    this.serveUI = serveUI;
  }

  @JsonProperty
  public boolean getEnableExtractionServer() {
    return enableExtractionServer;
  }

  public void setEnableExtractionServer(boolean enableExtractionServer) {
    this.enableExtractionServer = enableExtractionServer;
  }

  @JsonProperty
  public boolean getEnableGRPC() {
    return enableGRPC;
  }

  @JsonProperty
  public void setEnableGRPC(boolean enableGRPC) {
    this.enableGRPC = enableGRPC;
  }

  @JsonProperty
  public int getGrpcPort() {
    return grpcPort;
  }

  @JsonProperty
  public void setGrpcPort(int grpcPort) {
    this.grpcPort = grpcPort;
  }

  /**
   * A hack-flag to prevent the object serving using {@link MediaObjectDescriptor#getPath()} to find the actual media file. If this is true, the {@link MediaObjectDescriptor#getPath()}'s extension is appendet to {@link MediaObjectDescriptor#getObjectId()} and this is resolved to be directly under {@link #objectLocation}.
   */
  @JsonProperty
  public boolean isObjectsFilesAreIDed() {
    return objectsFilesAreIDed;
  }

  @JsonProperty
  public void setObjectsFilesAreIDed(boolean b) {
    objectsFilesAreIDed = b;
  }
}
