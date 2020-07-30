package org.vitrivr.cineast.core.features.multi;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.function.Supplier;
import org.vitrivr.cineast.core.db.PersistentOperator;
import org.vitrivr.cineast.core.db.setup.EntityCreator;

interface MultiPersistentOperator extends PersistentOperator {
  Iterator<? extends PersistentOperator> getSubOperators();

  @Override
  default void initalizePersistentLayer(Supplier<EntityCreator> supply) {
    for (Iterator<? extends PersistentOperator> it = this.getSubOperators(); it.hasNext(); ) {
      PersistentOperator operator = it.next();
      operator.initalizePersistentLayer(supply);
    }
  }

  @Override
  default void dropPersistentLayer(Supplier<EntityCreator> supply) {
    for (Iterator<? extends PersistentOperator> it = this.getSubOperators(); it.hasNext(); ) {
      PersistentOperator operator = it.next();
      operator.dropPersistentLayer(supply);
    }
  }

  @Override
  default List<String> getTableNames() {
    ArrayList<String> result = new ArrayList<>();
    for (Iterator<? extends PersistentOperator> it = this.getSubOperators(); it.hasNext(); ) {
      PersistentOperator operator = it.next();
      result.addAll(operator.getTableNames());
    }
    return result;
  }
}
