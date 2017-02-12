package org.vitrivr.cineast.api.rest.handlers.actions;

import gnu.trove.map.hash.TObjectDoubleHashMap;

import org.vitrivr.cineast.api.rest.handlers.abstracts.ParsingActionHandler;
import org.vitrivr.cineast.core.config.Config;
import org.vitrivr.cineast.core.config.QueryConfig;
import org.vitrivr.cineast.core.data.StringDoublePair;
import org.vitrivr.cineast.core.data.messages.query.Query;
import org.vitrivr.cineast.core.data.messages.query.QueryComponent;
import org.vitrivr.cineast.core.data.messages.query.QueryTerm;
import org.vitrivr.cineast.core.data.query.containers.QueryContainer;
import org.vitrivr.cineast.core.util.ContinousRetrievalLogic;

import java.util.*;

/**
 * @author rgasser
 * @version 1.0
 * @created 11.01.17
 */
public class FindObjectSimilarActionHandler extends ParsingActionHandler<Query> {
    @Override
    public Object invoke(Query query, Map<String, String> parameters) {

        /*
         * Prepare map that maps categories to QueryTerm components.
         */
        HashMap<String, ArrayList<QueryContainer>> categoryMap = new HashMap<>();
        for (QueryComponent component : query.getContainers()) {
            for (QueryTerm term :  component.getTerms()) {
                if(term.getCategories() == null){
                    continue;
                }
                term.getCategories().forEach((String category) -> {
                    if(!categoryMap.containsKey(category)){
                        categoryMap.put(category, new ArrayList<QueryContainer>());
                    }
                    categoryMap.get(category).add(term.toContainer());
                });
            }
        }

        QueryConfig qconf = Config.getQueryConfig();
        List<StringDoublePair> result;
        for(String category : categoryMap.keySet()){
            TObjectDoubleHashMap<String> map = new TObjectDoubleHashMap<>();
            for(QueryContainer qc : categoryMap.get(category)){

                float weight = qc.getWeight() > 0f ? 1f : -1f; //TODO better normalisation

                if(qc.hasId()){
                    result = ContinousRetrievalLogic.retrieve(qc.getId(), category, qconf);
                }else{
                    result = ContinousRetrievalLogic.retrieve(qc, category, qconf);
                }

                for (StringDoublePair pair : result) {
                    if (Double.isInfinite(pair.value) || Double.isNaN(pair.value)) {
                        continue;
                    }
                    if (map.contains(pair.key)) {
                        map.put(pair.key, map.get(pair.key) + pair.value * weight);
                        continue;
                    }
                    map.put(pair.key, pair.value * weight);
                }

                List<StringDoublePair> list = new ArrayList<>(map.size());
                Set<String> keys = map.keySet();
                for (String key : keys) {
                    double val = map.get(key);
                    if (val > 0) {
                        list.add(new StringDoublePair(key, val));
                    }
                }

                Collections.sort(list, StringDoublePair.COMPARATOR);

                int MAX_RESULTS = Config.getRetrieverConfig().getMaxResults();
                if (list.size() > MAX_RESULTS) {
                    list = list.subList(0, MAX_RESULTS);
                }
            }
        }

        return null;
    }

    @Override
    public Class<Query> inClass() {
        return Query.class;
    }
}
