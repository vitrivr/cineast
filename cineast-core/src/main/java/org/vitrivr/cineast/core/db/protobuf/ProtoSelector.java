package org.vitrivr.cineast.core.db.protobuf;

import java.io.File;
import java.io.FileNotFoundException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.vitrivr.cineast.core.db.ImporterSelector;
import org.vitrivr.cineast.standalone.importer.TupleInsertMessageImporter;

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
