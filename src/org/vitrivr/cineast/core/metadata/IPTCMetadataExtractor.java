package org.vitrivr.cineast.core.metadata;

import com.twelvemonkeys.imageio.metadata.Directory;
import com.twelvemonkeys.imageio.metadata.Entry;
import com.twelvemonkeys.imageio.metadata.iptc.IPTCReader;

import javax.imageio.ImageIO;
import javax.imageio.stream.ImageInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * @author rgasser
 * @version 1.0
 * @created 25.01.17
 */
public class IPTCMetadataExtractor implements MetadataExtractor {

    /**
     *
     */
    private static String[] KEYS = {
            "test"
    };

    @Override
    public Map<String,String> extract(Path path) throws IOException {
        IPTCReader reader = new IPTCReader();
        ImageInputStream stream = ImageIO.createImageInputStream(Files.newInputStream(path));
        Directory directory = reader.read(stream);

        Map<String, String> metadata = new HashMap<>();
        for (String key : KEYS) {
            Entry entry = directory.getEntryByFieldName(key);
            if (entry != null) {
                metadata.put(key, entry.getValueAsString());
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
        return null;
    }
}
