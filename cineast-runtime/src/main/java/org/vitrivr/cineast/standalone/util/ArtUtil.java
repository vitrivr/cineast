package org.vitrivr.cineast.standalone.util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.vitrivr.cineast.art.modules.visualization.SegmentDescriptorComparator;
import org.vitrivr.cineast.core.color.ColorConverter;
import org.vitrivr.cineast.core.color.RGBContainer;
import org.vitrivr.cineast.core.color.ReadableLabContainer;
import org.vitrivr.cineast.core.color.ReadableRGBContainer;
import org.vitrivr.cineast.core.data.entities.MediaSegmentDescriptor;
import org.vitrivr.cineast.core.data.providers.primitive.PrimitiveTypeProvider;
import org.vitrivr.cineast.core.db.DBSelector;
import org.vitrivr.cineast.core.db.dao.reader.MediaSegmentReader;

import com.eclipsesource.json.JsonArray;
import com.eclipsesource.json.JsonObject;

/**
 * Created by sein on 30.08.16.
 */
public class ArtUtil {
  private static final Logger LOGGER = LogManager.getLogger();

  /**
   * This reads a given shot into an integer array representing an image. This can be done on shots which have a given
   * size of values which represent a given position on a feature shot.
   *
   * @param shotId shot id to sue
   * @param selector DBSelector for the corresponding table
   * @param sizeX size x of the resulted image
   * @param sizeY size y of the resulted image
   * @return integer array representing an image in 3 integer rgb format
   */
  public static int[][][] shotToRGB(String shotId, DBSelector selector, int sizeX, int sizeY){
    java.util.List<Map<String, PrimitiveTypeProvider>> result = selector.getRows("id", shotId);

    int[][][] pixels = new int[sizeX][sizeY][3];

    for (Map<String, PrimitiveTypeProvider> row : result) {
      float[] arr = row.get("feature").getFloatArray();
      int count = 0;
      for(int x=0;x<sizeX;x++){
        for(int y=0;y<sizeY;y++) {
          RGBContainer rgbContainer = ColorConverter.LabtoRGB(new ReadableLabContainer(arr[count], arr[count + 1], arr[count + 2]));
          int color = rgbContainer.toIntColor();
          pixels[y][x][0] = ReadableRGBContainer.getRed(color);
          pixels[y][x][1] = ReadableRGBContainer.getGreen(color);
          pixels[y][x][2] = ReadableRGBContainer.getBlue(color);
          count+=3;
        }
      }
    }
    return pixels;
  }

  public static int[][][] shotToRGB(float[] featureData, int sizeX, int sizeY){
    int[][][] pixels = new int[sizeX][sizeY][3];

    int count = 0;
    for(int x=0;x<sizeX;x++){
      for(int y=0;y<sizeY;y++) {
        RGBContainer rgbContainer = ColorConverter.LabtoRGB(new ReadableLabContainer(featureData[count], featureData[count + 1], featureData[count + 2]));
        int color = rgbContainer.toIntColor();
        pixels[y][x][0] = ReadableRGBContainer.getRed(color);
        pixels[y][x][1] = ReadableRGBContainer.getGreen(color);
        pixels[y][x][2] = ReadableRGBContainer.getBlue(color);
        count+=3;
      }
    }
    return pixels;
  }

  public static int[][] shotToInt(String shotId, DBSelector selector, int sizeX, int sizeY){
    java.util.List<Map<String, PrimitiveTypeProvider>> result = selector.getRows("id", shotId);

    int[][] pixels = new int[sizeX][sizeY];

    for (Map<String, PrimitiveTypeProvider> row : result) {
      float[] arr = row.get("feature").getFloatArray();
      int count = 0;
      for(int x=0;x<sizeX;x++){
        for(int y=0;y<sizeY;y++) {
          RGBContainer rgbContainer = ColorConverter.LabtoRGB(new ReadableLabContainer(arr[count], arr[count + 1], arr[count + 2]));
          pixels[y][x] = rgbContainer.toIntColor();
          count+=3;
        }
      }
    }
    return pixels;
  }

