package org.vitrivr.cineast.core.features.example;

import java.util.List;
import java.util.function.Supplier;
import org.apache.commons.lang3.RandomUtils;
import org.vitrivr.cineast.core.config.ReadableQueryConfig;
import org.vitrivr.cineast.core.data.providers.primitive.PrimitiveTypeProvider;
import org.vitrivr.cineast.core.data.score.ScoreElement;
import org.vitrivr.cineast.core.data.segments.SegmentContainer;
import org.vitrivr.cineast.core.db.setup.AttributeDefinition;
import org.vitrivr.cineast.core.db.setup.AttributeDefinition.AttributeType;
import org.vitrivr.cineast.core.db.setup.EntityCreator;
import org.vitrivr.cineast.core.features.abstracts.AbstractFeatureModule;

public class PrimitiveFeatureExample extends AbstractFeatureModule {

  public static final String PRIMITIVE_FEATURE_EXAMPLE_TABLE_NAME = "features_primitiveexample";

  public PrimitiveFeatureExample() {
    super(PRIMITIVE_FEATURE_EXAMPLE_TABLE_NAME, 5, 1);
  }

  @Override
  public void processSegment(SegmentContainer shot) {
    this.persist(shot.getId(), PrimitiveTypeProvider.fromObject(RandomUtils.nextFloat(0, 5)));
  }

  @Override
  public List<ScoreElement> getSimilar(SegmentContainer sc, ReadableQueryConfig qc) {
    // not supported by default. Implement your own using this.selector
    throw new UnsupportedOperationException();
  }

  @Override
  public void initalizePersistentLayer(Supplier<EntityCreator> supply) {
    supply.get().createFeatureEntity(PRIMITIVE_FEATURE_EXAMPLE_TABLE_NAME, true, new AttributeDefinition("feature", AttributeType.FLOAT));
  }
}
