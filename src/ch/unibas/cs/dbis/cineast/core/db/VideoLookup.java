package ch.unibas.cs.dbis.cineast.core.db;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import ch.unibas.cs.dbis.cineast.core.util.LogHelper;

public class VideoLookup extends AbstractLookup{
	
	public VideoLookup(){
		super();
	}
	
	public VideoLookup(String database, String username, String password){
		super(database, username, password);
	}
	
	public VideoDescriptor lookUpVideo(String videoId){
		ResultSet set = null;
		try {
			PreparedStatement statement = connection.prepareStatement("SELECT * FROM cineast.videos WHERE cineast.videos.id = " + videoId);
			set = statement.executeQuery();
		} catch (SQLException e) {
			LOGGER.warn(LogHelper.SQL_MARKER, LogHelper.getStackTrace(e));
		}
		
		return new VideoDescriptor(set, videoId);
		
	}
	
public static class VideoDescriptor{
		
		private String videoId; 
		private int width, height, framecount;
		private float seconds, fps;
		private String name = null, path = null;
		
		VideoDescriptor(ResultSet rset, String videoId){
			this.videoId = videoId;
			if(rset != null){
				try {
					rset.next();
					this.name	= rset.getString(2);
					this.path	= rset.getString(3);
					this.width	= rset.getInt(4);
					this.height	= rset.getInt(5);
					this.framecount	= rset.getInt(6);
					this.seconds = rset.getFloat(7);
					this.fps = framecount / seconds;
					if(Float.isNaN(fps) || Float.isInfinite(fps)){
						this.fps = 0;
					}
				} catch (SQLException e) {
					LOGGER.warn(LogHelper.SQL_MARKER, "Error for VideoID {}", videoId);
					LOGGER.warn(LogHelper.SQL_MARKER, LogHelper.getStackTrace(e));
				}
			}
		}

		public String getVideoId() {
			return videoId;
		}

		public int getWidth() {
			return width;
		}

		public int getHeight() {
			return height;
		}

		public int getFramecount() {
			return framecount;
		}
		
		public float getSeconds() {
			return seconds;
		}

		public String getName() {
			return name;
		}

		public String getPath() {
			return path;
		}
		
		public float getFPS(){
			return fps;
		}

		@Override
		public String toString() {
			return "VideoDescriptor(" + videoId + ")";
		}

	}
	

}
