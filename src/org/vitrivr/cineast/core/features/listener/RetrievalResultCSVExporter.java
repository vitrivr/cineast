package org.vitrivr.cineast.core.features.listener;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.vitrivr.cineast.core.config.ReadableQueryConfig;
import org.vitrivr.cineast.core.data.StringDoublePair;
import org.vitrivr.cineast.core.runtime.RetrievalTask;
import org.vitrivr.cineast.core.util.LogHelper;

public class RetrievalResultCSVExporter implements RetrievalResultListener {

  private static File baseFolder = new File("retrieval_results"); // TODO make configurable
  private static final Logger LOGGER = LogManager.getLogger();

  @Override
  public void notify(List<StringDoublePair> resultList, RetrievalTask task) {
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

    try (PrintWriter writer = new PrintWriter(out)) {
      for(StringDoublePair sdp : resultList){
        writer.print(sdp.key);
        writer.print(',');
        writer.println(sdp.value);
      }
      writer.flush();
    } catch (FileNotFoundException e) {
      LOGGER.error("could not write file '{}': {}", out.getAbsolutePath(), LogHelper.getStackTrace(e));
    }

  }

}
