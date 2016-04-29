package ch.unibas.cs.dbis.cineast.core.db;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import ch.unibas.dmi.dbis.adam.http.Grpc.InsertMessage.TupleInsertMessage;

public class ProtobufFileWriter extends ProtobufTupleGenerator {

	private static File baseFolder = new File("proto");
		
	private FileOutputStream out;
	
	public ProtobufFileWriter(){
		super("id");
	}
	
	public ProtobufFileWriter(String...names){
		super(names);
	}
	
	@Override
	public boolean open(String name) {
		baseFolder.mkdirs();
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
			out = null;
			return true;
		} catch (IOException e) {
			return false;
		}
	}

	@Override
	public boolean idExists(String id) {
		return false;
	}

	@Override
	public boolean persist(PersistentTuple<TupleInsertMessage> tuple) {
		try {
			tuple.getPersistentRepresentation().writeTo(out);
			out.flush();
			return true;
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return false;
		}
	}

	public static void setFolder(File outputFolder) {
		if(outputFolder == null){
			throw new NullPointerException("outputfolder cannot be null");
		}
		baseFolder = outputFolder;
		baseFolder.mkdirs();
	}

}
