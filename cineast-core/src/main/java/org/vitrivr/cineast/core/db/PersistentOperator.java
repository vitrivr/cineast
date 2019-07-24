package org.vitrivr.cineast.core.db;

import java.util.function.Supplier;

import org.vitrivr.cineast.core.db.setup.EntityCreator;

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
