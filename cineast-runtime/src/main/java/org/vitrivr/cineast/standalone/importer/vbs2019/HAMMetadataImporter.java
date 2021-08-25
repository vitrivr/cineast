package org.vitrivr.cineast.standalone.importer.vbs2019;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.vitrivr.cineast.core.data.providers.primitive.PrimitiveTypeProvider;
import org.vitrivr.cineast.core.importer.Importer;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Path;
import java.util.*;

public class HAMMetadataImporter implements Importer<Map<String, PrimitiveTypeProvider>> {

    private static final Logger LOGGER = LogManager.getLogger();
    private String _id;
    private Iterator<Map.Entry<String, JsonNode>> _meta;
    private Iterator<JsonNode> _metaArray;
    private String _key;
    private JsonNode node;
    private Boolean empty;
    private Path root;
    private int currentRow;
    private BufferedReader csvReader;
    private int count = 0;

    public HAMMetadataImporter(Path root) throws IOException {
        empty = false;
        this.root = root;
        currentRow = 0;
        csvReader = new BufferedReader(new FileReader(root.toString()));
//        parser = mapper.getFactory().createParser(input.toFile());
//        node =  mapper.readTree(parser);
//        if (parser.nextToken() != JsonToken.START_ARRAY) {
//            throw new IOException("Invalid format");
//        }
//        if (parser.nextToken() != JsonToken.START_OBJECT) {
//            throw new IOException("Invalid format");
//        }
        //nextObjectMetadata(root);
    }

    private Optional<Iterator<Map.Entry<String, JsonNode>>> nextObjectMetadata(Path root) throws IOException {
//        BufferedReader csvReader = new BufferedReader(new FileReader(root.toString()));
//        String row;
//        while ((row = csvReader.readLine()) != null) {
//            String[] data = row.split(",");
//            // do something with the data
//        }
//        csvReader.close();
        return Optional.empty();
    }


    private synchronized Optional<Map<String, PrimitiveTypeProvider>> nextPair(String[] data) throws IOException {

        if (empty) {
            return Optional.empty();
        }
        final HashMap<String, PrimitiveTypeProvider> map = new HashMap<>(2);
            map.put("lesion_id", PrimitiveTypeProvider.fromObject(data[0]));
            map.put("id", PrimitiveTypeProvider.fromObject("i_" + data[1] + "_1"));
            map.put("dx", PrimitiveTypeProvider.fromObject(data[2]));
            map.put("dx_type", PrimitiveTypeProvider.fromObject(data[3]));
            map.put("age", PrimitiveTypeProvider.fromObject(data[4]));
            map.put("sex", PrimitiveTypeProvider.fromObject(data[5]));
            map.put("localization", PrimitiveTypeProvider.fromObject(data[6]));
            map.put("dataset", PrimitiveTypeProvider.fromObject(data[7]));
//        System.out.println("iterated");
//        String x = node.get("id").textValue();
//
//        map.put("id", PrimitiveTypeProvider.fromObject(node.get("id").textValue()));
//        map.put("domain", PrimitiveTypeProvider.fromObject(node.get("domain").asText()));
//        map.put("key", PrimitiveTypeProvider.fromObject(node.get("key").asText()));
//        map.put("value", PrimitiveTypeProvider.fromObject(node.get("value").asInt()));
        return Optional.of(map);

    }

    /**
     * @return Pair mapping a segmentID to a List of Descriptions
     */
    @Override
    public Map<String, PrimitiveTypeProvider> readNext() {
        try {
            String row = csvReader.readLine();
            Optional<Map<String, PrimitiveTypeProvider>> noder = Optional.empty();
            if (row != null) {
                String[] data = row.split(",");
                noder = nextPair(data);
                count = count + 1;
            } else {
                System.out.println(count);
                return null;
            }
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

