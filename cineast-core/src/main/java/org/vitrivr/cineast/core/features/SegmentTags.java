package org.vitrivr.cineast.core.features;

import static org.vitrivr.cineast.core.util.FeatureHelper.resolveTagsById;

import gnu.trove.map.hash.TObjectFloatHashMap;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.vitrivr.cineast.core.config.ReadableQueryConfig;
import org.vitrivr.cineast.core.data.entities.TagInstance;
import org.vitrivr.cineast.core.data.providers.primitive.PrimitiveTypeProvider;
import org.vitrivr.cineast.core.data.providers.primitive.StringTypeProvider;
import org.vitrivr.cineast.core.data.score.ScoreElement;
import org.vitrivr.cineast.core.data.score.SegmentScoreElement;
import org.vitrivr.cineast.core.data.segments.SegmentContainer;
import org.vitrivr.cineast.core.data.tag.IncompleteTag;
import org.vitrivr.cineast.core.data.tag.Tag;
import org.vitrivr.cineast.core.data.tag.WeightedTag;
import org.vitrivr.cineast.core.db.DBSelector;
import org.vitrivr.cineast.core.db.DBSelectorSupplier;
import org.vitrivr.cineast.core.db.PersistencyWriter;
import org.vitrivr.cineast.core.db.PersistencyWriterSupplier;
import org.vitrivr.cineast.core.db.dao.writer.BatchedTagWriter;
import org.vitrivr.cineast.core.db.setup.AttributeDefinition;
import org.vitrivr.cineast.core.db.setup.AttributeDefinition.AttributeType;
import org.vitrivr.cineast.core.db.setup.EntityCreator;
import org.vitrivr.cineast.core.features.extractor.Extractor;
import org.vitrivr.cineast.core.features.retriever.Retriever;
import org.vitrivr.cineast.core.util.FeatureHelper;

public class SegmentTags implements Extractor, Retriever {


    protected BatchedTagWriter writer;
    protected DBSelector selector;
    protected DBSelector selectorHelper;
    protected PersistencyWriter<?> phandler;
    private static final Logger LOGGER = LogManager.getLogger();

    public static final String SEGMENT_TAGS_TABLE_NAME = "features_segmenttags";
    public static Map<String, Integer> resolvedTags = new HashMap<>();
    public static Map<String, Float> topCaptionTerms = new HashMap<>();

    public SegmentTags() {
    }

    @Override
    public List<String> getTableNames() {
        return Collections.singletonList(SEGMENT_TAGS_TABLE_NAME);
    }

    @Override
    public void initalizePersistentLayer(Supplier<EntityCreator> supply) {
        supply.get().createIdEntity(SEGMENT_TAGS_TABLE_NAME, new AttributeDefinition("tagid", AttributeType.STRING), new AttributeDefinition("score", AttributeType.FLOAT));

        supply.get().createHashNonUniqueIndex(SEGMENT_TAGS_TABLE_NAME, "tagid");
    }

    @Override
    public void dropPersistentLayer(Supplier<EntityCreator> supply) {
        supply.get().dropEntity(SEGMENT_TAGS_TABLE_NAME);
    }

    @Override
    public void init(DBSelectorSupplier selectorSupply) {
        this.selector = selectorSupply.get();
        this.selectorHelper = selectorSupply.get();
        this.selector.open(SEGMENT_TAGS_TABLE_NAME);
    }


