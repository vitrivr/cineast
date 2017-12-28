package org.vitrivr.cineast.core.db.dao.reader;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.vitrivr.cineast.core.config.Config;
import org.vitrivr.cineast.core.data.entities.MultimediaMetadataDescriptor;
import org.vitrivr.cineast.core.data.providers.primitive.PrimitiveTypeProvider;
import org.vitrivr.cineast.core.db.DBSelector;

/**
 * Data access object that facilitates lookups in Cineast's metadata entity (cineast_metadata). Methods in this class
 * usually return MultimediaMetadataDescriptors.
 *
 * @see MultimediaMetadataDescriptor
 *
 * @author rgasser
 * @version 1.0
 * @created 10.02.17
 */
public class MultimediaMetadataReader extends AbstractEntityReader {

    /**
     * Default constructor.
     */
    public MultimediaMetadataReader(){
        this(Config.sharedConfig().getDatabase().getSelectorSupplier().get());
    }

    /**
     * Constructor for MultimediaMetadataReader
     *
     * @param selector DBSelector to use for the MultimediaMetadataReader instance.
     */
    public MultimediaMetadataReader(DBSelector selector) {
        super(selector);
        this.selector.open(MultimediaMetadataDescriptor.ENTITY);
    }

    /**
     * Looks up the metadata for a specific multimedia object.
     *
     * @param objectid ID of the multimedia object for which metadata should be retrieved.
     * @return List of MultimediaMetadataDescriptor object's. May be empty!
     */
    public  List<MultimediaMetadataDescriptor> lookupMultimediaMetadata(String objectid) {
        final List<Map<String, PrimitiveTypeProvider>> results = this.selector.getRows(MultimediaMetadataDescriptor.FIELDNAMES[0], objectid);
        if(results.isEmpty()){
            LOGGER.debug("Could not find MultimediaMetadataDescriptor with ID {}", objectid);
            return new ArrayList<>(0);
        }

        final ArrayList<MultimediaMetadataDescriptor> list = new ArrayList<>(results.size());
        results.forEach(r -> {
            try {
                list.add(new MultimediaMetadataDescriptor(r));
            } catch (DatabaseLookupException exception) {
                LOGGER.fatal("Could not map data returned for row {}. This is a programmer's error!", objectid);
            }
        });
        return list;
    }

    /**
     * Looks up the metadata for a multiple multimedia objects.
     *
     * @param objectids ID's of the multimedia object's for which metadata should be retrieved.
     * @return List of MultimediaMetadataDescriptor object's. May be empty!
     */
    public List<MultimediaMetadataDescriptor> lookupMultimediaMetadata(List<String> objectids) {
        final List<Map<String, PrimitiveTypeProvider>> results = this.selector.getRows(MultimediaMetadataDescriptor.FIELDNAMES[0], objectids);
        if(results.isEmpty()){
            LOGGER.debug("Could not find any MultimediaMetadataDescriptor for provided ID's.");
            return new ArrayList<>(0);
        }

        final ArrayList<MultimediaMetadataDescriptor> list = new ArrayList<>(results.size());
        results.forEach(r -> {
            try {
                list.add(new MultimediaMetadataDescriptor(r));
            } catch (DatabaseLookupException exception) {
                LOGGER.fatal("Could not map data. This is a programmer's error!");
            }
        });
        return list;
    }
}
