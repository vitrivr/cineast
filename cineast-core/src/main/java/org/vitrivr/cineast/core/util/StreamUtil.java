package org.vitrivr.cineast.core.util;

import java.util.Iterator;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

public class StreamUtil {
	public static <T> Stream<T> streamOfIterator(Iterator<T> iterator) {
		return StreamSupport.stream(
				((Iterable<T>) () -> iterator).spliterator(),
				false
		);
	}
}
