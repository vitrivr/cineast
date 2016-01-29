package ch.unibas.cs.dbis.cineast.core.db;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Properties;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import ch.unibas.cs.dbis.cineast.core.config.Config;
import ch.unibas.cs.dbis.cineast.core.util.LogHelper;

public class DBSelector {

	private static final Logger LOGGER = LogManager.getLogger();
	
	Connection connection;
	
	public DBSelector(){
		this(Config.getDBLocation(), Config.getDBUser(), Config.getDBPassword());
	}
	
	public DBSelector(String database, String username, String password){
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
	
	public ResultSet select(String query){
		try {
			PreparedStatement statement = connection.prepareStatement(query);
			LOGGER.debug(LogHelper.SQL_MARKER, query);
			return statement.executeQuery();
		} catch (SQLException e) {
			LOGGER.warn(LogHelper.SQL_MARKER, LogHelper.getStackTrace(e));
		} catch (Exception e){
			LOGGER.error(LogHelper.getStackTrace(e));
		}
		return null;
	}
	
	public void close(){
		try {
			this.connection.close();
		} catch (SQLException e) {
			LOGGER.warn(LogHelper.SQL_MARKER, LogHelper.getStackTrace(e));
		} catch (Exception e){
			LOGGER.error(LogHelper.getStackTrace(e));
		}
	}
	
}
