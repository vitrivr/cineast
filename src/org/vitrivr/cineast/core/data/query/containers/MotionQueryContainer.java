package org.vitrivr.cineast.core.data.query.containers;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import org.vitrivr.cineast.core.data.Pair;

import com.fasterxml.jackson.databind.JsonNode;

import georegression.struct.point.Point2D_F32;

public class MotionQueryContainer extends QueryContainer {

  private List<Pair<Integer, LinkedList<Point2D_F32>>> paths = new ArrayList<Pair<Integer, LinkedList<Point2D_F32>>>();
  private List<Pair<Integer, LinkedList<Point2D_F32>>> bgPaths = new ArrayList<Pair<Integer, LinkedList<Point2D_F32>>>();


  @Override
  public List<Pair<Integer, LinkedList<Point2D_F32>>> getPaths() {
    return this.paths;
  }

  @Override
  public List<Pair<Integer, LinkedList<Point2D_F32>>> getBgPaths() {
    return this.bgPaths;
  } 
  
  public void addPath(LinkedList<Point2D_F32> path){
    this.paths.add(new Pair<Integer, LinkedList<Point2D_F32>>(0, path));
  }
  
  public void addBgPath(LinkedList<Point2D_F32> path){
    this.bgPaths.add(new Pair<Integer, LinkedList<Point2D_F32>>(0, path));
  }

  public static QueryContainer fromJson(JsonNode jsonNode) {

    MotionQueryContainer _return = new MotionQueryContainer();
    
    if(jsonNode == null){
      return _return;
    }
    
    JsonNode foreground = jsonNode.get("foreground");
    ArrayList<LinkedList<Point2D_F32>> list = nodeToList(foreground);
    for(LinkedList<Point2D_F32> path : list){
      _return.addPath(path);
    }
    
    JsonNode background = jsonNode.get("background");
    list = nodeToList(background);
    for(LinkedList<Point2D_F32> path : list){
      _return.addBgPath(path);
    }
    
    return _return;
  }

  private static ArrayList<LinkedList<Point2D_F32>> nodeToList(JsonNode jsonNode) {
    if (jsonNode == null || !jsonNode.isArray()) {
      return new ArrayList<>();
    }
    ArrayList<LinkedList<Point2D_F32>> _return = new ArrayList<>(jsonNode.size());
    for (final JsonNode list : jsonNode) {
      JsonNode path = list.get("path");
      if (path == null || !path.isArray()) {
        continue;
      }
      int size = path.size();
      LinkedList<Point2D_F32> pathList = new LinkedList<Point2D_F32>();
      for (int i = 0; i < size; ++i) {
        JsonNode point = path.get(i);
        if (point == null) continue;
        pathList.add(new Point2D_F32(point.get("x").floatValue(), point.get("y").floatValue()));
      }
      _return.add(pathList);
    }
    return _return;
  }

}
