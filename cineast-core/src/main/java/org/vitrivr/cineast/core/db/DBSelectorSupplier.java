package org.vitrivr.cineast.core.db;

import java.util.function.Supplier;

/**
 * This interface describes a {@link Supplier} for {@link DBSelector}s.
 *
 * <strong>Important:</strong> This class is required because of signature clashes in {@link org.vitrivr.cineast.core.features.abstracts.AbstractFeatureModule} due
 * to type erasure!
 */
public interface DBSelectorSupplier extends Supplier<DBSelector> {

}
