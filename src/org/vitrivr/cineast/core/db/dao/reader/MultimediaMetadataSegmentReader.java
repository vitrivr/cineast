package org.vitrivr.cineast.core.db.dao.reader;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.vitrivr.cineast.core.config.Config;
import org.vitrivr.cineast.core.data.entities.MultimediaMetadataDescriptor;
import org.vitrivr.cineast.core.data.entities.MultimediaMetadataSegmentDescriptor;
import org.vitrivr.cineast.core.data.providers.primitive.PrimitiveTypeProvider;
import org.vitrivr.cineast.core.db.DBSelector;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class MultimediaMetadataSegmentReader extends AbstractEntityReader {

    private static final Logger LOGGER = LogManager.getLogger();

    public MultimediaMetadataSegmentReader(){
        this(Config.sharedConfig().getDatabase().getSelectorSupplier().get());
    }


    public MultimediaMetadataSegmentReader(DBSelector selector) {
        super(selector);
        this.selector.open(MultimediaMetadataSegmentDescriptor.ENTITY);
    }

    public List<MultimediaMetadataSegmentDescriptor> lookupMultimediaMetadata(String segmentid) {
        final List<Map<String, PrimitiveTypeProvider>> results = this.selector.getRows(MultimediaMetadataDescriptor.FIELDNAMES[0], segmentid);
        if(results.isEmpty()){
            LOGGER.debug("Could not find MultimediaMetadataSegmentDescriptor with ID {}", segmentid);
            return new ArrayList<>(0);
        }

        final ArrayList<MultimediaMetadataSegmentDescriptor> list = new ArrayList<>(results.size());
        results.forEach(r -> {
            try {
                list.add(new MultimediaMetadataSegmentDescriptor(r));
            } catch (DatabaseLookupException exception) {
                LOGGER.fatal("Could not map data returned for row {}. This is a programmer's error!", segmentid);
            }
        });
        return list;
    }

    public List<MultimediaMetadataSegmentDescriptor> lookupMultimediaMetadata(List<String> segmentIds) {
        final List<Map<String, PrimitiveTypeProvider>> results = this.selector.getRows(MultimediaMetadataDescriptor.FIELDNAMES[0], segmentIds);
        if(results.isEmpty()){
            LOGGER.debug("Could not find any MultimediaMetadataDescriptor for provided ID's.");
            return new ArrayList<>(0);
        }

        final ArrayList<MultimediaMetadataSegmentDescriptor> list = new ArrayList<>(results.size());
        results.forEach(r -> {
            try {
                list.add(new MultimediaMetadataSegmentDescriptor(r));
            } catch (DatabaseLookupException exception) {
                LOGGER.fatal("Could not map data. This is a programmer's error!");
            }
        });
        return list;
    }
}
