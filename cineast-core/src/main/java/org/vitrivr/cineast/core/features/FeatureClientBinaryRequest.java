package org.vitrivr.cineast.core.features;

import java.io.IOException;
import org.vitrivr.cineast.core.data.segments.SegmentContainer;
import org.vitrivr.cineast.core.features.abstracts.AbstractSegmentExporter;
import org.vitrivr.cineast.core.features.exporter.AudioSegmentExporter;
import org.vitrivr.cineast.core.util.web.WebClient;

public class FeatureClientBinaryRequest extends FeatureClientRequest {

  private final String key;
  public FeatureClientBinaryRequest(String requestString) {
    super();
    this.key = requestString;
  }

  @Override
  public String execute(SegmentContainer sc, WebClient client) throws IOException, InterruptedException {
    switch (this.key) {
      case "wav_binary":
        var wavExport = new AudioSegmentExporter();
        return client.postRawBinary(wavExport.exportToBinary(sc));
      default:
        LOGGER.warn("Unknown key: {}", this.key);
        return "";
    }
  }
}
