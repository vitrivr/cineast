package org.vitrivr.cineast.core.config;

import org.vitrivr.cineast.core.db.ADAMproSelector;
import org.vitrivr.cineast.core.db.ADAMproWriter;
import org.vitrivr.cineast.core.db.DBSelectorSupplier;
import org.vitrivr.cineast.core.db.JsonFileWriter;
import org.vitrivr.cineast.core.db.NoDBSelector;
import org.vitrivr.cineast.core.db.PersistencyWriterSupplier;
import org.vitrivr.cineast.core.db.ProtobufFileWriter;

import com.eclipsesource.json.JsonObject;

public final class DatabaseConfig {
	
	private final String host;
	private final int port;
	private final boolean plaintext;
	private final Writer writer;
	private final Selector selector;
	
	private static final PersistencyWriterSupplier ADAMPRO_WRITER_SUPPLY = () -> new ADAMproWriter();	
	private static final PersistencyWriterSupplier PROTO_WRITER_SUPPLY = () -> new ProtobufFileWriter();
	private static final PersistencyWriterSupplier JSON_WRITER_SUPPLY = () -> new JsonFileWriter();
	
	private static final DBSelectorSupplier NO_SELECTOR_SUPPLY = () -> new NoDBSelector();
	private static final DBSelectorSupplier ADAMPRO_SELECTOR_SUPPLY = () -> new ADAMproSelector();
	
	public static enum Writer{
		PROTO,
		JSON,
		ADAMPRO
	}
	
	public static enum Selector{
	  NONE,
		ADAMPRO
	}
	
	public static final String DEFAULT_HOST = "127.0.0.1";
	public static final int DEFAULT_PORT = 5890;
	public static final boolean DEFAULT_PLAINTEXT = false;
	public static final Writer DEFAULT_WRITER = Writer.ADAMPRO;
	public static final Selector DEFAULT_SELECTOR = Selector.ADAMPRO;
	
	
	public DatabaseConfig(String host, int port, boolean plaintext, Writer writer, Selector selector){
		if(host == null){
			throw new NullPointerException("Database location cannot be null");
		}
		if(port < 1 || port > 65535){
			throw new IllegalArgumentException(port + " is outside of valid port range");
		}
		
		this.host = host;
		this.port = port;
		this.plaintext = plaintext;
		this.writer = writer;
		this.selector = selector;
	}
	
	public DatabaseConfig(){
		this(DEFAULT_HOST, DEFAULT_PORT, DEFAULT_PLAINTEXT, DEFAULT_WRITER, DEFAULT_SELECTOR);
	}
	
	
	public String getHost(){
		return this.host;
	}
	
	public int getPort(){
		return this.port;
	}
	
	public boolean getPlaintext(){
		return this.plaintext;
	}
	
	public Writer getWriter(){
		return this.writer;
	}
	
	public Selector getSelector(){
		return this.selector;
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
		case NONE:
		  return NO_SELECTOR_SUPPLY;
		default:
			throw new IllegalStateException("no supplier for selector " + this.selector);
			
		}
	}
	
	/**
	 * 
	 * expects a json object of the follwing form:
	 * <pre>
	 * {
	 * 	"host" : (string)
	 * 	"port" : (int)
	 * 	"plaintext" : (boolean)
	 *  "writer" : PROTO | JSON | ADAMPRO
	 *  "selector" : NONE | ADAMPRO
	 * }
	 * </pre>
	 * @throws NullPointerException in case provided JsonObject is null
	 * @throws IllegalArgumentException in case one of the parameters is not of the correct type or not within the valid range
	 */
	public static DatabaseConfig parse(JsonObject obj) throws NullPointerException, IllegalArgumentException{
		if(obj == null){
			throw new NullPointerException("JsonObject was null");
		}
		
		String host = DEFAULT_HOST;
		if(obj.get("host") != null){
			try{
				host = obj.get("host").asString();
			} catch(UnsupportedOperationException notastring){
				throw new IllegalArgumentException("'host' was not a string in database configuration");
			}
		}
		
		int port = DEFAULT_PORT;
		if(obj.get("port") != null){
			try{
				port = obj.get("port").asInt();
			} catch(UnsupportedOperationException notastring){
				throw new IllegalArgumentException("'port' was not an integer in database configuration");
			}
		}
		
		boolean plaintext = DEFAULT_PLAINTEXT;
		if(obj.get("plaintext") != null){
			try{
				plaintext = obj.get("plaintext").asBoolean();
			} catch(UnsupportedOperationException notastring){
				throw new IllegalArgumentException("'plaintext' was not a boolean in database configuration");
			}
		}
		
		Writer writer = DEFAULT_WRITER;
		if(obj.get("writer") != null){
			String writerName = "";
			try{
				writerName = obj.get("writer").asString();
				writer = Writer.valueOf(writerName);
			} catch(UnsupportedOperationException notastring){
				throw new IllegalArgumentException("'writer' was not a string in database configuration");
			} catch(IllegalArgumentException notawriter){
				throw new IllegalArgumentException("'" + writerName + "' is not a valid value for 'writer'");
			}
		}
		
		Selector selector = DEFAULT_SELECTOR;
		if(obj.get("selector") != null){
			String selectorName = "";
			try{
				selectorName = obj.get("selector").asString();
				selector = Selector.valueOf(selectorName);
			} catch(UnsupportedOperationException notastring){
				throw new IllegalArgumentException("'selector' was not a string in database configuration");
			} catch(IllegalArgumentException notawriter){
				throw new IllegalArgumentException("'" + selectorName + "' is not a valid value for 'selector'");
			}
		}
		
		return new DatabaseConfig(host, port, plaintext, writer, selector);
		
	}
}
