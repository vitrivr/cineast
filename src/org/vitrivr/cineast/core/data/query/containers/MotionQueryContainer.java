package org.vitrivr.cineast.core.data.query.containers;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import org.vitrivr.cineast.core.data.Pair;
import org.vitrivr.cineast.core.util.MathHelper;

import com.fasterxml.jackson.databind.JsonNode;

import georegression.struct.point.Point2D_F32;

public class MotionQueryContainer implements QueryContainer {

  private String id = null;
  private float weight = 1f;
  private List<Pair<Integer, LinkedList<Point2D_F32>>> paths = new ArrayList<Pair<Integer, LinkedList<Point2D_F32>>>();
  private List<Pair<Integer, LinkedList<Point2D_F32>>> bgPaths = new ArrayList<Pair<Integer, LinkedList<Point2D_F32>>>();

  @Override
  public String getId() {
    return this.id;
  }

  @Override
  public String getSuperId() {
    return "";
  }

  @Override
  public void setId(String id) {
    this.id = id;
  }

  @Override
  public void setSuperId(String id) {  }

  @Override
  public List<Pair<Integer, LinkedList<Point2D_F32>>> getPaths() {
    return this.paths;
  }

  @Override
  public List<Pair<Integer, LinkedList<Point2D_F32>>> getBgPaths() {
    return this.bgPaths;
  }

  public float getWeight() {
    return this.weight;
  }

  public void setWeight(float weight) {
    if (Float.isNaN(weight)) {
      this.weight = 0f;
      return;
    }
    this.weight = MathHelper.limit(weight, -1f, 1f);
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
    
    JsonNode background = jsonNode.get("foreground");
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
      if (!list.isArray()) {
        continue;
      }
      int size = list.size();
      LinkedList<Point2D_F32> pathList = new LinkedList<Point2D_F32>();
      for (int i = 0; i < size; ++i) {
        JsonNode point = list.get(i);
        if (!point.isArray() || point.size() < 2) {
          continue;
        }
        pathList.add(new Point2D_F32(point.get(0).floatValue(), point.get(1).floatValue()));
      }
      _return.add(pathList);
    }
    return _return;
  }

}
