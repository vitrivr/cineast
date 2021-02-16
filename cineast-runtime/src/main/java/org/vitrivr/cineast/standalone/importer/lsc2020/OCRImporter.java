package org.vitrivr.cineast.standalone.importer.lsc2020;

import com.opencsv.CSVReader;
import com.opencsv.exceptions.CsvException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.vitrivr.cineast.core.data.entities.SimpleFulltextFeatureDescriptor;
import org.vitrivr.cineast.core.data.providers.primitive.PrimitiveTypeProvider;
import org.vitrivr.cineast.core.importer.Importer;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * Headerless csv file. column 0 is filename, column 1 is OCR
 * <code>B00000890_21I6X0_20180514_082338E,patages,"tensor(9.1535e-05, device='cuda:0')"</code>
 */
public class OCRImporter implements Importer<Map<String, PrimitiveTypeProvider>> {

    private static final Logger LOGGER = LogManager.getLogger(OCRImporter.class);

    private final Path root;

    private Iterator<Map<String, PrimitiveTypeProvider>> iterator;

    public OCRImporter(Path root) {
        this.root = root;
        try {
            CSVReader reader = new CSVReader(Files.newBufferedReader(root.resolve("OCR_result.csv")));
            iterator = reader.readAll().stream().map(line -> {
                String segment = LSCUtilities.pathToSegmentId(line[0]);
                PrimitiveTypeProvider value = PrimitiveTypeProvider.fromObject(line[1]);
                HashMap<String, PrimitiveTypeProvider> map = new HashMap<>(2);
                map.put(SimpleFulltextFeatureDescriptor.FIELDNAMES[0], PrimitiveTypeProvider.fromObject(segment));
                map.put(SimpleFulltextFeatureDescriptor.FIELDNAMES[1], value);
                return (Map<String, PrimitiveTypeProvider>)map; // Apparently, without explicit casting the compiler is a sad panda
            }).iterator();
            LOGGER.info("Successfully read and parsed the import file");
        } catch (IOException | CsvException e) {
            LOGGER.fatal("Could not read importfile", e);
        }
    }

    @Override
    public Map<String, PrimitiveTypeProvider> readNext() {
        if(iterator != null && iterator.hasNext()){
            return iterator.next();
        }else{
            return null;
        }
    }

    @Override
    public Map<String, PrimitiveTypeProvider> convert(Map<String, PrimitiveTypeProvider> data) {
        return data;
    }
}
