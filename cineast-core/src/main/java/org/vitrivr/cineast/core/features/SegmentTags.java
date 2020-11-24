package org.vitrivr.cineast.core.features;

import gnu.trove.map.hash.TObjectFloatHashMap;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
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
import org.vitrivr.cineast.core.data.tag.Preference;
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
import org.vitrivr.cineast.core.util.TagsPerSegment;

public class SegmentTags implements Extractor, Retriever {


  protected BatchedTagWriter writer;
  protected DBSelector selector;
  protected DBSelector selectorHelper;
  protected PersistencyWriter<?> phandler;
  private static final Logger LOGGER = LogManager.getLogger();

  public static final String SEGMENT_TAGS_TABLE_NAME = "features_segmenttags";

  public SegmentTags() {
  }


  @Override
  public List<String> getTableNames() {
    return Collections.singletonList(SEGMENT_TAGS_TABLE_NAME);
  }

  @Override
  public void initalizePersistentLayer(Supplier<EntityCreator> supply) {
    supply.get().createIdEntity(SEGMENT_TAGS_TABLE_NAME,
        new AttributeDefinition("tagid", AttributeType.STRING),
        new AttributeDefinition("score", AttributeType.FLOAT));

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
    Map<String, Preference> preferenceMap = new HashMap<>();
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
      if (wt.getPreference().equals(Preference.MUST)) {
        mustTagsSet.add(wt.getId());
      }
      if (wt.getPreference().equals(Preference.COULD)) {
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
    List<Map<String, PrimitiveTypeProvider>> rows = this.selector.getRows("tagid",
        tagids.stream().map(StringTypeProvider::new).collect(Collectors.toList()));

    if (!preferenceMap.isEmpty()) { // should always be the case
      /* create 3 seperate sets: one with 'NOT' tags, one with 'COULD' tags, one with 'MUST' tags
       * split 'row' in couldRows and mustRows for further processing */
      Set<String> notSegments = new HashSet<>();
      Set<String> couldSegments = new HashSet<>();
      Set<String> mustSegments = new HashSet<>();

      List<Map<String, PrimitiveTypeProvider>> couldRows = new ArrayList<>();
      List<Map<String, PrimitiveTypeProvider>> mustRows = new ArrayList<>();

      Map<String, TagsPerSegment> helperMap = new HashMap<>(); // map to summarise tags for each segmentId

      for (Map<String, PrimitiveTypeProvider> row : rows) {
        String currentTagId = row.get("tagid").getString();
        String currentSegmentId = row.get("id").getString();

        if (!helperMap.containsKey(currentSegmentId)) {
          helperMap.put(currentSegmentId, new TagsPerSegment(currentSegmentId, new HashSet<>(
              Collections.singleton(currentTagId))));
        } else {
          helperMap.get(currentSegmentId).addTags(currentTagId);
        }

        if (preferenceMap.get(currentTagId).equals(Preference.NOT)) {
          // add segmentID if NOT tags associated with this segment
          notSegments.add(currentSegmentId);
        }
        if (preferenceMap.get(currentTagId).equals(Preference.COULD)) {
          couldSegments.add(currentSegmentId);
          couldRows.add(row);
        }
        if (preferenceMap.get(currentTagId).equals(Preference.MUST)) {
          mustSegments.add(currentSegmentId);
          mustRows.add(row);
        }
      }

      List<TagsPerSegment> tagsForSegment = new ArrayList<>(helperMap.values());
      // eliminate all notSegments from set of mustSegment
      mustSegments.retainAll(notSegments);
      Map<String, Set<String>> mustMap = createTagSegmentIdsMap(mustTagsSet,
          mustRows); // <tag, Set of segmentIds>

      if (!mustTagsSet.isEmpty()) { // at least one 'must' tag
        Set<String> mustSegmentIdsSet = new HashSet<>(mustMap.get(mustTagsSet.iterator()
            .next())); // initiate mustSegmentIdsSet to start intersection process
        for (String tag : mustTagsSet) {
          mustSegmentIdsSet.retainAll(mustMap.get(tag)); // intersect all 'MUST' sets
        }

        return scoreSegmentsWithPreferences(notSegments, couldTagsSet,
            mustSegmentIdsSet, mustTagsSet, helperMap);
      } else { // only 'could' tags used in query
        Set<String> noPreferenceSegmentIdSet = new HashSet<>();
        for (Map<String, PrimitiveTypeProvider> row : rows) {
          String currentSegmentId = row.get("id").getString();
          if (notSegments.contains(currentSegmentId)) { // do not add the 'NOT' segments
            continue;
          }
          noPreferenceSegmentIdSet.add(currentSegmentId);
        }
        noPreferenceSegmentIdSet.retainAll(notSegments);
        return scoreSegmentsWithoutPreferences(couldTagsSet, tagsForSegment);
      }

    } else {
      LOGGER.error("preferenceMap should never be empty");
      return null;
    }
  }


  private List<ScoreElement> scoreSegmentsWithoutPreferences(Set<String> couldTagsSet,
      List<TagsPerSegment> tagsForSegment) {

    List<ScoreElement> _return = new ArrayList<>();
    for (TagsPerSegment couldSegment : tagsForSegment) {
      float score = ((float) couldSegment.getTags().size() / (couldTagsSet.size()));
      _return.add(new SegmentScoreElement(couldSegment.segmentID, score));
    }
    return _return;
  }


  private List<ScoreElement> scoreSegmentsWithPreferences(Set<String> notSegments,
      Set<String> couldTagsSet, Set<String> mustSegmentIdsSet, Set<String> mustTagsSet,
      Map<String, TagsPerSegment> helperMap) {
    /* Prepare the set of relevant ids (if this entity is used for filtering at a later stage) */
    List<ScoreElement> _return = new ArrayList<>();
    for (String mustSegmentId : mustSegmentIdsSet) {
      if (notSegments.contains(mustSegmentId)) { // we do not score the 'NOT' segments to the result
        continue;
      }
      float increment = (float) (1.0 / (couldTagsSet.size() + mustTagsSet.size()));
      float score = increment * mustTagsSet.size();
      if (!couldTagsSet.isEmpty()) { // the query contains 'could' and 'must' tags
        Set<String> tagSetForSegment = helperMap.get(mustSegmentId).tags;
        for (String couldTag : couldTagsSet) {
          if (tagSetForSegment.contains(couldTag)) {
            score += increment;
          }
        }
      }
      _return.add(new SegmentScoreElement(mustSegmentId, score));
    }
    return _return;
  }

  private Map<String, Set<String>> createTagSegmentIdsMap(Set<String> mustTagsSet,
      List<Map<String, PrimitiveTypeProvider>> mustRows) {
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

    List<Map<String, PrimitiveTypeProvider>> rows = this.selector
        .getRows("id", new StringTypeProvider(segmentId));

    if (rows.isEmpty()) {
      return Collections.emptyList();
    }

    ArrayList<WeightedTag> wtags = new ArrayList<>(rows.size());

    for (Map<String, PrimitiveTypeProvider> row : rows) {
      wtags.add(new IncompleteTag(row.get("tagid").getString(), "", "", row.get("score").getFloat(),
          Preference.valueOf("preference")));
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
