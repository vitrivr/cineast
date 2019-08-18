package org.vitrivr.cineast.core.features;

import gnu.trove.map.hash.TObjectFloatHashMap;

import java.util.*;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.vitrivr.cineast.core.config.ReadableQueryConfig;
import org.vitrivr.cineast.core.data.providers.primitive.PrimitiveTypeProvider;
import org.vitrivr.cineast.core.data.score.ScoreElement;
import org.vitrivr.cineast.core.data.score.SegmentScoreElement;
import org.vitrivr.cineast.core.data.segments.SegmentContainer;
import org.vitrivr.cineast.core.data.tag.Tag;
import org.vitrivr.cineast.core.data.tag.WeightedTag;
import org.vitrivr.cineast.core.db.DBSelector;
import org.vitrivr.cineast.core.db.DBSelectorSupplier;
import org.vitrivr.cineast.core.db.PersistencyWriter;
import org.vitrivr.cineast.core.db.PersistencyWriterSupplier;
import org.vitrivr.cineast.core.db.dao.reader.TagReader;
import org.vitrivr.cineast.core.db.setup.AttributeDefinition;
import org.vitrivr.cineast.core.db.setup.AttributeDefinition.AttributeType;
import org.vitrivr.cineast.core.db.setup.EntityCreator;
import org.vitrivr.cineast.core.features.extractor.Extractor;
import org.vitrivr.cineast.core.features.retriever.Retriever;

import java.util.function.Supplier;

public class Tags implements Extractor, Retriever {

  private static final String ENTITY_NAME = "feature_tags";

  private TagReader tagReader;

  private static final Logger LOGGER = LogManager.getLogger();

  protected DBSelector selector;
  protected PersistencyWriter<?> phandler;

  @Override
  public void initalizePersistentLayer(Supplier<EntityCreator> supply) {
    supply.get().createIdEntity(ENTITY_NAME, new AttributeDefinition("tag", AttributeType.STRING), new AttributeDefinition("score", AttributeType.FLOAT));
  }

  @Override
  public void dropPersistentLayer(Supplier<EntityCreator> supply) {
    supply.get().dropEntity(ENTITY_NAME);
  }

  @Override
  public void init(DBSelectorSupplier selectorSupply) {
    this.selector = selectorSupply.get();
    this.selector.open(ENTITY_NAME);
    this.tagReader = new TagReader(selectorSupply.get());
  }

  @Override
  public List<String> getTableNames() {
    return Collections.singletonList(ENTITY_NAME);
  }

  @Override
  public List<ScoreElement> getSimilar(SegmentContainer sc, ReadableQueryConfig qc) {

    TObjectFloatHashMap<String> tagScores = new TObjectFloatHashMap<>();

    for(Tag tag : sc.getTags()){
      if(!tag.hasId() && !tag.hasName()){
        LOGGER.info("skipping empty tag");
        continue;
      }

      if(!tag.hasId()){
        List<Tag> tags = this.tagReader.getTagsByName(tag.getName());

        for(Tag t : tags){
          tagScores.put(t.getId(), 1f);
        }

      }else{
        tagScores.put(tag.getId(), 1f);
      }

    }

    return processTagScores(tagScores);

  }

  @Override
  public List<ScoreElement> getSimilar(String shotId, ReadableQueryConfig qc) {

    List<Map<String, PrimitiveTypeProvider>> rows = this.selector.getRows("id", shotId);

    TObjectFloatHashMap<String> tagScores = new TObjectFloatHashMap<>();

    for(Map<String, PrimitiveTypeProvider> row : rows){
      String tagId = row.get("tag").getString();
      float score = row.get("score").getFloat();
      tagScores.put(tagId, score);
    }

    return processTagScores(tagScores);

  }

  /**
   * @param tagScores maps {@link Tag#getId()} to the weight / confidence of the corresponding tag.
   * This value is then multiplied with the score of the retrieved tag to get the score.
   */
  private List<ScoreElement> processTagScores(TObjectFloatHashMap<String> tagScores){
    List<Map<String, PrimitiveTypeProvider>> tagInstances = this.selector.getRows("tag", tagScores.keySet());

    TObjectFloatHashMap<String> result = new TObjectFloatHashMap<>();

    for(Map<String, PrimitiveTypeProvider> instance : tagInstances){
      String segmentId = instance.get("id").getString();
      String tagId = instance.get("tag").getString();
      float score = instance.get("score").getFloat();

      float addScore = score * tagScores.get(tagId);

      result.adjustOrPutValue(segmentId, addScore, addScore);
    }

    ArrayList<ScoreElement> _return = new ArrayList<>(result.size());

    float normalizer = 0f;

    for(String id: tagScores.keySet()){
      normalizer += tagScores.get(id);
    }

    for(String id: result.keySet()){
      _return.add(new SegmentScoreElement(id, result.get(id) / normalizer));
    }

    return _return;
  }

  @Override
  public void init(PersistencyWriterSupplier phandlerSupply) {
    this.phandler = phandlerSupply.get();
    this.phandler.setFieldNames("id", "tag", "score");
  }

  @Override
  public void processSegment(SegmentContainer segment) {
    for(Tag tag : segment.getTags()){
      if(!tag.hasId() && !tag.hasName()){
        LOGGER.info("skipping empty tag");
        continue;
      }

      if(!tag.hasId()){
        List<Tag> tags = this.tagReader.getTagsByName(tag.getName());

        if(tags.isEmpty()){
          //TODO create new tags with new IDs
          //TODO populate list accordingly
        }

        for(Tag t : tags){
          writeTag(segment.getId(), t.getId());
        }

      }else{
        if(tag instanceof WeightedTag){
          writeTag(segment.getId(), tag.getId(), ((WeightedTag)tag).getWeight());
        }else{
          writeTag(segment.getId(), tag.getId());
        }
      }

    }
  }

  protected void writeTag(String segmentId, String tagId){
    writeTag(segmentId, tagId, 1f);
  }

  protected void writeTag(String segmentId, String tagId, float score){
    this.phandler.persist(this.phandler.generateTuple(segmentId, tagId, score));
  }

  @Override
  public void finish() {
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
