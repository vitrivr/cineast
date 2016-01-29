package ch.unibas.cs.dbis.cineast.core.db;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import ch.unibas.cs.dbis.cineast.core.util.LogHelper;

public class CSVWriter implements PersistencyWriter<CSVTuple> {

	private File file;
	private static File folder;
	private PrintWriter writer;
	private int flushTrigger = 0;
	
	static{
		setFolder(new File("extracted"));
	}
	
	private static final Logger LOGGER = LogManager.getLogger();
	
	/**
	 * sets the folder to which all output files are written
	 * @param folder
	 */
	public static void setFolder(File folder){
		CSVWriter.folder = folder;
		folder.mkdirs();
	}
	
	@Override
	public boolean open(String name) {
		this.file = new File(folder, name + ".csv");
		try {
			this.writer = new PrintWriter(file);
		} catch (IOException e) {
			LOGGER.error(LogHelper.getStackTrace(e));
			return false;
		}
		return this.file.canWrite();
	}

	@Override
	public boolean check(String condition) {
		return false;
	}

	@Override
	public CSVTuple makeTuple(Object... objects) {
		CSVTuple tuple = new CSVTuple(this);
		for(Object o : objects){
			tuple.addElement(o);
		}
		return tuple;
	}

	@Override
	public void write(CSVTuple tuple) {
		this.writer.println(tuple.getPersistentRepresentation());
		if(++this.flushTrigger % 10 == 0){
			this.writer.flush();
		}
	}

	@Override
	public boolean close() {
		if(this.writer == null){
			return true;
		}
		this.writer.flush();
		this.writer.close();
		return true;
	}

}
