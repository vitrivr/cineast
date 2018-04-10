package org.vitrivr.cineast.core.runtime;

public interface ExecutionTimeCounter {

  /**
   * used to report task execution time for a particular class
   * @param name classname. is a string since we can't determine the classname of generic types at runtime
   * @param miliseconds the task duration in ms
   */
  void reportExecutionTime(String name, long miliseconds);

  /**
   * @return the average execution time for all tasks reported for this class or 0 if the class is unknown or null
   */
  long getAverageExecutionTime(String name);

}
