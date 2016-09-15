package org.vitrivr.cineast.core.util;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.vitrivr.cineast.art.modules.visualization.SegmentDescriptorComparator;
import org.vitrivr.cineast.core.color.ColorConverter;
import org.vitrivr.cineast.core.color.RGBContainer;
import org.vitrivr.cineast.core.color.ReadableLabContainer;
import org.vitrivr.cineast.core.data.providers.primitive.PrimitiveTypeProvider;
import org.vitrivr.cineast.core.db.DBSelector;
import org.vitrivr.cineast.core.db.SegmentLookup;

import java.util.*;

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
          pixels[y][x][0] = rgbContainer.getRed(color);
          pixels[y][x][1] = rgbContainer.getGreen(color);
          pixels[y][x][2] = rgbContainer.getBlue(color);
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
        pixels[y][x][0] = rgbContainer.getRed(color);
        pixels[y][x][1] = rgbContainer.getGreen(color);
        pixels[y][x][2] = rgbContainer.getBlue(color);
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
    SegmentLookup segmentLookup = new SegmentLookup();
    List<SegmentLookup.SegmentDescriptor> segments = segmentLookup.lookUpAllSegments(multimediaobjectId);
    Collections.sort(segments, new SegmentDescriptorComparator());
    List<String> segmentIds = new ArrayList();
    Map<String, Integer> sequenceMapping = new HashMap();
    for(SegmentLookup.SegmentDescriptor segment: segments){
      segmentIds.add(segment.getSegmentId());
      sequenceMapping.put(segment.getSegmentId(), segment.getSequenceNumber());
    }
    List<Map<String, PrimitiveTypeProvider>> featureData = selector.getRows("id", segmentIds.toArray(new String[segmentIds.size()]));

    //sort by sequence number
    Map<String, PrimitiveTypeProvider>[] featureDataSorted = new HashMap[featureData.size()];
    for(Map<String, PrimitiveTypeProvider> entry: featureData){
      featureDataSorted[sequenceMapping.get(entry.get("id").getString()) - 1] = entry;
    }

    return Arrays.asList(featureDataSorted);
  }
}
