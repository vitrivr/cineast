package org.vitrivr.cineast.core.run.path;

import java.nio.file.Path;
import java.util.Iterator;

public interface PathIteratorProvider {

    Iterator<Path> getPaths();

}
