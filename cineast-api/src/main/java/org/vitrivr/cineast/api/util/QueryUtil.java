package org.vitrivr.cineast.api.util;

import gnu.trove.map.hash.TObjectDoubleHashMap;
import org.vitrivr.cineast.api.messages.query.QueryComponent;
import org.vitrivr.cineast.api.messages.query.QueryTerm;
import org.vitrivr.cineast.core.config.QueryConfig;
import org.vitrivr.cineast.core.data.StringDoublePair;
import org.vitrivr.cineast.core.data.query.containers.QueryContainer;
import org.vitrivr.cineast.core.data.score.SegmentScoreElement;
import org.vitrivr.cineast.core.util.MathHelper;
import org.vitrivr.cineast.standalone.config.Config;
import org.vitrivr.cineast.standalone.util.ContinuousRetrievalLogic;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

//TODO maybe this should be moved to core?
public class QueryUtil {

    public static HashMap<String, ArrayList<QueryContainer>> groupByCategory(List<QueryComponent> queryComponents) {
        HashMap<String, ArrayList<QueryContainer>> categoryMap = new HashMap<>();
        for (QueryComponent component : queryComponents) {
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
        return categoryMap;
    }

    public static List<StringDoublePair> retrieveCategory(ContinuousRetrievalLogic continuousRetrievalLogic, ArrayList<QueryContainer> queryContainers, QueryConfig qconf, String category) {
        TObjectDoubleHashMap<String> scoreBySegmentId = new TObjectDoubleHashMap<>();
        for (QueryContainer qc : queryContainers) {

            if (qc == null) {
                continue;
            }

            float weight = MathHelper.limit(qc.getWeight(), -1f, 1f);

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
        return resultList;
    }

}
