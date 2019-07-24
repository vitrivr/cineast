package org.vitrivr.cineast.core.util.json;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Path;

import javax.annotation.Nullable;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.vitrivr.cineast.core.util.LogHelper;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Provides JSON deserialization capabilities by means of Jackson Databind library.
 *
 * @author rgasser
 * @version 1.0
 * @created 13.01.17
 */
public class JacksonJsonProvider implements JsonReader, JsonWriter {

  /**
   * Jackson ObjectMapper instance used to map to/from objects. As this class is thread safe it can
   * be shared and therefore be static!
   */
  private static final ObjectMapper MAPPER = new ObjectMapper();

  /** Logger used to log errors. */
  private static final Logger LOGGER = LogManager.getLogger();

  /*
   * Can be used to change the behaviour the ObjectMapper.
   */
  static {
    MAPPER.configure(DeserializationFeature.ACCEPT_SINGLE_VALUE_AS_ARRAY, true);
    MAPPER.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    MAPPER.configure(DeserializationFeature.READ_ENUMS_USING_TO_STRING, true);

    MAPPER.configure(JsonParser.Feature.ALLOW_UNQUOTED_FIELD_NAMES, true);
    MAPPER.configure(JsonParser.Feature.ALLOW_COMMENTS, true);
    MAPPER.configure(JsonParser.Feature.ALLOW_SINGLE_QUOTES, true);
    MAPPER.configure(JsonParser.Feature.ALLOW_NUMERIC_LEADING_ZEROS, true);
  }

  @Override
  @Nullable
  public <T> T toObject(String jsonString, Class<T> c) {
    try {
      return MAPPER.readValue(jsonString, c);
    } catch (IOException e) {
      logIOExceptionOfRead(e, jsonString);
      return null;
    }
  }

  @Override
  @Nullable
  public <T> T toObject(File json, Class<T> c) {
    try {
      return MAPPER.readValue(json, c);
    } catch (IOException e) {
      logIOExceptionOfRead(e, json);
      return null;
    }
  }

  @Nullable
  public JsonNode toJsonNode(String jsonString) {
    try {
      return MAPPER.readTree(jsonString);
    } catch (IOException e) {
      logIOExceptionOfRead(e, jsonString);
      return null;
    }
  }

  @Nullable
  public JsonNode toJsonNode(Path json) {
    try {
      return MAPPER.readTree(json.toFile());
    } catch (IOException e) {
      logIOExceptionOfRead(e, json.toFile());
      return null;
    }
  }

  private static void logIOExceptionOfRead(IOException e, String jsonString) {
    logIOExceptionOfReadWithMessage(e, "string '" + jsonString + "'");
  }

  private static void logIOExceptionOfRead(IOException e, File jsonFile) {
    logIOExceptionOfReadWithMessage(e, "file '" + jsonFile + "'");
  }

  private static void logIOExceptionOfReadWithMessage(IOException e, String jsonTypeMessage) {
    if (e instanceof JsonParseException) {
      LOGGER.error("Could not parse JSON {}: {}", jsonTypeMessage, LogHelper.getStackTrace(e));
    } else if (e instanceof JsonMappingException) {
      LOGGER.error("Could not map JSON {} to POJO. Please check your object definitions.\n{}",
          jsonTypeMessage, LogHelper.getStackTrace(e));
    } else if (e instanceof FileNotFoundException) {
      LOGGER.warn("Could not find JSON {}", jsonTypeMessage);
    } else {
      LOGGER.error("Could not read JSON {}: {}", jsonTypeMessage, LogHelper.getStackTrace(e));
    }
  }

  /**
   * Takes a Java Object (usually a POJO) and tries to serialize it into a JSON. If serialization
   * fails for some reason, this method should return JSON_EMPTY;
   */
  @Override
  public String toJson(Object object) {
    try {
      return MAPPER.writeValueAsString(object);
    } catch (JsonProcessingException e) {
      LOGGER.log(Level.ERROR,
          "Could serialize provided object. Please check your object definitions.", e);
      return JSON_EMPTY;
    }
  }
}
