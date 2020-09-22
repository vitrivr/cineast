package org.vitrivr.cineast.core.util;

import org.apache.logging.log4j.Marker;
import org.apache.logging.log4j.MarkerManager;

import java.io.PrintWriter;
import java.io.StringWriter;

public class LogHelper {

	private LogHelper() {
	}

	public static final Marker SQL_MARKER = MarkerManager.getMarker("SQL");

	public static String getStackTrace(Throwable e){
	    StringWriter sWriter = new StringWriter();
	    PrintWriter pWriter = new PrintWriter(sWriter);
	    e.printStackTrace(pWriter);
	    return sWriter.toString();
	}
}
