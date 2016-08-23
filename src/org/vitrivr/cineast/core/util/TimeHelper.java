package org.vitrivr.cineast.core.util;

import gnu.trove.stack.array.TDoubleArrayStack;

public class TimeHelper {

	private TimeHelper(){}
	
	private static TDoubleArrayStack tic = new TDoubleArrayStack();
	
	public static synchronized void tic(){
		tic.push(System.nanoTime() / 1000000d);
	}
	
	public static synchronized double toc(){
		double d = tic.pop();
		return (System.nanoTime() / 1000000d) - d;
	}
}
