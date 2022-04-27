package org.vitrivr.cineast.api.messages.lookup;

/**
 * Message from the requester specifying from which table and which column information should be fetched.
 */
public record ColumnSpecification(String column, String table) {

}
