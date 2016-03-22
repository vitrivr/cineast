package ch.unibas.cs.dbis.cineast.core.db;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Properties;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.common.base.Joiner;

import ch.unibas.cs.dbis.cineast.core.config.Config;
import ch.unibas.cs.dbis.cineast.core.data.LongDoublePair;
import ch.unibas.cs.dbis.cineast.core.db.ShotLookup.ShotDescriptor;
import ch.unibas.cs.dbis.cineast.core.util.LogHelper;
import gnu.trove.iterator.TLongIterator;
import gnu.trove.set.hash.TLongHashSet;

public final class DBResultCache {

private DBResultCache(){}
	
	private static final Logger LOGGER = LogManager.getLogger();
	private static DateFormat df = new SimpleDateFormat("SSS-ss-mm-HH-dd-MM-yyyy-");
	private static Connection dbConnection;
	private static HashSet<String> resultNames = new HashSet<>();
	
	private static final class ShutdownCacheThread extends Thread{
		
		@Override
		public void run(){
			for(String name : resultNames){
				deleteResultFromDB(name);
			}
			resultNames.clear();
			try {
				dbConnection.close();
			} catch (SQLException e) {
				LOGGER.warn(LogHelper.SQL_MARKER, LogHelper.getStackTrace(e));
			}
		}
		
	}
	
	static{
		//init db connection
		dbConnection = connectToDB();
		
		//register ShutdownHook
		Runtime.getRuntime().addShutdownHook(new ShutdownCacheThread());
	}
	
	private static Connection connectToDB(){
		Properties props = new Properties();

		props.setProperty("user", Config.getDatabaseConfig().getUser());
		props.setProperty("password", Config.getDatabaseConfig().getPassword());
		props.setProperty("tcpKeepAlive", "true");
		props.setProperty("socketTimeout", "60");

		Connection connection = null;
		
		try {
			connection = DriverManager.getConnection("jdbc:postgresql://" + Config.getDatabaseConfig().getLocation(), props);
		} catch (SQLException e) {
			LOGGER.fatal(LogHelper.SQL_MARKER, LogHelper.getStackTrace(e));
		}
		return connection;
	}
	
	private static String createUniqueName(){
		return df.format(Calendar.getInstance().getTime()) + Config.UNIQUE_ID;
	}
	
	private static long insertNewSetToDB(String name){
		long id = -1L;
		try {
			PreparedStatement statement = dbConnection.prepareStatement("INSERT INTO cineast.resultcachenames (name) VALUES (?) RETURNING id");
			statement.setString(1, name);
			ResultSet result = statement.executeQuery();
			if(result.next()){
				id = result.getLong(1);
			}
		} catch (SQLException e) {
			LOGGER.fatal(LogHelper.SQL_MARKER, LogHelper.getStackTrace(e));
		}
		return id;
	}
	
	private static long getIdByCacheName(String name){
		try {
			PreparedStatement statement = dbConnection.prepareStatement("SELECT id FROM cineast.resultcachenames WHERE name = ?");
			statement.setString(1, name);
			ResultSet rset = statement.executeQuery();
			if(rset.next()){
				return rset.getLong(1);
			}
		} catch (SQLException e) {
			LOGGER.fatal(LogHelper.SQL_MARKER, LogHelper.getStackTrace(e));
		}
		return -1L;
	}
	
	private static void addResultElementsToDB(long id, long[] results){
		try {
			PreparedStatement statement = dbConnection.prepareStatement("INSERT INTO cineast.resultcacheelements (chacheid, shotid) VALUES (?, ?)");
			for(long shorid : results){
				statement.setLong(1, id);
				statement.setLong(2, shorid);
				statement.addBatch();
			}
			statement.executeBatch();
		} catch (SQLException e) {
			LOGGER.fatal(LogHelper.SQL_MARKER, LogHelper.getStackTrace(e));
		}
	}
	
