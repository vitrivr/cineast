package org.vitrivr.cineast.core.config;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.vitrivr.cineast.core.db.DBSelectorSupplier;
import org.vitrivr.cineast.core.db.NoDBSelector;
import org.vitrivr.cineast.core.db.NoDBWriter;
import org.vitrivr.cineast.core.db.PersistencyWriterSupplier;
import org.vitrivr.cineast.core.db.adampro.*;
import org.vitrivr.cineast.core.db.cottontaildb.CottontailEntityCreator;
import org.vitrivr.cineast.core.db.cottontaildb.CottontailSelector;
import org.vitrivr.cineast.core.db.cottontaildb.CottontailWrapper;
import org.vitrivr.cineast.core.db.cottontaildb.CottontailWriter;
import org.vitrivr.cineast.core.db.json.JsonFileWriter;
import org.vitrivr.cineast.core.db.json.JsonSelector;
import org.vitrivr.cineast.core.db.memory.InMemoryEntityCreator;
import org.vitrivr.cineast.core.db.memory.InMemoryWriter;
import org.vitrivr.cineast.core.db.setup.EntityCreator;
import org.vitrivr.cineast.core.db.setup.NoEntityCreator;

import java.io.File;
import java.util.function.Supplier;

public final class DatabaseConfig {


  /**
   * Default value for batchsize.
   */
  public static final int DEFAULT_BATCH_SIZE = 1000;
  public static final String DEFAULT_HOST = "localhost";
  public static final int DEFAULT_PORT = 5890;
  public static final boolean DEFAULT_PLAINTEXT = true;
  public static final boolean SINGLE_CONNECTION = true;

  private String host = DEFAULT_HOST;
  private int port = DEFAULT_PORT;
  private boolean plaintext = DEFAULT_PLAINTEXT;
  private Writer writer = Writer.COTTONTAIL;
  private Selector selector = Selector.COTTONTAIL;

  private Integer batchsize = DEFAULT_BATCH_SIZE;

  private static final PersistencyWriterSupplier NO_WRITER_SUPPLY = NoDBWriter::new;

  private static final DBSelectorSupplier NO_SELECTOR_SUPPLY = NoDBSelector::new;

  private static final Supplier<EntityCreator> NO_CREATOR_SUPPLY = NoEntityCreator::new;


  public enum Writer {
    NONE,
    JSON,
    ADAMPRO,
    COTTONTAIL,
    INMEMORY
  }

  public enum Selector {
    NONE,
    JSON,
    ADAMPRO,
    ADAMPROSTREAM,
    COTTONTAIL,
    INMEMORY
  }

  private ADAMproWrapper adaMproWrapper = null;

  private synchronized void ensureAdamProWrapper(){
      if (this.adaMproWrapper == null){
          this.adaMproWrapper = new ADAMproWrapper(this);
      }
  }

  private CottontailWrapper cottontailWrapper = null;

  private synchronized void ensureCottontailWrapper(){
      if (this.cottontailWrapper == null){
          this.cottontailWrapper = new CottontailWrapper(this, true);
      }
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

  public synchronized PersistencyWriterSupplier getWriterSupplier() {
    switch (this.writer) {
      case NONE:
        return NO_WRITER_SUPPLY;
      case ADAMPRO:{
          if(SINGLE_CONNECTION){
              ensureAdamProWrapper();
              return () -> new ADAMproWriter(this.adaMproWrapper);
          }
          return () -> new ADAMproWriter(new ADAMproWrapper(this));
      }
      case JSON:{
          return () -> new JsonFileWriter(new File(this.host));
      }
      case COTTONTAIL: {
          if (SINGLE_CONNECTION){
              ensureCottontailWrapper();
              return () -> new CottontailWriter(this.cottontailWrapper);
          }
          return () -> new CottontailWriter(new CottontailWrapper(this, false));
      }
      case INMEMORY: {
        return InMemoryWriter::new;
      }
      default:
        throw new IllegalStateException("No supplier for writer " + this.writer);

    }
  }

  public synchronized DBSelectorSupplier getSelectorSupplier() {
    switch (this.selector) {
      case ADAMPRO:{
          if (SINGLE_CONNECTION){
              ensureAdamProWrapper();
              return () -> new ADAMproSelector(this.adaMproWrapper);
          }
          return () -> new ADAMproSelector(new ADAMproWrapper(this));
      }
      case ADAMPROSTREAM:{
          if (SINGLE_CONNECTION){
              ensureAdamProWrapper();
              return () -> new ADAMproStreamingSelector(this.adaMproWrapper);
          }
          return () -> new ADAMproStreamingSelector(new ADAMproWrapper(this));
      }
      case JSON: {
          return () -> new JsonSelector(new File(this.host));
      }
      case NONE:
        return NO_SELECTOR_SUPPLY;
      case COTTONTAIL:{
          if (SINGLE_CONNECTION){
              ensureCottontailWrapper();
              return () -> new CottontailSelector(this.cottontailWrapper);
          }
          return () -> new CottontailSelector(new CottontailWrapper(this, false));
      }
      default:
        throw new IllegalStateException("No supplier for selector " + this.selector);

    }
  }

  public synchronized Supplier<EntityCreator> getEntityCreatorSupplier() {
    switch (this.selector) {
      case ADAMPRO:
      case ADAMPROSTREAM:{
          if (SINGLE_CONNECTION){
              ensureAdamProWrapper();
              return () -> new ADAMproEntityCreator(this.adaMproWrapper);
          }
          return () -> new ADAMproEntityCreator(new ADAMproWrapper(this));
      }
      case NONE:
        return NO_CREATOR_SUPPLY;
      case COTTONTAIL:{
          if (SINGLE_CONNECTION){
              ensureCottontailWrapper();
              return () -> new CottontailEntityCreator(this.cottontailWrapper);
          }
          return () -> new CottontailEntityCreator(new CottontailWrapper(this, false));
      }
      case INMEMORY:
        return InMemoryEntityCreator::new;
      default:
        throw new IllegalStateException("No supplier for EntityCreator " + this.selector);
    }
  }
}
