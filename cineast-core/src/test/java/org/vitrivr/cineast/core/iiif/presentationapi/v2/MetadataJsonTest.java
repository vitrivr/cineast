package org.vitrivr.cineast.core.iiif.presentationapi.v2;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.fasterxml.jackson.core.JsonProcessingException;
import java.util.Collections;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.vitrivr.cineast.core.iiif.presentationapi.v2.models.Canvas;
import org.vitrivr.cineast.core.iiif.presentationapi.v2.models.Image;
import org.vitrivr.cineast.core.iiif.presentationapi.v2.models.Manifest;
import org.vitrivr.cineast.core.iiif.presentationapi.v2.models.Metadata;
import org.vitrivr.cineast.core.iiif.presentationapi.v2.models.Sequence;

/**
 * Tests the serialization of a {@link MetadataJson} object into a JSON string
 *
 * @author singaltanmay
 * @version 1.0
 * @created 27.06.21
 */
public class MetadataJsonTest {

  private final String DESCRIPTION = "bb699753-8d03-4366-8139-a68ed087f837";
  private final String ATTRIBUTION = "2de3ce77-6818-4ebd-af48-a7f86ee8b7d4";
  private final String METADATA_LABEL = "4e40d999-78f2-4c58-941d-146bc03238c1";
  private final String METADATA_VALUE = "3ff41a2f-0dac-472b-af62-b1e7e5970e04";
  private final String IMAGE_AT_ID = "e970bd92-4a67-495a-81cc-bad6f64ad6b1";
  private final String CANVAS_LABEL = "2465ff4f-a6d5-4e12-95ac-1ae0ef2b821a";
  private final long CANVAS_HEIGHT = 39857567;
  private final long CANVAS_WIDTH = 7868754;
  private Manifest manifest;

  @BeforeEach
  void setup() {
    manifest = mock(Manifest.class);
    when(manifest.getDescription()).thenReturn(DESCRIPTION);
    when(manifest.getAttribution()).thenReturn(ATTRIBUTION);
    final Metadata metadata = mock(Metadata.class);
    when(metadata.getLabel()).thenReturn(METADATA_LABEL);
    when(metadata.getValue()).thenReturn(METADATA_VALUE);
    when(manifest.getMetadata()).thenReturn(Collections.singletonList(metadata));
    final Image image = mock(Image.class);
    when(image.getAtId()).thenReturn(IMAGE_AT_ID);
    final Canvas canvas = mock(Canvas.class);
    when(canvas.getImages()).thenReturn(Collections.singletonList(image));
    when(canvas.getLabel()).thenReturn(CANVAS_LABEL);
    when(canvas.getHeight()).thenReturn(CANVAS_HEIGHT);
    when(canvas.getWidth()).thenReturn(CANVAS_WIDTH);
    final Sequence sequence = mock(Sequence.class);
    when(sequence.getCanvases()).thenReturn(Collections.singletonList(canvas));
    when(manifest.getSequences()).thenReturn(Collections.singletonList(sequence));
  }

  /**
   * Tests the serialization of a {@link MetadataJson} object into a JSON string
   */
  @DisplayName("toJsonStringTest(): Testing serialization of MetadataJson into JSON string")
  @Test
  void toJsonStringTest() throws JsonProcessingException {
    MetadataJson metadataJson = new MetadataJson(manifest);
    String jsonString = metadataJson.toJsonString();
    assertNotNull(jsonString);
    assertFalse(jsonString.isEmpty());
    System.out.println(jsonString);
    assertTrue(jsonString.contains("\"description\":\"" + DESCRIPTION + "\""));
    assertTrue(jsonString.contains("\"attribution\":\"" + ATTRIBUTION + "\""));
    assertTrue(jsonString.contains("\"metadata\":[{\"label\":\"" + METADATA_LABEL + "\",\"value\":\"" + METADATA_VALUE + "\"}]"));
    assertTrue(jsonString.contains("\"images\":[{\"label\":\"" + CANVAS_LABEL + "\",\"url\":\"" + IMAGE_AT_ID + "\",\"height\":" + CANVAS_HEIGHT + ",\"width\":" + CANVAS_WIDTH + "}]"));
  }

}
