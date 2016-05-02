package ch.unibas.cs.dbis.cineast.core.db;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.LinkedList;
import java.util.List;
import java.util.Properties;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import ch.unibas.cs.dbis.cineast.core.config.Config;
import ch.unibas.cs.dbis.cineast.core.config.DatabaseConfig;
import ch.unibas.cs.dbis.cineast.core.data.Shot;
import ch.unibas.cs.dbis.cineast.core.util.LogHelper;

public class ShotLookup {

	private static final Logger LOGGER = LogManager.getLogger();
	
	private Connection connection;
	
	public ShotLookup(){
		this(Config.getDatabaseConfig());
	}
	
	public ShotLookup(DatabaseConfig config){
		this(config.getLocation(), config.getUser(), config.getPassword());
	}
	
	public ShotLookup(String database, String username, String password){
		Properties props = new Properties();

		props.setProperty("user", username);
		props.setProperty("password", password);
		props.setProperty("tcpKeepAlive", "true");

		String url = "jdbc:postgresql://" + database;
		try {
			connection = DriverManager.getConnection(url, props);
		} catch (SQLException e) {
			LOGGER.fatal(LogHelper.SQL_MARKER, LogHelper.getStackTrace(e));
		}
	}
	
	public void close(){
		try {
			this.connection.close();
		} catch (SQLException e) {
			LOGGER.warn(LogHelper.SQL_MARKER, LogHelper.getStackTrace(e));
		}
	}
	
	public ShotDescriptor lookUpShot(String shotId){
		ResultSet set = null;
		try {
			PreparedStatement statement = connection.prepareStatement("SELECT * FROM cineast.videos JOIN cineast.shots ON (cineast.videos.id = cineast.shots.video) WHERE cineast.shots.id = " + shotId);
			set = statement.executeQuery();
		} catch (SQLException e) {
			LOGGER.warn(LogHelper.SQL_MARKER, LogHelper.getStackTrace(e));
		}
		
//		return new ShotDescriptor(set, shotId);
		return null;
		
	}
	
	
	public String lookUpVideoid(String name){
		
//		try {
//			PreparedStatement statement = connection.prepareStatement("SELECT id FROM cineast.videos WHERE name = \'" + name + "\'");
//			ResultSet set = statement.executeQuery();
//			if(set.next()){
//				return set.getInt(1);
//			}
//		} catch (SQLException e) {
//			LOGGER.warn(LogHelper.SQL_MARKER, LogHelper.getStackTrace(e));
//		}
//		return -1;
		
		return "";

	}
	
	public List<ShotDescriptor> lookUpVideo(String videoId){
		LinkedList<ShotDescriptor> _return = new LinkedList<ShotLookup.ShotDescriptor>();
//		try {
//			PreparedStatement statement = connection.prepareStatement("SELECT id FROM cineast.shots WHERE video = " + videoId);
//			ResultSet set = statement.executeQuery();
//			while(set.next()){
//				long shotId = set.getLong(1);
//				ShotDescriptor des = lookUpShot(shotId);
//				if(des.videoId == videoId){//sanity check
//					_return.add(des);
//				}
//			}
//		} catch (SQLException e) {
//			LOGGER.warn(LogHelper.SQL_MARKER, LogHelper.getStackTrace(e));
//		}
		
		return _return;
	}
	
	@Override
	protected void finalize() throws Throwable {
		this.connection.close();
		super.finalize();
	}

	public static class ShotDescriptor{
		
		private String shotId, videoId;
		private int  width = -1, height = -1, framecount = -1, shotNumber = -1, startFrame = -1, endFrame = -1;
		private float seconds = -1f, fps = 0;
		private String name = null, path = null;
		
//		ShotDescriptor(String shotId){
//			this.shotId = shotId;
//			if(rset != null){
//				try {
//					rset.next();
//					
//					
//					
////					this.videoId	= rset.getInt(1);
//					this.name	= rset.getString(2);
//					this.path	= rset.getString(3);
//					this.width	= rset.getInt(4);
//					this.height	= rset.getInt(5);
//					this.framecount	= rset.getInt(6);
//					this.seconds = rset.getFloat(7);
//					this.shotNumber = rset.getInt(9);
//					this.startFrame = rset.getInt(11);
//					this.endFrame = rset.getInt(12);
//					this.fps = framecount / seconds;
//					if(Float.isNaN(fps) || Float.isInfinite(fps)){
//						this.fps = 0;
//					}
//				} catch (SQLException e) {
//					LOGGER.warn(LogHelper.SQL_MARKER, "Error for ShotID {}", shotId);
//					LOGGER.warn(LogHelper.SQL_MARKER, LogHelper.getStackTrace(e));
//				}
//			}
//		}
		
		public ShotDescriptor(String videoId, int shotNumber, int startFrame, int endFrame) {
			this.videoId = videoId;
			this.shotId = Shot.generateShotID(videoId, shotNumber);
			this.startFrame = startFrame;
			this.endFrame = endFrame;
			this.framecount = endFrame - startFrame + 1;
			this.shotNumber = shotNumber;
		}

		public String getShotId() {
			return shotId;
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

		public int getShotNumber() {
			return shotNumber;
		}

		public int getStartFrame() {
			return startFrame;
		}

		public int getEndFrame() {
			return endFrame;
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
			return "ShotDescriptor(" + shotId + ")";
		}

	}
	
}
