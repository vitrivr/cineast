package org.vitrivr.cineast.art.modules.visualization;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.vitrivr.cineast.core.config.Config;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Paths;

/**
 * Created by sein on 15.09.16.
 */
public class VisualizationCache {
  private final String cachePath = Config.getVisualizationConfig().getVisualizationCachePath();
  private final boolean cacheEnabled = Config.getVisualizationConfig().getCacheEnabled();

  public VisualizationCache(){
    if(!cacheEnabled){
      return;
    }
    File cache = new File(cachePath);
    cache.mkdirs();
  }

  private static Logger LOGGER = LogManager.getLogger();

  public String cacheResult(String visualization, VisualizationType visualizationType, String objectId, String data){
    if(!cacheEnabled){
      return data;
    }
    File visualizationDir = new File(cachePath + "/" + visualizationType.toString());
    visualizationDir.mkdirs();
    File cache = new File(visualizationDir.getAbsolutePath() + "/" + objectId + "-" + visualization + ".cache");
    try {
      PrintWriter out = new PrintWriter(cache);
      out.println(data);
      out.close();
      LOGGER.debug("Cached visualization for '{}-{}-{}' ", visualization, visualizationType.toString(), objectId);
    } catch (FileNotFoundException e) {
      LOGGER.warn("Failed to cache '{}'", cache.getAbsoluteFile());
      e.printStackTrace();
    }
    return data;
  }

  public String getFromCache(String visualization, VisualizationType visualizationType, String objectId){
    if(!cacheEnabled){
      return null;
    }
    File visualizationDir = new File(cachePath + "/" + visualizationType.toString());
    visualizationDir.mkdirs();
    String filePath = visualizationDir.getAbsolutePath() + "/" + objectId + "-" + visualization + ".cache";
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
