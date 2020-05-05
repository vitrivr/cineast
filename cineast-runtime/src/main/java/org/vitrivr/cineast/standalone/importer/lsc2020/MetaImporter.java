package org.vitrivr.cineast.standalone.importer.lsc2020;

import com.opencsv.CSVReader;
import com.opencsv.exceptions.CsvException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.vitrivr.cineast.core.data.entities.MediaSegmentMetadataDescriptor;
import org.vitrivr.cineast.core.data.providers.primitive.PrimitiveTypeProvider;
import org.vitrivr.cineast.core.importer.Importer;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.*;

/**
 * Importer for the LSC 2020 metadata, delivered with the dataset.
 */
public class MetaImporter implements Importer<Map<String, PrimitiveTypeProvider>> {

    private static final Logger LOGGER = LogManager.getLogger(MetaImporter.class);

    public static final String METADATA_FILE_NAME = "lsc2020-metadata.csv";
    public static final String CONCEPTS_FILE_NAME = "lsc2020-visual-concepts.csv";

    public static final String META_NO_PATH_FILE = "meta-no-path.txt";
    public static final String WRITTEN_FILE = "written.txt";

    public static final String KEY_MINUTEID = "minute_id";
    public static final int CONCEPTS_MINUTEID_COL = 0;
    public static final int CONCEPTS_IMAGEPATH_COL = 2;

    //minute_id,utc_time,local_time,timezone,lat,lon,semantic_name,elevation,speed,heart,calories,activity_type,steps
    /*
    minute_id,
    utc_time,
    local_time,
    timezone,
    lat,
    lon,
    semantic_name,
    elevation,
    speed,
    heart,
    calories,
    activity_type,
    steps
     */
    public static final int META_MIN_COL = 0;
    public static final String META_MIN_NAME = "minute_id";
    public static final int META_UTC_COL = 1;
    public static final String META_UTC_NAME = "utc_time";
    public static final int META_LOCAL_COL = 2;
    public static final String META_LOCAL_NAME = "local_time";
    public static final int META_TIMEZONE_COL = 3;
    public static final String META_TIMEZONE_NAME = "timezone";
    public static final int META_LAT_COL = 4;
    public static final String META_LAT_NAME = "lat";
    public static final int META_LON_COL = 5;
    public static final String META_LON_NAME = "lon";
    public static final int META_SEMANTIC_COL = 6;
    public static final String META_SEMANTIC_NAME = "semantic_name";
    public static final int META_ELEVATION_COL = 7;
    public static final String META_ELEVATION_NAME = "elevation";
    public static final int META_SPEED_COL = 8;
    public static final String META_SPEED_NAME = "speed";
    public static final int META_HEART_COL = 9;
    public static final String META_HEART_NAME = "heart";
    public static final int META_CALORIES_COL = 10;
    public static final String META_CALORIES_NAME = "calories";
    public static final int META_ACTIVITY_COL = 11;
    public static final String META_ACTIVITY_NAME = "activity_type";
    public static final int META_STEPS_COL = 12;
    public static final String META_STEPS_NAME = "steps";

    //minute_id,utc_time,local_time,timezone,lat,lon,semantic_name,elevation,speed,heart,calories,activity_type,steps
    public static final String[] META_NAMES = new String[]{META_MIN_NAME, META_UTC_NAME, META_LOCAL_NAME, META_TIMEZONE_NAME, META_LAT_NAME, META_LON_NAME, META_SEMANTIC_NAME, META_ELEVATION_NAME, META_SPEED_NAME, META_HEART_NAME, META_CALORIES_NAME, META_ACTIVITY_NAME, META_STEPS_NAME
    };

    public static final String DEFAULT_DOMAIN = "LSCMETA";
    public static final String LOCATION_DOMAIN = "LOCATION";
    public static final String DATETIME_DOMAIN = "TIME";

    private HashMap<String, String> minuteIdPathMap = new HashMap<>();

    private List<String> metaNoPath = new ArrayList<>();
    private List<String> written = new ArrayList<>();

    private Path root;

    private Iterator<Map<String, PrimitiveTypeProvider>> currentDataIterator = null;
    private Iterator<String[]> iterator;

