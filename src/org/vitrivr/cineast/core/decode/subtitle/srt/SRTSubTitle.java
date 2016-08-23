package org.vitrivr.cineast.core.decode.subtitle.srt;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.vitrivr.cineast.core.decode.subtitle.SubTitle;
import org.vitrivr.cineast.core.util.LogHelper;

public class SRTSubTitle implements SubTitle {

	private static final Logger LOGGER = LogManager.getLogger();
	
	private HashMap<Integer, SRTSubtitleItem> items = new HashMap<>();
	
	private int maxId = -1;
	private long startTime = -1, endTime = -1;
	private final float framerate;

	public SRTSubTitle(File file, float framerate){
		
		this.framerate = framerate;
		
		LOGGER.info("Loading SRT subtitle from {}", file.getAbsolutePath());
		try {
			BufferedReader reader = new BufferedReader(new FileReader(file));
			
			String line1, line2, line3;
			StringBuffer text;
			int id = 0;
			long start, end;
			loop:
			while((line1 = reader.readLine()) != null){
				
				while(line1.isEmpty()){
					line1 = reader.readLine();
						if(line1 == null){
							break loop;
						}
					}

				try {
					id = Integer.parseInt(line1);
					
					this.maxId = Math.max(maxId, id);
					
					line2 = reader.readLine();
					if(line2 == null){ break; }
					String[] timing = line2.split(" --> ");
					if(timing.length != 2){ break; }
					
					start = parseTime(timing[0]);
					end = parseTime(timing[1]);
					
					if(this.startTime == -1){
						this.startTime = start;
					}
					
					this.endTime = end;
					
					text = new StringBuffer();
					while((line3 = reader.readLine()) != null && !line3.isEmpty()){
						text.append(line3);
						text.append('\n');
					}
					
					items.put(id, new SRTSubtitleItem(id, start, end, text.toString(), this));
				} catch (NumberFormatException e) {
					LOGGER.warn("Error while parsing subtitle item");
					LOGGER.warn(LogHelper.getStackTrace(e));
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
	
	
	@Override
	public String toString() {
		return "SRT Subtitle, " + getNumerOfItems() + " elements, maxId: " + this.maxId + ", startTime:  " + this.startTime + ", endTime:  " + this.endTime;
	}


	static long parseTime(String time){
		long h = 0, m = 0, s = 0, ms = 0;
		String[] splits = time.split(":");
		if(splits.length != 3){
			return -1;
		}
		
		h = Long.parseLong(splits[0]);
		m = Long.parseLong(splits[1]);
		splits = splits[2].split(",");
		
		if(splits.length != 2){
			return -1;
		}
		
		s = Long.parseLong(splits[0]);
		ms = Long.parseLong(splits[1]);
		
		return ms + 1000L * s + 60000L * m + 3600000L * h;
		
	}
	
	/* (non-Javadoc)
	 * @see subsync.SubTitle#getNumerOfItems()
	 */
	@Override
	public int getNumerOfItems(){
		return this.items.size();
	}
	
	/* (non-Javadoc)
	 * @see subsync.SubTitle#getItem(int)
	 */
	@Override
	public SRTSubtitleItem getItem(int id){
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
	
}
