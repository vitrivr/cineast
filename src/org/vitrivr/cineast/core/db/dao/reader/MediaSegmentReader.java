package org.vitrivr.cineast.core.db.dao.reader;

import static org.vitrivr.cineast.core.data.entities.MediaSegmentDescriptor.FIELDNAMES;

import com.google.common.collect.ListMultimap;
import com.google.common.collect.Maps;
import com.google.common.collect.Multimaps;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.vitrivr.cineast.core.config.Config;
import org.vitrivr.cineast.core.data.entities.MediaSegmentDescriptor;
import org.vitrivr.cineast.core.data.providers.primitive.PrimitiveTypeProvider;
import org.vitrivr.cineast.core.db.DBSelector;

public class MediaSegmentReader extends AbstractEntityReader {

  private static final Logger LOGGER = LogManager.getLogger();
  /** Default constructor. */
  public MediaSegmentReader() {
    this(Config.sharedConfig().getDatabase().getSelectorSupplier().get());
  }

  /**
   * Constructor for MediaSegmentReader
   *
   * @param dbSelector DBSelector to use for the MediaObjectMetadataReader instance.
   */
  public MediaSegmentReader(DBSelector dbSelector) {
    super(dbSelector);
    this.selector.open(MediaSegmentDescriptor.ENTITY);
  }

  private static Optional<MediaSegmentDescriptor> propertiesToDescriptor(
      Map<String, PrimitiveTypeProvider> properties) {

    if (properties.containsKey(FIELDNAMES[0])
        && properties.containsKey(FIELDNAMES[1])
        && properties.containsKey(FIELDNAMES[2])
        && properties.containsKey(FIELDNAMES[3])
        && properties.containsKey(FIELDNAMES[4])
        && properties.containsKey(FIELDNAMES[5])
        && properties.containsKey(FIELDNAMES[6])) {

      return Optional.of(
          new MediaSegmentDescriptor(
              properties.get(FIELDNAMES[1]).getString(),
              properties.get(FIELDNAMES[0]).getString(),
              properties.get(FIELDNAMES[2]).getInt(),
              properties.get(FIELDNAMES[3]).getInt(),
              properties.get(FIELDNAMES[4]).getInt(),
              properties.get(FIELDNAMES[5]).getFloat(),
              properties.get(FIELDNAMES[6]).getFloat()));

    } else {
      return Optional.empty();
    }
  }

  public Optional<MediaSegmentDescriptor> lookUpSegment(String segmentId) {
    Stream<MediaSegmentDescriptor> descriptors =
        this.lookUpSegmentsByField(FIELDNAMES[0], segmentId);
    return descriptors.findFirst();
  }

  public Map<String, MediaSegmentDescriptor> lookUpSegments(Iterable<String> segmentIds) {
    Stream<MediaSegmentDescriptor> descriptors =
        this.lookUpSegmentsByField(FIELDNAMES[0], segmentIds);
    return Maps.uniqueIndex(descriptors.iterator(), MediaSegmentDescriptor::getSegmentId);
  }

  public List<MediaSegmentDescriptor> lookUpSegmentsOfObject(String objectId) {
    Stream<MediaSegmentDescriptor> descriptors =
        this.lookUpSegmentsByField(FIELDNAMES[1], objectId);
    return descriptors.collect(Collectors.toList());
  }

  public ListMultimap<String, MediaSegmentDescriptor> lookUpSegmentsOfObjects(
      Iterable<String> objectIds) {
    Stream<MediaSegmentDescriptor> descriptors =
        this.lookUpSegmentsByField(FIELDNAMES[1], objectIds);
    return Multimaps.index(descriptors.iterator(), MediaSegmentDescriptor::getObjectId);
  }

  private Stream<MediaSegmentDescriptor> lookUpSegmentsByField(
      String fieldName, String fieldValue) {
    return lookUpSegmentsByField(fieldName, Collections.singletonList(fieldValue));
  }

  private Stream<MediaSegmentDescriptor> lookUpSegmentsByField(
      String fieldName, Iterable<String> fieldValues) {
    Set<String> uniqueFieldValues = new HashSet<>();
    fieldValues.forEach(value -> uniqueFieldValues.add(value));

    List<Map<String, PrimitiveTypeProvider>> segmentsProperties =
        this.selector.getRows(fieldName, uniqueFieldValues);
    return segmentsProperties
        .stream()
        .map(MediaSegmentReader::propertiesToDescriptor)
        .filter(Optional::isPresent)
        .map(Optional::get);
  }
}
