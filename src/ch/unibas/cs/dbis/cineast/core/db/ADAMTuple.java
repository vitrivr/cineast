package ch.unibas.cs.dbis.cineast.core.db;

import java.sql.PreparedStatement;
import java.sql.SQLException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import ch.unibas.cs.dbis.cineast.core.util.LogHelper;

public class ADAMTuple extends AbstractADAMTuple{

	private static final Logger LOGGER = LogManager.getLogger();

	public ADAMTuple(ADAMWriter phandler, String[] names, int count) {
		super(phandler, names, count);
	}

	@Override
	public PreparedStatement getPersistentRepresentation() {
		try {
			String sql = makeSQL();
			LOGGER.debug(LogHelper.SQL_MARKER, sql);
			return ((ADAMWriter)phandler).connection.prepareStatement(sql);
		} catch (SQLException e) {
			LOGGER.fatal(LogHelper.SQL_MARKER, LogHelper.getStackTrace(e));
		}
		return null;
	}
	
}