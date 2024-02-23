package org.vitrivr.cineast.core.features;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import org.vitrivr.cineast.core.data.segments.SegmentContainer;
import org.vitrivr.cineast.core.features.exporter.AudioSegmentExporter;
import org.vitrivr.cineast.core.util.web.ImageParser;
import org.vitrivr.cineast.core.util.web.MessageTemplate;
import org.vitrivr.cineast.core.util.web.WebClient;

public class FeatureClientJsonRequest extends FeatureClientRequest {
  private final MessageTemplate requestTemplate;
  public FeatureClientJsonRequest(String RequestTemplatePath) throws IOException {
    super();
    this.requestTemplate = new MessageTemplate(RequestTemplatePath);
  }
  @Override
  public String execute(SegmentContainer sc, WebClient client) throws IOException, InterruptedException {
    Map<String, String> keys = new HashMap<>();
    //iterate through keys
    for (var key : this.requestTemplate.getKeys()) {
      switch (key) {
        case "png_image":
          var bufImg = sc.getMostRepresentativeFrame().getImage().getBufferedImage();
          keys.put(key, ImageParser.bufferedImageToDataURL(bufImg, "png"));
          break;
        case "wav_dataurl":
          var wavExport = new AudioSegmentExporter();
          keys.put(key, wavExport.exportToDataUrl(sc));
          break;
        case "text":
          keys.put(key, sc.getText());
          break;
        default:
          LOGGER.warn("Unknown key: {}", key);
          break;
      }
    }
    String request = this.requestTemplate.formatString(keys);
    return client.postJsonString(request);
  }
}
