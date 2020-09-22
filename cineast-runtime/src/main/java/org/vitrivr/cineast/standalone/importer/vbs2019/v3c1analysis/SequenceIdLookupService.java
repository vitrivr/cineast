package org.vitrivr.cineast.standalone.importer.vbs2019.v3c1analysis;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.commons.io.FileUtils;

/**
 * Reads the msb-allshots.txt file from the V3C1 Metadata Repo and provides an easy-to-use lookup which takes the framenumber and returns the 0-based sequence number. We assume that sequences are sequentially ordered by file
 */
public class SequenceIdLookupService {

  private Map<String, List<Integer>> lookup = new HashMap<>();

  public SequenceIdLookupService(Path input) throws IOException {
    List<String> lines = FileUtils.readLines(input.toFile(), Charset.defaultCharset());
    //skip header
    lines.forEach(line -> {
      if (line.startsWith("video")) {
        return;
      }
      String[] split = line.split(";");
      String videoID = split[0].split("\\.")[0];
      lookup.putIfAbsent(videoID, new ArrayList<>());
      int start = Integer.parseInt(split[1]);
      lookup.get(videoID).add(start);
    });
    //order all lists
    lookup.values().forEach(list -> list.sort(Integer::compare));
  }

  public int getSequenceNumber(String movieID, int frame) {
    List<Integer> list = lookup.get(movieID);
    for (int i = 0; i < list.size(); i++) {
      if (list.get(i) > frame) {
        return i - 1;
      }
    }
    return list.size() - 1;
  }
}
