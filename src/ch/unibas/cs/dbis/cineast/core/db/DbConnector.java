package ch.unibas.cs.dbis.cineast.core.db;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Properties;

/**
 * 
 * copied from ch.unibas.cs.dbis.jadam.connectors.database.DbConnector
 *
 */
@Deprecated
public class DbConnector/* implements Connector*/ {

	private final ArrayList<Statement> statements;

	/**
	 * 
	 */
	private final Connection connection;
	
	/**
	 * @throws SQLException 
	 * 
	 */
	public DbConnector(String database, String username, String password) throws SQLException {
		// get settings file
		Properties props = new Properties();
				
		// connect to db
		props.setProperty("user", username);
		props.setProperty("password", password);
		props.setProperty("tcpKeepAlive", "true");
		props.setProperty("socketTimeout", "600");

		String url = "jdbc:postgresql://" + database;
		connection = DriverManager.getConnection(url, props);
		
		statements = new ArrayList<Statement>();
	}
	
	/**
	 * 
	 * @param connection
	 */
	public DbConnector(Connection connection){
		this.connection = connection;
		statements = new ArrayList<Statement>();
	}

	/**
	 * 
	 * @param schema
	 * @param query
	 * @param updatable
	 * @return
	 * @throws SQLException 
	 */
	public ResultSet select(String query, boolean updatable) throws SQLException {
		int resultSetType = updatable ? ResultSet.TYPE_SCROLL_SENSITIVE : ResultSet.TYPE_FORWARD_ONLY;
		int resultSetConcurrency = updatable ? ResultSet.CONCUR_UPDATABLE : ResultSet.CONCUR_READ_ONLY;
			
		Statement st = connection.createStatement(resultSetType, resultSetConcurrency);
		
		return st.executeQuery(query);
	}
	
	/**
	 * 
	 * @param schema
	 * @param query
	 * @return
	 * @throws SQLException 
	 */
	public ResultSet select(String query) throws SQLException {
		return select(query, false);
	}

	/**
	 * 
	 * @param schema
	 * @param query
	 * @throws SQLException 
	 */
	public void exec(String schema, String query) throws SQLException {
		//log.info(query);
		
		Statement st = connection.createStatement();
		st.execute("SET search_path TO " + schema + ";");
		
		statements.add(st);
		
		st.executeUpdate(query);
		st.close();
		
		statements.remove(st);
	}
	
    /**
     * 
     * @param query 
     */
    public void execNow(String query) throws SQLException{
        Statement st = connection.createStatement();
        st.execute(query);        
    }

	/**
	 * @throws SQLException 
	 * 
	 */
	public PreparedStatement getPreparedStatement(String schema, String query, boolean returnKeys) throws SQLException {
		PreparedStatement pstmt;
		
		if (returnKeys) {
			pstmt = connection.prepareStatement(query, Statement.RETURN_GENERATED_KEYS);
		} else {
			pstmt = connection.prepareStatement(query);
		}
			
		return pstmt;
	}
	
	public void cancelStatements() throws SQLException{
		for(Statement st : statements){
			st.cancel();
		}
	}
	
	
	public void close() throws SQLException{
		if(connection != null){
			connection.close();
		}
	}

}
