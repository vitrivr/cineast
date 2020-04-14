package org.vitrivr.cineast.standalone.importer.lsc2020;

import com.opencsv.CSVReader;
import com.opencsv.CSVReaderHeaderAware;
import com.opencsv.exceptions.CsvException;
import com.opencsv.exceptions.CsvValidationException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.ddogleg.struct.Tuple2;
import org.vitrivr.cineast.core.data.providers.primitive.PrimitiveTypeProvider;
import org.vitrivr.cineast.core.importer.Importer;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

public class VisualConceptTagImporter implements Importer<String[]> {

    public static final Logger LOGGER = LogManager.getLogger();

    public static final String BBOX_IGNORE = "bbox";

    private static boolean ignoreColumn(String colName) {
        return colName.contains(BBOX_IGNORE) || colName.contains("score") || colName.equals(MetaImporter.KEY_MINUTEID) || colName.contains("path");
    }

    private static String scoreForCategory(String cat) {
        return cat + "_score";
    }

    private static String scoreForConcept(String concept) {
        return concept.replace("class", "score");
    }

    private final Path root;

    private String[] headers;


    public VisualConceptTagImporter(Path root){
        this.root = root;
        try {
            readFile();
        } catch (CsvException | IOException e) {
            LOGGER.fatal("Error in reading file", e);
            LOGGER.throwing(new RuntimeException("Could not initialise importer due to exception in startup",e));
        }
    }

    private void readFile() throws IOException, CsvException {
        long start = System.currentTimeMillis();
        Path file = root.resolve(MetaImporter.CONCEPTS_FILE_NAME);
        CSVReader csvReader = new CSVReader(Files.newBufferedReader(file, StandardCharsets.UTF_8));
        List<String[]> content = csvReader.readAll();
        headers = content.remove(0);
        Map<String, List<String>> map = new HashMap<>();
        content.forEach(line -> {
            String imgPath = line[MetaImporter.CONCEPTS_IMAGEPATH_COL];
            String id = LSCUtilities.pathToSegmentId(LSCUtilities.cleanImagePath(imgPath));
            for (int i = 0; i < line.length; i++) {
                if(line[i].equals("NULL")){
                    continue;
                }
                String colName = headers[i];
                if (!ignoreColumn(colName)) {
                    if (map.containsKey(id)) {
                        map.get(id).add(line[i]);
                    } else {
                        map.put(id, new ArrayList<>());
                    }
                }
            }
        });
        mapIterator = map.entrySet().iterator();
        LOGGER.info("Read to memory in " + (System.currentTimeMillis() - start) + "ms");
    }

    private volatile Iterator<Map.Entry<String, List<String>>> mapIterator;
    private volatile String currentId;
    private volatile Iterator<String> currentIterator;


    @Override
    public String[] readNext() {
        do {
            if (currentId == null && mapIterator.hasNext()) {
                Map.Entry<String, List<String>> entry = mapIterator.next();
                currentId = entry.getKey();
                currentIterator = entry.getValue().iterator();
            }
            if (currentIterator.hasNext()) {
                return new String[]{currentId, currentIterator.next()};
            } else {
                currentIterator = null;
                currentId = null;
            }
        } while (currentIterator == null && mapIterator.hasNext());
        return null;
    }

    @Override
    public Map<String, PrimitiveTypeProvider> convert(String[] data) {
        Map<String, PrimitiveTypeProvider> map = new HashMap<>();
        map.put("id", PrimitiveTypeProvider.fromObject(data[0]));
        map.put("tagid", PrimitiveTypeProvider.fromObject(data[1]));
        map.put("score", PrimitiveTypeProvider.fromObject(1));
        return map;
    }
}
