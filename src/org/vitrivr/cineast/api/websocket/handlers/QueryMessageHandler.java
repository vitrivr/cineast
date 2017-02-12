package org.vitrivr.cineast.api.websocket.handlers;

import gnu.trove.map.hash.TObjectDoubleHashMap;

import org.eclipse.jetty.websocket.api.Session;

import org.vitrivr.cineast.api.websocket.handlers.abstracts.StatelessWebsocketMessageHandler;
import org.vitrivr.cineast.core.config.Config;
import org.vitrivr.cineast.core.config.QueryConfig;
import org.vitrivr.cineast.core.data.StringDoublePair;
import org.vitrivr.cineast.core.data.entities.MultimediaObjectDescriptor;
import org.vitrivr.cineast.core.data.entities.SegmentDescriptor;
import org.vitrivr.cineast.core.data.messages.query.Query;
import org.vitrivr.cineast.core.data.messages.query.QueryComponent;
import org.vitrivr.cineast.core.data.messages.query.QueryTerm;
import org.vitrivr.cineast.core.data.messages.result.*;
import org.vitrivr.cineast.core.data.query.containers.QueryContainer;
import org.vitrivr.cineast.core.db.dao.reader.MultimediaObjectLookup;
import org.vitrivr.cineast.core.db.dao.reader.SegmentLookup;
import org.vitrivr.cineast.core.util.ContinousRetrievalLogic;

import java.util.*;

/**
 * @author rgasser
 * @version 1.0
 * @created 12.01.17
 */
public class QueryMessageHandler extends StatelessWebsocketMessageHandler<Query> {
    /**
     *
     * @param session
     * @param message
     */
    @Override
    public void handle(Session session, Query message) {
        /* Begin of Query: Send QueryStart Message to Client. */
        QueryStart startMarker = new QueryStart();
        this.write(session, startMarker);

        /*
         * Prepare map that maps categories to QueryTerm components.
         */
        HashMap<String, ArrayList<QueryContainer>> categoryMap = new HashMap<>();
        for (QueryComponent component : message.getContainers()) {
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

                this.write(session, new SegmentQueryResult(startMarker.getQueryId(), this.loadSegments(list)));
                this.write(session, new ObjectQueryResult(startMarker.getQueryId(), this.loadObjects(list)));
                this.write(session, new SimilarityQueryResult(startMarker.getQueryId(), category, list));

            }
        }

        /* End of Query: Send QueryEnd Message to Client. */
        this.write(session, new QueryEnd(startMarker.getQueryId()));
    }

    /**
     *
     * @param results
     * @return
     */
    private List<SegmentDescriptor> loadSegments(List<StringDoublePair> results) {
        ArrayList<SegmentDescriptor> sdList = new ArrayList<>(results.size());
        SegmentLookup sl = new SegmentLookup();

        String[] ids = new String[results.size()];
        int i = 0;
        for(StringDoublePair sdp : results){
            ids[i++] = sdp.key;
        }

        Map<String, SegmentDescriptor> map = sl.lookUpShots(ids);

        for(String id : ids){
            SegmentDescriptor sd = map.get(id);
            if(sd != null){
                sdList.add(sd);
            }
        }

        return sdList;
    }

    /**
     *
     * @param results
     * @return
     */
    private List<MultimediaObjectDescriptor> loadObjects(List<StringDoublePair> results) {
        SegmentLookup sl = new SegmentLookup();
        MultimediaObjectLookup vl = new MultimediaObjectLookup();

        String[] ids = new String[results.size()];
        int i = 0;
        for(StringDoublePair sdp : results){
            ids[i++] = sdp.key;
        }

        Map<String, SegmentDescriptor> map = sl.lookUpShots(ids);

        HashSet<String> videoIds = new HashSet<>();
        for(String id : ids){
            SegmentDescriptor sd = map.get(id);
            if(sd == null){
                continue;
            }
            videoIds.add(sd.getObjectId());
        }

        String[] vids = new String[videoIds.size()];
        i = 0;
        for(String vid : videoIds){
            vids[i++] = vid;
        }

        ArrayList<MultimediaObjectDescriptor> vdList = new ArrayList<>(vids.length);

        Map<String, MultimediaObjectDescriptor> vmap = vl.lookUpVideos(vids);

        for(String vid : vids){
            vdList.add(vmap.get(vid));
        }

        return vdList;
    }
}