    private List<ScoreElement> getSimilar(Iterable<WeightedTag> tags, ReadableQueryConfig qc) {

        ArrayList<String> tagids = new ArrayList<>();
        Map<String, String> preferenceMap = new HashMap<>();
        Set<String> mustTagsSet = new HashSet<>();
        Set<String> couldTagsSet = new HashSet<>();
        TObjectFloatHashMap<String> tagWeights = new TObjectFloatHashMap<>();
        float weightSum = 0f;

        /* Sum weights for normalization at a later point*/
        for (WeightedTag wt : tags) {
            tagids.add(wt.getId());
            tagWeights.put(wt.getId(), wt.getWeight());
            // add tag and its preference to preferenceMap
            preferenceMap.put(wt.getId(), wt.getPreference());
            if (wt.getPreference().equals("must")) {
                mustTagsSet.add(wt.getId());
            }
            if (wt.getPreference().equals("could")) {
                couldTagsSet.add(wt.getId());
            }
            if (wt.getWeight() > 1) {
                LOGGER.error("Weight is > 1 -- this makes little sense.");
            }
            weightSum += Math.min(1, wt.getWeight());
        }

        if (tagids.isEmpty() || weightSum <= 0f) {
            return Collections.emptyList();
        }

        /* Retrieve all elements matching the provided ids */
        // String is either 'tagid', 'score' or 'id'
        List<Map<String, PrimitiveTypeProvider>> rows = this.selector.getRows("tagid", tagids.stream().map(StringTypeProvider::new).collect(Collectors.toList()));

        if (!preferenceMap.isEmpty()) { // should always be the case
            /* create 3 seperate sets: one with 'NOT' tags, one with 'COULD' tags, one with 'MUST' tags
             * split 'row' in couldRows and mustRows for further processing */
            Set<String> notSegments = new HashSet<>();
            Set<String> couldSegments = new HashSet<>();
            List<Map<String, PrimitiveTypeProvider>> couldRows = new ArrayList<>();
            Set<String> mustSegments = new HashSet<>();
            List<Map<String, PrimitiveTypeProvider>> mustRows = new ArrayList<>();

            for (Map<String, PrimitiveTypeProvider> row : rows) {
                String currentTagId = row.get("tagid").getString();
                String currentSegmentId = row.get("id").getString();
                preferenceMap.get(currentTagId);
                if (preferenceMap.get(currentTagId).equals("not")) { // add segmentID if NOT tags associated with this segment
                    notSegments.add(currentSegmentId);
                }
                if (preferenceMap.get(currentTagId).equals("could")) {
                    couldSegments.add(currentSegmentId);
                    couldRows.add(row);
                }
                if (preferenceMap.get(currentTagId).equals("must")) {
                    mustSegments.add(currentSegmentId);
                    mustRows.add(row);
                }
            }

            Map<String, Set<String>> mustMap = createTagSegmentIdsMap(mustTagsSet, mustRows); // <tag, Set of segmentIds>

            Set<String> mustSegmentIdsSet = new HashSet<>();
            if (!mustTagsSet.isEmpty()) {
                mustSegmentIdsSet.addAll(mustMap.get(mustTagsSet.iterator().next())); // initiate mustSegmentIdsSet to start intersection process
                for (String tag : mustTagsSet) {
                    mustSegmentIdsSet.retainAll(mustMap.get(tag)); // intersect all 'MUST' sets
                }
                getTopTags(mustSegmentIdsSet);
                getTopCaptionTerms(mustSegmentIdsSet);

                return scoreSegmentsWithPreferences(qc, notSegments, couldTagsSet, couldSegments, mustMap, mustSegmentIdsSet);
            } else {
                Set<String> noPreferenceSegmentIdSet = new HashSet<>();
                for (Map<String, PrimitiveTypeProvider> row : rows) {
                    String currentSegmentId = row.get("id").getString();
                    if (notSegments.contains(currentSegmentId)) { // do not add the 'NOT' segments
                        continue;
                    }
                    noPreferenceSegmentIdSet.add(currentSegmentId);
                }
                getTopTags(noPreferenceSegmentIdSet);
                return scoreSegmentsWithoutPreferences(couldTagsSet, noPreferenceSegmentIdSet);
            }

        } else {
            LOGGER.error("preferenceMap should never be empty");
            return null;
        }
    }

    private void getTopCaptionTerms(Set<String> mustSegmentIdsSet) {

    }

