package org.vitrivr.cineast.core.util;

import com.carrotsearch.hppc.DoubleArrayDeque;
import java.util.function.Supplier;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class TimeHelper {

  private static final Logger LOGGER = LogManager.getLogger();
  private static final DoubleArrayDeque tic = new DoubleArrayDeque();

  private TimeHelper() {
  }

  public static synchronized void tic() {
    tic.addFirst(System.nanoTime() / 1000000d);
  }

  public static synchronized double toc() {
    double d = tic.removeFirst();
    return (System.nanoTime() / 1000000d) - d;
  }

  public static <O> O timeCall(Supplier<O> fun, String name,
      Level level) {
    long start = System.currentTimeMillis();
    O res = fun.get();
    long stop = System.currentTimeMillis();
    LOGGER.log(level, "Finished {} in {} ms", name, stop - start);
    return res;
  }
}
