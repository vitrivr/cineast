package org.vitrivr.cineast.standalone.importer.lsc2020;

import com.opencsv.exceptions.CsvException;
import java.io.IOException;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.vitrivr.cineast.core.data.providers.primitive.PrimitiveTypeProvider;
import org.vitrivr.cineast.core.importer.Importer;

public class TemporalImporter implements Importer<Map<String, PrimitiveTypeProvider>> {

  public static final Logger LOGGER = LogManager.getLogger(TemporalImporter.class);

  private final Map<String, String[]> metadataMap;
  private final Map<String, String> filenameToMinuteIdMap;

  private final Iterator<Entry<String,String>> iterator;

  private final Path root;

  public TemporalImporter(final Path path){
    this.root = path;

    try{
      final LSCUtilities lsc = LSCUtilities.create(path);
      lsc.initMetadata();
      filenameToMinuteIdMap = lsc.getFilenameToMinuteIdLookUp();
      metadataMap = lsc.getMetaPerMinuteId();
      iterator = filenameToMinuteIdMap.entrySet().iterator();
    }catch (IOException | CsvException e) {
      LOGGER.error("Failed to prepare metadata readout due to {}", e,e);
      throw new RuntimeException("Failed to prepare metadata readout",e);
    }
    LOGGER.info("Initialisation finished successfully. Starting import now");
  }

  private long toEpochMillis(String minuteId){
    final LocalDateTime date = LSCUtilities.fromMinuteId(minuteId);
    return date.toInstant(ZoneOffset.UTC).toEpochMilli();
  }

  private Optional<Map<String, PrimitiveTypeProvider>> parseEntry(String key, String[] data){
    final long ms = toEpochMillis(filenameToMinuteIdMap.get(key));
    final PrimitiveTypeProvider msProvider = PrimitiveTypeProvider.fromObject(ms);
    final HashMap<String, PrimitiveTypeProvider> map = new HashMap<>();
    // id
    map.put("id", PrimitiveTypeProvider.fromObject(LSCUtilities.pathToSegmentId(key)));
    // temporal info
    map.put("segmentstart", msProvider);
    map.put("segmentend", PrimitiveTypeProvider.fromObject(ms+1)); // Segment ends one millis later
    map.put("startabs", msProvider);

    return Optional.of(map);
  }


  @Override
  public Map<String, PrimitiveTypeProvider> readNext() {
    while(iterator.hasNext()){
      Entry<String, String> next = iterator.next();
      Optional<Map<String, PrimitiveTypeProvider>> parsed = parseEntry(next.getKey(), metadataMap.get(next.getValue()));
      if(parsed.isPresent()){
        return parsed.get();
      }
    }
    return null;
  }

  @Override
  public Map<String, PrimitiveTypeProvider> convert(
      Map<String, PrimitiveTypeProvider> map) {
    return map;
  }
}
