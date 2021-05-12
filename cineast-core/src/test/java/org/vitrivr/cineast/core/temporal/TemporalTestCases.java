package org.vitrivr.cineast.core.temporal;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.vitrivr.cineast.core.data.StringDoublePair;
import org.vitrivr.cineast.core.data.TemporalObject;
import org.vitrivr.cineast.core.data.entities.MediaSegmentDescriptor;

public class TemporalTestCases {

  private Map<String, MediaSegmentDescriptor> segmentMap;
  private List<List<StringDoublePair>> containerResults;
  private float maxLength;
  private List<TemporalObject> expectedResults;

  public void buildTestCase1() {
    Map<String, MediaSegmentDescriptor> segmentMap = new HashMap<>();
    List<List<StringDoublePair>> containerResults = new ArrayList<>();

    segmentMap.put(descriptor1_1.getSegmentId(), descriptor1_1);
    segmentMap.put(descriptor1_2.getSegmentId(), descriptor1_2);
    segmentMap.put(descriptor2_1.getSegmentId(), descriptor2_1);
    segmentMap.put(descriptor2_2.getSegmentId(), descriptor2_2);

    List<StringDoublePair> containerList0 = new ArrayList<>();
    containerList0.add(new StringDoublePair(descriptor1_1.getSegmentId(), 1d));
    containerList0.add(new StringDoublePair(descriptor2_1.getSegmentId(), 0.5d));

    List<StringDoublePair> containerList1 = new ArrayList<>();
    containerList1.add(new StringDoublePair(descriptor1_2.getSegmentId(), 1d));
    containerList1.add(new StringDoublePair(descriptor2_2.getSegmentId(), 0.5d));

    containerResults.add(0, containerList0);
    containerResults.add(1, containerList1);

    List<TemporalObject> expectedResults = new ArrayList<>();
    List<String> segments1 = new ArrayList<>();
    segments1.add(descriptor1_1.getSegmentId());
    segments1.add(descriptor1_2.getSegmentId());
    TemporalObject t1 = new TemporalObject(segments1, descriptor1_1.getObjectId(), 1f);
    expectedResults.add(t1);
    List<String> segments2 = new ArrayList<>();
    segments2.add(descriptor2_1.getSegmentId());
    segments2.add(descriptor2_2.getSegmentId());
    TemporalObject t2 = new TemporalObject(segments2, descriptor2_1.getObjectId(), 0.5f);
    expectedResults.add(t2);

    this.segmentMap = segmentMap;
    this.containerResults = containerResults;
    this.maxLength = 20f;
    this.expectedResults = expectedResults;
  }

  public void buildTestCase2() {
    Map<String, MediaSegmentDescriptor> segmentMap = new HashMap<>();
    List<List<StringDoublePair>> containerResults = new ArrayList<>();

    segmentMap.put(descriptor1_1.getSegmentId(), descriptor1_1);
    segmentMap.put(descriptor1_2.getSegmentId(), descriptor1_2);
    segmentMap.put(descriptor1_3.getSegmentId(), descriptor1_3);

    List<StringDoublePair> containerList0 = new ArrayList<>();
    containerList0.add(new StringDoublePair(descriptor1_1.getSegmentId(), 1d));

    List<StringDoublePair> containerList1 = new ArrayList<>();
    containerList1.add(new StringDoublePair(descriptor1_2.getSegmentId(), 1d));
    containerList1.add(new StringDoublePair(descriptor1_3.getSegmentId(), 0.5d));

    containerResults.add(0, containerList0);
    containerResults.add(1, containerList1);

    List<TemporalObject> expectedResults = new ArrayList<>();
    List<String> segments1 = new ArrayList<>();
    segments1.add(descriptor1_1.getSegmentId());
    segments1.add(descriptor1_2.getSegmentId());
    segments1.add(descriptor1_3.getSegmentId());
    TemporalObject t1 = new TemporalObject(segments1, descriptor1_1.getObjectId(), 1f);
    expectedResults.add(t1);

    this.segmentMap = segmentMap;
    this.containerResults = containerResults;
    this.maxLength = 20f;
    this.expectedResults = expectedResults;
  }

