package org.vitrivr.cineast.tests.util.benchmarks;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import org.vitrivr.cineast.core.benchmark.model.BenchmarkImpl;

import java.util.*;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * @author rgasser
 * @version 1.0
 * @created 06.05.17
 */
public class BenchmarkImplTest {
    /**
     * Tests the start - and end duration of the benchmark. Performs 3 runs with
     * random timeouts.
     */
    @Test
    @DisplayName("Benchmark Start/End Test")
    public void testStartEndDuration() {
        Random random = new Random();
        for (int i=0;i<3;i++) {
            int timeout = random.nextInt(50) * 100;
            BenchmarkImpl benchmark = new BenchmarkImpl("test");
            try {
                Thread.sleep(timeout);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            benchmark.end();
            assertEquals(benchmark.elapsed(), timeout/1000.0f, 0.01f);
        }
    }

    /**
     * Tests the split durations of the BenchmarkImpl class
     */
    @Test
    @DisplayName("Benchmark Split Test")
    public void testSplitDuration() {
        Random random = new Random();
        int splits = random.nextInt(10) + 1;
        List<Integer> timeouts = new ArrayList<>(splits);
        BenchmarkImpl benchmark = new BenchmarkImpl("test");
        try {
            /* Create n splits with random timeout. */
            for (int s=0;s<splits;s++) {
                benchmark.split("TEST + " + s);
                int timeout = random.nextInt(50) * 100;
                timeouts.add(timeout);
                Thread.sleep(timeout);
            }
            benchmark.end();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        /* Check if number of splits and size of split-durations map is equal. */
        Map<String,Float> durations = benchmark.splitDurations();
        assertEquals(durations.size(), splits);
        int k = 0;
        for (Map.Entry<String,Float> split : durations.entrySet()) {
            assertEquals(split.getValue(), timeouts.get(k)/1000.0f, 0.01f);
            k++;
        }
    }
}
