package org.vitrivr.cineast.api.rest.handlers.actions;

import java.util.*;

import org.vitrivr.cineast.api.rest.handlers.abstracts.ParsingActionHandler;
import org.vitrivr.cineast.core.data.entities.SegmentDescriptor;
import org.vitrivr.cineast.core.data.messages.lookup.IdList;
import org.vitrivr.cineast.core.data.messages.result.ObjectQueryResult;
import org.vitrivr.cineast.core.db.dao.reader.SegmentLookup;

public class FindSegmentsByIdActionHandler extends ParsingActionHandler<IdList> {

    private final static String ID_NAME = ":id";

    /**
     * Processes a HTTP GET request.
     *
     * @param parameters Map containing named parameters in the URL.
     * @return {@link ObjectQueryResult}
     */
    @Override
    public Object doGet(Map<String, String> parameters) {
        final String segmentId = parameters.get(ID_NAME);
        final SegmentLookup sl = new SegmentLookup();
        final List<SegmentDescriptor> list = sl.lookUpSegment(segmentId).map(s -> {
            final List<SegmentDescriptor> segments = new ArrayList<>(1);
            segments.add(s);
            return segments;
        }).orElse(new ArrayList<>(0));
        sl.close();
        return list;
    }

    /**
     * Processes a HTTP POST request.
     *
     * @param context Object that is handed to the invocation, usually parsed from the request body. May be NULL!
     * @param parameters Map containing named parameters in the URL.
     * @return List of {@link SegmentDescriptor}s
     */
    @Override
    public List<SegmentDescriptor> doPost(IdList context, Map<String, String> parameters) {
        if (context == null || context.getIds().length == 0) {
            return new ArrayList<>(0);
        }
        final SegmentLookup sl = new SegmentLookup();
        final Map<String, SegmentDescriptor> segments = sl.lookUpSegments(Arrays.asList(context.getIds()));
        sl.close();
        return new ArrayList<>(segments.values());
    }

    @Override
    public Class<IdList> inClass() {
        return IdList.class;
    }
}
