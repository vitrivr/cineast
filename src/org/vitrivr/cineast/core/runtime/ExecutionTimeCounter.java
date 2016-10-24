package org.vitrivr.cineast.core.runtime;

public interface ExecutionTimeCounter {

  /**
   * used to report task execution time for a particular class
   * @param c the class which executed a task
   * @param miliseconds the task duration in ms
   */
  void reportExecutionTime(Class<?> c, long miliseconds);
  
  /**
   * @param c
   * @return the average execution time for all tasks reported for this class or 0 if the class is unknown or null
   */
  long getAverageExecutionTime(Class<?> c);
  
}
