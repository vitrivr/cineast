package org.vitrivr.cineast.core.metadata;

import com.drew.imaging.ImageMetadataReader;
import com.drew.imaging.ImageProcessingException;
import com.drew.metadata.Metadata;
import com.drew.metadata.exif.ExifSubIFDDirectory;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.vitrivr.cineast.core.data.entities.MultimediaMetadataDescriptor;
import org.vitrivr.cineast.core.util.LogHelper;

import java.io.IOException;
import java.nio.file.Path;
import java.util.*;

/**
 * @author rgasser
 * @version 1.0
 * @created 25.01.17
 */
public class EXIFMetadataExtractor implements MetadataExtractor {

    private static final Logger LOGGER = LogManager.getLogger();


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
     * Extracts the metadata from the specified path and returns a List of MultimediaMetadataDescriptor objects
     * (one for each metadata entry).
     *
     * @param objectId ID of the multimedia object for which metadata will be generated.
     * @param path Path to the file for which metadata should be extracted.
     * @return List of MultimediaMetadataDescriptors. The list may be empty but must always be returned!
     */
    @Override
    public List<MultimediaMetadataDescriptor> extract(String objectId, Path path) {
        ArrayList<MultimediaMetadataDescriptor> list = new ArrayList<>();
        try {
            Metadata metadata = ImageMetadataReader.readMetadata(path.toFile());
            ExifSubIFDDirectory directory = metadata.getFirstDirectoryOfType(ExifSubIFDDirectory.class);
            if (directory != null) {
                for (Map.Entry<String, Integer> entry : FIELDS.entrySet()) {
                    Object obj = directory.getObject(entry.getValue());
                    if (obj != null) {
                        list.add(MultimediaMetadataDescriptor.newMultimediaMetadataDescriptor(objectId, this.domain(), entry.getKey(), obj));
                    }
                }
            }
        } catch (ImageProcessingException | IOException e) {
            LOGGER.fatal("Could not extract metadata for object {} (file: {}) due to a serious error.", LogHelper.getStackTrace(e));
        }

        return list;
    }

    /**
     * @return
     */
    @Override
    public String domain() {
        return "EXIF";
    }

}
