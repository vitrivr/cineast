package org.vitrivr.cineast.api.util;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.RemovalListener;
import com.google.common.cache.RemovalNotification;
import org.jetbrains.annotations.NotNull;
import org.vitrivr.cineast.api.messages.lookup.QueryCacheInfo;
import org.vitrivr.cineast.core.data.Pair;
import org.vitrivr.cineast.core.data.StringDoublePair;
import org.vitrivr.cineast.core.data.TemporalObject;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

public class QueryResultCache {

    private static final ConcurrentHashMap<String, QueryCacheInfo> cacheInfoMap = new ConcurrentHashMap<>();
    private static final Cache<String, Map<String, List<StringDoublePair>>> queryResultCache = CacheBuilder.newBuilder()
            .maximumSize(100) //TODO make configurable
            .expireAfterWrite(10, TimeUnit.MINUTES) //TODO make configurable
            .removalListener(new RemovalListener<String, Map<String, List<StringDoublePair>>>() {
                @Override
                public void onRemoval(@NotNull RemovalNotification<String, Map<String, List<StringDoublePair>>> notification) {
                    String queryId = notification.getKey();
                    if (queryId != null) {
                        cacheInfoMap.remove(queryId);
                    }
                }
            })
            .build();

    @NotNull
    private static QueryCacheInfo generateInfo(String queryId, Map<String, List<StringDoublePair>> result) {

        HashSet<String> segmentIds = new HashSet<>();
        for (List<StringDoublePair> results : result.values()) {
            segmentIds.addAll(results.stream().map(StringDoublePair::key).collect(Collectors.toSet()));
        }

        return new QueryCacheInfo(queryId, System.currentTimeMillis(), segmentIds.size());
    }

    public static synchronized void cacheResult(String queryId, Map<String, List<StringDoublePair>> result) {
        QueryCacheInfo info = generateInfo(queryId, result);
        cacheInfoMap.put(queryId, info);
        queryResultCache.put(queryId, result);
    }

    public static List<QueryCacheInfo> getQueryCacheInfoList() {
        queryResultCache.cleanUp(); //trigger cleanup to avoid listing element that might not be available anymore shortly
        return cacheInfoMap.values().stream().toList();
    }

    public static Map<String, List<StringDoublePair>> getCachedResult(String queryId) {
        Map<String, List<StringDoublePair>> result = queryResultCache.getIfPresent(queryId);
        if (result == null) {
            return Collections.emptyMap();
        }
        return result;
    }

    public static void cacheResult(String queryId, List<TemporalObject> temporalResults) {
        cacheResult(queryId, convert(temporalResults));
    }

    //apparently, temporal aggregation does not respect categories ?!
    private static Map<String, List<StringDoublePair>> convert(List<TemporalObject> temporalResults) {

        List<StringDoublePair> pairs = new ArrayList<>(temporalResults.size() * 10);

        for (TemporalObject tobj : temporalResults) {
            double score = tobj.score();
            pairs.addAll(tobj.segments().stream().map(id -> new StringDoublePair(id, score)).toList());
        }

        HashMap<String, List<StringDoublePair>> map = new HashMap<>();
        map.put("temporal", pairs);

        return map;
    }

    public static void cacheResult(String queryId, ConcurrentLinkedQueue<Pair<String, List<StringDoublePair>>> rawCategoryResults) {
        cacheResult(queryId, convert(rawCategoryResults));
    }

    //basic temporal aggregation to adhere to caching format
    private static Map<String, List<StringDoublePair>> convert(Queue<Pair<String, List<StringDoublePair>>> rawCategoryResults) {
        Set<String> categories = rawCategoryResults.stream().map(x -> x.first).collect(Collectors.toSet());

        HashMap<String, List<StringDoublePair>> results = new HashMap<>(categories.size());

        for (String category : categories) {

            List<List<StringDoublePair>> categoryResults = rawCategoryResults.stream().filter(x -> x.first.equals(category)).map(x -> x.second).toList();

            if (categoryResults.isEmpty()) {
                continue;
            }

            if (categoryResults.size() == 1) {
                results.put(category, categoryResults.get(0));
            } else {

                HashSet<String> ids = new HashSet<>();
                List<StringDoublePair> faltList = categoryResults.stream().flatMap(Collection::stream).sorted(StringDoublePair.COMPARATOR).filter(pair -> { //just take best score per segment
                    if (!ids.contains(pair.key())) {
                        ids.add(pair.key());
                        return true;
                    } else {
                        return false;
                    }
                }).toList();

                results.put(category, faltList);

            }

        }

        return results;

    }

}
