package org.vitrivr.cineast.core.db.memory;

import static org.vitrivr.cineast.core.util.CineastConstants.GENERIC_ID_COLUMN_QUALIFIER;

import java.util.List;
import java.util.Optional;
import org.vitrivr.cineast.core.db.AbstractPersistencyWriter;
import org.vitrivr.cineast.core.db.PersistentTuple;
import org.vitrivr.cineast.core.db.memory.InMemoryStore.Entity;


/**
 * Implementation of a Cineast {@link org.vitrivr.cineast.core.db.PersistencyWriter} on top of the {@link InMemoryStore}.
 *
 * @see InMemoryStore
 *
 * @author Ralph Gasser
 * @version 1.0
 */
public class InMemoryWriter extends AbstractPersistencyWriter<PersistentTuple> {


  private InMemoryStore store = InMemoryStore.sharedInMemoryStore();

  private Entity entity = null;

  @Override
  public boolean open(String name) {
    final Optional<Entity> entity = this.store.getEntity(name);
    if (entity.isPresent()) {
      this.entity = entity.get();
      return true;
    } else {
      return false;
    }
  }

  @Override
  public boolean close() {
    this.entity = null;
    return true;
  }

  @Override
  public boolean exists(String key, String value) {
    if (key.equals(GENERIC_ID_COLUMN_QUALIFIER)) {
      return this.entity.has(value);
    } else {
      return false;
    }
  }

  @Override
  public boolean persist(List<PersistentTuple> tuples) {
    for (PersistentTuple tuple : tuples) {
      this.entity.put(tuple.getElements().get(0).toString(), tuple);
    }
    return true;
  }

  @Override
  public PersistentTuple getPersistentRepresentation(PersistentTuple tuple) {
    return tuple;
  }
}
