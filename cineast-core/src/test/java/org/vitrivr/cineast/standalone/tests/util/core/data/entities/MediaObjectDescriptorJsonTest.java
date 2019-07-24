package org.vitrivr.cineast.standalone.tests.util.core.data.entities;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.vitrivr.cineast.core.data.MediaType;
import org.vitrivr.cineast.core.data.entities.MediaObjectDescriptor;

/**
 * @author silvan on 06.04.18.
 */
class MediaObjectDescriptorJsonTest {

  @Test
  @DisplayName("Read from Json")
  void deserialize() throws IOException {
    String json = FileUtils.readFileToString(new File("resources/tests/mmobj.json"), Charset.defaultCharset());
    ObjectMapper mapper = new ObjectMapper();
    MediaObjectDescriptor descriptor = mapper.readValue(json, MediaObjectDescriptor.class);
    assertEquals("1337", descriptor.getObjectId());
    assertEquals("testName", descriptor.getName());
    assertEquals("test/path", descriptor.getPath());
    assertEquals(MediaType.IMAGE, descriptor.getMediatype());
    assertEquals(MediaType.IMAGE.getId(), descriptor.getMediatypeId());
    assertEquals(false, descriptor.exists());
  }

}
