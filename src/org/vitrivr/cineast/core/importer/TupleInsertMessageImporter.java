package org.vitrivr.cineast.core.importer;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.vitrivr.adam.grpc.AdamGrpc.InsertMessage.TupleInsertMessage;
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
	
	
}
