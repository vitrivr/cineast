package org.vitrivr.cineast.core.db.dao;

/**
 * Use '*' in either domain or key to retrieve simply all information. In general, if an empty specification list is provided, no metadata is returned.
 */
public record MetadataAccessSpecification(MetadataType type, String domain, String key) {

}
