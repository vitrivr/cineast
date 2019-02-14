package org.vitrivr.cineast.core.db.cottontaildb;

import ch.unibas.dmi.dbis.cottontail.grpc.CottontailGrpc.InsertMessage;
import java.util.List;
import org.vitrivr.cineast.core.db.AbstractPersistencyWriter;
import org.vitrivr.cineast.core.db.PersistentTuple;

public class CottontailWriter extends AbstractPersistencyWriter<InsertMessage> {

  @Override
  public boolean open(String name) {
    return false;
  }

  @Override
  public boolean close() {
    return false;
  }

  @Override
  public boolean idExists(String id) {
    return false;
  }

  @Override
  public boolean exists(String key, String value) {
    return false;
  }

  @Override
  public boolean persist(PersistentTuple tuple) {
    return false;
  }

  @Override
  public boolean persist(List<PersistentTuple> tuples) {
    return false;
  }

  @Override
  public InsertMessage getPersistentRepresentation(PersistentTuple tuple) {
    return null;
  }
}
