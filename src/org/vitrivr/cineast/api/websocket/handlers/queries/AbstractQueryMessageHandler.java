package org.vitrivr.cineast.api.websocket.handlers.queries;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import org.vitrivr.cineast.api.websocket.handlers.abstracts.StatelessWebsocketMessageHandler;
import org.vitrivr.cineast.core.config.Config;
import org.vitrivr.cineast.core.data.StringDoublePair;
import org.vitrivr.cineast.core.data.entities.MultimediaObjectDescriptor;
import org.vitrivr.cineast.core.data.entities.SegmentDescriptor;
import org.vitrivr.cineast.core.db.dao.reader.MultimediaObjectLookup;
import org.vitrivr.cineast.core.db.dao.reader.SegmentLookup;

/**
 * @author rgasser
 * @version 1.0
 * @created 27.04.17
 */
public abstract class AbstractQueryMessageHandler<T> extends StatelessWebsocketMessageHandler<T> {
    /** SegmentLookup instance used to read segments from the storage layer. */
    private final SegmentLookup segmentLookup = new SegmentLookup();

    /** MultimediaObjectLookup instance used to read multimedia objects from the storage layer. */
    private final MultimediaObjectLookup multimediaObjectLookup = new MultimediaObjectLookup();

    /** */
    protected final int MAX_RESULTS = Config.sharedConfig().getRetriever().getMaxResults();

    /**
     *
     * @param results
     * @return
     */
    protected List<SegmentDescriptor> loadSegments(List<StringDoublePair> results) {
        ArrayList<SegmentDescriptor> sdList = new ArrayList<>(results.size());

        String[] ids = new String[results.size()];
        int i = 0;
        for (StringDoublePair sdp : results) {
            ids[i++] = sdp.key;
        }

        Map<String, SegmentDescriptor> map = this.segmentLookup.lookUpSegments(Arrays.asList(ids));

        for (String id : ids) {
            SegmentDescriptor sd = map.get(id);
            if (sd != null) {
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
    protected List<MultimediaObjectDescriptor> loadObjects(List<StringDoublePair> results) {
        String[] ids = new String[results.size()];
        int i = 0;
        for (StringDoublePair sdp : results) {
            ids[i++] = sdp.key;
        }

        Map<String, SegmentDescriptor> map = this.segmentLookup.lookUpSegments(Arrays.asList(ids));

        HashSet<String> videoIds = new HashSet<>();
        for (String id : ids) {
            SegmentDescriptor sd = map.get(id);
            if (sd == null) {
                continue;
            }
            videoIds.add(sd.getObjectId());
        }

        String[] vids = new String[videoIds.size()];
        i = 0;
        for (String vid : videoIds) {
            vids[i++] = vid;
        }

        ArrayList<MultimediaObjectDescriptor> vdList = new ArrayList<>(vids.length);

        Map<String, MultimediaObjectDescriptor> vmap = this.multimediaObjectLookup.lookUpObjects(vids);

        for (String vid : vids) {
            vdList.add(vmap.get(vid));
        }

        return vdList;
    }
}
