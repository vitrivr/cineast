package org.vitrivr.cineast.core.extraction.decode.shotboundary;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.vitrivr.cineast.core.data.MediaType;
import org.vitrivr.cineast.core.data.entities.MediaSegmentDescriptor;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class TrecvidMasterShotReferenceDecoder {

	private TrecvidMasterShotReferenceDecoder(){}
	
	private static final Logger LOGGER = LogManager.getLogger();
	
	/**
	 * decodes shot boundaries in the format used for TRECVID
	 * @param msr the file containing the master shot reference
	 * @param videoId the video id
	 */
	public static List<MediaSegmentDescriptor> decode(File msr, String videoId){
		
		ArrayList<MediaSegmentDescriptor> _return = new ArrayList<>();
		
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

				/* TODO: Derive absolute start and end position of MediaSegmentDescriptor. */
				_return.add(new MediaSegmentDescriptor(videoId, MediaType.generateSegmentId(MediaType.VIDEO, videoId, shotCounter), shotCounter, start, end, -1.0f, -1.0f));
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
