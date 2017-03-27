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
import org.vitrivr.cineast.core.data.StringDoublePair;
import org.vitrivr.cineast.core.data.providers.primitive.PrimitiveTypeProvider;
import org.vitrivr.cineast.core.data.segments.SegmentContainer;
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
  public List<StringDoublePair> getSimilar(SegmentContainer sc, ReadableQueryConfig qc) {
    if (this.selector == null) {
      return new ArrayList<>(0);
    }

    HashMap<String, String> parameters = new HashMap<>();
    parameters.put("rows", Integer.toString(Config.sharedConfig().getRetriever().getMaxResultsPerModule()));

    List<SubtitleItem> subItems = sc.getSubtitleItems();
    
    if(subItems.isEmpty()){
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
    
    ArrayList<StringDoublePair> pairs = processResults(query, resultList);
    
    return pairs;
  }

  protected ArrayList<StringDoublePair> processResults(String query,
      List<Map<String, PrimitiveTypeProvider>> resultList) {
    ArrayList<StringDoublePair> pairs = new ArrayList<>(resultList.size());
    
    int words = query.split("\\s+").length;
    
    for(Map<String, PrimitiveTypeProvider> result : resultList){
      String id = result.get("id").getString();
      float score = result.get("ap_score").getFloat();
      
      pairs.add(new StringDoublePair(id, MathHelper.limit(score / words / 10f, 0f, 1f)));
    }
    return pairs;
  }

  @Override
  public List<StringDoublePair> getSimilar(String shotId, ReadableQueryConfig qc) {
    return new ArrayList<>(0); // currently not supported
  }

  @Override
  public void finish() {
    if (this.selector != null) {
      this.selector.close();
      this.selector = null;
    }

  }

}
