package org.vitrivr.cineast.core.db;

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
}