    private void getTopTags(Set<String> mustSegmentIdsSet) {
        List<String> allTagIdsInResultSet = FeatureHelper.retrieveTagsBySegmentId(new ArrayList<>(mustSegmentIdsSet), selectorHelper);
        Map<String, Integer> tagCounterMap = new LinkedHashMap<>();
        for (String item : allTagIdsInResultSet) {
            int counter = 1;
            if (tagCounterMap.containsKey(item)) {
                counter = tagCounterMap.get(item) + 1;
            }
            tagCounterMap.put(item, counter);
        }
        tagCounterMap = tagCounterMap.entrySet().stream().sorted(Map.Entry.<String, Integer>comparingByValue()).collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (oldValue, newValue) -> oldValue, LinkedHashMap::new));
        LOGGER.debug("calculating top 10 related tags");
        List<String> keys = new ArrayList<>(tagCounterMap.keySet());
        Collections.reverse(keys);
        keys = keys.stream().limit(10).collect(Collectors.toList());

        Map<String, Integer> topTags = new LinkedHashMap<>();
        // List<Tag> tagList = resolveTagsById(keys, selectorHelper);

        for (int i = 0; i < keys.size(); i++) {
            // LOGGER.debug("tag number i: {}", i);
            // LOGGER.debug("tagCounterMap.get(keys.get(i)): {}", keys.get(i));

            topTags.put(keys.get(i), tagCounterMap.get(keys.get(i)));
        }

        resolvedTags = topTags;

    }


    private List<ScoreElement> scoreSegmentsWithoutPreferences(Set<String> couldTagsSet, Set<String> noPreferenceSegmentIdSet) {
        float score = (float) (1.0 / (couldTagsSet.size() + 1));
        List<ScoreElement> _return = new ArrayList<>();
        for (String noPreferenceSegmentId : noPreferenceSegmentIdSet) {
            _return.add(new SegmentScoreElement(noPreferenceSegmentId, score));
        }
        return _return;
    }


    private List<ScoreElement> scoreSegmentsWithPreferences(ReadableQueryConfig qc, Set<String> notSegments, Set<String> couldTagsSet, Set<String> couldSegments, Map<String, Set<String>> mustMap, Set<String> mustSegmentIdsSet) {
        /* Prepare the set of relevant ids (if this entity is used for filtering at a later stage) */
        List<ScoreElement> _return = new ArrayList<>();
        Set<String> relevant = null;
        if (qc != null && qc.hasRelevantSegmentIds()) {
            relevant = qc.getRelevantSegmentIds();
        }
        for (String mustSegmentId : mustSegmentIdsSet) {
/*                if (!relevant.contains(mustSegmentId)) {
                    continue;
                }*/
            if (notSegments.contains(mustSegmentId)) { // we do not add the 'NOT' segments to the result
                continue;
            }

            float score = (float) (1.0 / (couldTagsSet.size() + 1));
            if (!couldTagsSet.isEmpty()) {
                for (String couldTag : couldTagsSet) {
                    int couldSize = couldTagsSet.size();
                    if (couldSegments.contains(mustSegmentId)) {
                        score += 1.0 / (couldTagsSet.size() + 1);
                    }
                    if (score > 1) {
                        LOGGER.warn("Score is larger than 1 - this makes little sense");
                        score += (float) (1.0 / (couldTagsSet.size() + 1));
                    }
                }
            }
            _return.add(new SegmentScoreElement(mustSegmentId, score));
        }
        return _return;
    }

    private Map<String, Set<String>> createTagSegmentIdsMap(Set<String> mustTagsSet, List<Map<String, PrimitiveTypeProvider>> mustRows) {
        Map<String, Set<String>> mustMap = new HashMap<>();
        for (String mustTag : mustTagsSet) {
            Set<String> segmentIds = new HashSet<>();
            for (Map<String, PrimitiveTypeProvider> mustRow : mustRows) {
                String id = mustRow.get("id").getString();
                String tag = mustRow.get("tagid").getString();
                if (mustTag.equals(tag)) {
                    if (mustMap.containsKey(tag)) { // add tag to existing entry for segment map
                        segmentIds = mustMap.get(tag);
                    }
                    segmentIds.add(id);
                    mustMap.put(mustTag, segmentIds);
                }
            }
        }
        return mustMap;
    }

    @Override
    public List<ScoreElement> getSimilar(SegmentContainer sc, ReadableQueryConfig qc) {

        List<Tag> tags = sc.getTags();
        if (tags.isEmpty()) {
            return Collections.emptyList();
        }

        ArrayList<WeightedTag> wtags = new ArrayList<>(tags.size());

        for (Tag t : tags) {
            if (t instanceof WeightedTag) {
                wtags.add((WeightedTag) t);
            } else {
                wtags.add(new IncompleteTag(t));
            }

        }

        return getSimilar(wtags, qc);

    }

    @Override
    public List<ScoreElement> getSimilar(String segmentId, ReadableQueryConfig qc) {

        List<Map<String, PrimitiveTypeProvider>> rows = this.selector.getRows("id", new StringTypeProvider(segmentId));

        if (rows.isEmpty()) {
            return Collections.emptyList();
        }

        ArrayList<WeightedTag> wtags = new ArrayList<>(rows.size());

        for (Map<String, PrimitiveTypeProvider> row : rows) {
            wtags.add(new IncompleteTag(row.get("tagid").getString(), "", "", row.get("score").getFloat(), "preference"));
        }

        return getSimilar(wtags, qc);
    }

    @Override
    public void init(PersistencyWriterSupplier phandlerSupply, int batchSize) {
        this.phandler = phandlerSupply.get();
        this.writer = new BatchedTagWriter(this.phandler, SEGMENT_TAGS_TABLE_NAME, batchSize);
    }

    @Override
    public void processSegment(SegmentContainer container) {
        List<Tag> tags = container.getTags();

        for (Tag t : tags) {
            persist(container.getId(), t);
        }

    }

    protected void persist(String segmentId, Tag t) {
        this.writer.write(new TagInstance(segmentId, t));
    }

    @Override
    public void finish() {
        if (this.writer != null) {
            this.writer.close();
            this.writer = null;
        }

        if (this.phandler != null) {
            this.phandler.close();
            this.phandler = null;
        }

        if (this.selector != null) {
            this.selector.close();
            this.selector = null;
        }
    }

}
