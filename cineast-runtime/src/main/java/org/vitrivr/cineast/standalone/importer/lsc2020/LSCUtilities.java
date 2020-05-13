package org.vitrivr.cineast.standalone.importer.lsc2020;

import com.opencsv.CSVReader;
import com.opencsv.exceptions.CsvException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class LSCUtilities {


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
    private static final Logger LOGGER = LogManager.getLogger(LSCUtilities.class);
    private static LSCUtilities instance = null;
    private final Path root;
    private List<String[]> headerlessMetaContents;
    private HashMap<String, String> minuteIdPathMap = new HashMap<>();

    private LSCUtilities(final Path root) throws IOException, CsvException {
        this.root = root;
        this.initLookup(minuteIdPathMap, root);
        headerlessMetaContents = readFile(root);
    }

    public static LSCUtilities create(final Path root) throws IOException, CsvException {
        if (instance == null) {
            instance = new LSCUtilities(root);
        } else {
            LOGGER.warn("Instance already created with path {}. Returning this one", instance.root);
        }
        return instance;
    }

    public static LSCUtilities getInstance() {
        if (instance != null) {
            return instance;
        } else {
            throw new IllegalStateException("There's no instance, use LSCUtilities.create first");
        }
    }

    /**
     * Writes the list as individual lines in a textfile.
     * The file must already be created
     *
     * @param directory The directory the file resides in
     * @param file      The file to write into
     * @param lines     The lines to write
     * @throws IOException If something goes wrong. Possibly due to the file not being created beforehand
     */
    public static void writeLines(Path directory, String file, List<String> lines) throws IOException {
        final Path p = directory.resolve(file);
        Files.write(p, lines, StandardOpenOption.TRUNCATE_EXISTING);
    }

    /**
     * Converts an imagepath to a corresponding segmentId.
     * <br>
     * In the LSC2020 dataset, image paths (from file lsc2020-visual-concepts.csv)
     * do contain more than the actual (image) file, hence some conversion is required
     * <br>
     * Prepends <code>is_</code>, removes anything before a slash ("<code>/</code>"), if present, and after a dot ("<code>.</code>") (i.e., file extension), if present
     *
     * @param path
     * @return
     */
    public static String pathToSegmentId(String path) {
        final int beginIdx = path.contains("/") ? path.lastIndexOf("/") + 1 : 0;
        final int endIdx = path.contains(".") ? path.lastIndexOf(".") : path.length();
        final String prefix = path.startsWith("is_") ? "" : "is_";
        return prefix + path.substring(beginIdx, endIdx);
    }

    public static String cleanImagePath(String path) {
        return path.substring("DATASETS/LSC2020/".length() + 1);
    }

    private void initLookup(HashMap<String, String> minuteIdPathMap, Path root) throws IOException {
        long start = System.nanoTime();
        Path file = root.resolve(CONCEPTS_FILE_NAME);
        LOGGER.info("Initialising Lookup from {}", file);
        List<String> contents = Files.readAllLines(file, StandardCharsets.UTF_8);
        contents.stream().forEach(s -> {
            if (!s.startsWith(KEY_MINUTEID)) {
                // Catch the header line, which is not important
                String[] items = s.split(","); // Csv
                String minuteId = items[CONCEPTS_MINUTEID_COL];
                String imagePath = cleanImagePath(items[CONCEPTS_IMAGEPATH_COL]);
                LOGGER.trace("Adding to lookup: {} <-> {}", minuteId, imagePath);
                minuteIdPathMap.put(minuteId, imagePath);
            }
        });
        long time = (System.nanoTime() - start) / 1000000L;
        LOGGER.info("Successfully initialised the lookup with {} entries in {}ms", minuteIdPathMap.size(), time);
    }

    private List<String[]> readFile(Path root) throws IOException, CsvException {
        Path file = root.resolve(METADATA_FILE_NAME);

        CSVReader csv = new CSVReader(Files.newBufferedReader(file, StandardCharsets.UTF_8));
        List<String[]> metadataFileContents = csv.readAll();
        metadataFileContents.remove(0); // remove header
        return metadataFileContents;
    }

    /**
     * Immutable
     * @return
     */
    public List<String[]> getHeaderlessMetaContents(){
        return Collections.unmodifiableList(headerlessMetaContents);
    }

    /**
     * Immutable
     * @return
     */
    public Map<String, String> getMinuteIdPathMap(){
        return Collections.unmodifiableMap(minuteIdPathMap);
    }
}
