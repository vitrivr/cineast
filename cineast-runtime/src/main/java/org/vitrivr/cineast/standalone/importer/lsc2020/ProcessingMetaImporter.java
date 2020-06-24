package org.vitrivr.cineast.standalone.importer.lsc2020;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.vitrivr.cineast.core.data.providers.primitive.PrimitiveTypeProvider;
import org.vitrivr.cineast.core.db.dao.reader.TagReader;
import org.vitrivr.cineast.core.importer.Importer;

import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.util.*;

public class ProcessingMetaImporter implements Importer<Map<String, PrimitiveTypeProvider>> {

    public static final List<Integer> TAG_CANDIDATES = Arrays.asList(LSCUtilities.META_SEMANTIC_COL, LSCUtilities.META_ACTIVITY_COL, LSCUtilities.META_TIMEZONE_COL, LSCUtilities.META_UTC_COL, LSCUtilities.META_LOCAL_COL);
    private static final Logger LOGGER = LogManager.getLogger(ProcessingMetaImporter.class);
    private final Type type;
    private Iterator<Map<String, PrimitiveTypeProvider>> currentDataIterator = null;
    private Iterator<String[]> iterator;
    private Map<String, String> minuteIdPathMap = new HashMap<>();
    private HashSet<String> uniqueList = new HashSet<>();

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
        switch(this.type){
            case TAG:
            case TAG_LOOKUP:
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
                break;
            case META_AS_TABLE:
                list.add(parseAsMeta(path, items));
                break;
        }
        return Optional.of(list);
    }

    private Map<String, PrimitiveTypeProvider> parseAsMeta(String segmentId, String[] items) {
        final HashMap<String, PrimitiveTypeProvider> map = new HashMap<>(items.length + 7);

        map.put("segmentid", PrimitiveTypeProvider.fromObject(segmentId));
        for (int i=0; i<items.length;i++){
            map.put(LSCUtilities.META_NAMES[i], PrimitiveTypeProvider.fromObject(items[i]));
        }
        final LocalDateTime utc = LSCUtilities.convertUtc(items[LSCUtilities.META_UTC_COL]);
        final ZonedDateTime local = LSCUtilities.convertLocal(items[LSCUtilities.META_LOCAL_COL], items[LSCUtilities.META_TIMEZONE_COL]);
        map.put("p_utc_standard", PrimitiveTypeProvider.fromObject(utc));
        map.put("p_local_standard", PrimitiveTypeProvider.fromObject(local));
        map.put("p_day_of_week", PrimitiveTypeProvider.fromObject(utc.getDayOfWeek().toString()));
        map.put("p_phase_of_day", PrimitiveTypeProvider.fromObject(LSCUtilities.extractPhaseOfDay(local)));
        map.put("p_month", PrimitiveTypeProvider.fromObject(LSCUtilities.extractMonth(utc)));
        map.put("p_year", PrimitiveTypeProvider.fromObject(LSCUtilities.extractYear(utc)));
        map.put("p_hour", PrimitiveTypeProvider.fromObject(LSCUtilities.extractHour(utc)));

        return map;
    }

    private Optional<Map<String, PrimitiveTypeProvider>> parse(String segmentid, String[] items, int index) {
        // TODO more types
        switch (this.type) {
            case TAG:
                return parseTag(segmentid, items, index);
            case TAG_LOOKUP:
                return parseTagForLookup(segmentid, items, index);
            default:
                return Optional.empty();
        }
    }

    private Optional<Map<String, PrimitiveTypeProvider>> parseTag(String segmentid, String[] items, int index) {
        if (TAG_CANDIDATES.contains(index)) {
            final String tag = metaAsTag(items, index);
            final HashMap<String, PrimitiveTypeProvider> map = new HashMap<>(3);
            map.put("id", PrimitiveTypeProvider.fromObject(segmentid));
            map.put("tagid", PrimitiveTypeProvider.fromObject(tag));
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
    private Optional<Map<String, PrimitiveTypeProvider>> parseTagForLookup(String segmentid, String[] items, int index) {
        if (TAG_CANDIDATES.contains(index)) {
            final String tag = metaAsTag(items, index);
            final HashMap<String, PrimitiveTypeProvider> map = new HashMap<>(3);
            map.put(TagReader.TAG_ID_COLUMNNAME, PrimitiveTypeProvider.fromObject(tag));
            map.put(TagReader.TAG_NAME_COLUMNNAME, PrimitiveTypeProvider.fromObject(tag));
            map.put(TagReader.TAG_DESCRIPTION_COLUMNNAME, PrimitiveTypeProvider.fromObject(""));
            if(onlyUnique()){
                if(!isUnique(tag)){
                    return Optional.empty();
                }
            }
            return Optional.of(map);
        }else{
            return Optional.empty();
        }
    }

    private String metaAsTag(String[] items, int index){
        String tag;
        if(index == LSCUtilities.META_UTC_COL){
            tag = LSCUtilities.convertUtc(items[index]).getDayOfWeek().toString();
        }else if(index == LSCUtilities.META_LOCAL_COL) {
            String zone = items[LSCUtilities.META_TIMEZONE_COL];
            tag = LSCUtilities.extractPhaseOfDay(LSCUtilities.convertLocal(items[index], zone));
        }else{
            tag = items[index];
        }
        return tag;
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

    /**
     * Checks if the needle is in the unique set. if so, it's not unique. otherwise its unique and added to the list
     * @param needle
     * @return
     */
    private boolean isUnique(String needle){
        boolean found = uniqueList.contains(needle);
        if(!found){
            uniqueList.add(needle);
        }
        return !found;
    }

    private boolean onlyUnique(){
        return type == Type.TAG_LOOKUP;
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
        TAG_LOOKUP,
        /**
         * Meta as is and certain processed metadata in a conventional table.
         * Requires the table to be created as {@link org.vitrivr.cineast.core.features.ConventionalTableRetriever} in the config
         */
        META_AS_TABLE
    }


}
