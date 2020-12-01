package org.vitrivr.cineast.standalone.importer.lsc2020;

import com.google.common.collect.Lists;
import com.opencsv.CSVReader;
import com.opencsv.exceptions.CsvException;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.SetUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.time.*;
import java.util.*;

public class LSCUtilities {

    public static final String LSC_REPORT_VALID_FILNE_NAME = "report-valid.txt";
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
    public static final Set<Integer> META_COLUMNS_IN_USE = new HashSet<>(Lists.newArrayList(META_MIN_COL, META_UTC_COL, META_LOCAL_COL, META_LAT_COL, META_LON_COL, META_TIMEZONE_COL, META_SEMANTIC_COL, META_ACTIVITY_COL));
    public static final String DEFAULT_DOMAIN = "LSCMETA";
    public static final String LOCATION_DOMAIN = "LOCATION";
    public static final String DATETIME_DOMAIN = "TIME";
    public static final String LSC_UTC_PREFIX = "UTC_";
    public static final String LSC_FORMAT = "yyyy-MM-dd_hh:mm";

    public static final String PROCESSED_META_UTC = "p_utc_standard";
    public static final String PROCESSED_META_LOCAL = "p_local_standard";
    public static final String PROCESSED_META_DATETIME = "p_datetime"; // based on filename, utc
    public static final String PROCESSED_META_DAY_OF_WEEK = "p_day_of_week";
    public static final String PROCESSED_META_PHASE_OF_DAY = "p_phase_of_day";
    public static final String PROCESSED_META_HOUR_OF_DAY = "p_hour";
    public static final String PROCESSED_META_MONTH = "p_month";
    public static final String PROCESSED_META_YEAR = "p_year";
    public static final String PROCESSED_META_DAY = "p_day";


    private static final Logger LOGGER = LogManager.getLogger(LSCUtilities.class);



    private static LSCUtilities instance = null;
    private final Path root;
    private List<String[]> headerlessMetaContents;

    @Deprecated private HashMap<String, String> minuteIdPathMap = new HashMap<>();

    private HashMap<String, String> filenameToMinuteIdMap = new HashMap<>();
    private HashMap<String, String[]> metaPerMinute = new HashMap<>();

    private LSCUtilities(final Path root) throws IOException, CsvException {
        this.root = root;
        // this.initLookup(minuteIdPathMap, root);
        LOGGER.info("Initialising lookup...");
        this.initMap(this.readListOfFiles(root));
        LOGGER.info("Initialisation done.");
        // headerlessMetaContents = readFile(root);
    }

