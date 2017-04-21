package org.vitrivr.cineast.core.config;

import java.util.function.Supplier;

import org.vitrivr.cineast.core.db.ADAMproSelector;
import org.vitrivr.cineast.core.db.ADAMproWriter;
import org.vitrivr.cineast.core.db.DBSelectorSupplier;
import org.vitrivr.cineast.core.db.JsonFileWriter;
import org.vitrivr.cineast.core.db.JsonSelector;
import org.vitrivr.cineast.core.db.NoDBSelector;
import org.vitrivr.cineast.core.db.PersistencyWriterSupplier;
import org.vitrivr.cineast.core.db.ProtoSelector;
import org.vitrivr.cineast.core.db.ProtobufFileWriter;
import org.vitrivr.cineast.core.setup.ADAMproEntityCreator;
import org.vitrivr.cineast.core.setup.EntityCreator;
import org.vitrivr.cineast.core.setup.NoEntityCreator;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public final class DatabaseConfig {
	private String host = "127.0.0.1";
	private int port =  5890;
	private boolean plaintext = false;
	private Writer writer = Writer.ADAMPRO;
	private Selector selector = Selector.ADAMPRO;
	
	private static final PersistencyWriterSupplier ADAMPRO_WRITER_SUPPLY = () -> new ADAMproWriter();	
	private static final PersistencyWriterSupplier PROTO_WRITER_SUPPLY = () -> new ProtobufFileWriter();
	private static final PersistencyWriterSupplier JSON_WRITER_SUPPLY = () -> new JsonFileWriter();
	
	private static final DBSelectorSupplier NO_SELECTOR_SUPPLY = () -> new NoDBSelector();
	private static final DBSelectorSupplier PROTO_SELECTOR_SUPPLY = () -> new ProtoSelector();
	private static final DBSelectorSupplier JSON_SELECTOR_SUPPLY = () -> new JsonSelector();
	private static final DBSelectorSupplier ADAMPRO_SELECTOR_SUPPLY = () -> new ADAMproSelector();
	
  private static final Supplier<EntityCreator> ADAMPRO_CREATOR_SUPPLY = () -> new ADAMproEntityCreator();
  private static final Supplier<EntityCreator> NO_CREATOR_SUPPLY = () -> new NoEntityCreator();

	public static enum Writer{
		PROTO,
		JSON,
		ADAMPRO
	}
	
	public static enum Selector{
	  NONE,
	  JSON,
	  PROTO,
		ADAMPRO
	}

	@JsonCreator
	public DatabaseConfig() {

	}

	@JsonProperty
	public String getHost(){
		return this.host;
	}
	public void setHost(String host) {
		if(host == null){
			throw new NullPointerException("Database location cannot be null");
		}
		this.host = host;
	}

	@JsonProperty
	public int getPort(){
		return this.port;
	}
	public void setPort(int port) {
		if(port < 1 || port > 65535){
			throw new IllegalArgumentException(port + " is outside of valid port range");
		}
		this.port = port;
	}

	@JsonProperty
	public boolean getPlaintext(){
		return this.plaintext;
	}
	public void setPlaintext(boolean plaintext) {
		this.plaintext = plaintext;
	}

	@JsonProperty
	public Writer getWriter(){
		return this.writer;
	}
	public void setWriter(Writer writer) {
		this.writer = writer;
	}

	@JsonProperty
	public Selector getSelector(){
		return this.selector;
	}
	public void setSelector(Selector selector) {
		this.selector = selector;
	}

	public PersistencyWriterSupplier getWriterSupplier(){
		switch(this.writer){
		case ADAMPRO:
			return ADAMPRO_WRITER_SUPPLY;
		case PROTO:
			return PROTO_WRITER_SUPPLY;
		case JSON:
		  return JSON_WRITER_SUPPLY;
		default:
			throw new IllegalStateException("no supplier for writer " + this.writer);
			
		}
	}
	
	public DBSelectorSupplier getSelectorSupplier(){
		switch(this.selector){
		case ADAMPRO:
			return ADAMPRO_SELECTOR_SUPPLY;
		case PROTO:
		  return PROTO_SELECTOR_SUPPLY;
		case JSON:
		  return JSON_SELECTOR_SUPPLY;
		case NONE:
		  return NO_SELECTOR_SUPPLY;
		default:
			throw new IllegalStateException("no supplier for selector " + this.selector);
			
		}
	}
	
	public Supplier<EntityCreator> getEntityCreatorSupplier(){
	  switch(this.selector){
    case ADAMPRO:
      return ADAMPRO_CREATOR_SUPPLY;
    case NONE:
      return NO_CREATOR_SUPPLY;
    default:
      throw new IllegalStateException("no supplier for EntityCreator " + this.selector);
    }
	}
}
