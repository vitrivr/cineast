package org.vitrivr.cineast.core.db;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;
import org.vitrivr.cineast.core.db.setup.EntityCreator;

public interface PersistentOperator {

  /**
   * Initializes the underlying layers in which the data is stored. Commonly, the entities which are used are created
   */
  void initalizePersistentLayer(Supplier<EntityCreator> supply);

  /**
   * Drops all underlying layers in which data is stored.
   */
  void dropPersistentLayer(Supplier<EntityCreator> supply);

  /**
   * Returns the table/entity names which this {@link PersistentOperator} uses to store/access its data. Defaults to an empty list, in which case no table/entity is used.
   *
   * @return Tables which this {@link PersistentOperator} uses to store/access its data.
   */
  default List<String> getEntityNames() {
    return new ArrayList<>(0);
  }
}
