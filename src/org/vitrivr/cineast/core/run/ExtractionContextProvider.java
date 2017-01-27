package org.vitrivr.cineast.core.run;

import org.vitrivr.cineast.core.config.IdConfig;
import org.vitrivr.cineast.core.data.MediaType;
import org.vitrivr.cineast.core.db.PersistencyWriterSupplier;
import org.vitrivr.cineast.core.features.extractor.Extractor;
import org.vitrivr.cineast.core.idgenerator.ObjectIdGenerator;
import org.vitrivr.cineast.core.metadata.MetadataExtractor;

import java.nio.file.Path;
import java.util.List;

/**
 * Provides a configuration context for an extraction run.
 *
 * @author rgasser
 * @version 1.0
 * @created 13.01.17
 */
public interface ExtractionContextProvider {
    /**
     * Determines the MediaType of the source material. Only one media-type
     * can be specified per ExtractionContextProvider.
     *
     * @return Media-type of the source material.
     */
    MediaType sourceType();

    /**
     * Determines the path of the input-file or folder.
     *
     * @return Path to the input-file or folder.
     */
    Path inputPath();

    /**
     * Limits the number of files that should be extracted. This a predicate is applied
     * before extraction starts. If extraction fails for some fails the effective number
     * of extracted files may be lower.
     *
     * @return A number greater than zero.
     */
    int limit();

    /**
     * Offset into the list of files that are being distracted.
     *
     * @return A positive number or zero
     */
    int skip();


    /**
     *  Limits the depth of recursion when extraction folders of files. Has no
     *  effect if the inputPath points to a file.
     *
     * @return A number greater than zero.
     */
    int depth();

    /**
     * Returns a list of extractor classes that should be used for
     * the extraction run!
     *
     * @return List of named extractors.
     */
    public List<Extractor> extractors();

    /**
     * Returns a list of exporter classes that should be invoked during extraction. Exporters
     * usually generate some representation and persistently store that information somewhere.
     *
     * @return List of named exporters.
     */
    public List<Extractor> exporters();

    /**
     * Returns a list of metadata extractor classes that should be invoked during extraction. MetadataExtractor's
     * usually read some metadata from a file.
     *
     * @return List of named exporters.
     */
    public List<MetadataExtractor> metadataExtractors();

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
     * NOCHECK          - No checks performed (may cause DB error if collision occurs).
     * CHECK_SKIP       - Check existence and skip object on collision.
     * CHECK_PROCEED    - Check existence and proceed with object on collision.
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
}
