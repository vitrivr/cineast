package org.vitrivr.cineast.core.metadata;

import com.twelvemonkeys.imageio.metadata.Directory;
import com.twelvemonkeys.imageio.metadata.Entry;
import com.twelvemonkeys.imageio.metadata.exif.EXIFReader;
import com.twelvemonkeys.imageio.metadata.jpeg.JPEG;
import com.twelvemonkeys.imageio.metadata.jpeg.JPEGSegment;
import com.twelvemonkeys.imageio.metadata.jpeg.JPEGSegmentUtil;

import javax.activation.MimetypesFileTypeMap;

import javax.imageio.ImageIO;
import javax.imageio.stream.ImageInputStream;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

/**
 * @author rgasser
 * @version 1.0
 * @created 25.01.17
 */
public class EXIFMetadataExtractor implements MetadataExtractor {

    /** Named key's that will be extracted from the metadata (if available). */
    private static final String[] KEYS = {
            "Make",
            "Model",
            "PixelXDimension",
            "PixelYDimension",
            "DateTimeOriginal",
            "Artist",
            "UserComment",
            "ImageDescription"
    };

    /** */
    private static final String PREFIX = "EXIF";

    /** Set containing mime-types of all files supported by this metadata extractor. */
    private static final Set<String> SUPPORTED = new HashSet<>();


    static {
        SUPPORTED.add("image/jpeg");
        SUPPORTED.add("image/tiff");
    }

    /** Instance of MimetypeFileTypeMap used to determine the mime-type of a file. */
    private final MimetypesFileTypeMap map = new MimetypesFileTypeMap();

    /** EXIF Reader instance used to read the EXIF metadata from images. */
    private final EXIFReader reader = new EXIFReader();



    @Override
    public Map<String,String> extract(Path path) throws IOException {

        String mimetype = this.map.getContentType(path.toString());
        HashMap<String, String> metadata = new HashMap<>();
        Directory directory = null;

        switch (mimetype) {
            case "image/jpeg":
                directory = this.extractFromJpeg(path);
                break;
            case "image/tiff":
                directory = this.extractFromTiff(path);
                break;
        }

        if (directory == null) return metadata;

        for (String key : KEYS) {
            Entry entry = directory.getEntryByFieldName(key);
            if (entry != null) {
                metadata.put(PREFIX + ":" + entry.getFieldName(), entry.getValueAsString());
            }
        }

        return metadata;
    }

    /**
     * Returns a set of the mime/types of supported files.
     *
     * @return Set of the mime-type of file formats that are supported by the current Decoder instance.
     */
    @Override
    public Set<String> supportedFiles() {
        return SUPPORTED;
    }

    /**
     *
     * @param path
     * @return
     * @throws IOException
     */
    private Directory extractFromJpeg(Path path) throws IOException {

        ImageInputStream stream = ImageIO.createImageInputStream(Files.newInputStream(path));
        List<JPEGSegment> exifSegment = JPEGSegmentUtil.readSegments(stream, JPEG.APP1, "Exif");
        if (exifSegment.size() == 0) return null;
        InputStream exifData = exifSegment.get(0).data();
        exifData.read();

        Directory outerDir = this.reader.read(ImageIO.createImageInputStream(exifData));
        Directory exifDir = null;
        if (outerDir.getEntryByFieldName("EXIF").getValue() instanceof Directory) {
            exifDir = (Directory)outerDir.getEntryByFieldName("EXIF").getValue();
        }

        exifData.close();
        stream.close();
        return exifDir;
    }

    /**
     *
     * @param path
     * @return
     * @throws IOException
     */
    private Directory extractFromTiff(Path path) throws IOException {
        ImageInputStream stream = ImageIO.createImageInputStream(Files.newInputStream(path));
        Directory outerDir = this.reader.read(stream);
        Directory exifDir = null;
        if (outerDir.getEntryByFieldName("EXIF").getValue() instanceof Directory) {
            exifDir = (Directory)outerDir.getEntryByFieldName("EXIF").getValue();
        }
        stream.close();
        return exifDir;
    }

}