    public static LSCUtilities create(final Path root) throws IOException, CsvException {
        if (instance == null) {
            instance = new LSCUtilities(root);
        } else if(!root.equals(instance.root)) {
            return instance;
        }else {
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
        if (path.startsWith("DATASETS/LSC2020/")) {
            return path.substring("DATASETS/LSC2020/".length() + 1);
        } else {
            return path;
        }
    }

    /**
     * Two potential naming formats:<br>
     * Simple Format: {@code YYYMMDD_HHMMSS_000.ext} and <br>
     * Extended Format: {@code prefix001_prefix_YYYMMDD_HHMMSS*.ext}
     * Extension is jpg
     * <p>
     * Example of Simple: {@code 20160914_192303_000.jpg}<br>
     * Example of Extended: {@code B00006542_21I6X0_20180505_140053E.JPG}
     *
     * </p>
     */
    public static Optional<String> filenameToMinuteId(String filename) {
        String[] parts = filename.split("_");
        switch (parts.length) {
            case 3: // simple
                return Optional.of(filename.substring(0, filename.lastIndexOf("_") - 2));
            case 4: // Extended
                String date = parts[2];
                String time = parts[3];
                return Optional.of(date + "_" + time.substring(0, 4));
            default:
                return Optional.empty();
        }
    }

    /**
     * Removes all but after the last "/"
     *
     * @param path
     * @return
     */
    public static String sanitizeFilename(String path) {
        int i = path.lastIndexOf("/");
        if (i > 0) {
            return path.substring(i + 1);
        } else {
            return path;
        }
    }

    public static String sanitizeFilename(Path path) {
        return sanitizeFilename(path.toString());
    }


    /**
     * Converts a lscUtcTime format {@code UTC_yyyy-MM-dd_hh:mm} datetime to a {@link LocalDateTime}
     * {@link LocalDateTime} is used, due to its definition of 'local time in ISO-8601 standard without time zone info'.
     *
     * @param lscUtcFormat The LSC UTC Timestamp in the format {@code UTC_yyyy-MM-dd_hh:mm}
     * @return
     */
    public static LocalDateTime convertUtc(String lscUtcFormat) {
        return LocalDateTime.ofEpochSecond(convert(lscUtcFormat, true, null), 0, ZoneOffset.UTC);
    }

    /**
     * Converts a lscUtcTime format {@code yyyy-MM-dd_hh:mm} datetime to a {@link ZonedDateTime}
     *
     * @param lscFormat The LSC UTC Timestamp in the format {@code yyyy-MM-dd_hh:mm}
     * @param zone      A zoneid in format {@code Area/Region}, see {@linkplain ZoneId}
     * @return
     */
    public static ZonedDateTime convertLocal(String lscFormat, String zone) {
        final long epochSec = convert(lscFormat, false, zone);
        return ZonedDateTime.ofInstant(Instant.ofEpochSecond(epochSec), ZoneId.of(zone)); // Zone is parseable, otherwise not reached here
    }

    public static String extractPhaseOfDay(ZonedDateTime dateTime) {
        final int hour = dateTime.getHour();
        String tag;
        if (hour > 7 && hour < 11) {
            tag = "MORNING";
        } else if (hour >= 11 && hour < 14) {
            tag = "NOON";
        } else if (hour >= 14 && hour < 17) {
            tag = "AFTERNOON";
        } else if (hour >= 17 && hour < 22) {
            tag = "EVENING";
        } else {
            tag = "NIGHT";
        }
        return tag;
    }

    public static int extractYear(LocalDateTime time) {
        return time.getYear();
    }

    public static int extractDay(LocalDateTime time){
        return time.getDayOfMonth();
    }

    public static String extractMonth(LocalDateTime time) {
        return time.getMonth().name();
    }

    public static int extractHour(LocalDateTime utc) {
        return utc.getHour();
    }

    public static String extractHourStr(LocalDateTime utc){
        return String.valueOf(extractHour(utc));
    }

    public static String extractYearStr(LocalDateTime utc){
        return String.valueOf(extractYear(utc));
    }

    public static String extractDayStr(LocalDateTime utc){
        return String.valueOf(extractHour(utc));
    }

    private static String getLscFormat(boolean utc) {
        return (utc ? LSC_UTC_PREFIX : "") + LSC_FORMAT;
    }

    private static long convert(String lscFormat, boolean utc, String zone) {
        if (StringUtils.isBlank(lscFormat) || "NULL".equalsIgnoreCase(lscFormat)) {
            throw new IllegalArgumentException("Cannot parse due to not in lscUtcFormat. Expected format: " + getLscFormat(utc) + ". Reason: BLANK. Given: " + lscFormat);
        }
        if (utc && !lscFormat.startsWith("UTC")) {
            throw new IllegalArgumentException("Cannot parse due to not in lscUtcFormat. Expected format: " + getLscFormat(utc) + ". Reason: START. Given: " + lscFormat);
        } else if (!utc && StringUtils.isBlank(zone)) {
            throw new IllegalArgumentException("Cannot parse due to not in lscUtcFormat. Expected format: " + getLscFormat(utc) + ". Reason: ZONEBLANK. Given: " + lscFormat);
        }
        ZoneId zoneId = null;
        try {
            if (!utc) {
                zoneId = ZoneId.of(zone);
            }
        } catch (DateTimeException e) {
            throw new IllegalArgumentException("Cannot parse due to not in lscUtcFormat. Expected format: " + getLscFormat(utc) + ". Reason: ZONE. Given: " + lscFormat, e);
        }
        final String[] parts = lscFormat.split("_"); // ["UTC", "yyyy-mm-dd", "hh:mm"]
        final int nbParts = utc ? 3 : 2;
        if (parts.length < nbParts) {
            throw new IllegalArgumentException("Cannot parse due to not in lscUtcFormat. Expected format: " + getLscFormat(utc) + ". Reason: DATE. Given: " + lscFormat);
        }
        final int dateIdx = utc ? 1 : 0;
        final String[] date = parts[dateIdx].split("-"); // ["yyyy", "mm", "dd"]
        if (date.length < 3) {
            throw new IllegalArgumentException("Cannot parse due to not in lscUtcFormat. Expected format: " + getLscFormat(utc) + ". Reason: TIME. Given: " + lscFormat);
        }
        final int timeIdx = utc ? 2 : 1;
        final String[] time = parts[timeIdx].split(":"); // ["hh", "mm"]
        if (time.length < 2) {
            throw new IllegalArgumentException("Cannot parse due to not in lscUtcFormat. Expected format: " + getLscFormat(utc) + ". Reason: TIME. Given: " + lscFormat);
        }
        try {
            final int year = Integer.parseInt(date[0]);
            final int month = Integer.parseInt(date[1]);
            final int day = Integer.parseInt(date[2]);
            final int hour = Integer.parseInt(time[0]);
            final int minute = Integer.parseInt(time[1]);
            if (utc) {
                return LocalDateTime.of(year, month, day, hour, minute).toEpochSecond(ZoneOffset.UTC);
            } else {
                return ZonedDateTime.of(year, month, day, hour, minute, 0, 0, zoneId).toEpochSecond();
            }
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Cannot parse due to not in lscUtcFormat. Expected format: " + getLscFormat(utc) + ". Reason: NUMBERFORMAT. Given: " + lscFormat, e);
        }
    }

    /**
     * Parses a minuteId {@code YYYYMMDD_HH:mm} to a {@link LocalDateTime}.
     * The result is to be treated as UTC timestamp
     */
    public static LocalDateTime fromMinuteId(String minuteId) {
        // YYYYMMDD_HHMM
        // 0123456789012
        try {
            final int year = Integer.parseInt(minuteId.substring(0, 4));
            final int month = Integer.parseInt(minuteId.substring(4, 6));
            final int day = Integer.parseInt(minuteId.substring(6, 8));
            final int hour = Integer.parseInt(minuteId.substring(9, 11));
            final int minute = Integer.parseInt(minuteId.substring(11));
            return LocalDateTime.of(year, month, day, hour, minute);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Cannot parse due to invalid number format in minuteId");
        }
    }


    private void readMetadata() throws IOException, CsvException {
        LOGGER.info("Reading metadata file...");
        headerlessMetaContents = readFile(root);
        LOGGER.info("Read metadata file");
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

    private List<String[]> readListOfFiles(Path root) throws IOException, CsvException {
        Path file = root.resolve(LSC_REPORT_VALID_FILNE_NAME);
        CSVReader csv = new CSVReader(Files.newBufferedReader(file, StandardCharsets.UTF_8));
        List<String[]> lookupContents = csv.readAll();
        lookupContents.remove(0); // Remove header
        return lookupContents;
    }

    private void initMap(List<String[]> listOfFiles){
        listOfFiles.stream().forEach(s ->{
            final String sanitizeFilename = sanitizeFilename(s[0]);
            final Optional<String> minuteId = filenameToMinuteId(sanitizeFilename);
            minuteId.ifPresent(value -> filenameToMinuteIdMap.put(sanitizeFilename, value));
        });
    }

    public void initMetadata() throws IOException, CsvException {
        LOGGER.info("Initialising metadata lookup...");
        readMetadata();
        initMetaPerMinuteId();
        LOGGER.info("Initialised metadata lookup.");
    }

    private void initMetaPerMinuteId(){
        headerlessMetaContents.stream().forEach(items ->{
            metaPerMinute.put(items[META_MIN_COL], items);
        });
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
     *
     * @return
     */
    @Deprecated
    public List<String[]> getHeaderlessMetaContents() {
        return Collections.unmodifiableList(headerlessMetaContents);
    }

    public Map<String, String[]> getMetaPerMinuteId(){
        return Collections.unmodifiableMap(metaPerMinute);
    }

    public Map<String,String> getFilenameToMinuteIdLookUp(){
        return Collections.unmodifiableMap(filenameToMinuteIdMap);
    }

    /**
     * Immutable
     *
     * @return
     */
    @Deprecated
    public Map<String, String> getMinuteIdPathMap() {
        return Collections.unmodifiableMap(minuteIdPathMap);
    }

}


