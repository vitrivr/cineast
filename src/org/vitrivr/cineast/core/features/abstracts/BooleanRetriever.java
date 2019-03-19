package org.vitrivr.cineast.core.features.abstracts;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import org.vitrivr.cineast.core.config.ReadableQueryConfig;
import org.vitrivr.cineast.core.data.score.ScoreElement;
import org.vitrivr.cineast.core.data.segments.SegmentContainer;
import org.vitrivr.cineast.core.db.BooleanExpression;
import org.vitrivr.cineast.core.db.DBSelector;
import org.vitrivr.cineast.core.db.DBSelectorSupplier;
import org.vitrivr.cineast.core.db.RelationalOperator;
import org.vitrivr.cineast.core.features.retriever.Retriever;
import org.vitrivr.cineast.core.setup.EntityCreator;

public abstract class BooleanRetriever implements Retriever {

  protected DBSelector selector;
  protected final String entity;
  protected final HashSet<String> attributes = new HashSet<>();

  protected BooleanRetriever(String entity, Collection<String> attributes){
    this.entity = entity;
    this.attributes.addAll(attributes);
  }

  protected abstract Collection<RelationalOperator> getSupportedOperators();

  @Override
  public void init(DBSelectorSupplier selectorSupply) {
    this.selector = selectorSupply.get();
    this.selector.open(entity);
  }

  public Collection<String> getAttributes(){
    return this.attributes.stream().map(x -> this.entity + "." + x).collect(Collectors.toSet());
  }

  protected boolean canProcess(BooleanExpression be){
    return getSupportedOperators().contains(be.getOperator()) && getAttributes().contains(be.getAttribute());
  }

  @Override
  public List<ScoreElement> getSimilar(SegmentContainer sc, ReadableQueryConfig qc) {

    List<BooleanExpression> relevantExpressions = sc.getBooleanExpressions().stream().filter(this::canProcess).collect(
        Collectors.toList());

    if (relevantExpressions.isEmpty()){
      return Collections.emptyList();
    }

    return getMatching(relevantExpressions, qc);
  }

  protected abstract List<ScoreElement> getMatching(List<BooleanExpression> expressions, ReadableQueryConfig qc);

  @Override
  public List<ScoreElement> getSimilar(String shotId, ReadableQueryConfig qc) {
    return Collections.emptyList();
  }

  @Override
  public void finish() {
    this.selector.close();
  }

  @Override
  public void initalizePersistentLayer(Supplier<EntityCreator> supply) {
    //nop
  }

  @Override
  public void dropPersistentLayer(Supplier<EntityCreator> supply) {
    //nop
  }
}