  public static int[][] shotToInt(float[] featureData, int sizeX, int sizeY){
    int[][] pixels = new int[sizeX][sizeY];

    int count = 0;
    for(int x=0;x<sizeX;x++) {
      for (int y = 0; y < sizeY; y++) {
        RGBContainer rgbContainer = ColorConverter.LabtoRGB(new ReadableLabContainer(featureData[count], featureData[count + 1], featureData[count + 2]));
        pixels[y][x] = rgbContainer.toIntColor();
        count += 3;
      }
    }
    return pixels;
  }

  public static List<Map<String, PrimitiveTypeProvider>> getFeatureData(DBSelector selector, String multimediaobjectId){
    MediaSegmentReader mediaSegmentReader = new MediaSegmentReader();
    List<MediaSegmentDescriptor> segments = mediaSegmentReader.lookUpSegmentsOfObject(multimediaobjectId);
    Collections.sort(segments, new SegmentDescriptorComparator());
    List<String> segmentIds = new ArrayList<>();
    Map<String, Integer> sequenceMapping = new HashMap<>();
    for(MediaSegmentDescriptor segment: segments){
      segmentIds.add(segment.getSegmentId());
      sequenceMapping.put(segment.getSegmentId(), segment.getSequenceNumber());
    }
    List<Map<String, PrimitiveTypeProvider>> featureData = selector.getRows("id", segmentIds.toArray(new String[segmentIds.size()]));

    //sort by sequence number
    @SuppressWarnings("unchecked")
    Map<String, PrimitiveTypeProvider>[] featureDataSorted = new HashMap[featureData.size()];
    for(Map<String, PrimitiveTypeProvider> entry: featureData){
      featureDataSorted[sequenceMapping.get(entry.get("id").getString()) - 1] = entry;
    }

    return Arrays.asList(featureDataSorted);
  }

  public static List<Map<String, PrimitiveTypeProvider>> getFeatureData(DBSelector selector, List<String> segmentIds){
    MediaSegmentReader sl = new MediaSegmentReader();
    Map<String, MediaSegmentDescriptor> segments = sl.lookUpSegments(segmentIds);
    sl.close();
    Map<String, Integer> sequenceMapping = new HashMap<>();
    for (MediaSegmentDescriptor segment : segments.values()) {
      segmentIds.add(segment.getSegmentId());
      sequenceMapping.put(segment.getSegmentId(), segment.getSequenceNumber());
    }
    List<Map<String, PrimitiveTypeProvider>> featureData = selector.getRows("id", segmentIds.toArray(new String[segmentIds.size()]));

    return featureData;
  }



  public static int minDistance(int[][] colors, int len, int[] color){
    int smallest = 0;
    int minDist = 3*256;
    for(int x=0;x<len;x++){
      int dist = Math.abs(color[0] - colors[x][0]) + Math.abs(color[1] - colors[x][1]) + Math.abs(color[2] - colors[x][2]);
      if(dist < minDist){
        minDist = dist;
        smallest = x;
      }
    }
    return smallest;
  }

  public static int[][] createColorDistribution2(){
    int[][] colors = new int[36][3];
    for(int x=0;x<6;x++){
      for(int y=0;y<6;y++) {
        switch(x){
          case 0:
            colors[x*6+y][0] = 255;
            colors[x*6+y][1] = 255*y/6;
            break;
          case 1:
            colors[x*6+y][1] = 255;
            colors[x*6+y][0] = 255 - 255*y/6;
            break;
          case 2:
            colors[x*6+y][1] = 255;
            colors[x*6+y][2] = 255*y/6;
            break;
          case 3:
            colors[x*6+y][2] = 255;
            colors[x*6+y][1] = 255 - 255*y/6;
            break;
          case 4:
            colors[x*6+y][2] = 255;
            colors[x*6+y][0] = 255*y/6;
            break;
          case 5:
            colors[x*6+y][0] = 255;
            colors[x*6+y][2] = 255 - 255*y/6;
            break;
        }
      }
    }
    return colors;
  }

  public static JsonObject createStreamGraphData(int[][] data, int[][]colors, JsonObject graph){
    JsonArray graphColors = new JsonArray();
    JsonArray signals = new JsonArray();
    int num = 0;
    for(int x=0;x<data[0].length;x++){
      JsonArray signal = new JsonArray();
      int count = 0;
      for(int y=0;y<data.length;y++){
        signal.add(data[y][x]*500 + 1);
        count += data[y][x]*500 + 1;
      }
      if(count > 0) {
        signals.add(signal);
        graphColors.add("rgb(" + colors[x][0] + "," + colors[x][1] + "," + colors[x][2] + ")");
        num++;
      }
    }
    graph.add("colors", graphColors);
    graph.add("data", signals);
    return graph;
  }

