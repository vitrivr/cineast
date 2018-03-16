package org.vitrivr.cineast.core.db.dao.reader;

import static org.vitrivr.cineast.core.data.entities.SegmentDescriptor.FIELDNAMES;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.vitrivr.cineast.core.config.Config;
import org.vitrivr.cineast.core.data.entities.SegmentDescriptor;
import org.vitrivr.cineast.core.data.providers.primitive.PrimitiveTypeProvider;
import org.vitrivr.cineast.core.db.DBSelector;

import com.google.common.collect.ListMultimap;
import com.google.common.collect.Maps;
import com.google.common.collect.Multimaps;

public class SegmentLookup extends AbstractEntityReader {

  private static final Logger LOGGER = LogManager.getLogger();
  /**
   * Default constructor.
   */
  public SegmentLookup() {
    this(Config.sharedConfig().getDatabase().getSelectorSupplier().get());
  }

  /**
   * Constructor for SegmentLookup
   *
   * @param dbSelector DBSelector to use for the MultimediaMetadataReader instance.
   */
  public SegmentLookup(DBSelector dbSelector) {
    super(dbSelector);
    this.selector.open(SegmentDescriptor.ENTITY);
  }

  public Optional<SegmentDescriptor> lookUpSegment(String segmentId) {
    Stream<SegmentDescriptor> descriptors = this.lookUpSegmentsByField(FIELDNAMES[0], segmentId);
    return descriptors.findFirst();
  }

  public Map<String, SegmentDescriptor> lookUpSegments(Iterable<String> segmentIds) {
    Stream<SegmentDescriptor> descriptors = this.lookUpSegmentsByField(FIELDNAMES[0], segmentIds);
    return Maps.uniqueIndex(descriptors.iterator(), SegmentDescriptor::getSegmentId);
  }

  public List<SegmentDescriptor> lookUpSegmentsOfObject(String objectId) {
    Stream<SegmentDescriptor> descriptors = this.lookUpSegmentsByField(FIELDNAMES[1], objectId);
    return descriptors.collect(Collectors.toList());
  }

  public ListMultimap<String, SegmentDescriptor> lookUpSegmentsOfObjects(
      Iterable<String> objectIds) {
    Stream<SegmentDescriptor> descriptors = this.lookUpSegmentsByField(FIELDNAMES[1], objectIds);
    return Multimaps.index(descriptors.iterator(), SegmentDescriptor::getObjectId);
  }

  private Stream<SegmentDescriptor> lookUpSegmentsByField(String fieldName, String fieldValue) {
    return lookUpSegmentsByField(fieldName, Collections.singletonList(fieldValue));
  }

  private Stream<SegmentDescriptor> lookUpSegmentsByField(String fieldName,
      Iterable<String> fieldValues) {
    List<Map<String, PrimitiveTypeProvider>> segmentsProperties = this.selector
        .getRows(fieldName, fieldValues);
    return segmentsProperties.stream()
        .map(SegmentLookup::propertiesToDescriptor)
        .filter(Optional::isPresent)
        .map(Optional::get);
  }

  private static Optional<SegmentDescriptor> propertiesToDescriptor(
      Map<String, PrimitiveTypeProvider> properties) {

    if (properties.containsKey(FIELDNAMES[0]) &&
        properties.containsKey(FIELDNAMES[1]) &&
        properties.containsKey(FIELDNAMES[2]) &&
        properties.containsKey(FIELDNAMES[3]) &&
        properties.containsKey(FIELDNAMES[4]) &&
        properties.containsKey(FIELDNAMES[5]) &&
        properties.containsKey(FIELDNAMES[6])) {

      return Optional.of(new SegmentDescriptor(
          properties.get(FIELDNAMES[1]).getString(),
          properties.get(FIELDNAMES[0]).getString(),
          properties.get(FIELDNAMES[2]).getInt(),
          properties.get(FIELDNAMES[3]).getInt(),
          properties.get(FIELDNAMES[4]).getInt(),
          properties.get(FIELDNAMES[5]).getFloat(),
          properties.get(FIELDNAMES[6]).getFloat()
      ));

    } else {
      return Optional.empty();
    }

  }

}
