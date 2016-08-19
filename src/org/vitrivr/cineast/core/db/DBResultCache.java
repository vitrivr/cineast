package org.vitrivr.cineast.core.db;

import java.sql.Connection;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashSet;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.vitrivr.cineast.core.config.Config;

public final class DBResultCache {

private DBResultCache(){}
	
	private static final Logger LOGGER = LogManager.getLogger();
	private static DateFormat df = new SimpleDateFormat("SSS-ss-mm-HH-dd-MM-yyyy-");
	
	private static HashSet<String> resultNames = new HashSet<>();
	
	private static final class ShutdownCacheThread extends Thread{
		
		@Override
		public void run(){
			for(String name : resultNames){
				deleteResultFromDB(name);
			}
			resultNames.clear();
			
		}
		
	}
	
	static{
		
		
		//register ShutdownHook
		Runtime.getRuntime().addShutdownHook(new ShutdownCacheThread());
	}
	
	private static Connection connectToDB(){
//		Properties props = new Properties();
//
//		props.setProperty("user", Config.getDatabaseConfig().getUser());
//		props.setProperty("password", Config.getDatabaseConfig().getPassword());
//		props.setProperty("tcpKeepAlive", "true");
//		props.setProperty("socketTimeout", "60");
//
//		Connection connection = null;
//		
//		try {
//			connection = DriverManager.getConnection("jdbc:postgresql://" + Config.getDatabaseConfig().getLocation(), props);
//		} catch (SQLException e) {
//			LOGGER.fatal(LogHelper.SQL_MARKER, LogHelper.getStackTrace(e));
//		}
//		return connection;
		return null;
	}
	
	private static String createUniqueName(){
		return df.format(Calendar.getInstance().getTime()) + Config.UNIQUE_ID;
	}
	
	private static long insertNewSetToDB(String name){
		long id = -1L;
//		try {
//			PreparedStatement statement = dbConnection.prepareStatement("INSERT INTO cineast.resultcachenames (name) VALUES (?) RETURNING id");
//			statement.setString(1, name);
//			ResultSet result = statement.executeQuery();
//			if(result.next()){
//				id = result.getLong(1);
//			}
//		} catch (SQLException e) {
//			LOGGER.fatal(LogHelper.SQL_MARKER, LogHelper.getStackTrace(e));
//		}
		return id;
	}
	
	private static long getIdByCacheName(String name){
//		try {
//			PreparedStatement statement = dbConnection.prepareStatement("SELECT id FROM cineast.resultcachenames WHERE name = ?");
//			statement.setString(1, name);
//			ResultSet rset = statement.executeQuery();
//			if(rset.next()){
//				return rset.getLong(1);
//			}
//		} catch (SQLException e) {
//			LOGGER.fatal(LogHelper.SQL_MARKER, LogHelper.getStackTrace(e));
//		}
		return -1L;
	}
	
	private static void addResultElementsToDB(long id, String[] results){
//		try {
//			PreparedStatement statement = dbConnection.prepareStatement("INSERT INTO cineast.resultcacheelements (chacheid, shotid) VALUES (?, ?)");
//			for(String shorid : results){
//				statement.setLong(1, id);
//				statement.setLong(2, shorid);
//				statement.addBatch();
//			}
//			statement.executeBatch();
//		} catch (SQLException e) {
//			LOGGER.fatal(LogHelper.SQL_MARKER, LogHelper.getStackTrace(e));
//		}
	}
	
	private static void deleteResultFromDB(String resultName){
//		try {
//			PreparedStatement statement = dbConnection.prepareStatement("DELETE FROM cineast.resultcachenames WHERE name = ?");
//			statement.setString(1, resultName);
//			statement.executeQuery();
//		} catch (SQLException e) {
//			LOGGER.fatal(LogHelper.SQL_MARKER, LogHelper.getStackTrace(e));
//		}
	}
	
	public static void deleteResult(String name){
//		if(resultNames.contains(name)){
//			deleteResultFromDB(name);
//			resultNames.remove(name);
//		}
	}
	
	private static synchronized String newCachedResult(String[] ids){
//		synchronized(dbConnection){
//			String name = createUniqueName();
//			long id = insertNewSetToDB(name);
//			addResultElementsToDB(id, ids);
//			resultNames.add(name);
//			LOGGER.info("Caching result {}", name);
//			return name;
//		}
		return "";
	}
	
	public static String newCachedResult(HashSet<String> result){
//		String[] tmp = new String[result.size()];
//		int i = 0;
//		for(String s : result){
//			tmp[i++] = s;
//		}
//		return newCachedResult(tmp);
		return "";
	}
	
//	public static String newCachedResult(TLongHashSet result){
//		long[] tmp = new long[result.size()];
//		int i = 0;
//		TLongIterator iter = result.iterator();
//		while(iter.hasNext()){
//			tmp[i++] = iter.next();
//		}
//		return newCachedResult(tmp);
//	}
	
	public static String cacheVideosByIds(List<Long> videoids){
		
//		Collections.sort(videoids);
//		
//		String name = Joiner.on('-').join(videoids);
//		
//		//test if this constellation is cached already
//		long id = getIdByCacheName(name);
//		
//		if(id < 0){ //if not, make new one
//			id = insertNewSetToDB(name);
//		}
//		
//		StringBuilder sb = new StringBuilder();
//		sb.append("INSERT INTO cineast.resultcacheelements (chacheid, shotid) SELECT ");
//		sb.append(id);
//		sb.append(", id FROM cineast.shots WHERE video IN (");
//		sb.append(Joiner.on(',').join(videoids));
//		sb.append(")");
//		
//		try {
//			PreparedStatement insert = dbConnection.prepareStatement(sb.toString());
//			insert.executeQuery();
//		} catch (SQLException e) {
//			LOGGER.fatal(LogHelper.SQL_MARKER, LogHelper.getStackTrace(e));
//		}
//		
//		return name;
		
		return "";
		
	}

	
	/**
	 * creates special result caches such as those generated from video ids.
	 * @param resultCacheName
	 */
	public static void createIfNecessary(String resultCacheName) {
//		if(resultCacheName == null){
//			return;
//		}
//		resultCacheName = resultCacheName.toLowerCase();
//		if(!resultCacheName.startsWith("v")){
//			return;
//		}
//		resultCacheName = resultCacheName.substring(1);
//		String[] idsStrings = resultCacheName.split("-");
//		int[] ids = new int[idsStrings.length];
//		for(int i = 0; i < idsStrings.length; ++i){
//			try{
//				ids[i] = Integer.parseInt(idsStrings[i]);
//			}catch(NumberFormatException e){
//				//ignore?!
//			}
//		}
//		Arrays.sort(ids);
//		StringBuilder builder = new StringBuilder();
//		builder.append('v');
//		for(int i = 0; i < ids.length - 1; ++i){
//			builder.append(ids[i]);
//			builder.append('-');
//		}
//		builder.append(ids[ids.length - 1]);
//		String newResultCacheName = builder.toString();
//		if(getIdByCacheName(newResultCacheName) > -1L){
//			return;
//		}
//		
//		long resultCacheId = insertNewSetToDB(newResultCacheName);
//		ShotLookup sl = new ShotLookup();
//		for(int videoId : ids){
//			List<ShotDescriptor> shots = sl.lookUpVideo(videoId);
//			long[] shotIds = new long[shots.size()];
//			int i = 0;
//			for(ShotDescriptor s : shots){
//				shotIds[i++] = s.getShotId();
//			}
//			addResultElementsToDB(resultCacheId, shotIds);
//		}
//		sl.close();
	}
	
}
