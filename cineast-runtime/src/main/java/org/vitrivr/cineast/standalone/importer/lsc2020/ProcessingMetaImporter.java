package org.vitrivr.cineast.standalone.importer.lsc2020;

import org.apache.commons.lang3.StringUtils;
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
    private final Map<String, String[]> metaPerMinuteId;
    private final Map<String, String> filenameToMinuteIdLookUp;
    private Iterator<Map<String, PrimitiveTypeProvider>> currentDataIterator = null;
    private Iterator<Map.Entry<String, String>> iterator;
    private Map<String, String> minuteIdPathMap = new HashMap<>();
    private HashSet<String> uniqueList = new HashSet<>();
    private HashSet<String> uniqueTemporalMetadata = new HashSet<>();

    public ProcessingMetaImporter(final Path path, final Type type) {
        this.type = type;

        // We do not initialise LSCUtilities, as they are created outside.
        LSCUtilities lsc = LSCUtilities.getInstance();
        filenameToMinuteIdLookUp = lsc.getFilenameToMinuteIdLookUp();
        metaPerMinuteId = lsc.getMetaPerMinuteId();
        iterator = filenameToMinuteIdLookUp.entrySet().iterator();
        LOGGER.info("Finished setup of Importer. Importing now...");
    }

    private Optional<List<Map<String, PrimitiveTypeProvider>>> parseLine(String path, String[] items) {
        String minuteId = items[LSCUtilities.META_MIN_COL];
        if (path == null) {
            // cannot resolve
            return Optional.empty();
        }
        String segmentId = LSCUtilities.cleanImagePath(path);
        segmentId = LSCUtilities.pathToSegmentId(segmentId);
        List<Map<String, PrimitiveTypeProvider>> list = new ArrayList<>();
        switch (this.type) {
            case TAG:
            case TAG_LOOKUP:
                Optional<List<Map<String, PrimitiveTypeProvider>>> maps = parseAsTag(segmentId, path, items);
                maps.ifPresent(list::addAll);

                break;
            case META_AS_TABLE:
                list.add(parseAsMeta(segmentId, path, items));
                break;
        }
        return Optional.of(list);
    }

    private Map<String, PrimitiveTypeProvider> parseAsMeta(String segmentId, String path, String[] items) {
        final HashMap<String, PrimitiveTypeProvider> map = new HashMap<>(items.length + 9);

        map.put("id", PrimitiveTypeProvider.fromObject(segmentId));
        for (int i = 0; i < items.length; i++) {
            switch (i) {
                case LSCUtilities.META_LAT_COL:
                case LSCUtilities.META_LON_COL:
                    if(StringUtils.isNotBlank(items[i]) && !items[i].equalsIgnoreCase("null")){
                        // Only add if useful

                        map.put(LSCUtilities.META_NAMES[i], PrimitiveTypeProvider.fromObject(Double.parseDouble(items[i])));
                    }else{
                        LOGGER.trace("Did not include "+segmentId+" :"+LSCUtilities.META_NAMES[i]+", beacuse its blank or null:"+ items[i]);
                        map.put(LSCUtilities.META_NAMES[i], PrimitiveTypeProvider.fromObject(Double.NaN));
                    }
                    break;
                default:
                    map.put(LSCUtilities.META_NAMES[i], PrimitiveTypeProvider.fromObject(items[i]));
                    break;
            }
        }
        final String minuteId = LSCUtilities.filenameToMinuteId(path).get(); // Purposely no isPresent check, because it should not be possible
        final LocalDateTime dt = LSCUtilities.fromMinuteId(minuteId);
        map.put(LSCUtilities.PROCESSED_META_DATETIME, PrimitiveTypeProvider.fromObject(dt.toString()));
        map.put(LSCUtilities.PROCESSED_META_DAY_OF_WEEK, PrimitiveTypeProvider.fromObject(String.valueOf(dt.getDayOfWeek())));
        map.put(LSCUtilities.PROCESSED_META_MONTH, PrimitiveTypeProvider.fromObject(LSCUtilities.extractMonth(dt)));
        // Numeric values
        map.put(LSCUtilities.PROCESSED_META_DAY, PrimitiveTypeProvider.fromObject(LSCUtilities.extractDay(dt)));
        map.put(LSCUtilities.PROCESSED_META_YEAR, PrimitiveTypeProvider.fromObject(LSCUtilities.extractYear(dt)));
        map.put(LSCUtilities.PROCESSED_META_HOUR_OF_DAY, PrimitiveTypeProvider.fromObject(LSCUtilities.extractHour(dt)));
        if (StringUtils.isNotBlank(items[LSCUtilities.META_UTC_COL]) && !items[LSCUtilities.META_UTC_COL].equalsIgnoreCase("null")) {
            map.put(LSCUtilities.PROCESSED_META_UTC, PrimitiveTypeProvider.fromObject(LSCUtilities.convertUtc(items[LSCUtilities.META_UTC_COL]).toString()));
        }
        // Processed temporal metadata, based on metadata file
        if ((StringUtils.isNotBlank(items[LSCUtilities.META_LOCAL_COL]) && !items[LSCUtilities.META_LOCAL_COL].equalsIgnoreCase("null"))
                && (StringUtils.isNotBlank(items[LSCUtilities.META_TIMEZONE_COL]) && !items[LSCUtilities.META_TIMEZONE_COL].equalsIgnoreCase("null"))) {
            final ZonedDateTime zdt = LSCUtilities.convertLocal(items[LSCUtilities.META_LOCAL_COL], items[LSCUtilities.META_TIMEZONE_COL]);
            map.put(LSCUtilities.PROCESSED_META_LOCAL, PrimitiveTypeProvider.fromObject(zdt.toString()));
            map.put(LSCUtilities.PROCESSED_META_PHASE_OF_DAY, PrimitiveTypeProvider.fromObject(LSCUtilities.extractPhaseOfDay(zdt)));
        }else{
            map.put(LSCUtilities.PROCESSED_META_LOCAL, PrimitiveTypeProvider.fromObject(null));
            map.put(LSCUtilities.PROCESSED_META_PHASE_OF_DAY, PrimitiveTypeProvider.fromObject(null));
        }

        return map;
    }

    private Optional<List<Map<String, PrimitiveTypeProvider>>> parseAsTag(String segmentid, String path, String[] items) {
        List<Map<String, PrimitiveTypeProvider>> list = new ArrayList<>();
        for (int i = 0; i < LSCUtilities.META_NAMES.length; i++) {
            if (items[i].equalsIgnoreCase("null")) {
                continue;
            }
            Optional<Map<String, PrimitiveTypeProvider>> data = parse(segmentid, path, items, i);
            if (!data.isPresent()) {
                continue;
            } else {
                list.add(data.get());
            }
        }
        // Additionally processed meta as tags: DayOfWeek, Month, Day, Year, HourOfDay
        final String minuteId = LSCUtilities.filenameToMinuteId(path).get(); // Purposely no isPresent check, because it should not be possible
        final LocalDateTime dt = LSCUtilities.fromMinuteId(minuteId);
        BiProducer<String, String, Map<String, PrimitiveTypeProvider>> parser;
        if (type == Type.TAG) {
            parser = this::toTag;
        } else if (type == Type.TAG_LOOKUP) {
            parser = this::toTagLookup;
        } else {
            parser = null; // To willfully throw NPE
        }
        String dow = String.valueOf(dt.getDayOfWeek());
        String month = LSCUtilities.extractMonth(dt);
        String year = LSCUtilities.extractYearStr(dt);
        String day = LSCUtilities.extractDayStr(dt);
        String hod = LSCUtilities.extractHourStr(dt);

        // Day of week
        if(onlyUnique()){
            // tag lookup, it must be unique
            if(isUniqueTemporalContext(dow)){
                list.add(parser.produce(segmentid, dow));
            }
        }else{
            list.add(parser.produce(segmentid, dow));
        }
        // Month
        if(onlyUnique()){
            if(isUniqueTemporalContext(month)){
                list.add(parser.produce(segmentid, month));
            }
        }else{
            list.add(parser.produce(segmentid, month));
        }
        // Year
        if(onlyUnique()){
            if(isUniqueTemporalContext(year)){
                list.add(parser.produce(segmentid, year));
            }
        }else{
            list.add(parser.produce(segmentid, year));
        }
        // Day
        if(onlyUnique()){
            if(isUniqueTemporalContext(day)){
                list.add(parser.produce(segmentid, day));
            }
        }else{
            list.add(parser.produce(segmentid, day));
        }
        // HourOfDay
        if(onlyUnique()){
            if(isUniqueTemporalContext(hod)){
                list.add(parser.produce(segmentid, hod));
            }
        }else{
            list.add(parser.produce(segmentid, hod));
        }
        if (list.isEmpty()) {
            return Optional.empty();
        } else {
            return Optional.of(list);
        }
    }

    private Optional<Map<String, PrimitiveTypeProvider>> parse(String segmentid, String path, String[] items, int index) {
        switch (this.type) {
            case TAG:
                return parseTag(segmentid, path, items, index);
            case TAG_LOOKUP:
                return parseTagForLookup(segmentid, path, items, index);
            default:
                return Optional.empty();
        }
    }

    private Optional<Map<String, PrimitiveTypeProvider>> parseTag(String segmentid, String path, String[] items, int index) {
        if (TAG_CANDIDATES.contains(index)) {
            final String tag = metaAsTag(items, index);
            return Optional.of(toTag(segmentid, tag));
        } else {
            return Optional.empty();
        }
    }

    private Map<String, PrimitiveTypeProvider> toTag(String segmentid, String tag) {
        return toTag(segmentid, tag, 1);
    }

    private Map<String, PrimitiveTypeProvider> toTag(String segmentid, String tag, int score) {
        final HashMap<String, PrimitiveTypeProvider> map = new HashMap<>(3);
        map.put("id", PrimitiveTypeProvider.fromObject(segmentid));
        map.put("tagid", PrimitiveTypeProvider.fromObject(tag));
        map.put("score", PrimitiveTypeProvider.fromObject(score));
        return map;
    }

    /**
     * Must only be called with index values for valid tags
     * tags: id === name, description is empty (no tag expansion done)
     */
    private Optional<Map<String, PrimitiveTypeProvider>> parseTagForLookup(String segmentid, String path, String[] items, int index) {
        if (TAG_CANDIDATES.contains(index)) {
            final String tag = metaAsTag(items, index);
            if (onlyUnique()) {
                if (!isUnique(tag)) {
                    return Optional.empty();
                }
            }
            return Optional.of(toTagLookup(segmentid, tag));
        } else {
            return Optional.empty();
        }
    }

    private Map<String, PrimitiveTypeProvider> toTagLookup(String segmentid, String tag) {
        final HashMap<String, PrimitiveTypeProvider> map = new HashMap<>(3);
        map.put(TagReader.TAG_ID_COLUMNNAME, PrimitiveTypeProvider.fromObject(tag));
        map.put(TagReader.TAG_NAME_COLUMNNAME, PrimitiveTypeProvider.fromObject(tag));
        map.put(TagReader.TAG_DESCRIPTION_COLUMNNAME, PrimitiveTypeProvider.fromObject(""));
        return map;
    }


    private String metaAsTag(String[] items, int index) {
        String tag;
        if (index == LSCUtilities.META_UTC_COL) {
            tag = LSCUtilities.convertUtc(items[index]).getDayOfWeek().toString();
        } else if (index == LSCUtilities.META_LOCAL_COL) {
            String zone = items[LSCUtilities.META_TIMEZONE_COL];
            tag = LSCUtilities.extractPhaseOfDay(LSCUtilities.convertLocal(items[index], zone));
        } else {
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
                Map.Entry<String, String> next = this.iterator.next();
                Optional<List<Map<String, PrimitiveTypeProvider>>> parsed = parseLine(next.getKey(), metaPerMinuteId.get(next.getValue()));
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
     *
     * @param needle
     * @return
     */
    private boolean isUnique(String needle) {
        boolean found = uniqueList.contains(needle);
        if (!found) {
            uniqueList.add(needle);
        }
        return !found;
    }

    private boolean isUniqueTemporalContext(String temp){
        boolean found = uniqueTemporalMetadata.contains(temp);
        if(!found){
            uniqueTemporalMetadata.add(temp);
        }
        return !found;
    }

    private boolean onlyUnique() {
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
         */
        META_AS_TABLE
    }

    @FunctionalInterface
    public interface BiProducer<I1, I2, O> {
        O produce(I1 i1, I2 i2);
    }

}
