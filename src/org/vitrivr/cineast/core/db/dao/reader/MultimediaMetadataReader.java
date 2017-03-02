package org.vitrivr.cineast.core.db.dao.reader;

import org.vitrivr.cineast.core.config.Config;
import org.vitrivr.cineast.core.data.entities.MultimediaMetadataDescriptor;
import org.vitrivr.cineast.core.data.providers.primitive.PrimitiveTypeProvider;
import org.vitrivr.cineast.core.db.DBSelector;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

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
        List<Map<String, PrimitiveTypeProvider>> results = this.selector.getRows(MultimediaMetadataDescriptor.FIELDNAMES[0], objectid);
        ArrayList<MultimediaMetadataDescriptor> list = new ArrayList<>();

        if(results.isEmpty()){
            LOGGER.debug("Could not find MultimediaMetadataDescriptor with ID {}", objectid);
            return null;
        }

        try {
            for (Map<String, PrimitiveTypeProvider> result : results) {
                list.add(new MultimediaMetadataDescriptor(result));
            }
            return list;
        } catch (DatabaseLookupException exception) {
            LOGGER.fatal("Could not map data returned for row {}. This is a programmer's error!", objectid);
            return null;
        }
    }

    /**
     * Looks up the metadata for a multiple multimedia objects.
     *
     * @param objectids ID's of the multimedia object's for which metadata should be retrieved.
     * @return List of MultimediaMetadataDescriptor object's. May be empty!
     */
    public List<MultimediaMetadataDescriptor> lookupMultimediaMetadata(String[] objectids) {
        List<Map<String, PrimitiveTypeProvider>> results = this.selector.getRows(MultimediaMetadataDescriptor.FIELDNAMES[0], objectids);

        ArrayList<MultimediaMetadataDescriptor> list = new ArrayList<>();

        if(results.isEmpty()){
            LOGGER.debug("Could not find any MultimediaMetadataDescriptor for provided ID's {}.", (Object[]) objectids);
            return list;
        }

        try {
            for (Map<String, PrimitiveTypeProvider> result : results) {
                list.add(new MultimediaMetadataDescriptor(result));
            }
            return list;
        } catch (DatabaseLookupException exception) {
            LOGGER.fatal("Could not map data returned for row. This is a programmer's error!");
            list.clear();
            return list;
        }
    }
}
