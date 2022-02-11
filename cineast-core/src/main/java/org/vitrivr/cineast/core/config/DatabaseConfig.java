package org.vitrivr.cineast.core.config;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.function.Supplier;
import org.vitrivr.cineast.core.db.DBSelector;
import org.vitrivr.cineast.core.db.DBSelectorSupplier;
import org.vitrivr.cineast.core.db.DataSource;
import org.vitrivr.cineast.core.db.PersistencyWriter;
import org.vitrivr.cineast.core.db.PersistencyWriterSupplier;
import org.vitrivr.cineast.core.db.setup.EntityCreator;

public final class DatabaseConfig {

  /**
   * Default value for batchsize.
   */
  public static final int DEFAULT_BATCH_SIZE = 1000;
  public static final String DEFAULT_HOST = "localhost";
  public static final int DEFAULT_PORT = 5890;
  public static final boolean DEFAULT_PLAINTEXT = true;

  private String host = DEFAULT_HOST;
  private int port = DEFAULT_PORT;
  private boolean plaintext = DEFAULT_PLAINTEXT;
  private DataSource writer = DataSource.COTTONTAIL;
  private DataSource selector = DataSource.COTTONTAIL;

  private Integer batchsize = DEFAULT_BATCH_SIZE;

  @JsonCreator
  public DatabaseConfig() {
  }

  @JsonProperty
  public String getHost() {
    return this.host;
  }

  public void setHost(String host) {
    if (host == null) {
      throw new NullPointerException("Database location cannot be null");
    }
    this.host = host;
  }

  @JsonProperty
  public int getPort() {
    return this.port;
  }
  public void setPort(int port) {
    if (port < 1 || port > 65535) {
      throw new IllegalArgumentException(port + " is outside of valid port range");
    }
    this.port = port;
  }

  @JsonProperty
  public boolean getPlaintext() {
    return this.plaintext;
  }

  public void setPlaintext(boolean plaintext) {
    this.plaintext = plaintext;
  }

  @JsonProperty
  public Integer getBatchsize() {
    return this.batchsize;
  }

  public void setBatchsize(Integer batchsize) {
    this.batchsize = batchsize;
  }

  @JsonProperty
  public DataSource getWriter() {
    return this.writer;
  }

  public void setWriter(DataSource writer) {
    this.writer = writer;
  }

  @JsonProperty
  public DataSource getSelector() {
    return this.selector;
  }

  public void setSelector(DataSource selector) {
    this.selector = selector;
  }

  public PersistencyWriterSupplier getWriterSupplier() {
    return this.writer.getWriterSupplier(this);
  }

  public Supplier<EntityCreator> getEntityCreatorSupplier() {
    return this.writer.getEntityCreatorSupplier(this);
  }

  public DBSelectorSupplier getSelectorSupplier() {
    return this.selector.getSelectorSupplier(this);
  }
}
