package org.vitrivr.cineast.core.config;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.vitrivr.cineast.core.db.DBSelectorSupplier;
import org.vitrivr.cineast.core.db.NoDBSelector;
import org.vitrivr.cineast.core.db.NoDBWriter;
import org.vitrivr.cineast.core.db.PersistencyWriterSupplier;
import org.vitrivr.cineast.core.db.adampro.ADAMproEntityCreator;
import org.vitrivr.cineast.core.db.adampro.ADAMproSelector;
import org.vitrivr.cineast.core.db.adampro.ADAMproStreamingSelector;
import org.vitrivr.cineast.core.db.adampro.ADAMproWriter;
import org.vitrivr.cineast.core.db.cottontaildb.CottontailEntityCreator;
import org.vitrivr.cineast.core.db.cottontaildb.CottontailSelector;
import org.vitrivr.cineast.core.db.cottontaildb.CottontailWriter;
import org.vitrivr.cineast.core.db.json.JsonFileWriter;
import org.vitrivr.cineast.core.db.json.JsonSelector;
import org.vitrivr.cineast.core.db.protobuf.ProtoSelector;
import org.vitrivr.cineast.core.db.protobuf.ProtobufFileWriter;
import org.vitrivr.cineast.core.db.setup.EntityCreator;
import org.vitrivr.cineast.core.db.setup.NoEntityCreator;

import java.util.function.Supplier;

public final class DatabaseConfig {


  /**
   * Default value for batchsize.
   */
  public static final int DEFAULT_BATCH_SIZE = 1000;
  public static final String DEFAULT_HOST = "127.0.0.1";
  public static final int DEFAULT_PORT = 5890;
  public static final boolean DEFAULT_PLAINTEXT = true;

  private String host = DEFAULT_HOST;
  private int port = DEFAULT_PORT;
  private boolean plaintext = DEFAULT_PLAINTEXT;
  private Writer writer = Writer.ADAMPRO;
  private Selector selector = Selector.ADAMPRO;

  private Integer batchsize = DEFAULT_BATCH_SIZE;

  private static final PersistencyWriterSupplier NO_WRITER_SUPPLY = NoDBWriter::new;
  private static final PersistencyWriterSupplier ADAMPRO_WRITER_SUPPLY = ADAMproWriter::new;
  private static final PersistencyWriterSupplier PROTO_WRITER_SUPPLY = ProtobufFileWriter::new;
  private static final PersistencyWriterSupplier JSON_WRITER_SUPPLY = JsonFileWriter::new;
  private static final PersistencyWriterSupplier COTTONTAIL_WRITER_SUPPLY = CottontailWriter::new;

  private static final DBSelectorSupplier NO_SELECTOR_SUPPLY = NoDBSelector::new;
  private static final DBSelectorSupplier PROTO_SELECTOR_SUPPLY = ProtoSelector::new;
  private static final DBSelectorSupplier JSON_SELECTOR_SUPPLY = JsonSelector::new;
  private static final DBSelectorSupplier ADAMPRO_SELECTOR_SUPPLY = ADAMproSelector::new;
  private static final DBSelectorSupplier ADAMPRO_STREAM_SELECTOR_SUPPLY = ADAMproStreamingSelector::new;
  private static final DBSelectorSupplier COTTONTAIL_SELECTOR_SUPPLY = CottontailSelector::new;

  private static final Supplier<EntityCreator> ADAMPRO_CREATOR_SUPPLY = ADAMproEntityCreator::new;
  private static final Supplier<EntityCreator> ADAMPRO_STREAM_CREATOR_SUPPLY = ADAMproEntityCreator::new;
  private static final Supplier<EntityCreator> NO_CREATOR_SUPPLY = NoEntityCreator::new;
  private static final Supplier<EntityCreator> COTTONTAIL_CREATOR_SUPPLY = CottontailEntityCreator::new;


  public static enum Writer {
    NONE,
    PROTO,
    JSON,
    ADAMPRO,
    COTTONTAIL
  }

  public static enum Selector {
    NONE,
    JSON,
    PROTO,
    ADAMPRO,
    ADAMPROSTREAM,
    COTTONTAIL
  }

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
    return batchsize;
  }

  public void setBatchsize(Integer batchsize) {
    this.batchsize = batchsize;
  }

  @JsonProperty
  public Writer getWriter() {
    return this.writer;
  }

  public void setWriter(Writer writer) {
    this.writer = writer;
  }

  @JsonProperty
  public Selector getSelector() {
    return this.selector;
  }

  public void setSelector(Selector selector) {
    this.selector = selector;
  }

  public PersistencyWriterSupplier getWriterSupplier() {
    switch (this.writer) {
      case NONE:
        return NO_WRITER_SUPPLY;
      case ADAMPRO:
        return ADAMPRO_WRITER_SUPPLY;
      case PROTO:
        return PROTO_WRITER_SUPPLY;
      case JSON:
        return JSON_WRITER_SUPPLY;
      case COTTONTAIL:
        return COTTONTAIL_WRITER_SUPPLY;
      default:
        throw new IllegalStateException("no supplier for writer " + this.writer);

    }
  }

  public DBSelectorSupplier getSelectorSupplier() {
    switch (this.selector) {
      case ADAMPRO:
        return ADAMPRO_SELECTOR_SUPPLY;
      case ADAMPROSTREAM:
        return ADAMPRO_STREAM_SELECTOR_SUPPLY;
      case PROTO:
        return PROTO_SELECTOR_SUPPLY;
      case JSON:
        return JSON_SELECTOR_SUPPLY;
      case NONE:
        return NO_SELECTOR_SUPPLY;
      case COTTONTAIL:
        return COTTONTAIL_SELECTOR_SUPPLY;
      default:
        throw new IllegalStateException("no supplier for selector " + this.selector);

    }
  }

  public Supplier<EntityCreator> getEntityCreatorSupplier() {
    switch (this.selector) {
      case ADAMPRO:
        return ADAMPRO_CREATOR_SUPPLY;
      case ADAMPROSTREAM:
        return ADAMPRO_STREAM_CREATOR_SUPPLY;
      case NONE:
        return NO_CREATOR_SUPPLY;
      case COTTONTAIL:
        return COTTONTAIL_CREATOR_SUPPLY;
      default:
        throw new IllegalStateException("no supplier for EntityCreator " + this.selector);
    }
  }
}
