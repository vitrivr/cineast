package org.vitrivr.cineast.api.rest.handlers.actions;

import java.util.List;
import java.util.Map;

import org.vitrivr.cineast.api.messages.general.AnyMessage;
import org.vitrivr.cineast.api.messages.result.MediaObjectQueryResult;
import org.vitrivr.cineast.api.rest.RestHttpMethod;
import org.vitrivr.cineast.api.rest.handlers.abstracts.ParsingActionHandler;
import org.vitrivr.cineast.core.data.entities.MediaObjectDescriptor;
import org.vitrivr.cineast.core.db.dao.reader.MediaObjectReader;
import org.vitrivr.cineast.standalone.config.Config;

/**
 * @author rgasser
 * @version 1.0
 * @created 10.01.17
 */
public class FindObjectAllActionHandler extends ParsingActionHandler<AnyMessage, MediaObjectDescriptor> {

    public static final String TYPE_NAME = ":type";

    /**
     * Processes a HTTP GET request.
     *
     * @param parameters Map containing named parameters in the URL.
     * @return {@link MediaObjectQueryResult}
     */
    @Override
    public List<MediaObjectDescriptor> doGet(Map<String, String> parameters) {
        // TODO :type is not being used
        final MediaObjectReader ol = new MediaObjectReader(Config.sharedConfig().getDatabase().getSelectorSupplier().get());
        final List<MediaObjectDescriptor> multimediaobjectIds = ol.getAllObjects();
        ol.close();
        return multimediaobjectIds;
    }

    @Override
    public Class<AnyMessage> inClass() {
        return AnyMessage.class;
    }

    @Override
    public String getRoute() {
        return "find/objects/all/"+ TYPE_NAME;
    }

    @Override
    public String getDescription(RestHttpMethod method) {
        return "Find all objects for a certain type";
    }

    @Override
    public Class<MediaObjectDescriptor> outClass() {
        return MediaObjectDescriptor.class;
    }

    @Override
    public boolean isResponseCollection() {
        return true;
    }
}