    private Optional<List<Map<String, PrimitiveTypeProvider>>> parseLine(String[] items) {
        if (items.length != META_NAMES.length) {
            LOGGER.error("THe line's number of entries is illegal. Expected={}, Found={}. Line={}", META_NAMES.length, items.length, items);
            throw new RuntimeException("Too few metadata entries.");
        }
        String minuteId = items[0];
        String path = minuteIdPathMap.get(minuteId);
        if (path == null) {
            metaNoPath.add(minuteId);
            return Optional.empty();
        }
        List<Map<String, PrimitiveTypeProvider>> list = new ArrayList<>();
        for (int i = 0; i < META_NAMES.length; i++) {
            if(items[i].equalsIgnoreCase("null")){
                continue;
            }
            list.add(parseMeta(path, items, i));
        }
        written.add(minuteId);
        return Optional.of(list);
    }

    private HashMap<String, PrimitiveTypeProvider> parseMeta(String path, String[] items, int index) {
        return parseMeta(path, items, index, DEFAULT_DOMAIN);
    }

    private HashMap<String, PrimitiveTypeProvider> parseMeta(String path, String[] items, int index, String domain) {
        final HashMap<String, PrimitiveTypeProvider> map = new HashMap<>();
        // "id"
        map.put(MediaSegmentMetadataDescriptor.FIELDNAMES[0], PrimitiveTypeProvider.fromObject(LSCUtilities.pathToSegmentId(path)));
        // domain
        map.put(MediaSegmentMetadataDescriptor.FIELDNAMES[1], PrimitiveTypeProvider.fromObject(domain));
        // key
        map.put(MediaSegmentMetadataDescriptor.FIELDNAMES[2], PrimitiveTypeProvider.fromObject(META_NAMES[index]));
        // value
        map.put(MediaSegmentMetadataDescriptor.FIELDNAMES[3], PrimitiveTypeProvider.fromObject(items[index]));
        return map;
    }

    public MetaImporter(Path path) {
        this.root = path;
        try {
            initLookup();
        } catch (IOException e) {
            LOGGER.error("Failed to initialise the lookup due to {}", e, e);
            throw new RuntimeException("Failed to init importer. Init lookup failed.", e);
        }
        try {
            prepareFile();
        } catch (IOException | CsvException e) {
            LOGGER.error("Failed to prepare metadata readout due to {}", e, e);
            throw new RuntimeException("Failed to prepare metadata readout", e);
        }
        try {
            Files.createFile(root.resolve(META_NO_PATH_FILE));
            Files.createFile(root.resolve(WRITTEN_FILE));
        }catch(IOException e){
            LOGGER.error("Could not open important housekeeping files", e);
        }
        LOGGER.info("Finished setup of Importer. Importing now...");
    }

    private void initLookup() throws IOException {
        long start = System.nanoTime();
        Path file = root.resolve(CONCEPTS_FILE_NAME);
        LOGGER.info("Initialising Lookup from {}", file);
        List<String> contents = Files.readAllLines(file, StandardCharsets.UTF_8);
        contents.stream().forEach(s -> {
            if (!s.startsWith(KEY_MINUTEID)) {
                // Catch the header line, which is not important
                String[] items = s.split(","); // Csv
                String minuteId = items[CONCEPTS_MINUTEID_COL];
                String imagePath = LSCUtilities.cleanImagePath(items[CONCEPTS_IMAGEPATH_COL]);
                LOGGER.trace("Adding to lookup: {} <-> {}", minuteId, imagePath);
                minuteIdPathMap.put(minuteId, imagePath);
            }
        });
        long time = (System.nanoTime() - start) / 1000000L;
        LOGGER.info("Successfully initialised the lookup with {} entries in {}ms", minuteIdPathMap.size(),time);
    }

    private void prepareFile() throws IOException, CsvException {
        Path file = root.resolve(METADATA_FILE_NAME);

        CSVReader csv = new CSVReader(Files.newBufferedReader(file, StandardCharsets.UTF_8));
        List<String[]> metadataFileContents = csv.readAll();
        metadataFileContents.remove(0); // remove header
        iterator = metadataFileContents.iterator();
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
            writeLines(META_NO_PATH_FILE, metaNoPath);
            writeLines(WRITTEN_FILE,written);
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
