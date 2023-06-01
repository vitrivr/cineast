package org.vitrivr.cineast.core.features;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpClient.Version;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

public class FeatureClient {

  private String endpoint;

  public FeatureClient(String endpoint){
    this.endpoint = endpoint;
  }

  private final HttpClient httpClient = HttpClient.newBuilder()
      .version(Version.HTTP_1_1)
      .build();

  public String getResponse(String body) throws IOException, InterruptedException {
    HttpRequest request = HttpRequest.newBuilder()
        .POST(HttpRequest.BodyPublishers.ofString(body))
        .uri(URI.create(endpoint + "/extract"))
        .header("Content-Type", "application/json")
        .build();

    HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

    if (response.statusCode() != 200) {
      throw new IllegalStateException("received response code " + response.statusCode());
    }
    return response.body();
  }

}
