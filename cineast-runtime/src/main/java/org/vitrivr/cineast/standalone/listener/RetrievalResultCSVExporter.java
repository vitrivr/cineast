package org.vitrivr.cineast.standalone.listener;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.vitrivr.cineast.core.config.DatabaseConfig;
import org.vitrivr.cineast.core.config.ReadableQueryConfig;
import org.vitrivr.cineast.core.data.entities.MediaObjectDescriptor;
import org.vitrivr.cineast.core.data.entities.MediaSegmentDescriptor;
import org.vitrivr.cineast.core.data.score.ScoreElement;
import org.vitrivr.cineast.core.db.dao.reader.MediaObjectReader;
import org.vitrivr.cineast.core.db.dao.reader.MediaSegmentReader;
import org.vitrivr.cineast.core.util.LogHelper;
import org.vitrivr.cineast.standalone.runtime.RetrievalTask;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.*;

public class RetrievalResultCSVExporter implements RetrievalResultListener {

  private static File baseFolder = new File("retrieval_results"); // TODO make configurable
  private static final Logger LOGGER = LogManager.getLogger();

  private final MediaSegmentReader mediaSegmentReader;
  private final MediaObjectReader mediaObjectReader;

  public RetrievalResultCSVExporter(DatabaseConfig databaseConfig){
    mediaSegmentReader = new MediaSegmentReader(databaseConfig.getSelectorSupplier().get());
    mediaObjectReader = new MediaObjectReader(databaseConfig.getSelectorSupplier().get());
  }

  @Override
  public void notify(List<ScoreElement> resultList, RetrievalTask task) {
    ReadableQueryConfig qc = task.getConfig();
    String queryIdString;
    if (qc == null || qc.getQueryId() == null) {
      LOGGER.error("could not determine query id, using 'null'");
      queryIdString = "null";
    } else {
      queryIdString = qc.getQueryId().toString();
    }

    String filename = task.getRetriever().getClass().getSimpleName() + ".csv";
    File outFolder = new File(baseFolder, queryIdString);
    outFolder.mkdirs();
    File out = new File(outFolder, filename);


    ArrayList<String> ids = new ArrayList<>(resultList.size());
    for(ScoreElement e : resultList){
      ids.add(e.getId());
    }
    
    Map<String, MediaSegmentDescriptor> segments = mediaSegmentReader.lookUpSegments(ids);
    Set<String> objectIds = new HashSet<>();
    
    for(MediaSegmentDescriptor sd : segments.values()){
      objectIds.add(sd.getObjectId());
    }
    
    Map<String, MediaObjectDescriptor> objects = mediaObjectReader.lookUpObjects(objectIds);
    
    try (PrintWriter writer = new PrintWriter(out)) {
      
      //header
      writer.println("\"rank\", \"id\", \"score\", \"path\"");
      
      int rank = 1;
      for (ScoreElement e : resultList) {
        writer.print(rank++);
        writer.print(',');
        writer.print(e.getId());
        writer.print(',');
        writer.print(e.getScore());
        writer.print(',');
        writer.print('"');
        writer.print(
            objects.get(segments.get(e.getId()).getObjectId()).getPath().replace('\\', '/'));
        writer.println('"');
      }
      writer.flush();
    } catch (FileNotFoundException e) {
      LOGGER.error("could not write file '{}': {}", out.getAbsolutePath(), LogHelper.getStackTrace(e));
    }

  }
}
