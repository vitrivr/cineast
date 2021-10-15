package org.vitrivr.cineast.standalone.config;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;


public final class MonitoringConfig {

  public boolean enablePrometheus = false;
  public int prometheusPort = 4569;

  @JsonProperty
  public int getPrometheusPort() {
    return prometheusPort;
  }

  public void setPrometheusPort(int prometheusPort) {
    this.prometheusPort = prometheusPort;
  }

  @JsonCreator
  public MonitoringConfig(){}

  @JsonProperty
  public boolean getEnablePrometheus() {
    return enablePrometheus;
  }
  public void setEnablePrometheus(boolean enablePrometheus) {
    this.enablePrometheus = enablePrometheus;
  }
}
