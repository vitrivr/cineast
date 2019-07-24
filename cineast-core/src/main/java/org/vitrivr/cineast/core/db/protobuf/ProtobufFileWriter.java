package org.vitrivr.cineast.core.db.protobuf;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.vitrivr.cineast.core.config.Config;
import org.vitrivr.cineast.core.db.PersistentTuple;
import org.vitrivr.cineast.core.util.LogHelper;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;


public class ProtobufFileWriter extends ProtobufTupleGenerator {

	private static File baseFolder = new File(Config.sharedConfig().getExtractor().getOutputLocation(), "proto");
  private static final Logger LOGGER = LogManager.getLogger();

		
	private FileOutputStream out;
	
	public ProtobufFileWriter(){
		super("id", "feature");
	}
	
	public ProtobufFileWriter(String...names){
		super(names);
	}
	
	@Override
	public boolean open(String name) {
		baseFolder.mkdirs();
		if(this.out != null){
		  return false;
		}
		try {
			this.out = new FileOutputStream(new File(baseFolder, name + ".bin"));
			return true;
		} catch (FileNotFoundException e) {
			return false;
		}
	}

	@Override
	public boolean close() {
		if(out == null){
			return true;
		}
		
		try {
			out.flush();
			out.close();
			return true;
		} catch (IOException e) {
			return false;
		}finally{
		  out = null;
		}
	}

	@Override
	public boolean idExists(String id) {
		return false;
	}

	@Override
	public boolean persist(PersistentTuple tuple) {
		try {
			getPersistentRepresentation(tuple).writeDelimitedTo(out);
			out.flush();
			return true;
		} catch (IOException e) {
		  LOGGER.error("error in persist: {}", LogHelper.getStackTrace(e));
			return false;
		}
	}

  @Override
	public boolean persist(List<PersistentTuple> persistentTuples) {
		boolean success = true;
	  for(PersistentTuple t : persistentTuples){
		  success &= persist(t);
		}
	  return success;
	}


	public static void setDefaultFolder(File outputFolder) {
		if(outputFolder == null){
			throw new NullPointerException("outputfolder cannot be null");
		}
		baseFolder = outputFolder;
		baseFolder.mkdirs();
	}

	@Override
	public boolean exists(String key, String value) {
		return false;
	}

  @Override
  protected void finalize() throws Throwable {
    close();
    super.finalize();
  }


}
