package org.vitrivr.cineast.core.features.extractor;

import org.vitrivr.cineast.core.features.abstracts.MetadataFeatureModule;

public interface ExtractorInitializer {

	void initialize(Extractor e);
	
	void initialize(MetadataFeatureModule<?> e);
	
}
