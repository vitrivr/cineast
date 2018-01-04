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
     * Performs a lookup for the {@link SegmentDescriptor} identified by the provided ID's and returns a list of the
     * {@link SegmentDescriptor}s that were found.
     *
     * @param segmentIds List of segment ID's that should be looked up.
     * @return List of found {@link SegmentDescriptor}
     */
    protected List<SegmentDescriptor> loadSegments(List<String> segmentIds) {
        final Map<String, SegmentDescriptor> map = this.segmentLookup.lookUpSegments(segmentIds);
        final ArrayList<SegmentDescriptor> sdList = new ArrayList<>(map.size());
        segmentIds.stream().filter(map::containsKey).forEach(s -> sdList.add(map.get(s)));
        return sdList;
    }

    /**
     * Performs a lookup for the {@link MultimediaObjectDescriptor} identified by the provided object ID's and returns
     * a list of the {@link SegmentDescriptor}s that were found.
     *
     * @param objectIds  List of object ID's that should be looked up.
     * @return List of found {@link MultimediaObjectDescriptor}
     */
    protected List<MultimediaObjectDescriptor> loadObjects(List<String> objectIds) {
        final Map<String, MultimediaObjectDescriptor> map = this.multimediaObjectLookup.lookUpObjects(objectIds);
        final ArrayList<MultimediaObjectDescriptor> vdList = new ArrayList<>(map.size());
        objectIds.stream().filter(map::containsKey).forEach(s -> vdList.add(map.get(s)));
        return vdList;
    }
}
