package org.vitrivr.cineast.core.db.dao.reader;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import org.vitrivr.cineast.core.config.Config;
import org.vitrivr.cineast.core.data.entities.MultimediaMetadataDescriptor;
import org.vitrivr.cineast.core.data.providers.primitive.PrimitiveTypeProvider;
import org.vitrivr.cineast.core.db.DBSelector;

/**
 * Data access object that facilitates lookups in Cineast's metadata entity (cineast_metadata).
 * Methods in this class usually return MultimediaMetadataDescriptors.
 *
 * @author rgasser
 * @version 1.0
 * @created 10.02.17
 * @see MultimediaMetadataDescriptor
 */
public class MultimediaMetadataReader extends AbstractEntityReader {

  /**
   * Default constructor.
   */
  public MultimediaMetadataReader() {
    this(Config.sharedConfig().getDatabase().getSelectorSupplier().get());
  }

  /**
   * Constructor for MultimediaMetadataReader
   *
   * @param selector DBSelector to use for the MultimediaMetadataReader instance.
   */
  public MultimediaMetadataReader(DBSelector selector) {
    super(selector);
    this.selector.open(MultimediaMetadataDescriptor.ENTITY);
  }

  /**
   * Looks up the metadata for a specific multimedia object.
   *
   * @param objectId ID of the multimedia object for which metadata should be retrieved.
   * @return List of MultimediaMetadataDescriptor object's. May be empty!
   */
  public List<MultimediaMetadataDescriptor> lookupMultimediaMetadata(String objectId) {
    return lookupMultimediaMetadata(ImmutableList.of(objectId));
  }

  /**
   * Looks up the metadata for a multiple multimedia objects.
   *
   * @param objectIds ID's of the multimedia object's for which metadata should be retrieved.
   * @return List of MultimediaMetadataDescriptor object's. May be empty!
   */
  public List<MultimediaMetadataDescriptor> lookupMultimediaMetadata(String... objectIds) {
    return lookupMultimediaMetadata(Arrays.asList(objectIds));
  }

  /**
   * Looks up the metadata for a multiple multimedia objects.
   *
   * @param objectIds ID's of the multimedia object's for which metadata should be retrieved.
   * @return List of MultimediaMetadataDescriptor object's. May be empty!
   */
  public List<MultimediaMetadataDescriptor> lookupMultimediaMetadata(Iterable<String> objectIds) {
    List<Map<String, PrimitiveTypeProvider>> results = this.selector
        .getRows(MultimediaMetadataDescriptor.FIELDNAMES[0], objectIds);

    if (results.isEmpty() && !Iterables.isEmpty(objectIds)) {
      LOGGER.debug("Could not find any MultimediaMetadataDescriptor for provided ID's {}.",
          objectIds);
    }

    List<MultimediaMetadataDescriptor> descriptors = new ArrayList<>(results.size());
    for (Map<String, PrimitiveTypeProvider> result : results) {
      try {
        descriptors.add(new MultimediaMetadataDescriptor(result));
      } catch (DatabaseLookupException e) {
        LOGGER.error("Could not map {} to descriptor, ignoring... This is a programmer's error!",
            result);
      }
    }
    return descriptors;
  }
}
