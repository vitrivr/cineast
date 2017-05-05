package org.vitrivr.cineast.core.metadata;

import com.drew.metadata.exif.ExifSubIFDDirectory;
import com.google.common.collect.Maps;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.vitrivr.cineast.core.data.entities.MultimediaMetadataDescriptor;
import org.vitrivr.cineast.core.util.MetadataUtil;

/**
 * @author rgasser
 * @version 1.0
 * @created 25.01.17
 */
public class EXIFMetadataExtractor implements MetadataExtractor {
  /** Named key's that will be extracted from the EXIF metadata (if available). */
  private static final HashMap<String, Integer> FIELDS = new HashMap<>();

  static {
    FIELDS.put("Copyright", ExifSubIFDDirectory.TAG_COPYRIGHT);
    FIELDS.put("Datetime original", ExifSubIFDDirectory.TAG_DATETIME_ORIGINAL);
    FIELDS.put("Datetime digitized", ExifSubIFDDirectory.TAG_DATETIME_DIGITIZED);
    FIELDS.put("Height", ExifSubIFDDirectory.TAG_IMAGE_HEIGHT);
    FIELDS.put("Width", ExifSubIFDDirectory.TAG_IMAGE_WIDTH);
    FIELDS.put("Author", ExifSubIFDDirectory.TAG_WIN_AUTHOR);
    FIELDS.put("Keywords", ExifSubIFDDirectory.TAG_WIN_KEYWORDS);
    FIELDS.put("Subject", ExifSubIFDDirectory.TAG_WIN_SUBJECT);
    FIELDS.put("Title", ExifSubIFDDirectory.TAG_WIN_TITLE);
    FIELDS.put("Comment", ExifSubIFDDirectory.TAG_USER_COMMENT);
  }

  /**
   * Extracts the metadata from the specified path and returns a List of
   * MultimediaMetadataDescriptor objects (one for each metadata entry).
   *
   * @param objectId ID of the multimedia object for which metadata will be generated.
   * @param path Path to the file for which metadata should be extracted.
   * @return List of MultimediaMetadataDescriptors. The list may be empty but must always be
   *         returned!
   */
  @Override
  public List<MultimediaMetadataDescriptor> extract(String objectId, Path path) {
    return MetadataUtil
        .getMetadataDirectoryOfType(path, ExifSubIFDDirectory.class)
        .map(ifdDirectory -> Maps.transformValues(FIELDS, tag -> ifdDirectory.getObject(tag))
            .entrySet().stream()
            .filter(e -> e.getValue() != null)
            .map(e -> MultimediaMetadataDescriptor
                .of(objectId, this.domain(), e.getKey(), e.getValue()))
        ).orElse(Stream.empty()).collect(Collectors.toList());
  }

  @Override
  public String domain() {
    return "EXIF";
  }
}
