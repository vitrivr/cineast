package org.vitrivr.cineast.api.util;

import gnu.trove.map.hash.TObjectDoubleHashMap;
import org.vitrivr.cineast.api.messages.query.QueryComponent;
import org.vitrivr.cineast.api.messages.query.QueryTerm;
import org.vitrivr.cineast.core.config.ReadableQueryConfig;
import org.vitrivr.cineast.core.data.Pair;
import org.vitrivr.cineast.core.data.StringDoublePair;
import org.vitrivr.cineast.core.data.providers.primitive.PrimitiveTypeProvider;
import org.vitrivr.cineast.core.data.providers.primitive.StringTypeProvider;
import org.vitrivr.cineast.core.data.query.containers.QueryContainer;
import org.vitrivr.cineast.core.data.score.SegmentScoreElement;
import org.vitrivr.cineast.core.db.DBSelector;
import org.vitrivr.cineast.core.features.AudioTranscriptionSearch;
import org.vitrivr.cineast.core.features.DescriptionTextSearch;
import org.vitrivr.cineast.core.features.OCRSearch;
import org.vitrivr.cineast.core.features.SegmentTags;
import org.vitrivr.cineast.core.util.MathHelper;
import org.vitrivr.cineast.standalone.config.Config;
import org.vitrivr.cineast.standalone.util.ContinuousRetrievalLogic;

import java.util.*;

//TODO maybe this should be moved to core?
public class QueryUtil {

    public static HashMap<String, ArrayList<QueryContainer>> groupComponentsByCategory(List<QueryComponent> queryComponents) {
        HashMap<String, ArrayList<QueryContainer>> categoryMap = new HashMap<>();
        for (QueryComponent component : queryComponents) {
            for (QueryTerm term : component.getTerms()) {
                if (term.getCategories() == null) {
                    continue;
                }
                term.getCategories().forEach((String category) -> {
                    if (!categoryMap.containsKey(category)) {
                        categoryMap.put(category, new ArrayList<>());
                    }
                    categoryMap.get(category).add(term.toContainer());
                });
            }
        }
        return categoryMap;
    }

    public static HashMap<String, ArrayList<Pair<QueryContainer, ReadableQueryConfig>>> groupTermsByCategory(List<org.vitrivr.cineast.api.grpc.data.QueryTerm> terms) {
        HashMap<String, ArrayList<Pair<QueryContainer, ReadableQueryConfig>>> categoryMap = new HashMap<>();
        for (org.vitrivr.cineast.api.grpc.data.QueryTerm term : terms) {
            if (term.getCategories().isEmpty()) {
                continue;
            }
            term.getCategories().forEach((String category) -> {
                if (!categoryMap.containsKey(category)) {
                    categoryMap.put(category, new ArrayList<>());
                }
                categoryMap.get(category).add(new Pair<>(term.getContainer(), term.getQueryConfig()));
            });

        }
        return categoryMap;
    }

    public static List<StringDoublePair> retrieveCategory(ContinuousRetrievalLogic continuousRetrievalLogic, List<Pair<QueryContainer, ReadableQueryConfig>> queryContainers, String category) {
        TObjectDoubleHashMap<String> scoreBySegmentId = new TObjectDoubleHashMap<>();
        for (Pair<QueryContainer, ReadableQueryConfig> pair : queryContainers) {

            if (pair == null) {
                continue;
            }

            QueryContainer qc = pair.first;
            ReadableQueryConfig qconf = pair.second;

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

        final int MAX_RESULTS = queryContainers.get(0).second.getMaxResults().orElse(Config.sharedConfig().getRetriever().getMaxResults());
        List<StringDoublePair> resultList = list;
        if (list.size() > MAX_RESULTS) {
            resultList = resultList.subList(0, MAX_RESULTS);
        }
        return resultList;
    }

    public static List<StringDoublePair> retrieve(ContinuousRetrievalLogic continuousRetrievalLogic, QueryContainer queryContainer, ReadableQueryConfig config, String category) {
        float weight = MathHelper.limit(queryContainer.getWeight(), -1f, 1f);
        TObjectDoubleHashMap<String> scoreBySegmentId = new TObjectDoubleHashMap<>();

        List<SegmentScoreElement> scoreResults;
        if (queryContainer.hasId()) {
            scoreResults = continuousRetrievalLogic.retrieve(queryContainer.getId(), category, config);
        } else {
            scoreResults = continuousRetrievalLogic.retrieve(queryContainer, category, config);
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

        final List<StringDoublePair> list = new ArrayList<>(scoreBySegmentId.size());
        scoreBySegmentId.forEachEntry((segmentId, score) -> {
            if (score > 0) {
                list.add(new StringDoublePair(segmentId, score));
            }
            return true;
        });

        return list;
    }

    public static List<String> retrieveTagsBySegmentId(String segmentId) {
        List<String> result = new ArrayList<>();
        DBSelector selector = Config.sharedConfig().getDatabase().getSelectorSupplier().get();
        selector.open(SegmentTags.SEGMENT_TAGS_TABLE_NAME);
        List<Map<String, PrimitiveTypeProvider>> rows = selector.getRows("id", new StringTypeProvider(segmentId));
        rows.forEach(row -> result.add(row.get("tagid").getString()));
        return result;
    }

    public static List<String> retrieveCaptionBySegmentId(String segmentId) {
        List<String> result = new ArrayList<>();
        DBSelector selector = Config.sharedConfig().getDatabase().getSelectorSupplier().get();
        selector.open(DescriptionTextSearch.DESCRIPTION_TEXT_TABLE_NAME);
        List<Map<String, PrimitiveTypeProvider>> rows = selector.getRows("id", new StringTypeProvider(segmentId));
        rows.forEach(row -> result.add(row.get("feature").getString()));
        return result;
    }

    public static List<String> retrieveOCRBySegmentId(String segmentId) {
        List<String> result = new ArrayList<>();
        DBSelector selector = Config.sharedConfig().getDatabase().getSelectorSupplier().get();
        selector.open(OCRSearch.OCR_TABLE_NAME);
        List<Map<String, PrimitiveTypeProvider>> rows = selector.getRows("id", new StringTypeProvider(segmentId));
        rows.forEach(row -> result.add(row.get("feature").getString()));
        return result;
    }

    public static List<String> retrieveASRBySegmentId(String segmentId) {
        List<String> result = new ArrayList<>();
        DBSelector selector = Config.sharedConfig().getDatabase().getSelectorSupplier().get();
        selector.open(AudioTranscriptionSearch.AUDIO_TRANSCRIPTION_TABLE_NAME);
        List<Map<String, PrimitiveTypeProvider>> rows = selector.getRows("id", new StringTypeProvider(segmentId));
        rows.forEach(row -> result.add(row.get("feature").getString()));
        return result;
    }

}
