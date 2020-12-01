package org.vitrivr.cineast.core.db.dao.reader;

import java.util.Arrays;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import org.apache.commons.lang3.time.StopWatch;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.vitrivr.cineast.core.data.entities.MediaObjectMetadataDescriptor;
import org.vitrivr.cineast.core.data.providers.primitive.PrimitiveTypeProvider;
import org.vitrivr.cineast.core.data.providers.primitive.StringTypeProvider;
import org.vitrivr.cineast.core.db.DBSelector;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.vitrivr.cineast.core.util.TimeHelper;

/**
 * Data access object that facilitates lookups in Cineast's metadata entity (cineast_metadata). Methods in this class usually return MultimediaMetadataDescriptors.
 *
 * @author rgasser
 * @version 1.0
 * @created 10.02.17
 * @see MediaObjectMetadataDescriptor
 */
public class MediaObjectMetadataReader extends AbstractEntityReader {

  private static final Logger LOGGER = LogManager.getLogger();


  /**
   * Constructor for MediaObjectMetadataReader
   *
   * @param selector DBSelector to use for the MediaObjectMetadataReader instance.
   */
  public MediaObjectMetadataReader(DBSelector selector) {
    super(selector);
    this.selector.open(MediaObjectMetadataDescriptor.ENTITY);
  }

  /**
   * Looks up the metadata for a specific multimedia object.
   *
   * @param objectid ID of the multimedia object for which metadata should be retrieved.
   * @return List of MediaObjectMetadataDescriptor object's. May be empty!
   */
  public List<MediaObjectMetadataDescriptor> lookupMultimediaMetadata(String objectid) {
    final List<Map<String, PrimitiveTypeProvider>> results = this.selector.getRows(MediaObjectMetadataDescriptor.FIELDNAMES[0], new StringTypeProvider(objectid));
    if (results.isEmpty()) {
      LOGGER.debug("Could not find MediaObjectMetadataDescriptor with ID {}", objectid);
      return new ArrayList<>(0);
    }

    final ArrayList<MediaObjectMetadataDescriptor> list = new ArrayList<>(results.size());
    results.forEach(r -> {
      try {
        list.add(new MediaObjectMetadataDescriptor(r));
      } catch (DatabaseLookupException exception) {
        LOGGER.fatal("Could not map data returned for row {}. This is a programmer's error!", objectid);
      }
    });
    return list;
  }

  /**
   * Looks up the metadata for a multiple multimedia objects.
   *
   * @param objectids ID's of the multimedia object's for which metadata should be retrieved.
   * @return List of MediaObjectMetadataDescriptor object's. May be empty!
   */
  public List<MediaObjectMetadataDescriptor> lookupMultimediaMetadata(List<String> objectids) {
    StopWatch watch = StopWatch.createStarted();
    LOGGER.traceEntry();
    final List<Map<String, PrimitiveTypeProvider>> results = this.selector.getRows(MediaObjectMetadataDescriptor.FIELDNAMES[0], objectids);
    if (results.isEmpty()) {
      LOGGER.debug("Could not find any metadata for provided object IDs. Excerpt: {}, ID count: {}", Arrays.toString(objectids.subList(0, Math.min(5, objectids.size())).toArray()), objectids.size());
    }

    final ArrayList<MediaObjectMetadataDescriptor> list = new ArrayList<>(results.size());
    results.forEach(r -> {
      try {
        list.add(new MediaObjectMetadataDescriptor(r));
      } catch (DatabaseLookupException exception) {
        LOGGER.fatal("Could not map data. This is a programmer's error!");
      }
    });
    watch.stop();
    LOGGER.debug("Performed object metadata lookup for {} ids in {} ms. {} results.", objectids.size(), watch.getTime(TimeUnit.MILLISECONDS), list.size());
    return list;
  }
}
