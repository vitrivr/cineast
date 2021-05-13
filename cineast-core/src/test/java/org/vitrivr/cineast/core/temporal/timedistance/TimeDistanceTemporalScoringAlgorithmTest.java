package org.vitrivr.cineast.core.temporal.timedistance;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.vitrivr.cineast.core.data.StringDoublePair;
import org.vitrivr.cineast.core.data.TemporalObject;
import org.vitrivr.cineast.core.data.entities.MediaSegmentDescriptor;
import org.vitrivr.cineast.core.temporal.TemporalTestCases;

public class TimeDistanceTemporalScoringAlgorithmTest {
  @Test
  @DisplayName("Sanity temporal scoring")
  public void testSanity() {
    Map<String, MediaSegmentDescriptor> emptyMap = new HashMap<>();
    List<List<StringDoublePair>> emptyList = new ArrayList<>();
    TimeDistanceTemporalScoringAlgorithm testAlgorithm = new TimeDistanceTemporalScoringAlgorithm(emptyMap, emptyList, new ArrayList<>(), -1f);
    List<TemporalObject> result = testAlgorithm.score();
    assert result.size() == 0;
  }

  @Test
  @DisplayName("First temporal test case")
  public void testTemporalTestCase1() {
    TemporalTestCases temporalTestCases = new TemporalTestCases();
    temporalTestCases.buildTestCase1();
    TimeDistanceTemporalScoringAlgorithm testAlgorithm = new TimeDistanceTemporalScoringAlgorithm(temporalTestCases.getSegmentMap(), temporalTestCases.getContainerResults(), temporalTestCases.getTimeDistances(), temporalTestCases.getMaxLength());
    List<TemporalObject> result = testAlgorithm.score();

    assert temporalTestCases.assertListSame(temporalTestCases.getExpectedResults(), result);
    assert result.size() == 2;
  }

  @Test
  @DisplayName("Second temporal test case")
  public void testTemporalTestCase2() {
    TemporalTestCases temporalTestCases = new TemporalTestCases();
    temporalTestCases.buildTestCase2();
    TimeDistanceTemporalScoringAlgorithm testAlgorithm = new TimeDistanceTemporalScoringAlgorithm(temporalTestCases.getSegmentMap(), temporalTestCases.getContainerResults(), temporalTestCases.getTimeDistances(), temporalTestCases.getMaxLength());
    List<TemporalObject> result = testAlgorithm.score();

    assert temporalTestCases.assertListSame(temporalTestCases.getExpectedResults(), result);
    assert result.size() == 1;
  }

  @Test
  @DisplayName("Third temporal test case")
  public void testTemporalTestCase3() {
    TemporalTestCases temporalTestCases = new TemporalTestCases();
    temporalTestCases.buildTestCase3();
    TimeDistanceTemporalScoringAlgorithm testAlgorithm = new TimeDistanceTemporalScoringAlgorithm(temporalTestCases.getSegmentMap(), temporalTestCases.getContainerResults(), temporalTestCases.getTimeDistances(), temporalTestCases.getMaxLength());
    List<TemporalObject> result = testAlgorithm.score();

    assert temporalTestCases.assertListSame(temporalTestCases.getExpectedResults(), result);
    assert result.size() == 1;
  }

  @Test
  @DisplayName("Fourth temporal test case")
  public void testTemporalTestCase4() {
    TemporalTestCases temporalTestCases = new TemporalTestCases();
    temporalTestCases.buildTestCase4();
    TimeDistanceTemporalScoringAlgorithm testAlgorithm = new TimeDistanceTemporalScoringAlgorithm(temporalTestCases.getSegmentMap(), temporalTestCases.getContainerResults(), temporalTestCases.getTimeDistances(), temporalTestCases.getMaxLength());
    List<TemporalObject> result = testAlgorithm.score();

    assert temporalTestCases.assertListSame(temporalTestCases.getExpectedResults(), result);
    assert result.size() == 3;
  }
}
