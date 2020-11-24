package org.vitrivr.cineast.core.features.abstracts;

import static org.vitrivr.cineast.core.util.CineastConstants.GENERIC_ID_COLUMN_QUALIFIER;

import org.apache.commons.lang3.tuple.Triple;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.vitrivr.cineast.core.config.ReadableQueryConfig;
import org.vitrivr.cineast.core.data.providers.primitive.PrimitiveTypeProvider;
import org.vitrivr.cineast.core.data.providers.primitive.ProviderDataType;
import org.vitrivr.cineast.core.data.score.BooleanSegmentScoreElement;
import org.vitrivr.cineast.core.data.score.ScoreElement;
import org.vitrivr.cineast.core.data.segments.SegmentContainer;
import org.vitrivr.cineast.core.db.BooleanExpression;
import org.vitrivr.cineast.core.db.DBSelector;
import org.vitrivr.cineast.core.db.DBSelectorSupplier;
import org.vitrivr.cineast.core.db.RelationalOperator;
import org.vitrivr.cineast.core.db.setup.EntityCreator;
import org.vitrivr.cineast.core.features.retriever.MultipleInstantiatableRetriever;
import org.vitrivr.cineast.core.features.retriever.Retriever;

import java.util.*;
import java.util.function.Supplier;
import java.util.stream.Collectors;

public abstract class BooleanRetriever implements MultipleInstantiatableRetriever {

  private static final Logger LOGGER = LogManager.getLogger();
  protected DBSelector selector;
  protected final String entity;
  protected final HashSet<String> attributes = new HashSet<>();
  protected final HashMap<String, ProviderDataType> columnTypes = new HashMap<>();

  protected BooleanRetriever(String entity, Collection<String> attributes){
    this.entity = entity;
    this.attributes.addAll(attributes);
  }

  protected BooleanRetriever(Map<String, String> properties){
    if(!properties.containsKey("entity")){
      throw new RuntimeException("no entity specified in properties map of BooleanRetriever");
    }
    this.entity = properties.get("entity");

    if(properties.containsKey("attribute")){
      List<String> attrs = Arrays.stream(properties.get("attribute").split(",")).map(String::trim)
          .collect(
              Collectors.toList());
      this.attributes.addAll(attrs);
    }

  }

  @Override
  public List<String> getTableNames() {
    return Collections.singletonList(entity);
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

    List<BooleanExpression> relevantExpressions = sc.getBooleanExpressions().stream().filter(this::canProcess).collect(Collectors.toList());

    if (relevantExpressions.isEmpty()){
      LOGGER.debug("No relevant expressions in {} for query {}", this.getClass().getSimpleName(), sc.toString());
      return Collections.emptyList();
    }

    return getMatching(relevantExpressions, qc);
  }

  protected List<ScoreElement> getMatching(List<BooleanExpression> expressions, ReadableQueryConfig qc){

    List<Map<String, PrimitiveTypeProvider>> rows = selector.getRowsAND(
        expressions.stream().map(be -> Triple.of(
            // strip entity if it was given via config
            be.getAttribute().contains(this.entity) ? be.getAttribute().substring(this.entity.length()+1) : be.getAttribute(),
            be.getOperator(),
            be.getValues()
        )).collect(Collectors.toList()),
        GENERIC_ID_COLUMN_QUALIFIER, // for compound ops, we want to join via id. Cottontail (the official storage layer) does not use this identifier
        Collections.singletonList(GENERIC_ID_COLUMN_QUALIFIER),  // we're only interested in the ids
        qc);
    // we're returning a boolean score element since the score is always 1 if a query matches here
    return rows.stream().map(row -> new BooleanSegmentScoreElement(row.get(GENERIC_ID_COLUMN_QUALIFIER).getString())).collect(Collectors.toList());
  }

  @Override
  public List<ScoreElement> getSimilar(String segmentId, ReadableQueryConfig qc) { //nop
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

  public ProviderDataType getColumnType(String column){
    return this.columnTypes.get(column);
  }


  @Override
  public int hashCode() {
    return Objects.hash(this.getClass().getName(), entity, attributes);
  }

  @Override
  public boolean equals(Object obj) {
    if(obj == null){
      return false;
    }
    if(!(obj instanceof BooleanRetriever)){
      return false;
    }
    return this.hashCode() == obj.hashCode();
  }
}
