package org.vitrivr.cineast.core.util.json;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.vitrivr.cineast.core.util.LogHelper;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

/**
 * Provides JSON deserialization capabilities by means of Jackson Databind library.
 *
 * @author rgasser
 * @version 1.0
 * @created 13.01.17
 */
public class JacksonJsonProvider implements JsonReader, JsonWriter{

    /** Jackson ObjectMapper instance used to map to/from objects. As this class is thread safe
     * it can be shared and therefore be static!
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
    }

    @Override
    public <T> T toObject(String jsonString, Class<T> c) {
        try {
            return MAPPER.readValue(jsonString, c);
        } catch (JsonParseException e) {
            LOGGER.log(Level.ERROR, "Could not parse JSON.");
            return null;
        } catch (JsonMappingException e) {
            LOGGER.log(Level.ERROR, "Could not map JSON to POJO. Please check your object definitions.");
            return null;
        } catch (IOException e) {
            LOGGER.log(Level.ERROR, "Could not read JSON.", e);
            return null;
        }
    }

    @Override
    public <T> T toObject(File json, Class<T> c) {
        try {
            return MAPPER.readValue(json, c);
        }  catch (JsonParseException e) {
            LOGGER.log(Level.ERROR, "Could not parse JSON file under '{}'.", json.toString());
            return null;
        } catch (JsonMappingException e) {
            LOGGER.log(Level.ERROR, "Could not map JSON under '{}' to POJO. Please check your object definitions.", json.toString());
            return null;
        } catch (FileNotFoundException e) {
            LOGGER.log(Level.WARN, "Could not find file under '{}'.", json.toString());
            return null;
        } catch (IOException e) {
            LOGGER.log(Level.ERROR, "Could not read JSON file under '{}'.", json.toString(), LogHelper.getStackTrace(e));
            return null;
        }
    }

    /**
     * Takes a Java Object (usually a POJO) and tries to serialize it into a JSON. If serialization
     * fails for some reason, this method should return JSON_EMPTY;
     *
     * @param object
     * @return
     */
    @Override
    public String toJson(Object object) {
        try {
            return MAPPER.writeValueAsString(object);
        } catch (JsonProcessingException e) {
            LOGGER.log(Level.ERROR, "Could serialize provided object. Please check your object definitions.", e);
            return JSON_EMPTY;
        }
    }
}
