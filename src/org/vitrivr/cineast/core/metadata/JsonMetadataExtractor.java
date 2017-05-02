package org.vitrivr.cineast.core.metadata;

import com.fasterxml.jackson.core.type.TypeReference;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import org.vitrivr.cineast.core.data.entities.MultimediaMetadataDescriptor;
import org.vitrivr.cineast.core.util.json.JacksonJsonProvider;

public class JsonMetadataExtractor implements MetadataExtractor {
  private static final String DOMAIN = "JSON";
  private static final String JSON_EXTENSION = "json";

  private static final JacksonJsonProvider jacksonJsonReader = new JacksonJsonProvider();

  @Override
  public String domain() {
    return DOMAIN;
  }

  @Override
  public List<MultimediaMetadataDescriptor> extract(String objectId, Path path) {
    Optional<Map<String, Object>> jsonValues = extractJsonMetadata(path);
    return jsonValues
        .map(values -> this.createDescriptors(objectId, values))
        .orElse(Collections.emptyList());
  }

  public static Optional<Map<String, Object>> extractJsonMetadata(Path objectPath) {
    String fileName = objectPath.getFileName().toString();
    String fileNameWithoutExtension = com.google.common.io.Files.getNameWithoutExtension(fileName);
    Path metadataPath = objectPath.resolveSibling(fileNameWithoutExtension + '.' + JSON_EXTENSION);
    if (Files.notExists(metadataPath)) {
      return Optional.empty();
    } else {
      @SuppressWarnings("unchecked") // Cast is fine because JSON objects must have String as keys
      Map<String, Object> values = jacksonJsonReader.toObject(metadataPath.toFile(), Map.class);
      return Optional.ofNullable(values);
    }
  }

  private List<MultimediaMetadataDescriptor> createDescriptors(String objectId,
      Map<String, Object> jsonValues) {
    return jsonValues.entrySet().stream()
        .map(e -> MultimediaMetadataDescriptor
            .newMultimediaMetadataDescriptor(objectId, this.domain(), e.getKey(), e.getValue()))
        .collect(Collectors.toList());
  }
}
