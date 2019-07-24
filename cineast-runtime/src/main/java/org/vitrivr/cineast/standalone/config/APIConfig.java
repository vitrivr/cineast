package org.vitrivr.cineast.standalone.config;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public final class APIConfig {

  private boolean enableWebsocket = true;
  private boolean enableWebsocketSecure = true;
	private boolean enableExtractionServer = true;
	private boolean enableRest = false;
  private boolean enableRestSecure = false;
	private String keystore;
	private String keystorePassword;

	private int httpPort = 4567;
	private int httpsPort = 4568;
	private int maxMessageSize = 5120 * 1000; /* Maximum size of a single WebSocket message (binary or text). */

	private boolean enableLegacy = false;
	private int legacyPort = 12345;

	private boolean allowExtraction = true;
	private boolean enableCLI = false;

	private int threadPoolSize = 8;

	private boolean serveContent = false;
	private boolean serveUI = false;
	private String thumbnailLocation = "";
	private String objectLocation = "";
	private String uiLocation = "";

	@JsonCreator
	public APIConfig() {}

	@JsonProperty
  public boolean getEnableWebsocket(){
    return this.enableWebsocket;
  }
  public void setEnableWebsocket(boolean enableWebsocket) {
    this.enableWebsocket = enableWebsocket;
  }

  @JsonProperty
  public boolean getEnableWebsocketSecure(){
    return this.enableWebsocketSecure;
  }
  public void setEnableWebsocketSecure(boolean enableWebsocket) {
    this.enableWebsocketSecure = enableWebsocket;
  }

  @JsonProperty
  public boolean getEnableRest() {return this.enableRest;}
  public void setEnableRest(boolean enableRest) {
    this.enableRest = enableRest;
  }

  @JsonProperty
  public boolean getEnableRestSecure() {return this.enableRestSecure;}
  public void setEnableRestSecure(boolean enableRest) {
    this.enableRestSecure = enableRest;
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
  public int getHttpPort() {
    return httpPort;
  }
  public void setHttpPort(int httpPort) {
    if(httpPort < 1){
      throw new IllegalArgumentException("httpPort must be > 0");
    }
    this.httpPort = httpPort;
  }

  @JsonProperty
  public int getHttpsPort() {
    return httpsPort;
  }
  public void setHttpsPort(int httpsPort) {
    if(httpsPort < 1){
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
	public boolean getEnableLegacy() {
		return enableLegacy;
	}
	public void setEnableLegacy(boolean enableLegacy) {
		this.enableLegacy = enableLegacy;
	}

	@JsonProperty
	public int getLegacyPort() {
		return legacyPort;
	}
	public void setLegacyPort(int legacyPort) {
		this.legacyPort = legacyPort;
	}

	@JsonProperty
	public boolean getAllowExtraction(){
		return this.allowExtraction;
	}
	public void setAllowExtraction(boolean allowExtraction) {
		this.allowExtraction = allowExtraction;
	}

	@JsonProperty
	public boolean getEnableCli(){
		return this.enableCLI;
	}
	public void setEnableCLI(boolean enableCLI) {
		this.enableCLI = enableCLI;
	}

	@JsonProperty
	public int getThreadPoolSize() {
		return threadPoolSize;
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

  public void setThreadPoolSize(int threadPoolSize) {
		this.threadPoolSize = threadPoolSize;
	}

	@JsonProperty
	public boolean getServeContent(){
		return this.serveContent;
	}
	public void setServeContent(boolean serveContent) {
		this.serveContent = serveContent;
	}

	@JsonProperty
	public boolean getServeUI(){
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
}
