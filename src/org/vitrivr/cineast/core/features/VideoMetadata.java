package org.vitrivr.cineast.core.features;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.vitrivr.cineast.core.data.StringDoublePair;
import org.vitrivr.cineast.core.data.providers.primitive.PrimitiveTypeProvider;
import org.vitrivr.cineast.core.db.SegmentLookup;
import org.vitrivr.cineast.core.data.entities.SegmentDescriptor;
import org.vitrivr.cineast.core.features.abstracts.SolrTextRetriever;
import org.vitrivr.cineast.core.util.MathHelper;

public class VideoMetadata extends SolrTextRetriever {

  private static final int STEPSIZE = 10;
  
  @Override
  protected String getEntityName() {
    return "features_meta";
  }


  @Override
  protected ArrayList<StringDoublePair> processResults(String query,
      List<Map<String, PrimitiveTypeProvider>> resultList) {
    
    
    SegmentLookup lookup = new SegmentLookup();
    
    ArrayList<StringDoublePair> pairs = new ArrayList<>(resultList.size() * 50);
    
    int words = query.split("\\s+").length;
    
    for(Map<String, PrimitiveTypeProvider> result : resultList){
      String id = result.get("id").getString();
      float score = MathHelper.limit(result.get("ap_score").getFloat() / words / 10f, 0f, 1f);
     
      List<SegmentDescriptor> segments = lookup.lookUpAllSegments(id);
      for(int i = 0; i < segments.size(); i += STEPSIZE){
        pairs.add(new StringDoublePair(segments.get(i).getSegmentId(), score));
      }
    }
    
    lookup.close();
    
    return pairs;
  }

}
