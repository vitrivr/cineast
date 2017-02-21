package org.vitrivr.cineast.art.modules.visualization;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.vitrivr.cineast.core.config.Config;

/**
 * Created by sein on 15.09.16.
 */
public class VisualizationCache {
  private static final String cachePath = Config.sharedConfig().getVisualization().getCachePath();
  private static final boolean cacheEnabled = Config.sharedConfig().getVisualization().isCacheEnabled();

  private static Logger LOGGER = LogManager.getLogger();

  static {
    if(cacheEnabled){
      File cache = new File(cachePath);
      cache.mkdirs();
    }
  }

  public static String cacheResult(String visualization, VisualizationType visualizationType, List<String> objectIds, String data){
    if(!cacheEnabled){
      return data;
    }
    File visualizationDir = new File(cachePath + "/" + visualizationType.toString());
    visualizationDir.mkdirs();
    File cache = new File(visualizationDir.getAbsolutePath() + "/" + String.join("-", objectIds.toArray(new String[objectIds.size()])) + "-" + visualization + ".cache");
    try {
      PrintWriter out = new PrintWriter(cache);
      out.println(data);
      out.close();
      LOGGER.debug("Cached visualization for '{}-{}-{}' ", visualization, visualizationType.toString(), objectIds);
    } catch (FileNotFoundException e) {
      LOGGER.warn("Failed to cache '{}'", cache.getAbsoluteFile());
      e.printStackTrace();
    }
    return data;
  }

  public static String getFromCache(String visualization, VisualizationType visualizationType, List<String> objectIds){
    if(!cacheEnabled){
      return null;
    }
    File visualizationDir = new File(cachePath + "/" + visualizationType.toString());
    visualizationDir.mkdirs();
    String filePath = visualizationDir.getAbsolutePath() + "/" + String.join("-", objectIds.toArray(new String[objectIds.size()])) + "-" + visualization + ".cache";
    File cache = new File(filePath);
    String data = null;
    if(cache.exists()){
      try {
        data = new String(Files.readAllBytes(Paths.get(filePath)));
      } catch (IOException e) {
        e.printStackTrace();
      }
    }
    return data;
  }
}
