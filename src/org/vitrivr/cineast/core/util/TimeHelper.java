package org.vitrivr.cineast.core.util;

import gnu.trove.stack.array.TDoubleArrayStack;
import java.util.function.Function;
import java.util.function.Supplier;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class TimeHelper {

	private TimeHelper(){}
	
	private static TDoubleArrayStack tic = new TDoubleArrayStack();

	private static final Logger LOGGER = LogManager.getLogger();

	public static synchronized void tic(){
		tic.push(System.nanoTime() / 1000000d);
	}
	
	public static synchronized double toc(){
		double d = tic.pop();
		return (System.nanoTime() / 1000000d) - d;
	}

	public static <O> O timeCall(Supplier<O> fun, String name,
			Level level){
		long start = System.currentTimeMillis();
		O res = fun.get();
		long stop = System.currentTimeMillis();
		LOGGER.log(level, "Finished {} in {} ms", name, stop-start);
		return res;
	}
}
