package org.vitrivr.cineast.api.rest.handlers.actions;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.vitrivr.cineast.api.messages.lookup.IdList;
import org.vitrivr.cineast.api.messages.result.MediaSegmentQueryResult;
import org.vitrivr.cineast.api.rest.RestHttpMethod;
import org.vitrivr.cineast.api.rest.handlers.abstracts.ParsingActionHandler;
import org.vitrivr.cineast.core.data.entities.MediaSegmentDescriptor;
import org.vitrivr.cineast.core.db.dao.reader.MediaSegmentReader;
import org.vitrivr.cineast.standalone.config.Config;

public class FindSegmentsByIdActionHandler extends ParsingActionHandler<IdList, MediaSegmentQueryResult> {

    private final static String ID_NAME = "id";

    @Override
    public List<RestHttpMethod> supportedMethods() {
        return Arrays.asList(RestHttpMethod.GET, RestHttpMethod.POST);
    }

    /**
     * Processes a HTTP GET request.
     *
     * @param parameters Map containing named parameters in the URL.
     * @return {@link MediaSegmentQueryResult}
     */
    @Override
    public MediaSegmentQueryResult doGet(Map<String, String> parameters) {
        final String segmentId = parameters.get(ID_NAME);
        final MediaSegmentReader sl = new MediaSegmentReader(Config.sharedConfig().getDatabase().getSelectorSupplier().get());
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
        final MediaSegmentReader sl = new MediaSegmentReader(Config.sharedConfig().getDatabase().getSelectorSupplier().get());
        final Map<String, MediaSegmentDescriptor> segments = sl.lookUpSegments(Arrays.asList(context.getIds()));
        sl.close();
        return new MediaSegmentQueryResult("", new ArrayList<>(segments.values())) ;
    }

    @Override
    public Class<IdList> inClass() {
        return IdList.class;
    }

    @Override
    public String getRoute() {
        return "find/segments/by/id";
    }

    @Override
    public String routeForGet() {
        return String.format("find/segments/by/id/:%s", ID_NAME);
    }

    @Override
    public String getDescription(RestHttpMethod method) {
        return "Finds segments for specified ids";
    }

    @Override
    public Class<MediaSegmentQueryResult> outClass() {
        return MediaSegmentQueryResult.class;
    }
}
