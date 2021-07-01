package org.vitrivr.cineast.standalone.importer.lsc2020;

import com.opencsv.exceptions.CsvException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.vitrivr.cineast.core.data.entities.MediaSegmentMetadataDescriptor;
import org.vitrivr.cineast.core.data.providers.primitive.PrimitiveTypeProvider;
import org.vitrivr.cineast.core.importer.Importer;

/**
 * Importer for the LSC 2020 metadata, delivered with the dataset.
 */
public class MetaImporter implements Importer<Map<String, PrimitiveTypeProvider>> {

  private static final Logger LOGGER = LogManager.getLogger(MetaImporter.class);
  private final Map<String, String[]> metadataMap;
  private final boolean logsEnabled = Boolean.parseBoolean(System.getProperty("org.vitrivr.cineast.standalone.importer.lsc2020.MetaImporter.logsEnabled", "false"));
  private final int logCadence = Integer.parseInt(System.getProperty("org.vitrivr.cineast.standalone.importer.lsc2020.MetaImporter.logCadence", "1000"));
  private Map<String, String> filenameToMinuteId;
  private List<String> metaNoPath = new ArrayList<>();
  private List<String> written = new ArrayList<>();
  private Path root;
  private Iterator<Map<String, PrimitiveTypeProvider>> currentDataIterator = null;
  private Iterator<Map.Entry<String, String>> iterator;
  private int nextsSinceLastDump = 0;

  public MetaImporter(Path path) {
    this.root = path;
    try {
      LSCUtilities lsc = LSCUtilities.create(path);
      lsc.initMetadata();
      filenameToMinuteId = lsc.getFilenameToMinuteIdLookUp();
      metadataMap = lsc.getMetaPerMinuteId();
      iterator = filenameToMinuteId.entrySet().iterator();
    } catch (IOException | CsvException e) {
      LOGGER.error("Failed to prepare metadata readout due to {}", e, e);
      throw new RuntimeException("Failed to prepare metadata readout", e);
    }


    createLogFiles();
    LOGGER.info("Finished setup of Importer. Importing now...");
  }

  static HashMap<String, PrimitiveTypeProvider> meta(String filename, String domain, String name,
      String value) {
    final HashMap<String, PrimitiveTypeProvider> map = new HashMap<>();
    // "id"
    map.put(MediaSegmentMetadataDescriptor.FIELDNAMES[0],
        PrimitiveTypeProvider.fromObject(LSCUtilities.pathToSegmentId(filename)));
    // domain
    map.put(MediaSegmentMetadataDescriptor.FIELDNAMES[1], PrimitiveTypeProvider.fromObject(domain));
    // key
    map.put(MediaSegmentMetadataDescriptor.FIELDNAMES[2], PrimitiveTypeProvider.fromObject(name));
    // value
    map.put(MediaSegmentMetadataDescriptor.FIELDNAMES[3], PrimitiveTypeProvider.fromObject(value));
    return map;
  }

  /**
   * Will create (and delete if pre-existing) log files. Only executed if logs are enabled
   */
  private void createLogFiles() {
    if (logsEnabled) {
      try {
        Path noPathFile = root.resolve(LSCUtilities.META_NO_PATH_FILE);
        Path processedFile = root.resolve(LSCUtilities.WRITTEN_FILE);
        Files.deleteIfExists(noPathFile);
        Files.deleteIfExists(processedFile);
        Files.createFile(processedFile);
      } catch (IOException e) {
        LOGGER.error("Could not open important housekeeping files", e);
      }
    }
  }

