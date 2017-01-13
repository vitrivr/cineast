package org.vitrivr.cineast.core.decode.shotboundary;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.vitrivr.cineast.core.data.MediaType;
import org.vitrivr.cineast.core.db.SegmentLookup.SegmentDescriptor;

public class TrecvidMasterShotReferenceDecoder {

	private TrecvidMasterShotReferenceDecoder(){}
	
	private static final Logger LOGGER = LogManager.getLogger();
	
	/**
	 * decodes shot boundaries in the format used for TRECVID
	 * @param msr the file containing the master shot reference
	 * @param videoId the video id
	 */
	public static List<SegmentDescriptor> decode(File msr, String videoId){
		
		ArrayList<SegmentDescriptor> _return = new ArrayList<>();
		
		try {
			BufferedReader reader = new BufferedReader(new FileReader(msr));
			String line = null;
			int shotCounter = 0;
			
			while((line = reader.readLine()) != null){
				line = line.trim();
				
				if(line.isEmpty()){ //skip empty lines
					continue;
				}
				
				if(!Character.isDigit(line.charAt(0))){//line does not start with a number
					continue;
				}
				
				String[] split = line.split(" ");
				if(split.length < 2){//there are not two blocks on this line
					continue;
				}
				
				int start, end;
				try{
					start = 1 + Integer.parseInt(split[0]); //TRECVID msr starts with 0
					end = 1 + Integer.parseInt(split[1]);
				}catch(NumberFormatException e){
					continue;
				}
				
				++shotCounter;
				
				_return.add(new SegmentDescriptor(videoId, MediaType.generateId(MediaType.VIDEO, videoId, shotCounter), shotCounter, start, end));
				
			}
			
			reader.close();
			
		} catch (FileNotFoundException e) {
			LOGGER.error("error in TrecvidMasterShotReferenceDecoder.decode, file '{}' was not found", msr.getAbsolutePath());
		} catch (IOException e) {
			LOGGER.error("error while reading file '{}' in TrecvidMasterShotReferenceDecoder.decode: {}", msr.getAbsolutePath(), e.getMessage());
		}
		
		return _return;
	}
	
}
