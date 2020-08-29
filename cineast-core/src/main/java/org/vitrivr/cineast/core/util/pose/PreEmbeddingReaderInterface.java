package org.vitrivr.cineast.core.util.pose;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.Iterator;
import org.apache.commons.lang3.tuple.ImmutablePair;

public interface PreEmbeddingReaderInterface extends AutoCloseable {
  Iterator<ImmutablePair<Integer, float[][]>> frameIterator();
}
