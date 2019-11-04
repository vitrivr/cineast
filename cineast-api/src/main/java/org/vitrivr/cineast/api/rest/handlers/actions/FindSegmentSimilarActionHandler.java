package org.vitrivr.cineast.api.rest.handlers.actions;

import org.vitrivr.cineast.api.rest.exceptions.MethodNotSupportedException;
import org.vitrivr.cineast.api.rest.handlers.abstracts.ParsingActionHandler;
import org.vitrivr.cineast.api.util.QueryUtil;
import org.vitrivr.cineast.core.config.QueryConfig;
import org.vitrivr.cineast.core.config.ReadableQueryConfig;
import org.vitrivr.cineast.core.data.Pair;
import org.vitrivr.cineast.core.data.StringDoublePair;
import org.vitrivr.cineast.api.messages.query.SimilarityQuery;
import org.vitrivr.cineast.api.messages.result.SimilarityQueryResultBatch;
import org.vitrivr.cineast.core.data.query.containers.QueryContainer;
import org.vitrivr.cineast.standalone.config.ConstrainedQueryConfig;
import org.vitrivr.cineast.standalone.util.ContinuousRetrievalLogic;

import java.util.*;
import java.util.stream.Collectors;

/**
 * @author rgasser
 * @version 1.0
 * @created 11.01.17
 */
public class FindSegmentSimilarActionHandler extends ParsingActionHandler<SimilarityQuery> {

    private final ContinuousRetrievalLogic continuousRetrievalLogic;
    public FindSegmentSimilarActionHandler(ContinuousRetrievalLogic retrievalLogic){
        this.continuousRetrievalLogic = retrievalLogic;
    }

    /**
     * Processes a HTTP GET request. Always throws a {@link MethodNotSupportedException}
     *
     * @param parameters Map containing named parameters in the URL.
     * @throws MethodNotSupportedException Always
     */
    public Object doGet(Map<String, String> parameters) throws MethodNotSupportedException {
        throw new MethodNotSupportedException("HTTP GET is not supported for FindSegmentSimilarActionHandler.");
    }

    /**
     * Processes a HTTP GET request. Performs a similarity search given the {@link SimilarityQuery}.
     *
     * @param query The {@link SimilarityQuery} objects used to perform the search.
     * @param parameters Map containing named parameters in the URL.
     * @return
     */
    @Override
    public SimilarityQueryResultBatch doPost(SimilarityQuery query, Map<String, String> parameters) {

        HashMap<String, List<StringDoublePair>> returnMap = new HashMap<>();
        /*
         * Prepare map that maps categories to QueryTerm components.
         */
        HashMap<String, ArrayList<QueryContainer>> categoryMap = QueryUtil.groupComponentsByCategory(query.getComponents());

        ReadableQueryConfig qconf = new ConstrainedQueryConfig();

        for (String category : categoryMap.keySet()) {
            List<Pair<QueryContainer, ReadableQueryConfig>> containerList = categoryMap.get(category).stream().map(x -> new Pair<>(x, qconf)).collect(Collectors.toList());
            returnMap.put(category, QueryUtil.retrieveCategory(continuousRetrievalLogic, containerList, category));
        }

        return new SimilarityQueryResultBatch(returnMap, qconf.getQueryId().toString());
    }


    @Override
    public Class<SimilarityQuery> inClass() {
        return SimilarityQuery.class;
    }
}
