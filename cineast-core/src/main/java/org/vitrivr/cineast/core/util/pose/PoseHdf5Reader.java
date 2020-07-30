package org.vitrivr.cineast.core.util.pose;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Path;
import ucar.nc2.Group;
import ucar.nc2.NetcdfFile;
import ucar.nc2.NetcdfFiles;

public class PoseHdf5Reader implements AutoCloseable {
  final private NetcdfFile hdfFile;

  public PoseHdf5Reader(Path path) {
    try {
      this.hdfFile = NetcdfFiles.open(path.toString());
    } catch (IOException e) {
      throw new UncheckedIOException(e);
    }
  }

  public FrameIterator unsegFrameIterator() {
    Group group = this.hdfFile.findGroup("timeline");
    return new FrameIterator(group);
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
