package org.vitrivr.cineast.core.run;

import org.vitrivr.cineast.core.config.IdConfig;
import org.vitrivr.cineast.core.data.MediaType;
import org.vitrivr.cineast.core.db.PersistencyWriterSupplier;
import org.vitrivr.cineast.core.idgenerator.ObjectIdGenerator;

import java.nio.file.Path;
import java.util.List;

/**
 * @author rgasser
 * @version 1.0
 * @created 13.01.17
 */
public interface ExtractionContextProvider {
    /**
     * Determines the path of the input-file or folder.
     *
     * @return Path to the input-file or folder.
     */
    public Path inputPath();

    /**
     * Determines the MediaType of the source material. Only one media-type
     * can be specified per ExtractionContextProvider.
     *
     * @return Media-type of the source material.
     */
    public MediaType sourceType();

    /**
     * Returns a list of named categories for which features should be extracted. These
     * categories must exist and be configured in the config.json of Cineast!
     *
     * @return List of named categories.
     */
    public List<String> getCategories();

    /**
     * Returns a list of named exporter classes that should be invoked during extraction. Exporters
     * usually generate some representation and persistently store that information somewhere.
     *
     * @return List of named exporters.
     */
    public List<String> getExporters();

    /**
     * Returns an instance of ObjectIdGenerator that should be used to generated MultimediaObject ID's
     * during an extraction run.
     *
     * @return ObjectIdGenerator
     */
    ObjectIdGenerator objectIdGenerator();

    /**
     * Returns the ExistenceCheck mode that should be applied during the extraction run. Can either be:
     *
     * NOCHECK          - No checks performed (may cause DB error).
     * CHECK_SKIP       - Check and skip object on collision.
     * CHECK_PROCEED    - Check and and proceed with object on collision.
     *
     * @return ExistenceCheck mode for current run.
     */
    IdConfig.ExistenceCheck existenceCheck();

    /**
     * Returns the PersistencyWriterSupplier that can be used during the extraction run to
     * obtain PersistencyWriter instance.
     *
     * @return PersistencyWriterSupplier instance used obtain a PersistencyWriter.
     */
    PersistencyWriterSupplier persistencyWriter();

    /**
     * Limits the number of files that should be extracted. This a predicate is applied
     * before extraction starts. If extraction fails for some fails the effective number
     * of extracted files may be lower.
     *
     * @return A number greater than zero.
     */
    int limit();

    /**
     *  Limits the depth of recursion when extraction folders of files. Has no
     *  effect if the inputPath points to a file.
     *
     * @return A number greater than zero.
     */
    int depth();
}
