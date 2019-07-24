package org.vitrivr.cineast.core.features;

import gnu.trove.iterator.TObjectFloatIterator;
import gnu.trove.map.hash.TObjectFloatHashMap;
import org.vitrivr.cineast.core.config.ReadableQueryConfig;
import org.vitrivr.cineast.core.data.entities.TagInstance;
import org.vitrivr.cineast.core.data.providers.primitive.PrimitiveTypeProvider;
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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

public class SegmentTags implements Extractor, Retriever {

  protected BatchedTagWriter writer;
  protected DBSelector selector;
  protected final String tableName;
  protected PersistencyWriter<?> phandler;

  protected SegmentTags(String tableName) {
    this.tableName = tableName;
  }

  public SegmentTags() {
    this("features_segmenttags");
  }

  @Override
  public void initalizePersistentLayer(Supplier<EntityCreator> supply) {
    supply.get().createIdEntity(this.tableName,
        new AttributeDefinition("tagid", AttributeType.STRING),
        new AttributeDefinition("score", AttributeType.FLOAT));
  }

  @Override
  public void dropPersistentLayer(Supplier<EntityCreator> supply) {
    supply.get().dropEntity(this.tableName);
  }

  @Override
  public void init(DBSelectorSupplier selectorSupply) {
    this.selector = selectorSupply.get();
    this.selector.open(this.tableName);
  }

  
  private List<ScoreElement> getSimilar(Iterable<WeightedTag> tags) {

    ArrayList<String> tagids = new ArrayList<>();
    TObjectFloatHashMap<String> tagWeights = new TObjectFloatHashMap<>();
    float weightSum = 0f;

    for (WeightedTag wt : tags) {
      tagids.add(wt.getId());
      tagWeights.put(wt.getId(), wt.getWeight());
      weightSum += wt.getWeight();
    }

    if (tagids.isEmpty() || weightSum <= 0f) {
      return Collections.emptyList();
    }

    List<Map<String, PrimitiveTypeProvider>> rows = this.selector.getRows("tagid", tagids);

    TObjectFloatHashMap<String> segmentScores = new TObjectFloatHashMap<>();

    for (Map<String, PrimitiveTypeProvider> row : rows) {
      String segment = row.get("id").getString();
      String tagid = row.get("tagid").getString();
      float score = row.get("score").getFloat()
          * (tagWeights.containsKey(tagid) ? tagWeights.get(tagid) : 0f);

      segmentScores.adjustOrPutValue(segment, score, score);

    }

    ArrayList<ScoreElement> _return = new ArrayList<>(segmentScores.size());

    TObjectFloatIterator<String> iter = segmentScores.iterator();

    while (iter.hasNext()) {
      iter.advance();
      if(iter.value() > 0f){
        _return.add(new SegmentScoreElement(iter.key(), iter.value() / weightSum));
      }
    }
    
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
      if(t instanceof WeightedTag){
        wtags.add((WeightedTag)t);
      }else{
        wtags.add(new IncompleteTag(t));
      }
      
    }

    return getSimilar(wtags);
    
  }

  @Override
  public List<ScoreElement> getSimilar(String segmentId, ReadableQueryConfig qc) {

    List<Map<String, PrimitiveTypeProvider>> rows = this.selector.getRows("id", segmentId);
    
    if (rows.isEmpty()) {
      return Collections.emptyList();
    }
    
    ArrayList<WeightedTag> wtags = new ArrayList<>(rows.size());
    
    for (Map<String, PrimitiveTypeProvider> row : rows) {
      wtags.add(new IncompleteTag(row.get("tagid").getString(), "", "", row.get("score").getFloat()));
    }
    
    return getSimilar(wtags);
  }

  @Override
  public void init(PersistencyWriterSupplier phandlerSupply) {
    this.phandler = phandlerSupply.get();
    this.writer = new BatchedTagWriter(this.phandler, this.tableName, 10);
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
