package org.vitrivr.cineast.core.util;

import static java.util.Arrays.asList;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.vitrivr.cineast.core.data.providers.primitive.PrimitiveTypeProvider;
import org.vitrivr.cineast.core.data.providers.primitive.StringTypeProvider;
import org.vitrivr.cineast.core.data.tag.Tag;
import org.vitrivr.cineast.core.db.DBSelector;
import org.vitrivr.cineast.core.db.dao.reader.TagReader;
import org.vitrivr.cineast.core.features.AudioTranscriptionSearch;
import org.vitrivr.cineast.core.features.DescriptionTextSearch;
import org.vitrivr.cineast.core.features.OCRSearch;
import org.vitrivr.cineast.core.features.SegmentTags;


public class FeatureHelper {

    private static final Logger LOGGER = LogManager.getLogger();


    public static List<String> retrieveTagsBySegmentId(List<String> segmentIdList, DBSelector selector) {
        // LOGGER.debug("tag query");
        List<String> result = new ArrayList<>();
        selector.open(SegmentTags.SEGMENT_TAGS_TABLE_NAME);
        List<Map<String, PrimitiveTypeProvider>> rows = selector.getRows("id", segmentIdList);
        rows.forEach(row -> result.add(row.get("tagid").getString()));
        return result;
    }

    public static List<Tag> resolveTagsById(List<String> tagIds, DBSelector selector) {
        TagReader tagReader = new TagReader(selector);

        return tagReader.getTagsById(tagIds.toArray(new String[0]));

    }

    public static Map<String, Set<String>> retrieveCaptionBySegmentId(List<String> segmentIds, DBSelector selector) {
        Map<String, Set<String>> result = new HashMap<>();
        selector.open(DescriptionTextSearch.DESCRIPTION_TEXT_TABLE_NAME);
        List<Map<String, PrimitiveTypeProvider>> rows = selector.getRows("id", segmentIds);
        rows.forEach(row -> {
            String id = row.get("id").getString();
            Set<String> captionSet = result.get(id);
            if (captionSet == null) {
                captionSet = new HashSet<>();
            }
            captionSet.addAll(asList(filterStopWords(row.get("feature").getString())));

            result.put(id, captionSet);
        });
        return result;
    }

    private static String[] filterStopWords(String caption) {
        Set<String> stopWords = new HashSet<>(Arrays
            .asList("i", "me", "my", "myself", "we", "our", "ours", "ourselves", "you", "your", "yours", "yourself", "yourselves", "he", "him", "his", "himself", "she", "her", "hers", "herself", "it", "its", "itself", "they", "them", "their", "theirs", "themselves", "what", "which", "who", "whom", "this", "that", "these", "those", "am", "is", "are", "was", "were", "be", "been", "being", "have", "has", "had", "having", "do", "does", "did", "doing", "a", "an", "the", "and", "but", "if", "or",
                "because", "as", "until", "while", "of", "at", "by", "for", "with", "about", "against", "between", "into", "through", "during", "before", "after", "above", "below", "to", "from", "up", "down", "in", "out", "on", "off", "over", "under", "again", "further", "then", "once", "here", "there", "when", "where", "why", "how", "all", "any", "both", "each", "few", "more", "most", "other", "some", "such", "no", "nor", "not", "only", "own", "same", "so", "than", "too", "very", "s", "t",
                "can", "will", "just", "don", "should", "now"));

        return Arrays.stream(caption.replaceAll("[^a-zA-Z ]", "").toLowerCase().split("\\s+")).filter(i -> !stopWords.contains(i)).toArray(String[]::new);
    }

    public static List<String> retrieveOCRBySegmentId(String segmentId, DBSelector selector) {
        List<String> result = new ArrayList<>();
        selector.open(OCRSearch.OCR_TABLE_NAME);
        List<Map<String, PrimitiveTypeProvider>> rows = selector.getRows("id", new StringTypeProvider(segmentId));
        rows.forEach(row -> result.add(row.get("feature").getString()));
        return result;
    }

    public static List<String> retrieveASRBySegmentId(String segmentId, DBSelector selector) {
        List<String> result = new ArrayList<>();
        selector.open(AudioTranscriptionSearch.AUDIO_TRANSCRIPTION_TABLE_NAME);
        List<Map<String, PrimitiveTypeProvider>> rows = selector.getRows("id", new StringTypeProvider(segmentId));
        rows.forEach(row -> result.add(row.get("feature").getString()));
        return result;
    }

}
