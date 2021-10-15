package org.vitrivr.cineast.core.extraction.idgenerator;

import org.vitrivr.cineast.core.data.MediaType;

import java.nio.file.Path;

/**
 * Classes implementing this interface are intended to generate ID's for multimedia-objects. The classes should
 * be designed in such a way that:
 *
 * - The same instance of the class can be re-used (e.g. for an extraction run).
 * - Two invocations of next() generate different ID's, unless path & type are identical for both invocations AND the class
 *   intends to generate ID's specific for the combination of these values.
 *
 */
public interface ObjectIdGenerator {
    /**
     * Generates the next objectId and returns it as a string. That objectId should
     * already contain the MediaType prefix, if the ID type supports media-type prefixing.
     *
     * Important: If the supply of ID's is depleted OR no ID could be generated for some reason,
     * this method returns null!
     *
     * @param path Path to the file for which an ID should be generated.
     * @param type MediaType of the file for which an ID should be generated.
     * @return Next ID in the sequence or null
     */
    String next(Path path, MediaType type);
}
