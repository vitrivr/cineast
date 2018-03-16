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
import org.vitrivr.cineast.core.data.providers.primitive.ProviderDataType;
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
    Optional<String>  segmentId      = getField(properties, FIELDNAMES[0], String.class);
    Optional<String>  objectId       = getField(properties, FIELDNAMES[1], String.class);
    Optional<Integer> sequenceNumber = getField(properties, FIELDNAMES[2], Integer.class);
    Optional<Integer> sequenceStart  = getField(properties, FIELDNAMES[3], Integer.class);
    Optional<Integer> sequenceEnd    = getField(properties, FIELDNAMES[4], Integer.class);
    Optional<Float>   absoluteStart  = getField(properties, FIELDNAMES[5], Float.class);
    Optional<Float>   absoluteEnd    = getField(properties, FIELDNAMES[6], Float.class);

    if (!segmentId.isPresent()
        || !objectId.isPresent()
        || !sequenceNumber.isPresent()
        || !sequenceStart.isPresent()
        || !sequenceEnd.isPresent()
        || !absoluteStart.isPresent()
        || !absoluteEnd.isPresent()) {
      return Optional.empty();
    } else {
      return Optional.of(new SegmentDescriptor(
          objectId.get(),
          segmentId.get(),
          sequenceNumber.get(),
          sequenceStart.get(),
          sequenceEnd.get(),
          absoluteStart.get(),
          absoluteEnd.get())
      );
    }
  }

  private static <T> Optional<T> getField(Map<String, PrimitiveTypeProvider> properties,
      String fieldName, Class<T> fieldClass) {
    Optional<PrimitiveTypeProvider> provider = Optional.ofNullable(properties.get(fieldName));
    if (!provider.isPresent()) {
      LOGGER.error("Field {} not found in segment.", fieldName);
    }

    Optional<Object> object = provider
        .map(PrimitiveTypeProvider::getObject)
        .filter(fieldClass::isInstance);

    if (!object.isPresent()) {
      ProviderDataType givenType = provider
          .map(PrimitiveTypeProvider::getType)
          .orElse(ProviderDataType.UNKNOWN);
      LOGGER.error("Invalid data type for field {} in segment, expected {}, got {}.",
          fieldName, fieldClass.toString(), givenType);
    }

    return object.map(fieldClass::cast);
  }
}
