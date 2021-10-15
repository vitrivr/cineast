package org.vitrivr.cineast.standalone.importer.lsc2020;

import com.opencsv.CSVReader;
import com.opencsv.exceptions.CsvException;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.vitrivr.cineast.core.data.entities.SimpleFulltextFeatureDescriptor;
import org.vitrivr.cineast.core.data.providers.primitive.PrimitiveTypeProvider;
import org.vitrivr.cineast.core.importer.Importer;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class CaptionImporter implements Importer<Map<String, PrimitiveTypeProvider>> {


    private static final Logger LOGGER = LogManager.getLogger(CaptionImporter.class);

    public static final String CAPTIONS_FILE_NAME = "lsc2020-captions.csv";

    public static final int ID_COLUMN = 1;
    public static final int CAPTION_COLUMN = 2;

    private Path root;

    public CaptionImporter(Path root) {
        this.root = root;
        try {
            prepareFile();
        } catch (IOException | CsvException e) {
            throw LOGGER.throwing(Level.ERROR, new RuntimeException("Could not preprae", e));
        }
    }

    private Iterator<String[]> iterator;

    private void prepareFile() throws IOException, CsvException {
        long start = System.currentTimeMillis();
        Path file = root.resolve(CAPTIONS_FILE_NAME);
        CSVReader csv = new CSVReader(Files.newBufferedReader(file, StandardCharsets.UTF_8));
        List<String[]> metadataFileContents = csv.readAll();
        metadataFileContents.remove(0); // Headers not required
        iterator = metadataFileContents.iterator();
        LOGGER.info("Preparation done in {}ms", (System.currentTimeMillis() - start));
    }

    private Map<String, PrimitiveTypeProvider> parseLine(String[] line) {
        final HashMap<String, PrimitiveTypeProvider> map = new HashMap<>(2);
        map.put(SimpleFulltextFeatureDescriptor.FIELDNAMES[0], PrimitiveTypeProvider.fromObject(LSCUtilities.pathToSegmentId(line[ID_COLUMN])));
        map.put(SimpleFulltextFeatureDescriptor.FIELDNAMES[1], PrimitiveTypeProvider.fromObject(line[CAPTION_COLUMN]));
        return map;
    }

    @Override
    public Map<String, PrimitiveTypeProvider> readNext() {
        if (iterator.hasNext()) {
            return parseLine(iterator.next());
        }
        return null;
    }

    @Override
    public Map<String, PrimitiveTypeProvider> convert(Map<String, PrimitiveTypeProvider> data) {
        return data;
    }
}
