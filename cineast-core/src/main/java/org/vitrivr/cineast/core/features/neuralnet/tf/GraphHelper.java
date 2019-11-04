package org.vitrivr.cineast.core.features.neuralnet.tf;

import org.tensorflow.Graph;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class GraphHelper {

  private GraphHelper(){}

  /**
   * filters operations which are not part of a provided graph
   */
  public static List<String> filterOperations(List<String> operations, Graph graph){

    if(operations == null || operations.isEmpty() || graph == null){
      return Collections.emptyList();
    }

    ArrayList<String> _return = new ArrayList<>(operations.size());

    for(String operation : operations){
      if(graph.operation(operation) != null){
        _return.add(operation);
      }
    }

    return _return;

  }

}
