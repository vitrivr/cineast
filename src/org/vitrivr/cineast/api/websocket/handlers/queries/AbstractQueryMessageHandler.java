package org.vitrivr.cineast.api.websocket.handlers.queries;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.vitrivr.cineast.api.websocket.handlers.abstracts.StatelessWebsocketMessageHandler;

import org.vitrivr.cineast.core.data.entities.MediaObjectDescriptor;
import org.vitrivr.cineast.core.data.entities.MediaSegmentDescriptor;
import org.vitrivr.cineast.core.db.dao.reader.MediaObjectReader;
import org.vitrivr.cineast.core.db.dao.reader.MediaSegmentReader;

/**
 * @author rgasser
 * @version 1.0
 * @created 27.04.17
 */
public abstract class AbstractQueryMessageHandler<T> extends StatelessWebsocketMessageHandler<T> {
    /** MediaSegmentReader instance used to read segments from the storage layer. */
    private final MediaSegmentReader mediaSegmentReader = new MediaSegmentReader();

    /** MediaObjectReader instance used to read multimedia objects from the storage layer. */
    private final MediaObjectReader mediaObjectReader = new MediaObjectReader();

    /**
     * Performs a lookup for the {@link MediaSegmentDescriptor} identified by the provided ID's and returns a list of the
     * {@link MediaSegmentDescriptor}s that were found.
     *
     * @param segmentIds List of segment ID's that should be looked up.
     * @return List of found {@link MediaSegmentDescriptor}
     */
    protected List<MediaSegmentDescriptor> loadSegments(List<String> segmentIds) {
        final Map<String, MediaSegmentDescriptor> map = this.mediaSegmentReader.lookUpSegments(segmentIds);
        final ArrayList<MediaSegmentDescriptor> sdList = new ArrayList<>(map.size());
        segmentIds.stream().filter(map::containsKey).forEach(s -> sdList.add(map.get(s)));
        return sdList;
    }

    /**
     * Performs a lookup for the {@link MediaObjectDescriptor} identified by the provided object ID's and returns
     * a list of the {@link MediaSegmentDescriptor}s that were found.
     *
     * @param objectIds  List of object ID's that should be looked up.
     * @return List of found {@link MediaObjectDescriptor}
     */
    protected List<MediaObjectDescriptor> loadObjects(List<String> objectIds) {
        final Map<String, MediaObjectDescriptor> map = this.mediaObjectReader.lookUpObjects(objectIds);
        final ArrayList<MediaObjectDescriptor> vdList = new ArrayList<>(map.size());
        objectIds.stream().filter(map::containsKey).forEach(s -> vdList.add(map.get(s)));
        return vdList;
    }
}
