package org.vitrivr.cineast.core.features;

import org.vitrivr.cineast.core.config.ReadableQueryConfig;
import org.vitrivr.cineast.core.data.score.ScoreElement;
import org.vitrivr.cineast.core.data.segments.SegmentContainer;
import org.vitrivr.cineast.core.features.abstracts.AbstractFeatureModule;

import java.util.Collections;
import java.util.List;


public class ObjectInstances extends AbstractFeatureModule {

    protected ObjectInstances() {
        super("features_ObjectInstances",
                100f, //TODO figure out proper value
                1280);
    }

    @Override
    public void processSegment(SegmentContainer shot) {
        //TODO implement extraction
    }

    @Override
    public List<ScoreElement> getSimilar(SegmentContainer sc, ReadableQueryConfig qc) {
        //TODO implement extraction
        return Collections.emptyList();
    }

}
