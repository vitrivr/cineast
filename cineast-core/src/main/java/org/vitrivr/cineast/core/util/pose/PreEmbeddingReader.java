package org.vitrivr.cineast.core.util.pose;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Path;
import java.util.Iterator;
import org.apache.commons.lang3.tuple.ImmutablePair;
import ucar.nc2.NetcdfFile;
import ucar.nc2.NetcdfFiles;

public class PreEmbeddingReader implements PreEmbeddingReaderInterface {
  final private NetcdfFile hdfFile;

  public PreEmbeddingReader(Path path) {
    try {
      System.out.println(path.toString());
      this.hdfFile = NetcdfFiles.open(path.toString());
    } catch (IOException e) {
      throw new UncheckedIOException(e);
    }
  }

  @Override
  public Iterator<ImmutablePair<Integer, float[][]>> frameIterator() {
    return this.hdfFile.getRootGroup().getVariables().stream().map(
        var -> {
          try {
            return new ImmutablePair<>(Integer.getInteger(var.getShortName()),
                (float[][]) var.read().copyToNDJavaArray());
          } catch (IOException exc) {
            throw new UncheckedIOException(exc);
          }
        }
    ).iterator();
  }

  @Override
  public void close() {
    try {
      this.hdfFile.close();
    } catch (IOException e) {
      throw new UncheckedIOException(e);
    }
  }
}
