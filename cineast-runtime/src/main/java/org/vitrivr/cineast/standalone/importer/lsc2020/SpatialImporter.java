package org.vitrivr.cineast.standalone.importer.lsc2020;

import com.opencsv.exceptions.CsvException;
import java.io.IOException;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.vitrivr.cineast.core.data.Location;
import org.vitrivr.cineast.core.data.providers.LocationProvider;
import org.vitrivr.cineast.core.data.providers.primitive.PrimitiveTypeProvider;
import org.vitrivr.cineast.core.features.SpatialDistance;
import org.vitrivr.cineast.core.features.abstracts.MetadataFeatureModule;
import org.vitrivr.cineast.core.importer.Importer;

/**
 * FloatVector: [lat,lon]. Column name is feature and entity name is SpatialDistance::entityName == features_SpatialDistance
 */
public class SpatialImporter implements Importer<Map<String, PrimitiveTypeProvider>> {

  private static final Logger LOGGER = LogManager.getLogger(SpatialImporter.class);

  private final Map<String, String[]> metadataMap;
  private final Map<String, String> filenameToMinuteIdMap;

  private final Iterator<Entry<String,String>> iterator;

  private final Path root;

  public SpatialImporter(Path path){
    this.root = path;

    try {
      LSCUtilities lsc = LSCUtilities.create(path);
      lsc.initMetadata();
      filenameToMinuteIdMap = lsc.getFilenameToMinuteIdLookUp();
      metadataMap = lsc.getMetaPerMinuteId();
      iterator = filenameToMinuteIdMap.entrySet().iterator();
    } catch (IOException | CsvException e) {
      LOGGER.error("Failed to prepare metadata readout due to {}", e,e);
      throw new RuntimeException("Failed to prepare metadata readout",e);
    }
    LOGGER.info("Initialisation finished successfully. Starting import...");
  }

  private Optional<Map<String, PrimitiveTypeProvider>> parseEntry(String key, String[] data){
    String lat = data[LSCUtilities.META_LAT_COL];
    String lon = data[LSCUtilities.META_LON_COL];

    boolean ignore = false;
    float fLat = Float.NaN, fLon = Float.NaN;
    try{
    fLat = Float.parseFloat(lat);
    }catch(NumberFormatException e){
      LOGGER.warn("Could not parse latitute for {}, as it was {}", key, lat,e);
      ignore = true;
    }
    try{
      fLon = Float.parseFloat(lon);
    }catch(NumberFormatException e){
      LOGGER.warn("Could not parse longitutde for {}, as it was {}", key, lon, e);
      ignore = true;
    }
    if(ignore){
      return Optional.empty();
    }

    final HashMap<String, PrimitiveTypeProvider> map = new HashMap<>();
    // id
    map.put("id", PrimitiveTypeProvider.fromObject(LSCUtilities.pathToSegmentId(key)));
    // feature
    map.put("feature", PrimitiveTypeProvider.fromObject(Location.of(fLat, fLon)));
    return Optional.of(map);
  }


  @Override
  public Map<String, PrimitiveTypeProvider> readNext() {
    do{
      Entry<String, String> next = iterator.next();
      Optional<Map<String, PrimitiveTypeProvider>> parsed = parseEntry(next.getKey(), metadataMap.get(next.getValue()));
      if(parsed.isPresent()){
        return parsed.get();
      }
    }while(iterator.hasNext());
    return null;
  }



  @Override
  public Map<String, PrimitiveTypeProvider> convert(Map<String, PrimitiveTypeProvider> data) {
    return data;
  }
}
