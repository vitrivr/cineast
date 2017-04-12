package org.vitrivr.cineast.core.features.abstracts;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.vitrivr.cineast.core.config.Config;
import org.vitrivr.cineast.core.config.ReadableQueryConfig;
import org.vitrivr.cineast.core.data.CorrespondenceFunction;
import org.vitrivr.cineast.core.data.providers.primitive.PrimitiveTypeProvider;
import org.vitrivr.cineast.core.data.score.ScoreElement;
import org.vitrivr.cineast.core.data.score.SegmentScoreElement;
import org.vitrivr.cineast.core.data.segments.SegmentContainer;
import org.vitrivr.cineast.core.data.providers.primitive.PrimitiveTypeProvider;
import org.vitrivr.cineast.core.db.ADAMproSelector;
import org.vitrivr.cineast.core.db.DBSelector;
import org.vitrivr.cineast.core.db.DBSelectorSupplier;
import org.vitrivr.cineast.core.decode.subtitle.SubtitleItem;
import org.vitrivr.cineast.core.features.retriever.Retriever;
import org.vitrivr.cineast.core.setup.EntityCreator;
import org.vitrivr.cineast.core.util.MathHelper;

/**
 * This is a proof of concept class and will probably be replaced by a more general solution to text
 * retrieval in the future
 * 
 *
 */
public abstract class SolrTextRetriever implements Retriever {

  private ADAMproSelector selector = null; // this is necessary since there is no abstraction for
                                           // the way external providers are handled in ADAMpro
                                           // (yet)

  private static final Logger LOGGER = LogManager.getLogger();

  @Override
  public void initalizePersistentLayer(Supplier<EntityCreator> supply) {
  }

  @Override
  public void dropPersistentLayer(Supplier<EntityCreator> supply) {
  }

  protected abstract String getEntityName();

  @Override
  public void init(DBSelectorSupplier selectorSupply) {
    DBSelector s = selectorSupply.get();
    if (s instanceof ADAMproSelector) {
      this.selector = (ADAMproSelector) s;
      this.selector.open(getEntityName());
    } else {
      LOGGER.warn(
          "SolrTextRetriever only works with ADAMproSelectors, {} is currently not supported",
          s.getClass().getSimpleName());
    }
  }

  @Override
  public List<ScoreElement> getSimilar(String shotId, ReadableQueryConfig qc) {
    return new ArrayList<>(0); // currently not supported
  }

  @Override
  public List<ScoreElement> getSimilar(SegmentContainer sc, ReadableQueryConfig qc) {
    if (this.selector == null) {
      return new ArrayList<>(0);
    }

    HashMap<String, String> parameters = new HashMap<>();
    parameters.put("rows", Integer.toString(Config.sharedConfig().getRetriever().getMaxResultsPerModule()));

    List<SubtitleItem> subItems = sc.getSubtitleItems();
    
    if (subItems.isEmpty()) {
      return new ArrayList<>(0);
    }
    
    StringBuilder sb = new StringBuilder();
    sb.append('(');
    for (SubtitleItem subItem : subItems) {
      sb.append(subItem.getText());
      sb.append(' ');
    }
    sb.append(')');
    String query = sb.toString();
   

    parameters.put("query", "feature:" + query);

    List<Map<String, PrimitiveTypeProvider>> resultList = this.selector.getFromExternal("solr",
        parameters);

    return processResults(query, resultList);
  }

  // Internally, this method only creates SegmentScoreElements
  protected List<ScoreElement> processResults(String query,
      List<Map<String, PrimitiveTypeProvider>> resultList) {
    int words = query.split("\\s+").length;
    // Using CorrespondenceFunction to ensure that the scores are within [0,1]
    CorrespondenceFunction f = CorrespondenceFunction.fromFunction(score -> score / words / 10f);

    List<ScoreElement> scoreElements = new ArrayList<>(resultList.size());
    for (Map<String, PrimitiveTypeProvider> result : resultList) {
      String id = result.get("id").getString();
      double score = f.applyAsDouble(result.get("ap_score").getFloat());
      scoreElements.add(new SegmentScoreElement(id, score));
    }
    return scoreElements;
  }

  @Override
  public void finish() {
    if (this.selector != null) {
      this.selector.close();
      this.selector = null;
    }
  }
}
