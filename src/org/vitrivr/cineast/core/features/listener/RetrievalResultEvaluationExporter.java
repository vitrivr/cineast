package org.vitrivr.cineast.core.features.listener;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.CopyOption;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.vitrivr.cineast.core.config.Config;
import org.vitrivr.cineast.core.config.ReadableQueryConfig;
import org.vitrivr.cineast.core.data.StringDoublePair;
import org.vitrivr.cineast.core.data.entities.MultimediaObjectDescriptor;
import org.vitrivr.cineast.core.data.entities.SegmentDescriptor;
import org.vitrivr.cineast.core.db.dao.reader.MultimediaObjectLookup;
import org.vitrivr.cineast.core.db.dao.reader.SegmentLookup;
import org.vitrivr.cineast.core.runtime.RetrievalTask;

public class RetrievalResultEvaluationExporter implements RetrievalResultListener {

  private static File baseFolder = new File("retrieval_results"); // TODO make configurable
  private static File queryImageLocation = new File(
      Config.sharedConfig().getExtractor().getOutputLocation(), "queryImages");
  private static File collectionLocation = new File(".");
  private static final Logger LOGGER = LogManager.getLogger();

  private static final CopyOption[] COPY_OPTIONS = new CopyOption[] {
      StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.COPY_ATTRIBUTES };

  @Override
  public void notify(List<StringDoublePair> resultList, RetrievalTask task) {
    ReadableQueryConfig qc = task.getConfig();
    String queryIdString;
    
    ArrayList<String> ids = new ArrayList<>(resultList.size() + 1);
    
    if (qc == null || qc.getQueryId() == null) {
      LOGGER.error("could not determine query id, using 'null'");
      queryIdString = "null";
    } else {
      queryIdString = qc.getQueryId().toString();
    }

    String folderName = task.getRetriever().getClass().getSimpleName();
    File queryFolder = new File(baseFolder, queryIdString);
    File outFolder = new File(queryFolder, folderName);
    File dataFolder = new File(outFolder, "data");

    dataFolder.mkdirs();
    File out = new File(outFolder, "meta.json");

    SegmentLookup sl = new SegmentLookup();
    
    
    for(StringDoublePair sdp : resultList){
      ids.add(sdp.key);
    }
    
    if(task.getSegmentId() != null){
      ids.add(task.getSegmentId());
    }
    
    Map<String, SegmentDescriptor> segments = sl.lookUpSegments(ids);
    Set<String> objectIds = new HashSet<>();
    
    for(SegmentDescriptor sd : segments.values()){
      objectIds.add(sd.getObjectId());
    }
    
    MultimediaObjectLookup ol = new MultimediaObjectLookup();
    Map<String, MultimediaObjectDescriptor> objects = ol.lookUpObjects(objectIds);

    try {
      PrintWriter writer = new PrintWriter(out);
      PrintWriter missing = new PrintWriter(new File(outFolder, "missing.txt"));

      writer.println('{');

      if (task.getSegmentId() == null) {
        //TODO
        LOGGER.error("External media input not yet supported");
        writer.close();
        return;
      } else {
        SegmentDescriptor segment = sl.lookUpSegment(task.getSegmentId());
        MultimediaObjectDescriptor mmobject = ol.lookUpObjectById(segment.getObjectId());
        
        String path = objects.get(
            segments.get(
                task.getSegmentId()
                ).getObjectId()
            ).getPath()
            .replace('\\', '/');

//        try {
//          Files.copy((new File(collectionLocation, path)).toPath(),
//              (new File(dataFolder, path)).toPath(), COPY_OPTIONS);
//        } catch (IOException e) {
          missing.println(path);
//        }
        
        writer.print("\"queryObject\":");
        writer.println("\"" + path + "\",");

      }

      writer.println("\"resultSet\":[");

      int rank = 1;
      Iterator<StringDoublePair> iter = resultList.iterator();
      while (iter.hasNext()) {

        writer.println('{');

        StringDoublePair sdp = iter.next();

       
        String path = objects.get(segments.get(sdp.key).getObjectId()).getPath().replace('\\', '/');

//        try {
//          Files.copy((new File(collectionLocation, path)).toPath(),
//              (new File(dataFolder, path)).toPath(), COPY_OPTIONS);
//        } catch (IOException e) {
          missing.println(path);
//        }

        writer.print("\"mediaObject\":");
        writer.print("\"" + path + "\"");
        writer.println(',');

        writer.print("\"score\":");
        writer.print(sdp.value);
        writer.println(',');

        writer.print("\"rank\":");
        writer.println(rank++);

        if (iter.hasNext()) {
          writer.println("},");
        } else {
          writer.println('}');
        }
      }

      writer.println(']');

      writer.println('}');
      writer.flush();
      writer.close();
      
      missing.flush();
      missing.close();

    } catch (FileNotFoundException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }

    sl.close();
    ol.close();

  }

}