  private Optional<List<Map<String, PrimitiveTypeProvider>>> parseLine(String filename,
      String[] items) {
    if (items.length != LSCUtilities.META_NAMES.length) {
      LOGGER.error("The line's number of entries is illegal. Expected={}, Found={}. Line={}",
          LSCUtilities.META_NAMES.length, items.length, items);
      throw new RuntimeException("Too few metadata entries.");
    }
    String minuteId = LSCUtilities.filenameToMinuteId(filename).get();
    List<Map<String, PrimitiveTypeProvider>> list = new ArrayList<>();
//        for (int i = 0; i < LSCUtilities.META_NAMES.length; i++) {
//            if(items[i].equalsIgnoreCase("null")){
//                continue;
//            }
//            list.add(parseMeta(filename, items, i));
//        }
    for (int i = 0; i < LSCUtilities.META_NAMES.length; i++) {
      if (LSCUtilities.META_COLUMNS_IN_USE.contains(i)) {
        if (items[i].equalsIgnoreCase("null")) {
          continue;
        }
        list.add(parseMeta(filename, items, i));
      }
    }
    // Processed temporal metadata, based on filename
    final LocalDateTime dt = LSCUtilities.fromMinuteId(minuteId);
    list.add(meta(filename, LSCUtilities.DEFAULT_DOMAIN, LSCUtilities.PROCESSED_META_DATETIME,
        dt.toString()));
    list.add(meta(filename, LSCUtilities.DEFAULT_DOMAIN, LSCUtilities.PROCESSED_META_DAY_OF_WEEK,
        String.valueOf(dt.getDayOfWeek())));
    list.add(meta(filename, LSCUtilities.DEFAULT_DOMAIN, LSCUtilities.PROCESSED_META_MONTH,
        LSCUtilities.extractMonth(dt)));
    list.add(meta(filename, LSCUtilities.DEFAULT_DOMAIN, LSCUtilities.PROCESSED_META_DAY,
        LSCUtilities.extractDayStr(dt)));
    list.add(meta(filename, LSCUtilities.DEFAULT_DOMAIN, LSCUtilities.PROCESSED_META_YEAR,
        LSCUtilities.extractYearStr(dt)));
    list.add(meta(filename, LSCUtilities.DEFAULT_DOMAIN, LSCUtilities.PROCESSED_META_HOUR_OF_DAY,
        LSCUtilities.extractHourStr(dt)));
    if (StringUtils.isNotBlank(items[LSCUtilities.META_UTC_COL])
        && !items[LSCUtilities.META_UTC_COL].equalsIgnoreCase("null")) {
      list.add(meta(filename, LSCUtilities.DEFAULT_DOMAIN, LSCUtilities.PROCESSED_META_UTC,
          LSCUtilities.convertUtc(items[LSCUtilities.META_UTC_COL]).toString()));
    }
    // Processed temporal metadata, based on metadata file
    if ((StringUtils.isNotBlank(items[LSCUtilities.META_LOCAL_COL])
        && !items[LSCUtilities.META_LOCAL_COL].equalsIgnoreCase("null"))
        && (StringUtils.isNotBlank(items[LSCUtilities.META_TIMEZONE_COL])
        && !items[LSCUtilities.META_TIMEZONE_COL].equalsIgnoreCase("null"))) {
      final ZonedDateTime zdt = LSCUtilities
          .convertLocal(items[LSCUtilities.META_LOCAL_COL], items[LSCUtilities.META_TIMEZONE_COL]);
      list.add(meta(filename, LSCUtilities.DEFAULT_DOMAIN, LSCUtilities.PROCESSED_META_LOCAL,
          zdt.toString()));
      list.add(meta(filename, LSCUtilities.DEFAULT_DOMAIN, LSCUtilities.PROCESSED_META_PHASE_OF_DAY,
          LSCUtilities.extractPhaseOfDay(zdt)));
    }
    written.add(minuteId);
    return Optional.of(list);
  }

  private HashMap<String, PrimitiveTypeProvider> parseMeta(String path, String[] items, int index) {
    String name = LSCUtilities.META_NAMES[index];
    if (name.equalsIgnoreCase(LSCUtilities.META_LAT_NAME) || name
        .equalsIgnoreCase(LSCUtilities.META_LON_NAME)) {
      return meta(path, "LOCATION" /*SpatialDistance.METADATA_DOMAIN*/,
          LSCUtilities.META_NAMES[index], items[index]);
    } else {
      return meta(path, LSCUtilities.DEFAULT_DOMAIN, LSCUtilities.META_NAMES[index], items[index]);
    }
  }

  @Override
  public Map<String, PrimitiveTypeProvider> readNext() {
    // Has to read new line
    do {
      if (this.currentDataIterator == null && this.iterator.hasNext()) {
        //LOGGER.trace("Init / Next: dataIt==null && it.hasNext");
        Map.Entry<String, String> entry = iterator.next();
        Optional<List<Map<String, PrimitiveTypeProvider>>> parsed = parseLine(entry.getKey(),
            metadataMap.get(entry.getValue()));
        parsed.ifPresent(maps -> this.currentDataIterator = maps.iterator());
      }
      if (this.currentDataIterator != null && this.currentDataIterator.hasNext()) {
        //LOGGER.trace("dataNext: dataIt.hasNext");
        Map<String, PrimitiveTypeProvider> out = this.currentDataIterator.next();
        if (!currentDataIterator.hasNext()) {
          // reset, so Init / Next occurs
          currentDataIterator = null;
        }
        //LOGGER.trace("Returning metadata: {}", out);
        writeLogs();
        return out;
      }
    } while (this.currentDataIterator == null && this.iterator.hasNext());
    LOGGER.info("No more to read. Stopping");
    writeLogs();
    return null;
  }

  private void writeLogs() {
    if (!logsEnabled) {
      return;
    }
    if (nextsSinceLastDump++> logCadence) {
      try {
        writeLines(LSCUtilities.META_NO_PATH_FILE, metaNoPath);
        writeLines(LSCUtilities.WRITTEN_FILE, written);
      } catch (IOException e) {
        LOGGER.error("Could not write crucial housekeeping info. Continuing", e);
      }
      nextsSinceLastDump = 0;
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
