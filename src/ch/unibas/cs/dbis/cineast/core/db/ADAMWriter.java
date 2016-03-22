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

public abstract class ADAMWriter implements PersistencyWriter<AbstractADAMTuple> {

	private static final Logger LOGGER = LogManager.getLogger();

	Connection connection;
	String name;
	String returning;
	
	public ADAMWriter(){
		this(Config.getDatabaseConfig().getLocation(),
				Config.getDatabaseConfig().getUser(),
				Config.getDatabaseConfig().getPassword());
	}
	
	public ADAMWriter(String database, String username, String password){
		this(database, username, password, null);
	}
	
	public ADAMWriter(String database, String username, String password, String returning){
		Properties props = new Properties();

		props.setProperty("user", username);
		props.setProperty("password", password);
		props.setProperty("tcpKeepAlive", "true");
		props.setProperty("socketTimeout", "60");

		String url = "jdbc:postgresql://" + database;
		try {
			connection = DriverManager.getConnection(url, props);
		} catch (SQLException e) {
			LOGGER.fatal(LogHelper.SQL_MARKER, LogHelper.getStackTrace(e));
		}
		
		this.returning = returning;
	}
	
	@Override
	public boolean open(String name) {
		this.name = name;
		return true;
	}


	@Override
	public AbstractADAMTuple makeTuple(Object... objects) {
		AbstractADAMTuple tuple;
		if(this.returning == null){
			tuple = new ADAMTuple(this, getParameterNames(), getParameterCount());
		}else{
			tuple = new ReturningADAMTuple(this, getParameterNames(), getParameterCount(), this.returning);
		}
		
		for(Object o : objects){
			tuple.addElement(o);
		}
		return tuple;
	}

	@Override
	public void write(AbstractADAMTuple tuple) {
		try {
			if(tuple instanceof ReturningADAMTuple){
				ResultSet result = tuple.getPersistentRepresentation().executeQuery();
				if(result.next()){
					((ReturningADAMTuple)tuple).setResult(result.getLong(1));
				}
			}else{
				tuple.getPersistentRepresentation().execute();
			}			
		} catch (SQLException e) {
			LOGGER.fatal(LogHelper.SQL_MARKER, LogHelper.getStackTrace(e));
		}
	}

	@Override
	public boolean close() {
		try {
			connection.close();
			return true;
		} catch (SQLException e) {
			
			LOGGER.warn(LogHelper.SQL_MARKER, LogHelper.getStackTrace(e));
			return false;
		}
	}
	
	@Override
	public boolean check(String condition) {
		try {
			PreparedStatement statement = this.connection.prepareStatement(condition);
			LOGGER.debug(LogHelper.SQL_MARKER, condition);
			ResultSet set = statement.executeQuery();
			return set.next();
		} catch (SQLException e) {
			LOGGER.warn(LogHelper.SQL_MARKER, LogHelper.getStackTrace(e));
		}
		return false;
	}
	
	public abstract int getParameterCount();
	public abstract String[] getParameterNames();

}

