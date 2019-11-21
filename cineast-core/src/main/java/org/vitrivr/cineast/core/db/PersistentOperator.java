package org.vitrivr.cineast.core.db;

import java.util.ArrayList;
import java.util.List;
import org.vitrivr.cineast.core.db.setup.EntityCreator;

import java.util.function.Supplier;

public interface PersistentOperator {

	/**
	 *
	 * @param supply
	 */
	void initalizePersistentLayer(Supplier<EntityCreator> supply);

	/**
	 *
	 * @param supply
	 */
	void dropPersistentLayer(Supplier<EntityCreator> supply);

	/**
	 * Returns the table/entity names which this {@link PersistentOperator} uses to store/access its data.
	 * Defaults to an empty list, in which case no table/entity is used.
	 *
	 * @return Tables which this {@link PersistentOperator} uses to store/access its data.
	 */
	default List<String> getTableNames() {
		return new ArrayList<>(0);
	}
}
