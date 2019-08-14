package org.vitrivr.cineast.standalone.importer.vbs2019;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import java.io.IOException;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.NoSuchElementException;
import java.util.Optional;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.vitrivr.cineast.core.data.entities.MediaObjectMetadataDescriptor;
import org.vitrivr.cineast.core.data.providers.primitive.DoubleTypeProvider;
import org.vitrivr.cineast.core.data.providers.primitive.LongTypeProvider;
import org.vitrivr.cineast.core.data.providers.primitive.PrimitiveTypeProvider;
import org.vitrivr.cineast.core.data.providers.primitive.StringTypeProvider;
import org.vitrivr.cineast.core.importer.Importer;

public class ObjectMetadataImporter implements Importer<Map<String, PrimitiveTypeProvider>> {

    private static final Logger LOGGER = LogManager.getLogger();
    private final ObjectMapper mapper;
    private final JsonParser parser;
    private String _id;
    private Iterator<Entry<String, JsonNode>> _meta;
    private Iterator<JsonNode> _metaArray;
    private String _key;

    public ObjectMetadataImporter(Path input) throws IOException {
        mapper = new ObjectMapper();
        parser = mapper.getFactory().createParser(input.toFile());
        if (parser.nextToken() != JsonToken.START_ARRAY) {
            throw new IOException("Invalid format");
        }
        if (parser.nextToken() != JsonToken.START_OBJECT) {
            throw new IOException("Invalid format");
        }
        nextObjectMetadata();
    }

    private Optional<Iterator<Entry<String, JsonNode>>> nextObjectMetadata() throws IOException {
        if (parser.nextToken() != JsonToken.START_OBJECT) {
            LOGGER.info("File done");
            return Optional.empty();
        }
        ObjectNode node = mapper.readTree(parser);
        if (node == null) {
            LOGGER.info("File is done");
            return Optional.empty();
        }
        _meta = node.fields();
        if (_meta == null) {
            throw new IOException("Empty file");
        }
        _id = "v_" + node.get("v3cId").textValue();
        return Optional.of(_meta);
    }

    private synchronized Optional<Map<String, PrimitiveTypeProvider>> nextPair() throws IOException {
        while (_meta == null || !_meta.hasNext()) {
            //isempty() only after java 11
            if (!nextObjectMetadata().isPresent()) {
                return Optional.empty();
            }
        }
        if (_metaArray != null && _metaArray.hasNext()) {
            //return active meta array element
        }
        Entry<String, JsonNode> next = _meta.next();
        JsonNode jsonVal = next.getValue();
        String key = next.getKey();
        PrimitiveTypeProvider primitiveVal = null;
        if (next.getValue().isArray()) {
            _metaArray = next.getValue().iterator();
            _key = next.getKey();
            if (_metaArray.hasNext()) {
                jsonVal = _metaArray.next();
            } else {
                return nextPair();
            }
        }
        if (jsonVal.isFloatingPointNumber()) {
            primitiveVal = new DoubleTypeProvider(jsonVal.asDouble());
        }
        if (jsonVal.isNumber()) {
            primitiveVal = new LongTypeProvider(jsonVal.asLong());
        }
        if (jsonVal.isTextual()) {
            primitiveVal = new StringTypeProvider(jsonVal.asText());
        }
        if (primitiveVal == null) {
            LOGGER.warn("Unknown type {}", jsonVal.getNodeType());
        }

        final HashMap<String, PrimitiveTypeProvider> map = new HashMap<>(2);
        map.put(MediaObjectMetadataDescriptor.FIELDNAMES[0], PrimitiveTypeProvider.fromObject(_id));
        map.put(MediaObjectMetadataDescriptor.FIELDNAMES[1], PrimitiveTypeProvider.fromObject("vimeo"));
        map.put(MediaObjectMetadataDescriptor.FIELDNAMES[2], PrimitiveTypeProvider.fromObject(key));
        map.put(MediaObjectMetadataDescriptor.FIELDNAMES[3], primitiveVal);
        return Optional.of(map);
    }

    /**
     * @return Pair mapping a segmentID to a List of Descriptions
     */
    @Override
    public Map<String, PrimitiveTypeProvider> readNext() {
        try {
            Optional<Map<String, PrimitiveTypeProvider>> node = nextPair();
            //isEmpty() only since java 11
            if (!node.isPresent()) {
                return null;
            }
            return node.get();
        } catch (NoSuchElementException | IOException e) {
            return null;
        }
    }

    @Override
    public Map<String, PrimitiveTypeProvider> convert(Map<String, PrimitiveTypeProvider> data) {
        return data;
    }
}
