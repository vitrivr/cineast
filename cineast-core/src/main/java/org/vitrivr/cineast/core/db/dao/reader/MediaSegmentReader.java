package org.vitrivr.cineast.core.db.dao.reader;

import static org.vitrivr.cineast.core.data.entities.MediaSegmentDescriptor.FIELDNAMES;

import com.google.common.collect.ListMultimap;
import com.google.common.collect.Lists;
import com.google.common.collect.Multimaps;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.vitrivr.cineast.core.data.entities.MediaSegmentDescriptor;
import org.vitrivr.cineast.core.data.providers.primitive.PrimitiveTypeProvider;
import org.vitrivr.cineast.core.data.providers.primitive.StringTypeProvider;
import org.vitrivr.cineast.core.db.DBSelector;
import org.vitrivr.cineast.core.util.DBQueryIDGenerator;

public class MediaSegmentReader extends AbstractEntityReader {

  /**
   * Constructor for MediaSegmentReader
   *
   * @param dbSelector DBSelector to use for the MediaObjectMetadataReader instance.
   */
  public MediaSegmentReader(DBSelector dbSelector) {
    super(dbSelector);
    this.selector.open(MediaSegmentDescriptor.ENTITY);
  }

  private static Optional<MediaSegmentDescriptor> propertiesToDescriptor(Map<String, PrimitiveTypeProvider> properties) {

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
    Stream<MediaSegmentDescriptor> descriptors = this.lookUpSegmentsByField(FIELDNAMES[0], segmentId);
    return descriptors.findFirst();
  }

  public Map<String, MediaSegmentDescriptor> lookUpSegments(Iterable<String> segmentIds) {
    return lookUpSegments(segmentIds, null);
  }

  public Map<String, MediaSegmentDescriptor> lookUpSegments(Iterable<String> segmentIds, String queryID) {
    Stream<MediaSegmentDescriptor> descriptors = this.lookUpSegmentsByField(FIELDNAMES[0], segmentIds, queryID);
    //this implicitly deduplicates the stream
    Map<String, MediaSegmentDescriptor> _return = new HashMap<>();
    descriptors.forEach(msd -> _return.put(msd.getSegmentId(), msd));
    return _return;
  }

  public List<MediaSegmentDescriptor> lookUpSegmentsOfObject(String objectId) {
    Stream<MediaSegmentDescriptor> descriptors = this.lookUpSegmentsByField(FIELDNAMES[1], objectId);
    return descriptors.collect(Collectors.toList());
  }

  public ListMultimap<String, MediaSegmentDescriptor> lookUpSegmentsOfObjects(Iterable<String> objectIds) {
    Stream<MediaSegmentDescriptor> descriptors = this.lookUpSegmentsByField(FIELDNAMES[1], objectIds);
    return Multimaps.index(descriptors.iterator(), MediaSegmentDescriptor::getObjectId);
  }

  public List<MediaSegmentDescriptor> lookUpSegmentByNumber(String objectId, int segmentNumber) {
    List<MediaSegmentDescriptor> all = this.lookUpSegmentsOfObject(objectId);
    return all.stream().filter(it -> it.getSequenceNumber() == segmentNumber).collect(Collectors.toList());
  }

  public List<MediaSegmentDescriptor> lookUpSegmentsByNumberRange(String objectId, int lower, int upper) { //TODO implementing this without selecting all segments would require additional functionality in DBSelector
    List<MediaSegmentDescriptor> all = this.lookUpSegmentsOfObject(objectId);
    return all.stream().filter(it -> it.getSequenceNumber() >= lower && it.getSequenceNumber() <= upper).collect(Collectors.toList());
  }

  private Stream<MediaSegmentDescriptor> lookUpSegmentsByField(String fieldName, String fieldValue) {
    return lookUpSegmentsByField(fieldName, Collections.singletonList(fieldValue));
  }

  private Stream<MediaSegmentDescriptor> lookUpSegmentsByField(String fieldName, Iterable<String> fieldValues) {
    return lookUpSegmentsByField(fieldName, fieldValues, null);
  }

  private Stream<MediaSegmentDescriptor> lookUpSegmentsByField(String fieldName, Iterable<String> fieldValues, String queryID) {
    String dbQueryID = DBQueryIDGenerator.generateQueryID("seg-lookup", queryID);
    Set<PrimitiveTypeProvider> uniqueFieldValues = new HashSet<>();
    fieldValues.forEach(el -> uniqueFieldValues.add(new StringTypeProvider(el)));

    List<Map<String, PrimitiveTypeProvider>> segmentsProperties = this.selector.getRows(fieldName, Lists.newArrayList(uniqueFieldValues), dbQueryID);
    return segmentsProperties
        .stream()
        .map(MediaSegmentReader::propertiesToDescriptor)
        .filter(Optional::isPresent)
        .map(Optional::get);
  }
}
