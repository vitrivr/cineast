package org.vitrivr.cineast.core.decode.shotboundary;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.List;

import org.vitrivr.cineast.core.data.entities.MediaSegmentDescriptor;

public final class ShotBoundaryDecoder {

	private ShotBoundaryDecoder(){}
	
	public static List<MediaSegmentDescriptor> decode(File boundaryFile, String videoId) throws NullPointerException, SecurityException, FileNotFoundException{
		if(boundaryFile == null){
			throw new NullPointerException("boundaryFile was null in ShotBoundaryDecoder.decode()");
		}
		
		if(videoId == null){
			throw new NullPointerException("videoId was null in ShotBoundaryDecoder.decode()");
		}
		
		if(!boundaryFile.exists() || !boundaryFile.isFile()){
			throw new FileNotFoundException("'" + boundaryFile.getAbsolutePath() + "' was not found in ShotBoundaryDecoder.decode()");
		}
		
		if(!boundaryFile.canRead()){
			throw new SecurityException("'" + boundaryFile.getAbsolutePath() + "' cannot be read in ShotBoundaryDecoder.decode()");
		}
		
		String extension = "";

		int i = boundaryFile.getName().lastIndexOf('.');
		if (i > 0) {
		    extension = boundaryFile.getName().substring(i+1);
		}
		
		switch(extension){
		
		case "msb":
		case "sb":{
			return TrecvidMasterShotReferenceDecoder.decode(boundaryFile, videoId);
		}
		
		default:
			throw new IllegalArgumentException("'" + extension + "' is not a recognized file extension in ShotBoundaryDecoder.decode()");
		}
		
		
	}
	
}
