package org.vitrivr.cineast.api.rest.handlers;

import org.vitrivr.cineast.api.rest.handlers.basic.ParsingActionHandler;
import org.vitrivr.cineast.core.config.Config;
import org.vitrivr.cineast.core.config.QueryConfig;
import org.vitrivr.cineast.core.data.StringDoublePair;
import org.vitrivr.cineast.core.data.queries.Query;
import org.vitrivr.cineast.core.data.queries.QueryComponent;
import org.vitrivr.cineast.core.data.queries.QueryTerm;

import java.util.*;

/**
 * @author rgasser
 * @version 1.0
 * @created 11.01.17
 */
public class FindObjectSimilarActionHandler extends ParsingActionHandler<Query> {
    @Override
    public Object invoke(Query query, Map<String, String> parameters) {

        /**
         * Prepare map that maps categories to QueryTerm components.
         */
        HashMap<String, ArrayList<QueryTerm>> categoryMap = new HashMap<>();
        for (QueryComponent component : query.getComponents()) {
            for (QueryTerm term :  component.getTerms()) {
                if(term.getWeight() == 0f || term.getCategories() == null){
                    continue;
                }
                term.getCategories().forEach((String category) -> {
                    if(!categoryMap.containsKey(category)){
                        categoryMap.put(category, new ArrayList<QueryTerm>());
                    }
                    categoryMap.get(category).add(term);
                });
            }
        }

        QueryConfig qconf = Config.getQueryConfig();
        List<StringDoublePair> result;

        return qconf;
    }

    @Override
    public Class<Query> inClass() {
        return Query.class;
    }
}
