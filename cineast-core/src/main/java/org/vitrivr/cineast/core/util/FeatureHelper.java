package org.vitrivr.cineast.core.util;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
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

    public static List<String> retrieveCaptionBySegmentId(String segmentId, DBSelector selector) {
        List<String> result = new ArrayList<>();
        selector.open(DescriptionTextSearch.DESCRIPTION_TEXT_TABLE_NAME);
        List<Map<String, PrimitiveTypeProvider>> rows = selector.getRows("id", new StringTypeProvider(segmentId));
        rows.forEach(row -> result.add(row.get("feature").getString()));
        return result;
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
