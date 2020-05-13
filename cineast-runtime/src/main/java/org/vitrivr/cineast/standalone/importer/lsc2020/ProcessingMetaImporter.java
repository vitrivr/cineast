package org.vitrivr.cineast.standalone.importer.lsc2020;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.vitrivr.cineast.core.data.providers.primitive.PrimitiveTypeProvider;
import org.vitrivr.cineast.core.db.dao.reader.TagReader;
import org.vitrivr.cineast.core.importer.Importer;

import java.nio.file.Path;
import java.util.*;

public class ProcessingMetaImporter implements Importer<Map<String, PrimitiveTypeProvider>> {

    public static final List<Integer> TAG_CANDATES = Arrays.asList(LSCUtilities.META_SEMANTIC_COL, LSCUtilities.META_ACTIVITY_COL, LSCUtilities.META_TIMEZONE_COL);
    private static final Logger LOGGER = LogManager.getLogger(ProcessingMetaImporter.class);
    private final Type type;
    private Iterator<Map<String, PrimitiveTypeProvider>> currentDataIterator = null;
    private Iterator<String[]> iterator;
    private Map<String, String> minuteIdPathMap = new HashMap<>();

    public ProcessingMetaImporter(final Path path, final Type type) {
        this.type = type;
        minuteIdPathMap = LSCUtilities.getInstance().getMinuteIdPathMap();
        iterator = LSCUtilities.getInstance().getHeaderlessMetaContents().iterator();
        LOGGER.info("Finished setup of Importer. Importing now...");
    }

    private Optional<List<Map<String, PrimitiveTypeProvider>>> parseLine(String[] items) {
        String minuteId = items[LSCUtilities.META_MIN_COL];
        String path = minuteIdPathMap.get(minuteId);
        if (path == null) {
            // cannot resolve
            return Optional.empty();
        }
        path = LSCUtilities.cleanImagePath(path);
        path = LSCUtilities.pathToSegmentId(path);
        List<Map<String, PrimitiveTypeProvider>> list = new ArrayList<>();
        for (int i = 0; i < LSCUtilities.META_NAMES.length; i++) {
            if (items[i].equalsIgnoreCase("null")) {
                continue;
            }
            Optional<Map<String, PrimitiveTypeProvider>> data = parse(path, items, i);
            if (!data.isPresent()) {
                continue;
            } else {
                list.add(data.get());
            }
        }
        return Optional.of(list);
    }

    private Optional<Map<String, PrimitiveTypeProvider>> parse(String path, String[] items, int index) {
        // TODO more types
        switch (this.type) {
            case TAG:
                return parseTag(path, items, index);
            case TAG_LOOKUP:
                return parseTagForLookup(path, items, index);
            default:
                return Optional.empty();
        }
    }

    private Optional<Map<String, PrimitiveTypeProvider>> parseTag(String path, String[] items, int index) {
        if (TAG_CANDATES.contains(index)) {
            final HashMap<String, PrimitiveTypeProvider> map = new HashMap<>(3);
            map.put("id", PrimitiveTypeProvider.fromObject(path));
            map.put("tagid", PrimitiveTypeProvider.fromObject(items[index]));
            map.put("score", PrimitiveTypeProvider.fromObject(1));
            return Optional.of(map);
        } else {
            return Optional.empty();
        }
    }

    /**
     * Must only be called with index values for valid tags
     * tags: id === name, description is empty (no tag expansion done)
     */
    private Optional<Map<String, PrimitiveTypeProvider>> parseTagForLookup(String path, String[] items, int index) {
        final HashMap<String, PrimitiveTypeProvider> map = new HashMap<>(3);
        map.put(TagReader.TAG_ID_COLUMNNAME, PrimitiveTypeProvider.fromObject(path));
        map.put(TagReader.TAG_NAME_COLUMNNAME, PrimitiveTypeProvider.fromObject(items[index]));
        map.put(TagReader.TAG_DESCRIPTION_COLUMNNAME, PrimitiveTypeProvider.fromObject(""));
        return Optional.of(map);
    }

    @Override
    public Map<String, PrimitiveTypeProvider> readNext() {
        // Has to read new line
        do {
            if (this.currentDataIterator == null && this.iterator.hasNext()) {
                LOGGER.trace("Init / Next: dataIt==null && it.hasNext");
                Optional<List<Map<String, PrimitiveTypeProvider>>> parsed = parseLine(this.iterator.next());
                parsed.ifPresent(maps -> this.currentDataIterator = maps.iterator());
            }
            if (this.currentDataIterator != null && this.currentDataIterator.hasNext()) {
                LOGGER.trace("dataNext: dataIt.hasNext");
                Map<String, PrimitiveTypeProvider> out = this.currentDataIterator.next();
                if (!currentDataIterator.hasNext()) {
                    // reset, so Init / Next occurs
                    currentDataIterator = null;
                }
                LOGGER.trace("Returning metadata: {}", out);
                return out;
            }
        } while (this.currentDataIterator == null && this.iterator.hasNext());
        LOGGER.info("No more to read. Stopping");
        return null;
    }

    @Override
    public Map<String, PrimitiveTypeProvider> convert(Map<String, PrimitiveTypeProvider> data) {
        return data;
    }

    public enum Type {
        /**
         * Tags for retrieval
         */
        TAG,
        /**
         * Tags for tag lookup (i.e. autocomplete in vitrivr-ng
         */
        TAG_LOOKUP
    }
}
