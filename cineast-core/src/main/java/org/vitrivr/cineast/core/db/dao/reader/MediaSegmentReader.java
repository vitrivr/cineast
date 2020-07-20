package org.vitrivr.cineast.core.db.dao.reader;

import com.google.common.collect.ListMultimap;
import com.google.common.collect.Multimaps;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.vitrivr.cineast.core.data.MediaType;
import org.vitrivr.cineast.core.data.entities.MediaSegmentDescriptor;
import org.vitrivr.cineast.core.data.providers.primitive.PrimitiveTypeProvider;
import org.vitrivr.cineast.core.data.providers.primitive.StringProvider;
import org.vitrivr.cineast.core.data.providers.primitive.StringProviderImpl;
import org.vitrivr.cineast.core.data.providers.primitive.StringTypeProvider;
import org.vitrivr.cineast.core.db.DBSelector;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.vitrivr.cineast.core.data.entities.MediaSegmentDescriptor.FIELDNAMES;

public class MediaSegmentReader extends AbstractEntityReader {

  private static final Logger LOGGER = LogManager.getLogger();

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
              properties.get(FIELDNAMES[6]).getFloat(),
              true));

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
    //this implicitly deduplicates the stream
    Map<String, MediaSegmentDescriptor> _return = new HashMap<>();
    descriptors.forEach(msd -> _return.put(msd.getSegmentId(), msd));
    return _return;
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

  public List<MediaSegmentDescriptor> lookUpSegmentByNumber(String objectId, int segmentNumber){
    List<MediaSegmentDescriptor> all = this.lookUpSegmentsOfObject(objectId);
    return all.stream().filter(it -> it.getSequenceNumber() == segmentNumber).collect(Collectors.toList());
  }

  public List<MediaSegmentDescriptor> lookUpSegmentsByNumberRange(String objectId, int lower, int upper){ //TODO implementing this without selecting all segments would require additional functionality in DBSelector
    List<MediaSegmentDescriptor> all = this.lookUpSegmentsOfObject(objectId);
    return all.stream().filter(it -> it.getSequenceNumber() >= lower && it.getSequenceNumber() <= upper).collect(Collectors.toList());
  }

  private Stream<MediaSegmentDescriptor> lookUpSegmentsByField(
      String fieldName, String fieldValue) {
    return lookUpSegmentsByField(fieldName, Collections.singletonList(fieldValue));
  }

  private Stream<MediaSegmentDescriptor> lookUpSegmentsByField(
      String fieldName, Iterable<String> fieldValues) {
    Set<PrimitiveTypeProvider> uniqueFieldValues = new HashSet<>();
    fieldValues.forEach(el -> uniqueFieldValues.add(new StringTypeProvider(el)));

    List<Map<String, PrimitiveTypeProvider>> segmentsProperties =
        this.selector.getRows(fieldName, uniqueFieldValues);
    return segmentsProperties
        .stream()
        .map(MediaSegmentReader::propertiesToDescriptor)
        .filter(Optional::isPresent)
        .map(Optional::get);
  }
}
