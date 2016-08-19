package org.vitrivr.cineast.core.decode.subtitle.cc;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.vitrivr.cineast.core.decode.subtitle.SubTitle;
import org.vitrivr.cineast.core.decode.subtitle.SubtitleItem;
import org.vitrivr.cineast.core.util.LogHelper;

import gnu.trove.map.hash.TIntObjectHashMap;

public class CCSubTitle implements SubTitle {

	private static final Logger LOGGER = LogManager.getLogger();
	private static final DateFormat dateFormat = new SimpleDateFormat("yyyyMMddHHmmss");
	
	private int maxId = -1;
	private long startTime = -1, endTime = -1;
	private final float framerate;
	private TIntObjectHashMap<CCSubTitleItem> items = new TIntObjectHashMap<>();
	
	public CCSubTitle(File file, float frameRate){
		
		this.framerate = frameRate;
		
		LOGGER.info("Loading CC subtitle from {}", file.getAbsolutePath());
		
		try {
			BufferedReader reader = new BufferedReader(new FileReader(file));
			
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
					
					this.items.put(id, new CCSubTitleItem(id, start, end, split[3], this));
					
					break;
				}
				}
				
			}
			reader.close();
			
		} catch (FileNotFoundException e) {
			LOGGER.warn("Error while loading subtitle");
			LOGGER.warn(LogHelper.getStackTrace(e));
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
			LOGGER.error("error while parsing time {} in CCSubTitle", str);
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

	@Override
	public SubtitleItem getItem(int id) {
		if(this.items.containsKey(id)){
			return this.items.get(id);
		}
		LOGGER.warn("Subtitle does not contain item " + id);
		return null;
	}

	@Override
	public float getFrameRate() {
		return this.framerate;
	}
	
	@Override
	public String toString() {
		return "CC Subtitle, " + getNumerOfItems() + " elements, maxId: " + this.maxId + ", startTime:  " + this.startTime + ", endTime:  " + this.endTime;
	}

}