	private static void deleteResultFromDB(String resultName){
		try {
			PreparedStatement statement = dbConnection.prepareStatement("DELETE FROM cineast.resultcachenames WHERE name = ?");
			statement.setString(1, resultName);
			statement.executeQuery();
		} catch (SQLException e) {
			LOGGER.fatal(LogHelper.SQL_MARKER, LogHelper.getStackTrace(e));
		}
	}
	
	public static void deleteResult(String name){
		if(resultNames.contains(name)){
			deleteResultFromDB(name);
			resultNames.remove(name);
		}
	}
	
	private static synchronized String newCachedResult(long[] ids){
		synchronized(dbConnection){
			String name = createUniqueName();
			long id = insertNewSetToDB(name);
			addResultElementsToDB(id, ids);
			resultNames.add(name);
			LOGGER.info("Caching result {}", name);
			return name;
		}
	}
	
	public static String newCachedResult(List<LongDoublePair> result){
		long[] tmp = new long[result.size()];
		int i = 0;
		for(LongDoublePair ldp : result){
			tmp[i++] = ldp.key;
		}
		return newCachedResult(tmp);
	}
	
	public static String newCachedResult(TLongHashSet result){
		long[] tmp = new long[result.size()];
		int i = 0;
		TLongIterator iter = result.iterator();
		while(iter.hasNext()){
			tmp[i++] = iter.next();
		}
		return newCachedResult(tmp);
	}
	
	public static String cacheVideosByIds(List<Long> videoids){
		
		Collections.sort(videoids);
		
		String name = Joiner.on('-').join(videoids);
		
		//test if this constellation is cached already
		long id = getIdByCacheName(name);
		
		if(id < 0){ //if not, make new one
			id = insertNewSetToDB(name);
		}
		
		StringBuilder sb = new StringBuilder();
		sb.append("INSERT INTO cineast.resultcacheelements (chacheid, shotid) SELECT ");
		sb.append(id);
		sb.append(", id FROM cineast.shots WHERE video IN (");
		sb.append(Joiner.on(',').join(videoids));
		sb.append(")");
		
		try {
			PreparedStatement insert = dbConnection.prepareStatement(sb.toString());
			insert.executeQuery();
		} catch (SQLException e) {
			LOGGER.fatal(LogHelper.SQL_MARKER, LogHelper.getStackTrace(e));
		}
		
		return name;
		
	}

	
	/**
	 * creates special result caches such as those generated from video ids.
	 * @param resultCacheName
	 */
	public static void createIfNecessary(String resultCacheName) {
		if(resultCacheName == null){
			return;
		}
		resultCacheName = resultCacheName.toLowerCase();
		if(!resultCacheName.startsWith("v")){
			return;
		}
		resultCacheName = resultCacheName.substring(1);
		String[] idsStrings = resultCacheName.split("-");
		int[] ids = new int[idsStrings.length];
		for(int i = 0; i < idsStrings.length; ++i){
			try{
				ids[i] = Integer.parseInt(idsStrings[i]);
			}catch(NumberFormatException e){
				//ignore?!
			}
		}
		Arrays.sort(ids);
		StringBuilder builder = new StringBuilder();
		builder.append('v');
		for(int i = 0; i < ids.length - 1; ++i){
			builder.append(ids[i]);
			builder.append('-');
		}
		builder.append(ids[ids.length - 1]);
		String newResultCacheName = builder.toString();
		if(getIdByCacheName(newResultCacheName) > -1L){
			return;
		}
		
		long resultCacheId = insertNewSetToDB(newResultCacheName);
		ShotLookup sl = new ShotLookup();
		for(int videoId : ids){
			List<ShotDescriptor> shots = sl.lookUpVideo(videoId);
			long[] shotIds = new long[shots.size()];
			int i = 0;
			for(ShotDescriptor s : shots){
				shotIds[i++] = s.getShotId();
			}
			addResultElementsToDB(resultCacheId, shotIds);
		}
		sl.close();
	}
	
}
