package org.vitrivr.cineast.standalone.importer.lsc2020;

import com.opencsv.exceptions.CsvException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.vitrivr.cineast.core.data.entities.MediaSegmentMetadataDescriptor;
import org.vitrivr.cineast.core.data.providers.primitive.PrimitiveTypeProvider;
import org.vitrivr.cineast.core.importer.Importer;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.*;

/**
 * Importer for the LSC 2020 metadata, delivered with the dataset.
 */
public class MetaImporter implements Importer<Map<String, PrimitiveTypeProvider>> {

    private static final Logger LOGGER = LogManager.getLogger(MetaImporter.class);

    private Map<String, String> minuteIdPathMap = new HashMap<>();

    private List<String> metaNoPath = new ArrayList<>();
    private List<String> written = new ArrayList<>();

    private Path root;

    private Iterator<Map<String, PrimitiveTypeProvider>> currentDataIterator = null;
    private Iterator<String[]> iterator;

    private Optional<List<Map<String, PrimitiveTypeProvider>>> parseLine(String[] items) {
        if (items.length != LSCUtilities.META_NAMES.length) {
            LOGGER.error("THe line's number of entries is illegal. Expected={}, Found={}. Line={}", LSCUtilities.META_NAMES.length, items.length, items);
            throw new RuntimeException("Too few metadata entries.");
        }
        String minuteId = items[0];
        String path = minuteIdPathMap.get(minuteId);
        if (path == null) {
            metaNoPath.add(minuteId);
            return Optional.empty();
        }
        List<Map<String, PrimitiveTypeProvider>> list = new ArrayList<>();
        for (int i = 0; i < LSCUtilities.META_NAMES.length; i++) {
            if(items[i].equalsIgnoreCase("null")){
                continue;
            }
            list.add(parseMeta(path, items, i));
        }
        written.add(minuteId);
        return Optional.of(list);
    }

    private HashMap<String, PrimitiveTypeProvider> parseMeta(String path, String[] items, int index) {
        return parseMeta(path, items, index, LSCUtilities.DEFAULT_DOMAIN);
    }

    private HashMap<String, PrimitiveTypeProvider> parseMeta(String path, String[] items, int index, String domain) {
        final HashMap<String, PrimitiveTypeProvider> map = new HashMap<>();
        // "id"
        map.put(MediaSegmentMetadataDescriptor.FIELDNAMES[0], PrimitiveTypeProvider.fromObject(LSCUtilities.pathToSegmentId(path)));
        // domain
        map.put(MediaSegmentMetadataDescriptor.FIELDNAMES[1], PrimitiveTypeProvider.fromObject(domain));
        // key
        map.put(MediaSegmentMetadataDescriptor.FIELDNAMES[2], PrimitiveTypeProvider.fromObject(LSCUtilities.META_NAMES[index]));
        // value
        map.put(MediaSegmentMetadataDescriptor.FIELDNAMES[3], PrimitiveTypeProvider.fromObject(items[index]));
        return map;
    }

    public MetaImporter(Path path) {
        this.root = path;
        try {
            LSCUtilities lsc = LSCUtilities.create(path);
            minuteIdPathMap = lsc.getMinuteIdPathMap();
            iterator = lsc.getHeaderlessMetaContents().iterator();
        } catch (IOException | CsvException e) {
            LOGGER.error("Failed to prepare metadata readout due to {}", e, e);
            throw new RuntimeException("Failed to prepare metadata readout", e);
        }
        try {
            Files.createFile(root.resolve(LSCUtilities.META_NO_PATH_FILE));
            Files.createFile(root.resolve(LSCUtilities.WRITTEN_FILE));
        }catch(IOException e){
            LOGGER.error("Could not open important housekeeping files", e);
        }
        LOGGER.info("Finished setup of Importer. Importing now...");
    }

    @Override
    public Map<String, PrimitiveTypeProvider> readNext() {
        // Has to read new line
        do {
            if (this.currentDataIterator == null && this.iterator.hasNext()) {
                LOGGER.trace("Init / Next: dataIt==null && it.hasNext");
                Optional<List<Map<String, PrimitiveTypeProvider>>> parsed = parseLine(this.iterator.next());
                parsed.ifPresent(maps -> this.currentDataIterator = maps.iterator());
            }
            if (this.currentDataIterator != null && this.currentDataIterator.hasNext()) {
                LOGGER.trace("dataNext: dataIt.hasNext");
                Map<String, PrimitiveTypeProvider> out = this.currentDataIterator.next();
                if (!currentDataIterator.hasNext()) {
                    // reset, so Init / Next occurs
                    currentDataIterator = null;
                }
                LOGGER.trace("Returning metadata: {}", out);
                writeLogs();
                return out;
            }
        } while (this.currentDataIterator == null && this.iterator.hasNext());
        LOGGER.info("No more to read. Stopping");
        writeLogs();
        return null;
    }

    private void writeLogs() {
        try {
            writeLines(LSCUtilities.META_NO_PATH_FILE, metaNoPath);
            writeLines(LSCUtilities.WRITTEN_FILE,written);
        } catch (IOException e) {
            LOGGER.error("Could not write crucial housekeeping info. Continuing", e);
        }
    }

    private void writeLines(String file, List<String> lines) throws IOException {
        Path p = this.root.resolve(file);
        Files.write(p, lines, StandardOpenOption.TRUNCATE_EXISTING);
    }

    @Override
    public Map<String, PrimitiveTypeProvider> convert(Map<String, PrimitiveTypeProvider> data) {
        return data; // See ObjectMetadataImporter -- not sure why
    }
}
