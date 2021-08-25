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

public class BooleanData implements Importer<Map<String, PrimitiveTypeProvider>> {

    private static final Logger LOGGER = LogManager.getLogger();
    private final ObjectMapper mapper;
    private final JsonParser parser;
    private String _id;
    private Iterator<Entry<String, JsonNode>> _meta;
    private Iterator<JsonNode> _metaArray;
    private String _key;
    private JsonNode node;
    private Boolean empty;

    public BooleanData(Path input) throws IOException {
        empty = false;
        mapper = new ObjectMapper();
        parser = mapper.getFactory().createParser(input.toFile());
        node =  mapper.readTree(parser);
//        if (parser.nextToken() != JsonToken.START_ARRAY) {
//            throw new IOException("Invalid format");
//        }
//        if (parser.nextToken() != JsonToken.START_OBJECT) {
//            throw new IOException("Invalid format");
//        }
        nextObjectMetadata();
    }

    private Optional<Iterator<Entry<String, JsonNode>>> nextObjectMetadata() throws IOException {
//        if (parser.nextToken() != JsonToken.START_OBJECT) {
//            LOGGER.info("File done");
//            return Optional.empty();
//        }
//        ObjectNode node = mapper.readTree(parser);
        if (node == null) {
            LOGGER.info("File is done");
            return Optional.empty();
        }
        _meta = node.fields();
        if (_meta == null) {
            throw new IOException("Empty file");
        }
        _id = node.get("id").textValue();
        Entry<String, JsonNode> next = _meta.next();
        return Optional.of(_meta);
    }


    private synchronized Optional<Map<String, PrimitiveTypeProvider>> nextPair() throws IOException {

        if (empty) {
            return Optional.empty();
        }
//        while (_meta == null || !_meta.hasNext()) {
//            //isempty() only after java 11
//            if (!nextObjectMetadata().isPresent()) {
//                return Optional.empty();
//            }
//        }
//        if (_metaArray != null && _metaArray.hasNext()) {
//            //return active meta array element
//        }
        System.out.println("iterated");
        String x = node.get("id").textValue();

        final HashMap<String, PrimitiveTypeProvider> map = new HashMap<>(2);
        map.put("id", PrimitiveTypeProvider.fromObject(node.get("id").textValue()));
        map.put("domain", PrimitiveTypeProvider.fromObject(node.get("domain").asText()));
        map.put("key", PrimitiveTypeProvider.fromObject(node.get("key").asText()));
        map.put("value", PrimitiveTypeProvider.fromObject(node.get("value").asInt()));
        empty = true;
        return Optional.of(map);

    }

    /**
     * @return Pair mapping a segmentID to a List of Descriptions
     */
    @Override
    public Map<String, PrimitiveTypeProvider> readNext() {
        try {
            Optional<Map<String, PrimitiveTypeProvider>> noder = nextPair();
            //isEmpty() only since java 11
            if (!noder.isPresent()) {
                return null;
            }
            return noder.get();
        } catch (NoSuchElementException | IOException e) {
            return null;
        }
    }











    @Override
    public Map<String, PrimitiveTypeProvider> convert(Map<String, PrimitiveTypeProvider> data) {
        return data;
    }
}
