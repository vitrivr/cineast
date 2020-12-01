package org.vitrivr.cineast.standalone.importer.lsc2020;

import com.opencsv.CSVReader;
import com.opencsv.exceptions.CsvException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.vitrivr.cineast.core.data.providers.primitive.PrimitiveTypeProvider;
import org.vitrivr.cineast.core.db.dao.reader.TagReader;
import org.vitrivr.cineast.core.importer.Importer;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

public class VisualConceptTagImporter implements Importer<String[]> {

    public static final Logger LOGGER = LogManager.getLogger();

    public static final String BBOX_IGNORE = "bbox";
    private List<String[]> content;
    private Set<String> uniqueTags;

    private static boolean ignoreColumn(String colName) {
        return colName.contains(BBOX_IGNORE) || colName.contains("score") || colName.equals(LSCUtilities.KEY_MINUTEID) || colName.contains("path");
    }

    private static String scoreForCategory(String cat) {
        return cat + "_score";
    }

    private static String scoreForConcept(String concept) {
        return concept.replace("class", "score");
    }

    private final Path root;

    private String[] headers;

    private final boolean tagReaderOnly;

    private Iterator<String> tagIterator;

    public VisualConceptTagImporter(Path root) {
        this(root, false);
    }

    public VisualConceptTagImporter(Path root, boolean tagReaderOnly) {
        this.root = root;
        this.tagReaderOnly = tagReaderOnly;
        LOGGER.info("LSC 2020 Visual Concept Importer "+(tagReaderOnly ? "Tag Lookup" : "Tag Import"));
        try {
            readFile();
            if(tagReaderOnly){
                readTags();
            }else{
                readFileTagsPerSegment();
            }
            LOGGER.info("Finished initalisation. Importing now...");
        } catch (CsvException | IOException e) {
            LOGGER.fatal("Error in reading file", e);
            LOGGER.throwing(new RuntimeException("Could not initialise importer due to exception in startup", e));
        }
    }

    private void readFile() throws IOException, CsvException {
        LOGGER.info("Reading concepts file...");
        long start = System.currentTimeMillis();
        Path file = root.resolve(LSCUtilities.CONCEPTS_FILE_NAME);
        CSVReader csvReader = new CSVReader(Files.newBufferedReader(file, StandardCharsets.UTF_8));
        content = csvReader.readAll();
        headers = content.remove(0);
        LOGGER.info("Finished reading in " + (System.currentTimeMillis() - start) + "ms");
    }

    private void readFileTagsPerSegment() {
        LOGGER.info("Parsing tags per segment");
        long start = System.currentTimeMillis();
        Map<String, List<String>> map = new HashMap<>();
        content.forEach(line -> {
            String imgPath = line[LSCUtilities.CONCEPTS_IMAGEPATH_COL];
            String id = LSCUtilities.pathToSegmentId(LSCUtilities.cleanImagePath(imgPath));
            for (int i = 3; i < line.length; i++) {
                if (ignoreContent(line[i])) {
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
        LOGGER.info("Finished parsing tags per segment in " + (System.currentTimeMillis() - start) + "ms");
    }

    private static boolean ignoreContent(String tag){
        return tag == null || tag.isEmpty() || tag.equals("NULL");
    }

    private void readTags() {
        LOGGER.info("Parsing tags...");
        long start = System.currentTimeMillis();
        uniqueTags = new HashSet<>();
        content.forEach(l -> {
            for(int i =3; i<l.length;i++){
                if(ignoreColumn(headers[i])){
                    continue;
                }else{
                    if (!ignoreContent(l[i])) {
                        uniqueTags.add(l[i]);
                    }
                }
            }
        });
        tagIterator = uniqueTags.iterator();
        LOGGER.info("Finished parsing tags in " + (System.currentTimeMillis() - start) + "ms");
    }

    private volatile Iterator<Map.Entry<String, List<String>>> mapIterator;
    private volatile String currentId;
    private volatile Iterator<String> currentIterator;


    private String[] readNextPerSegment() {
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

    private String[] readNextTag() {
        if (tagIterator.hasNext()) {
            String tag = tagIterator.next();
            return new String[]{tag, tag, ""}; // id, name, desc
        } else {
            return null;
        }
    }

    @Override
    public String[] readNext() {
        if (tagReaderOnly) {
            return readNextTag();
        } else {
            return readNextPerSegment();
        }
    }

    private Map<String, PrimitiveTypeProvider> convertPerSegment(String[] data) {
        Map<String, PrimitiveTypeProvider> map = new HashMap<>();
        map.put("id", PrimitiveTypeProvider.fromObject(data[0]));
        map.put("tagid", PrimitiveTypeProvider.fromObject(data[1]));
        map.put("score", PrimitiveTypeProvider.fromObject(1));
        return map;
    }

    private Map<String, PrimitiveTypeProvider> convertAsTag(String[] data) {
        Map<String, PrimitiveTypeProvider> map = new HashMap<>();
        map.put(TagReader.TAG_ID_COLUMNNAME, PrimitiveTypeProvider.fromObject(data[0]));
        map.put(TagReader.TAG_NAME_COLUMNNAME, PrimitiveTypeProvider.fromObject(data[1]));
        map.put(TagReader.TAG_DESCRIPTION_COLUMNNAME, PrimitiveTypeProvider.fromObject(data[2]));
        return map;
    }

    @Override
    public Map<String, PrimitiveTypeProvider> convert(String[] data) {
        if (tagReaderOnly) {
            return convertAsTag(data);
        } else {
            return convertPerSegment(data);
        }
    }
}
