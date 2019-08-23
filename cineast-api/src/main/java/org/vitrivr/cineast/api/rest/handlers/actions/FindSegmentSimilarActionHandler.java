package org.vitrivr.cineast.api.rest.handlers.actions;

import gnu.trove.map.hash.TObjectDoubleHashMap;
import org.vitrivr.cineast.api.rest.exceptions.MethodNotSupportedException;
import org.vitrivr.cineast.api.rest.handlers.abstracts.ParsingActionHandler;
import org.vitrivr.cineast.core.config.QueryConfig;
import org.vitrivr.cineast.core.data.StringDoublePair;
import org.vitrivr.cineast.api.messages.query.QueryComponent;
import org.vitrivr.cineast.api.messages.query.QueryTerm;
import org.vitrivr.cineast.api.messages.query.SimilarityQuery;
import org.vitrivr.cineast.api.messages.result.SimilarityQueryResultBatch;
import org.vitrivr.cineast.core.data.query.containers.QueryContainer;
import org.vitrivr.cineast.core.data.score.SegmentScoreElement;
import org.vitrivr.cineast.standalone.config.Config;
import org.vitrivr.cineast.standalone.util.ContinuousRetrievalLogic;

import java.util.*;
import spark.route.HttpMethod;

/**
 * @author rgasser
 * @version 1.0
 * @created 11.01.17
 */
public class FindSegmentSimilarActionHandler extends ParsingActionHandler<SimilarityQuery, SimilarityQueryResultBatch> {

    private final ContinuousRetrievalLogic continuousRetrievalLogic;
    public FindSegmentSimilarActionHandler(ContinuousRetrievalLogic retrievalLogic){
        this.continuousRetrievalLogic = retrievalLogic;
    }

    {
        // ONLY supports POST
        supportedHttpMethods.clear();
        supportedHttpMethods.add(HttpMethod.post);
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
    public SimilarityQueryResultBatch doPost(SimilarityQuery query, Map<String, String> parameters) { //FIXME duplicate fusion logic

        HashMap<String, List<StringDoublePair>> returnMap = new HashMap<>();

        // TODO: Remove code duplication shared with FindObjectSimilarActionHandler
        /*
         * Prepare map that maps categories to QueryTerm components.
         */
        HashMap<String, ArrayList<QueryContainer>> categoryMap = new HashMap<>();
        for (QueryComponent component : query.getComponents()) {
            for (QueryTerm term : component.getTerms()) {
                if (term.getCategories() == null) {
                    continue;
                }
                term.getCategories().forEach((String category) -> {
                    if (!categoryMap.containsKey(category)) {
                        categoryMap.put(category, new ArrayList<QueryContainer>());
                    }
                    categoryMap.get(category).add(term.toContainer());
                });
            }
        }

        QueryConfig qconf = QueryConfig.newQueryConfigFromOther(Config.sharedConfig().getQuery());
        for (String category : categoryMap.keySet()) {
            TObjectDoubleHashMap<String> scoreBySegmentId = new TObjectDoubleHashMap<>();
            for (QueryContainer qc : categoryMap.get(category)) {

                if (qc == null) {
                    continue;
                }

                float weight = qc.getWeight() > 0f ? 1f : -1f; //TODO better normalisation

                List<SegmentScoreElement> scoreResults;
                if (qc.hasId()) {
                    scoreResults = continuousRetrievalLogic.retrieve(qc.getId(), category, qconf);
                } else {
                    scoreResults = continuousRetrievalLogic.retrieve(qc, category, qconf);
                }

                for (SegmentScoreElement element : scoreResults) {
                    String segmentId = element.getSegmentId();
                    double score = element.getScore();
                    if (Double.isInfinite(score) || Double.isNaN(score)) {
                        continue;
                    }
                    double weightedScore = score * weight;
                    scoreBySegmentId.adjustOrPutValue(segmentId, weightedScore, weightedScore);
                }

            }
            final List<StringDoublePair> list = new ArrayList<>(scoreBySegmentId.size());
            scoreBySegmentId.forEachEntry((segmentId, score) -> {
                if (score > 0) {
                    list.add(new StringDoublePair(segmentId, score));
                }
                return true;
            });

            Collections.sort(list, StringDoublePair.COMPARATOR);

            final int MAX_RESULTS = Config.sharedConfig().getRetriever().getMaxResults();
            List<StringDoublePair> resultList = list;
            if (list.size() > MAX_RESULTS) {
                resultList = resultList.subList(0, MAX_RESULTS);
            }
            returnMap.put(category, resultList);
        }

        return new SimilarityQueryResultBatch(returnMap, qconf.getQueryId().toString());
    }

    @Override
    public Class<SimilarityQuery> inClass() {
        return SimilarityQuery.class;
    }

    @Override
    public String getRoute() {
        return "find/segments/similar";
    }

    @Override
    public String getDescription() {
        return "Finds similar segments based on the given query";
    }

    @Override
    public Class<SimilarityQueryResultBatch> outClass() {
        return SimilarityQueryResultBatch.class;
    }
}