  public void buildTestCase3() {
    Map<String, MediaSegmentDescriptor> segmentMap = new HashMap<>();
    List<List<StringDoublePair>> containerResults = new ArrayList<>();

    segmentMap.put(descriptor1_1.getSegmentId(), descriptor1_1);
    segmentMap.put(descriptor1_2.getSegmentId(), descriptor1_2);
    segmentMap.put(descriptor1_3.getSegmentId(), descriptor1_3);
    segmentMap.put(descriptor1_4.getSegmentId(), descriptor1_4);
    segmentMap.put(descriptor1_5.getSegmentId(), descriptor1_5);
    segmentMap.put(descriptor1_6.getSegmentId(), descriptor1_6);
    segmentMap.put(descriptor1_7.getSegmentId(), descriptor1_7);

    List<StringDoublePair> containerList0 = new ArrayList<>();
    containerList0.add(new StringDoublePair(descriptor1_1.getSegmentId(), 1d));

    List<StringDoublePair> containerList1 = new ArrayList<>();
    containerList1.add(new StringDoublePair(descriptor1_2.getSegmentId(), 1d));
    containerList1.add(new StringDoublePair(descriptor1_3.getSegmentId(), 0.5d));
    containerList1.add(new StringDoublePair(descriptor1_7.getSegmentId(), 2d));

    List<StringDoublePair> containerList2 = new ArrayList<>();
    containerList2.add(new StringDoublePair(descriptor1_3.getSegmentId(), 0.5d));
    containerList2.add(new StringDoublePair(descriptor1_3.getSegmentId(), 0.5d));
    containerList2.add(new StringDoublePair(descriptor1_4.getSegmentId(), 1d));
    containerList2.add(new StringDoublePair(descriptor1_5.getSegmentId(), 0.5d));
    containerList2.add(new StringDoublePair(descriptor1_5.getSegmentId(), 1d));

    containerResults.add(0, containerList0);
    containerResults.add(1, containerList1);
    containerResults.add(2, containerList2);

    List<TemporalObject> expectedResults = new ArrayList<>();
    List<String> segments1 = new ArrayList<>();
    segments1.add(descriptor1_1.getSegmentId());
    segments1.add(descriptor1_2.getSegmentId());
    segments1.add(descriptor1_3.getSegmentId());
    segments1.add(descriptor1_4.getSegmentId());
    segments1.add(descriptor1_5.getSegmentId());
    segments1.add(descriptor1_7.getSegmentId());
    TemporalObject t1 = new TemporalObject(segments1, descriptor1_1.getObjectId(), 1f);
    expectedResults.add(t1);

    this.segmentMap = segmentMap;
    this.containerResults = containerResults;
    this.maxLength = 30f;
    this.expectedResults = expectedResults;
  }

  public void buildTestCase4() {
    // TODO
  }

  public void buildTestCase5() {
    // TODO
  }

  public Map<String, MediaSegmentDescriptor> getSegmentMap() {
    return segmentMap;
  }

  public List<List<StringDoublePair>> getContainerResults() {
    return containerResults;
  }

  public float getMaxLength() {
    return maxLength;
  }

  public List<TemporalObject> getExpectedResults() {
    return expectedResults;
  }

  private MediaSegmentDescriptor descriptor1_1 = new MediaSegmentDescriptor("Test1", "Test1_1", 0, 0, 10, 0f, 10f, true);
  private MediaSegmentDescriptor descriptor1_2 = new MediaSegmentDescriptor("Test1", "Test1_2", 0, 10, 20, 10f, 20f, true);
  private MediaSegmentDescriptor descriptor1_3 = new MediaSegmentDescriptor("Test1", "Test1_3", 0, 20, 30, 20f, 30f, true);
  private MediaSegmentDescriptor descriptor1_4 = new MediaSegmentDescriptor("Test1", "Test1_4", 0, 30, 40, 30f, 40f, true);
  private MediaSegmentDescriptor descriptor1_5 = new MediaSegmentDescriptor("Test1", "Test1_5", 0, 40, 50, 40f, 50f, true);
  private MediaSegmentDescriptor descriptor1_6 = new MediaSegmentDescriptor("Test1", "Test1_6", 0, 50, 60, 50f, 60f, true);
  private MediaSegmentDescriptor descriptor1_7 = new MediaSegmentDescriptor("Test1", "Test1_7", 0, 60, 70, 60f, 70f, true);

  private MediaSegmentDescriptor descriptor2_1 = new MediaSegmentDescriptor("Test2", "Test2_1", 0, 0, 10, 0f, 10f, true);
  private MediaSegmentDescriptor descriptor2_2 = new MediaSegmentDescriptor("Test2", "Test2_2", 0, 10, 20, 10f, 20f, true);
  private MediaSegmentDescriptor descriptor2_3 = new MediaSegmentDescriptor("Test2", "Test2_3", 0, 20, 30, 20f, 30f, true);
  private MediaSegmentDescriptor descriptor2_4 = new MediaSegmentDescriptor("Test2", "Test2_4", 0, 30, 40, 30f, 40f, true);
  private MediaSegmentDescriptor descriptor2_5 = new MediaSegmentDescriptor("Test2", "Test2_5", 0, 40, 50, 40f, 50f, true);
  private MediaSegmentDescriptor descriptor2_6 = new MediaSegmentDescriptor("Test2", "Test2_6", 0, 50, 60, 50f, 60f, true);
  private MediaSegmentDescriptor descriptor2_7 = new MediaSegmentDescriptor("Test2", "Test2_7", 0, 60, 70, 60f, 70f, true);

  private MediaSegmentDescriptor descriptor3_1 = new MediaSegmentDescriptor("Test3", "Test3_1", 0, 0, 10, 0f, 10f, true);
  private MediaSegmentDescriptor descriptor3_2 = new MediaSegmentDescriptor("Test3", "Test3_2", 0, 10, 20, 10f, 20f, true);
  private MediaSegmentDescriptor descriptor3_3 = new MediaSegmentDescriptor("Test3", "Test3_3", 0, 20, 30, 20f, 30f, true);
  private MediaSegmentDescriptor descriptor3_4 = new MediaSegmentDescriptor("Test3", "Test3_4", 0, 30, 40, 30f, 40f, true);
  private MediaSegmentDescriptor descriptor3_5 = new MediaSegmentDescriptor("Test3", "Test3_5", 0, 40, 50, 40f, 50f, true);
  private MediaSegmentDescriptor descriptor3_6 = new MediaSegmentDescriptor("Test3", "Test3_6", 0, 50, 60, 50f, 60f, true);
  private MediaSegmentDescriptor descriptor3_7 = new MediaSegmentDescriptor("Test3", "Test3_7", 0, 60, 70, 60f, 70f, true);
}
