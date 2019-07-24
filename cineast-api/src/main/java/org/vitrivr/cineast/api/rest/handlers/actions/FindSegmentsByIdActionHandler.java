package org.vitrivr.cineast.api.rest.handlers.actions;

import org.vitrivr.cineast.api.rest.handlers.abstracts.ParsingActionHandler;
import org.vitrivr.cineast.core.data.entities.MediaSegmentDescriptor;
import org.vitrivr.cineast.core.data.messages.lookup.IdList;
import org.vitrivr.cineast.core.data.messages.result.MediaSegmentQueryResult;
import org.vitrivr.cineast.core.db.dao.reader.MediaSegmentReader;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class FindSegmentsByIdActionHandler extends ParsingActionHandler<IdList> {

    private final static String ID_NAME = ":id";

    /**
     * Processes a HTTP GET request.
     *
     * @param parameters Map containing named parameters in the URL.
     * @return {@link MediaSegmentQueryResult}
     */
    @Override
    public MediaSegmentQueryResult doGet(Map<String, String> parameters) {
        final String segmentId = parameters.get(ID_NAME);
        final MediaSegmentReader sl = new MediaSegmentReader();
        final List<MediaSegmentDescriptor> list = sl.lookUpSegment(segmentId).map(s -> {
            final List<MediaSegmentDescriptor> segments = new ArrayList<>(1);
            segments.add(s);
            return segments;
        }).orElse(new ArrayList<>(0));
        sl.close();
        return new MediaSegmentQueryResult("",list);
    }

    /**
     * Processes a HTTP POST request.
     *
     * @param context Object that is handed to the invocation, usually parsed from the request body. May be NULL!
     * @param parameters Map containing named parameters in the URL.
     * @return MediaSegmentQueryResult
     */
    @Override
    public MediaSegmentQueryResult doPost(IdList context, Map<String, String> parameters) {
        if (context == null || context.getIds().length == 0) {
            return new MediaSegmentQueryResult("",new ArrayList<>(0));
        }
        final MediaSegmentReader sl = new MediaSegmentReader();
        final Map<String, MediaSegmentDescriptor> segments = sl.lookUpSegments(Arrays.asList(context.getIds()));
        sl.close();
        return new MediaSegmentQueryResult("", new ArrayList<>(segments.values())) ;
    }

    @Override
    public Class<IdList> inClass() {
        return IdList.class;
    }
}
