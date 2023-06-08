package org.vitrivr.cineast.core.util.web;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpClient.Version;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

public class WebClient {

  private final String endpoint;

  public WebClient(String endpoint) {
    this.endpoint = endpoint;
  }

  private final HttpClient httpClient = HttpClient.newBuilder()
      .version(Version.HTTP_1_1)
      .build();

  public String postJsonString(String body) throws IOException, InterruptedException {
    HttpRequest request = HttpRequest.newBuilder()
        .POST(HttpRequest.BodyPublishers.ofString(body))
        .uri(URI.create(endpoint))
        .header("Content-Type", "application/json")
        .build();

    HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

    if (response.statusCode() != 200) {
      throw new IllegalStateException("received response code " + response.statusCode());
    }
    return response.body();
  }

  public String postRawBinary(byte[] body) throws IOException, InterruptedException {
    HttpRequest request = HttpRequest.newBuilder()
        .POST(HttpRequest.BodyPublishers.ofByteArray(body))
        .uri(URI.create(endpoint))
        .header("Content-Type", "application/octet-stream")
        .build();

    HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

    if (response.statusCode() != 200) {
      throw new IllegalStateException("received response code " + response.statusCode());
    }
    return response.body();
  }
}
