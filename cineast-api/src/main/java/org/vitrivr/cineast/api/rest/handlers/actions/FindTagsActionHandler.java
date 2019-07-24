package org.vitrivr.cineast.api.rest.handlers.actions;

import org.vitrivr.cineast.api.rest.handlers.abstracts.ParsingActionHandler;
import org.vitrivr.cineast.api.messages.lookup.IdList;
import org.vitrivr.cineast.api.messages.result.MediaObjectQueryResult;
import org.vitrivr.cineast.core.data.tag.Tag;
import org.vitrivr.cineast.core.db.dao.TagHandler;

import java.util.Map;

public class FindTagsActionHandler extends ParsingActionHandler<IdList> {

    private static TagHandler tagHandler = new TagHandler();


    public FindTagsActionHandler() {
        tagHandler.initCache();
    }

    /**
     * Processes a HTTP GET request. Finds all the {@link Tag}s that have been cached.
     *
     * @param parameters Map containing named parameters in the URL.
     * @return {@link MediaObjectQueryResult}
     */
    @Override
    public Object doGet(Map<String, String> parameters) {
        return tagHandler.getAllCached();
    }

    @Override
    public Class<IdList> inClass() {
        return IdList.class;
    }
}
