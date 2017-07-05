package org.vitrivr.cineast.core.db.protobuf;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.vitrivr.cineast.core.config.ReadableQueryConfig;
import org.vitrivr.cineast.core.data.FixedSizePriorityQueue;
import org.vitrivr.cineast.core.data.distance.DistanceElement;
import org.vitrivr.cineast.core.data.providers.primitive.FloatTypeProvider;
import org.vitrivr.cineast.core.data.providers.primitive.PrimitiveTypeProvider;
import org.vitrivr.cineast.core.db.ImporterSelector;
import org.vitrivr.cineast.core.db.MergeOperation;
import org.vitrivr.cineast.core.importer.Importer;
import org.vitrivr.cineast.core.importer.TupleInsertMessageImporter;
import org.vitrivr.cineast.core.util.distance.FloatArrayDistance;
import org.vitrivr.cineast.core.util.distance.PrimitiveTypeMapDistanceComparator;

public class ProtoSelector extends ImporterSelector<TupleInsertMessageImporter> {

    private static final Logger LOGGER = LogManager.getLogger();

    @Override
    protected TupleInsertMessageImporter newImporter(File f) {
        try {
            return new TupleInsertMessageImporter(f);
        } catch (FileNotFoundException e) {
            LOGGER.error("cannot access file '{}'", f.getAbsolutePath());
        }
        return null;
    }

    @Override
    protected String getFileExtension() {
        return ".bin";
    }

}
