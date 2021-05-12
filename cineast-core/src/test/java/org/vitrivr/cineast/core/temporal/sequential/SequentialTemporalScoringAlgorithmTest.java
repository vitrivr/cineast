package org.vitrivr.cineast.core.temporal.sequential;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.vitrivr.cineast.core.data.StringDoublePair;
import org.vitrivr.cineast.core.data.TemporalObject;
import org.vitrivr.cineast.core.data.entities.MediaSegmentDescriptor;
import org.vitrivr.cineast.core.temporal.TemporalTestCases;

public class SequentialTemporalScoringAlgorithmTest {

  @Test
  @DisplayName("Sanity temporal scoring")
  public void testSanity() {
    Map<String, MediaSegmentDescriptor> emptyMap = new HashMap<>();
    List<List<StringDoublePair>> emptyList = new ArrayList<>();
    SequentialTemporalScoringAlgorithm testAlgorithm = new SequentialTemporalScoringAlgorithm(emptyMap, emptyList, -1f);
    List<TemporalObject> result = testAlgorithm.score();
    assert result.size() == 0;
  }

  @Test
  @DisplayName("First temporal test case")
  public void testTemporalTestCase1() {
    TemporalTestCases temporalTestCases = new TemporalTestCases();
    temporalTestCases.buildTestCase1();
    SequentialTemporalScoringAlgorithm testAlgorithm = new SequentialTemporalScoringAlgorithm(temporalTestCases.getSegmentMap(), temporalTestCases.getContainerResults(), temporalTestCases.getMaxLength());
    List<TemporalObject> result = testAlgorithm.score();

    assert assertListSame(temporalTestCases.getExpectedResults(), result);
    assert result.size() == 2;
  }

  @Test
  @DisplayName("Second temporal test case")
  public void testTemporalTestCase2() {
    TemporalTestCases temporalTestCases = new TemporalTestCases();
    temporalTestCases.buildTestCase2();
    SequentialTemporalScoringAlgorithm testAlgorithm = new SequentialTemporalScoringAlgorithm(temporalTestCases.getSegmentMap(), temporalTestCases.getContainerResults(), temporalTestCases.getMaxLength());
    List<TemporalObject> result = testAlgorithm.score();

    assert assertListSame(temporalTestCases.getExpectedResults(), result);
    assert result.size() == 1;
  }

  @Test
  @DisplayName("Third temporal test case")
  public void testTemporalTestCase3() {
    TemporalTestCases temporalTestCases = new TemporalTestCases();
    temporalTestCases.buildTestCase3();
    SequentialTemporalScoringAlgorithm testAlgorithm = new SequentialTemporalScoringAlgorithm(temporalTestCases.getSegmentMap(), temporalTestCases.getContainerResults(), temporalTestCases.getMaxLength());
    List<TemporalObject> result = testAlgorithm.score();

    assert assertListSame(temporalTestCases.getExpectedResults(), result);
    assert result.size() == 1;
  }

  public boolean assertListSame(List<TemporalObject> l1, List<TemporalObject> l2) {
    if (l1.size() == l2.size()) {
      Iterator<TemporalObject> it1 = l1.iterator();
      Iterator<TemporalObject> it2 = l2.iterator();
      while (it1.hasNext() && it2.hasNext()) {
        TemporalObject temporalObject1 = it1.next();
        TemporalObject temporalObject2 = it2.next();
        if (!(temporalObject1.getScore() == temporalObject2.getScore() && temporalObject1.getObjectId().equals(temporalObject2.getObjectId()) && temporalObject1.getSegments().size() == temporalObject2.getSegments().size())) {
          return false;
        }
        Iterator<String> innerIt1 = temporalObject1.getSegments().iterator();
        Iterator<String> innerIt2 = temporalObject2.getSegments().iterator();
        while (innerIt1.hasNext() && innerIt2.hasNext()) {
          String string1 = innerIt1.next();
          String string2 = innerIt2.next();
          if (!string1.equals(string2)) {
            return false;
          }
        }
      }
      return true;
    }
    return false;
  }

}
