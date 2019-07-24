package org.vitrivr.cineast.standalone.importer;

import java.io.EOFException;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.vitrivr.adampro.grpc.AdamGrpc.DataMessage;
import org.vitrivr.adampro.grpc.AdamGrpc.InsertMessage.TupleInsertMessage;
import org.vitrivr.cineast.core.data.providers.primitive.PrimitiveTypeProvider;
import org.vitrivr.cineast.core.db.DataMessageConverter;

public class TupleInsertMessageImporter implements Importer<TupleInsertMessage>{

	private final FileInputStream inStream;
	private static final Logger LOGGER = LogManager.getLogger();
	
	public TupleInsertMessageImporter(File inputFile) throws FileNotFoundException{
		this.inStream = new FileInputStream(inputFile);
	}
	
	@Override
  public TupleInsertMessage readNext(){
	  while(true){
  		try {
  			return TupleInsertMessage.parseDelimitedFrom(this.inStream);
  		} catch (EOFException eof) {
  		  return null;
  		}	catch (IOException e) {
  			LOGGER.error("error while reading TupleInsertMessage, skipping");
  		}
	  }
	}

	@Override
	public Map<String, PrimitiveTypeProvider> convert(TupleInsertMessage message) {
		if(message == null){
			return null;
		}
		
		Map<String, DataMessage> data = message.getDataMap();
		
		HashMap<String, PrimitiveTypeProvider> map = new HashMap<>();
		
		for(String key : data.keySet()){
			map.put(key, DataMessageConverter.convert(data.get(key)));
		}
		
		return map;
	}
	
	
}
