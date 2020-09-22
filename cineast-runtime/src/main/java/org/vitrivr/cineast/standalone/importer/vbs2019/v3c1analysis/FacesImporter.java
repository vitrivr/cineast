package org.vitrivr.cineast.standalone.importer.vbs2019.v3c1analysis;

import java.io.IOException;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Optional;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.LineIterator;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.vitrivr.cineast.core.data.entities.MediaSegmentMetadataDescriptor;
import org.vitrivr.cineast.core.data.providers.primitive.PrimitiveTypeProvider;
import org.vitrivr.cineast.core.importer.Importer;

/**
 * Imports the faces as given by the https://github.com/klschoef/V3C1Analysis repo. File is expected to have a meaningless header
 */
public class FacesImporter implements Importer<Map<String, PrimitiveTypeProvider>> {

  private static final Logger LOGGER = LogManager.getLogger();
  private final LineIterator lineIterator;
  private final SequenceIdLookupService lookupService;
  private final int noFaces;


  public FacesImporter(Path input, int noFaces, SequenceIdLookupService lookupService) throws IOException {
    this.noFaces = noFaces;
    lineIterator = FileUtils.lineIterator(input.toFile());
    this.lookupService = lookupService;
    if (!lineIterator.hasNext()) {
      throw new IOException("Empty file");
    }
    // skip header
    String header = lineIterator.next();
  }

  private synchronized Optional<Map<String, PrimitiveTypeProvider>> nextPair() {
    if (!lineIterator.hasNext()) {
      return Optional.empty();
    }
    String id = lineIterator.next();
    String videoID = id.split("/")[0];
    int frameNo = Integer.parseInt(id.split("_")[1]);
    int segmentID = lookupService.getSequenceNumber(videoID, frameNo);
    Map<String, PrimitiveTypeProvider> _return = new HashMap<>();
    _return.put(MediaSegmentMetadataDescriptor.FIELDNAMES[0], PrimitiveTypeProvider.fromObject("v_" + videoID + "_" + segmentID));
    _return.put(MediaSegmentMetadataDescriptor.FIELDNAMES[1], PrimitiveTypeProvider.fromObject("v3c1"));
    _return.put(MediaSegmentMetadataDescriptor.FIELDNAMES[2], PrimitiveTypeProvider.fromObject("faces"));
    _return.put(MediaSegmentMetadataDescriptor.FIELDNAMES[3], PrimitiveTypeProvider.fromObject(noFaces));
    return Optional.of(_return);
  }

  @Override
  public Map<String, PrimitiveTypeProvider> readNext() {
    try {
      Optional<Map<String, PrimitiveTypeProvider>> node = nextPair();
      return node.orElse(null);
    } catch (NoSuchElementException e) {
      return null;
    }
  }

  @Override
  public Map<String, PrimitiveTypeProvider> convert(Map<String, PrimitiveTypeProvider> data) {
    return data;
  }
}
