package org.vitrivr.cineast.core.features;

import gnu.trove.impl.Constants;
import gnu.trove.map.hash.TObjectFloatHashMap;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
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
import org.vitrivr.cineast.core.util.MathHelper;

public class SegmentTags implements Extractor, Retriever {

  protected BatchedTagWriter writer;
  protected DBSelector selector;
  protected PersistencyWriter<?> phandler;
  private static final Logger LOGGER = LogManager.getLogger();

  public static final String SEGMENT_TAGS_TABLE_NAME = "features_segmenttags";

  public static final String TAG_ID_QUALIFIER = "tagid";

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
    supply.get().createHashNonUniqueIndex(SEGMENT_TAGS_TABLE_NAME, "id");
  }

  @Override
  public void dropPersistentLayer(Supplier<EntityCreator> supply) {
    supply.get().dropEntity(SEGMENT_TAGS_TABLE_NAME);
  }

  @Override
  public void init(DBSelectorSupplier selectorSupply) {
    this.selector = selectorSupply.get();
    this.selector.open(SEGMENT_TAGS_TABLE_NAME);
  }


  private List<ScoreElement> getSimilar(Iterable<WeightedTag> tags, ReadableQueryConfig qc) {

    ArrayList<String> tagids = new ArrayList<>();
    TObjectFloatHashMap<String> tagWeights = new TObjectFloatHashMap<>();
    float weightSum = 0f;

    /* Sum weights for normalization at a later point*/
    for (WeightedTag wt : tags) {
      tagids.add(wt.getId());
      tagWeights.put(wt.getId(), wt.getWeight());
      if (wt.getWeight() > 1) {
        LOGGER.error("Weight is > 1 -- this makes little sense.");
      }
      weightSum += Math.min(1, wt.getWeight());
    }

    if (tagids.isEmpty() || weightSum <= 0f) {
      return Collections.emptyList();
    }

    /* Retrieve all elements matching the provided ids */
    List<Map<String, PrimitiveTypeProvider>> rows = this.selector.getRows("tagid", tagids.stream().map(StringTypeProvider::new).collect(Collectors.toList()));

    Map<String, TObjectFloatHashMap<String>> maxScoreByTag = new HashMap<>();

    /* Prepare the set of relevant ids (if this entity is used for filtering at a later stage) */
    Set<String> relevant = null;
    if (qc != null && qc.hasRelevantSegmentIds()) {
      relevant = qc.getRelevantSegmentIds();
    }

    /* Iterate over all matches */
    for (Map<String, PrimitiveTypeProvider> row : rows) {

      String segmentId = row.get("id").getString();

      /* Skip segments which are not desired by the query-config */
      if (relevant != null && !relevant.contains(segmentId)) {
        continue;
      }

      String tagid = row.get("tagid").getString();
      float score = row.get("score").getFloat()
          * (tagWeights.containsKey(tagid) ? tagWeights.get(tagid) : 0f);

      if (score > 1) {
        LOGGER.warn("Score is larger than 1 - this makes little sense");
        score = 1f;
      }

      /* Update maximum score by tag*/
      maxScoreByTag.putIfAbsent(segmentId, new TObjectFloatHashMap<>());
      float prev = maxScoreByTag.get(segmentId).get(tagid);
      if (prev == Constants.DEFAULT_FLOAT_NO_ENTRY_VALUE) {
        maxScoreByTag.get(segmentId).put(tagid, score);
      } else {
        maxScoreByTag.get(segmentId).put(tagid, Math.max(score, prev));
      }
    }

    ArrayList<ScoreElement> _return = new ArrayList<>();

    final float normalizer = weightSum;

    /* per segment, the max score for all tags is summed and divided by the normalizer */
    maxScoreByTag.forEach((segmentId, tagScores) -> _return.add(new SegmentScoreElement(segmentId, MathHelper.sum(tagScores.values()) / normalizer)));

    return _return;
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
      wtags.add(new IncompleteTag(row.get("tagid").getString(), "", "", row.get("score").getFloat()));
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
