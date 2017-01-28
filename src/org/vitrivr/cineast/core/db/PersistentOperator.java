package org.vitrivr.cineast.core.db;

import java.util.function.Supplier;

import org.vitrivr.cineast.core.setup.EntityCreator;

public interface PersistentOperator {

	void initalizePersistentLayer(Supplier<EntityCreator> supply);

	void dropPersistentLayer(Supplier<EntityCreator> supply);
	
}
