package org.vitrivr.cineast.core.decode.subtitle.cc;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.vitrivr.cineast.core.decode.subtitle.SubTitleDecoder;
import org.vitrivr.cineast.core.decode.subtitle.SubtitleItem;
import org.vitrivr.cineast.core.util.LogHelper;

public class CCSubTitleDecoder implements SubTitleDecoder {

	private static final Logger LOGGER = LogManager.getLogger();
	private static final DateFormat dateFormat = new SimpleDateFormat("yyyyMMddHHmmss");
	
	private int maxId = -1;
	private long startTime = -1, endTime = -1;
	private int pointer = 0;
	private ArrayList<CCSubTitleItem> items = new ArrayList<>();
	
	public CCSubTitleDecoder(Path file){

		LOGGER.info("Loading CC subtitle from {}", file);
		
		try (final BufferedReader reader = Files.newBufferedReader(file)) {
			String line;
			while((line = reader.readLine()) != null){
				String command = line.substring(0, 3);
				switch (command) {
				case "TOP":{
					String[] split = line.split("\\|");
					this.startTime = parseTime(split[1]);
					break;
				}
				case "COL":{//ignore
					break;
				}
				case "UID":{//ignore
					break;
				}
				case "DUR":{//ignore
					break;
				}
				case "VID":{//ignore
					break;
				}
				case "SRC":{//ignore
					break;
				}
				case "CMT":{//ignore
					break;
				}
				case "LBT":{//ignore
					break;
				}
				case "END":{
					String[] split = line.split("\\|");
					this.endTime = parseTime(split[1]);
					break;
				}
				default:{
					String[] split = line.split("\\|");
					if(!split[2].startsWith("CC")){
						break;
					}
					
					long start = parseFrame(split[0]);
					long end = parseFrame(split[1]);
					
					int id = ++maxId;
					
					this.items.add(new CCSubTitleItem(id, start, end, split[3]));
					
					break;
				}
				}
				
			}
			reader.close();
			
		} catch (IOException e) {
			LOGGER.warn("Error while loading subtitle");
			LOGGER.warn(LogHelper.getStackTrace(e));
		}
	}
	
	private static long parseTime(String str){
		try{
			String s = str.substring(0,14);
			return dateFormat.parse(s).getTime();
		}catch(ParseException e){
			LOGGER.error("error while parsing time {} in CCSubTitleDecoder", str);
			return -1;
		}
		 
	}
	
	private long parseFrame(String str){
		
		long dt = parseTime(str) - this.startTime;
		
		if(str.length() > 15){
			float frac = Float.parseFloat(str.substring(14));
			dt += 1000 * frac;
		}
		
		return dt;
	}
	
	@Override
	public int getNumerOfItems() {
		return this.items.size();
	}

	/**
	 *
	 * @param index
	 * @return
	 */
	public SubtitleItem get(int index) {
		return this.items.get(index);
	}

	/**
	 *
	 * @return
	 */
	public SubtitleItem getLast() {
		return this.items.get(pointer);
	}

	/**
	 * Increments the internal pointer by one. Returns true, if increment was successful and false otherwise.
	 *
	 * @return True if increment was successful and false otherwise.
	 */
	public boolean increment() {
		if (this.pointer + 1 < this.items.size()) {
			this.pointer += 1;
			return true;
		} else {
			return false;
		}
	}

	/**
	 * Rewinds the {@link SubTitleDecoder} stream and sets the internal pointer to 0.
	 */
	public void rewind() {
		this.pointer = 0;
	}

	@Override
	public String toString() {
		return "CC Subtitle, " + getNumerOfItems() + " elements, maxId: " + this.maxId + ", startTime:  " + this.startTime + ", endTime:  " + this.endTime;
	}

}
