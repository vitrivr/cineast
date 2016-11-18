package org.vitrivr.cineast.core.importer;

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
import org.vitrivr.cineast.core.util.LogHelper;

public class TupleInsertMessageImporter implements Importer<TupleInsertMessage>{

	private final FileInputStream inStream;
	private static final Logger LOGGER = LogManager.getLogger();
	
	public TupleInsertMessageImporter(File inputFile) throws FileNotFoundException{
		this.inStream = new FileInputStream(inputFile);
	}
	
	public TupleInsertMessage readNext(){
		try {
			return TupleInsertMessage.parseDelimitedFrom(this.inStream);
		} catch (IOException e) {
			LOGGER.error("error while reading TupleInsertMessage: {}", LogHelper.getStackTrace(e));
			return null;
		}
	}

	@Override
	public Map<String, PrimitiveTypeProvider> convert(TupleInsertMessage message) {
		if(message == null){
			return null;
		}
		
		Map<String, DataMessage> data = message.getData();
		
		HashMap<String, PrimitiveTypeProvider> map = new HashMap<>();
		
		for(String key : data.keySet()){
			map.put(key, DataMessageConverter.convert(data.get(key)));
		}
		
		return map;
	}
	
	
}
