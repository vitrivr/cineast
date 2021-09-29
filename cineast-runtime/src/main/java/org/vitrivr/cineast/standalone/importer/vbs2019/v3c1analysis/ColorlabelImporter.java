package org.vitrivr.cineast.standalone.importer.vbs2019.v3c1analysis;

import java.io.IOException;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Optional;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.LineIterator;
import org.vitrivr.cineast.core.data.entities.MediaSegmentMetadataDescriptor;
import org.vitrivr.cineast.core.data.providers.primitive.PrimitiveTypeProvider;
import org.vitrivr.cineast.core.importer.Importer;

/**
 * Imports the colorlabels as given by the https://github.com/klschoef/V3C1Analysis repo. File is expected to be in format:
 *
 * every line contains the segmentid formatted xxxxx_yyyy, where before the _ is 0-padded to length 5 the movie id and behind the _ the non-0 padded segmentID
 */
public class ColorlabelImporter implements Importer<Map<String, PrimitiveTypeProvider>> {

  private final String label;
  private LineIterator lineIterator = null;


  public ColorlabelImporter(Path input, String label) throws IOException {
    this.label = label;
    lineIterator = FileUtils.lineIterator(input.toFile());
    if (!lineIterator.hasNext()) {
      throw new IOException("Empty file");
    }
  }

  private synchronized Optional<Map<String, PrimitiveTypeProvider>> nextPair() {
    if (!lineIterator.hasNext()) {
      return Optional.empty();
    }
    String id = lineIterator.next();
    Map<String, PrimitiveTypeProvider> _return = new HashMap<>();
    _return.put(MediaSegmentMetadataDescriptor.FIELDNAMES[0], PrimitiveTypeProvider.fromObject("v_"+id));
    _return.put(MediaSegmentMetadataDescriptor.FIELDNAMES[1], PrimitiveTypeProvider.fromObject("v3c1"));
    _return.put(MediaSegmentMetadataDescriptor.FIELDNAMES[2], PrimitiveTypeProvider.fromObject("colorlabels"));
    _return.put(MediaSegmentMetadataDescriptor.FIELDNAMES[3], PrimitiveTypeProvider.fromObject(this.label));
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
