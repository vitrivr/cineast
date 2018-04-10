package org.vitrivr.cineast.tests.util.core.data.entities;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.vitrivr.cineast.core.data.MediaType;
import org.vitrivr.cineast.core.data.messages.session.ExtractionContainerMessage;

/**
 * @author silvan on 06.04.18.
 */
class MetadataDescriptorJsonTest {


  @Test
  @DisplayName("Json read")
  void testJsonInput() throws IOException {
    String json = FileUtils
        .readFileToString(new File("resources/tests/items.json"), Charset.defaultCharset());
    ObjectMapper mapper = new ObjectMapper();
    ExtractionContainerMessage message = mapper.readValue(json, ExtractionContainerMessage.class);
    assertEquals("nameOne", message.getItems()[0].getObject().getName());
    assertEquals("/home/test/one.png", message.getItems()[0].getPathForExtraction().toAbsolutePath().toString());
    assertEquals("Web", message.getItems()[0].getMetadata()[0].getDomain());
    assertEquals("key", message.getItems()[0].getMetadata()[0].getKey());
    assertEquals("testval", message.getItems()[0].getMetadata()[0].getValue());
    assertEquals("nameTwo", message.getItems()[1].getObject().getName());
    assertEquals(MediaType.IMAGE, message.getItems()[1].getObject().getMediatype());
  }
}
