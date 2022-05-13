package org.vitrivr.cineast.api.messages.interfaces;


import java.util.List;
import org.vitrivr.cineast.core.config.QueryConfig;
import org.vitrivr.cineast.core.db.dao.MetadataAccessSpecification;

/**
 * A {@link Query} represents a Query to be implemented i.e. a request for a similarity-search.
 * <br>
 * It is an interface so it can be extended by records with appropriate names
 */
public interface Query extends Message {

  /**
   * The {@link QueryConfig} used to configure the query. May be null!
   */
  QueryConfig config();

  /**
   * Which metadata should be fetched for a query
   */
  List<MetadataAccessSpecification> metadataAccessSpec();
}
