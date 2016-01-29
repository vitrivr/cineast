package ch.unibas.cs.dbis.cineast.core.db;

import java.sql.PreparedStatement;
import java.sql.SQLException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import ch.unibas.cs.dbis.cineast.core.util.LogHelper;

public class ReturningADAMTuple extends AbstractADAMTuple implements LongReturning{

	private static final Logger LOGGER = LogManager.getLogger();
	
	private String returning;
	private long returnValue = -1;
	
	public ReturningADAMTuple(ADAMWriter phandler, String[] names, int count, String returning) {
		super(phandler, names, count);
		this.returning = returning;
	}

	@Override
	public PreparedStatement getPersistentRepresentation() {
		try {
			String sql = makeSQL() + " RETURNING " + returning;
			LOGGER.debug(LogHelper.SQL_MARKER, sql);
			return ((ADAMWriter)phandler).connection.prepareStatement(sql);
		} catch (SQLException e) {
			LOGGER.fatal(LogHelper.SQL_MARKER, LogHelper.getStackTrace(e));
		}
		return null;
	}
	
	public long getReturnValue(){
		return this.returnValue;
	}

	void setResult(long returnValue) {
		this.returnValue = returnValue;
	}

}