  public static int[][][] createColorDistribution3(){
    int[][][] colors = new int[3][216][3];
    for(int x=0;x<6;x++){
      if (x == 0 || x == 1 || x == 5) {
        colors[0][x][0] = 255;
      }
      if (x >= 1 && x <= 3) {
        colors[0][x][1] = 255;
      }
      if (x >= 3 && x <= 5) {
        colors[0][x][2] = 255;
      }
      for(int y=0;y<6;y++) {
        int pos = x*6+y+3;
        if(pos > 35){
          pos -= 36;
        }
        switch(x){
          case 0:
            colors[1][pos][0] = 255;
            colors[1][pos][1] = 255*y/6;
            for(int z=0;z<6;z++){
              int pos2 =  x*36+y*6+z+18;
              if(pos2 > 215){
                pos2 -= 216;
              }
              colors[2][pos2][0] = 255;
              colors[2][pos2][1] = 255*y/6 + 255*z/36;
            }
            break;
          case 1:
            colors[1][pos][1] = 255;
            colors[1][pos][0] = 255 - 255*(y-3)/6 - 127;
            for(int z=0;z<6;z++){
              int pos2 =  x*36+y*6+z+18;
              if(pos2 > 215){
                pos2 -= 216;
              }
              colors[2][pos2][1] = 255;
              colors[2][pos2][0] = 255 - 255*(y-3)/6 - 255*z/36 - 127;
            }
            break;
          case 2:
            colors[1][pos][1] = 255;
            colors[1][pos][2] = 255*(y-3)/6 + 127;
            for(int z=0;z<6;z++){
              int pos2 =  x*36+y*6+z+18;
              if(pos2 > 215){
                pos2 -= 216;
              }
              colors[2][pos2][1] = 255;
              colors[2][pos2][2] = 255*(y-3)/6 + 255*z/36 + 127;
            }
            break;
          case 3:
            colors[1][pos][2] = 255;
            colors[1][pos][1] = 255 - 255*(y-3)/6 - 127;
            for(int z=0;z<6;z++){
              int pos2 =  x*36+y*6+z+18;
              if(pos2 > 215){
                pos2 -= 216;
              }
              colors[2][pos2][2] = 255;
              colors[2][pos2][1] = 255 - 255*(y-3)/6 - 255*z/36 - 127;
            }
            break;
          case 4:
            colors[1][pos][2] = 255;
            colors[1][pos][0] = 255*(y-3)/6 + 127;
            for(int z=0;z<6;z++){
              int pos2 =  x*36+y*6+z+18;
              if(pos2 > 215){
                pos2 -= 216;
              }
              colors[2][pos2][2] = 255;
              colors[2][pos2][0] = 255*(y-3)/6 + 255*z/36 + 127;
            }
            break;
          case 5:
            colors[1][pos][0] = 255;
            colors[1][pos][2] = 255 - 255*(y-3)/6 - 127;
            for(int z=0;z<6;z++){
              int pos2 =  x*36+y*6+z+18;
              if(pos2 > 215){
                pos2 -= 216;
              }
              colors[2][pos2][0] = 255;
              colors[2][pos2][2] = 255 - 255*(y-3)/6 - 255*z/36 - 127;
            }
            break;
        }
      }
    }
    return colors;
  }

  public static JsonArray getSunburstChildren(int[][] data, int[][][] colors, int step, int offset){
    JsonArray sub = new JsonArray();

    for(int x=offset;x<6+offset;x++){
      JsonObject sub1 = new JsonObject();
      sub1.add("name", "range" + step + "-" + x);
      sub1.add("size", data[step][x]);
      int[] color = colors[step][x];
      sub1.add("color", color[0] + "," + color[1] + "," + color[2]);
      if(step + 1 < data.length){
        //children
        sub1.add("children", getSunburstChildren(data, colors, step + 1, x*6));
      }
      else{
        //no children
      }
      if(data[step][x] > 0) {
        sub.add(sub1);
      }
    }

    return sub;
  }
}
