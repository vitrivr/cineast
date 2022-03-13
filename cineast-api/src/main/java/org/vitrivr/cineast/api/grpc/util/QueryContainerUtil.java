package org.vitrivr.cineast.api.grpc.util;

import static org.vitrivr.cineast.core.config.ReadableQueryConfig.Hints;

import georegression.struct.point.Point2D_F32;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;
import javax.imageio.ImageIO;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.vitrivr.cineast.api.grpc.CineastGrpc;
import org.vitrivr.cineast.api.grpc.data.QueryStage;
import org.vitrivr.cineast.api.grpc.data.QueryTerm;
import org.vitrivr.cineast.core.config.QueryConfig;
import org.vitrivr.cineast.core.config.ReadableQueryConfig;
import org.vitrivr.cineast.core.data.Location;
import org.vitrivr.cineast.core.data.SemanticMap;
import org.vitrivr.cineast.core.data.StringDoublePair;
import org.vitrivr.cineast.core.data.entities.MediaObjectMetadataDescriptor;
import org.vitrivr.cineast.core.data.entities.MediaSegmentMetadataDescriptor;
import org.vitrivr.cineast.core.data.frames.AudioDescriptor;
import org.vitrivr.cineast.core.data.frames.AudioFrame;
import org.vitrivr.cineast.core.data.providers.primitive.BooleanTypeProvider;
import org.vitrivr.cineast.core.data.providers.primitive.DoubleTypeProvider;
import org.vitrivr.cineast.core.data.providers.primitive.FloatTypeProvider;
import org.vitrivr.cineast.core.data.providers.primitive.IntTypeProvider;
import org.vitrivr.cineast.core.data.providers.primitive.LongTypeProvider;
import org.vitrivr.cineast.core.data.providers.primitive.NothingProvider;
import org.vitrivr.cineast.core.data.providers.primitive.PrimitiveTypeProvider;
import org.vitrivr.cineast.core.data.providers.primitive.StringTypeProvider;
import org.vitrivr.cineast.core.data.query.containers.AbstractQueryTermContainer;
import org.vitrivr.cineast.core.data.query.containers.AudioQueryTermContainer;
import org.vitrivr.cineast.core.data.query.containers.BooleanQueryTermContainer;
import org.vitrivr.cineast.core.data.query.containers.IdQueryTermContainer;
import org.vitrivr.cineast.core.data.query.containers.ImageQueryTermContainer;
import org.vitrivr.cineast.core.data.query.containers.InstantQueryTermContainer;
import org.vitrivr.cineast.core.data.query.containers.LocationQueryTermContainer;
import org.vitrivr.cineast.core.data.query.containers.ModelQueryTermContainer;
import org.vitrivr.cineast.core.data.query.containers.SemanticMapQueryTermContainer;
import org.vitrivr.cineast.core.data.query.containers.TagQueryTermContainer;
import org.vitrivr.cineast.core.data.query.containers.TextQueryTermContainer;
import org.vitrivr.cineast.core.db.BooleanExpression;
import org.vitrivr.cineast.core.db.RelationalOperator;
import org.vitrivr.cineast.core.util.LogHelper;

public class QueryContainerUtil {

  private static final Logger LOGGER = LogManager.getLogger();

  public static AudioQueryTermContainer audioQueryContainer(CineastGrpc.AudioQueryContainer container) {
    return new AudioQueryTermContainer(container.getAudioFramesList().stream().map(QueryContainerUtil::audioFrame).collect(Collectors.toList()));
  }

  public static AudioFrame audioFrame(CineastGrpc.AudioFrame frame) {
    AudioDescriptor descriptor = new AudioDescriptor(frame.getSamplingRate(), frame.getChannels(), frame.getDuration());
    return new AudioFrame(frame.getIdx(), frame.getTimestamp(), frame.getData().toByteArray(), descriptor);
  }

  public static BooleanQueryTermContainer booleanQueryContainer(CineastGrpc.BooleanQueryContainer container) {

    return new BooleanQueryTermContainer(
        container.getExpressionsList().stream().map(QueryContainerUtil::booleanExpression).collect(Collectors.toList())
    );

  }

  public static BooleanExpression booleanExpression(CineastGrpc.BooleanExpression expression) {
    return new BooleanExpression(
        expression.getAttribute(),
        relationalOperator(expression.getOperator()),
        expression.getValuesList().stream().map(QueryContainerUtil::primitiveTypeProvider).collect(Collectors.toList())
    );
  }

  public static RelationalOperator relationalOperator(CineastGrpc.BooleanExpression.Operator operator) {
    switch (operator) {
      case EQ:
        return RelationalOperator.EQ;
      case NEQ:
        return RelationalOperator.NEQ;
      case GEQ:
        return RelationalOperator.GEQ;
      case LEQ:
        return RelationalOperator.LEQ;
      case GREATER:
        return RelationalOperator.GREATER;
      case LESS:
        return RelationalOperator.LESS;
      case BETWEEN:
        return RelationalOperator.BETWEEN;
      case LIKE:
        return RelationalOperator.LIKE;
      case NLIKE:
        return RelationalOperator.NLIKE;
      case ISNULL:
        return RelationalOperator.ISNULL;
      case ISNOTNULL:
        return RelationalOperator.ISNOTNULL;
      case IN:
        return RelationalOperator.IN;
      case UNRECOGNIZED:
        return null;
      /* TODO: Support MATCH operator. */
    }

    return null;
  }

  public static PrimitiveTypeProvider primitiveTypeProvider(CineastGrpc.PrimitiveType type) {
    switch (type.getValueCase()) {
      case STRINGVALUE:
        return new StringTypeProvider(type.getStringValue());
      case INTVALUE:
        return new IntTypeProvider(type.getIntValue());
      case LONGVALUE:
        return new LongTypeProvider(type.getLongValue());
      case FLOATVALUE:
        return new FloatTypeProvider(type.getFloatValue());
      case DOUBLEVALUE:
        return new DoubleTypeProvider(type.getDoubleValue());
      case BOOLEANVALUE:
        return new BooleanTypeProvider(type.getBooleanValue());
      case VALUE_NOT_SET:
        return new NothingProvider();
    }
    return new NothingProvider();
  }

  public static IdQueryTermContainer idQueryContainer(CineastGrpc.IdQueryContainer container) {
    return new IdQueryTermContainer(container.getSegmentId().getId());
  }

  public static ImageQueryTermContainer imageQueryContainer(CineastGrpc.ImageQueryContainer container) {
    try {
      return new ImageQueryTermContainer(
          ImageIO.read(new ByteArrayInputStream(container.getImage().toByteArray()))
      );
    } catch (IOException e) {
      LOGGER.error("Error in reading image from ImageQueryContainer: {}", LogHelper.getStackTrace(e));
    }
    return null;
  }

  public static InstantQueryTermContainer instantQueryContainer(CineastGrpc.InstantQueryContainer container) {
    return new InstantQueryTermContainer(container.getInstant());
  }

  public static LocationQueryTermContainer locationQueryContainer(CineastGrpc.LocationQueryContainer container) {
    return new LocationQueryTermContainer(Location.of(container.getLongitude(), container.getLatitude()));
  }

  public static ModelQueryTermContainer modelQueryContainer(CineastGrpc.ModelQueryContainer container) {
    //TODO figure out better mesh representation
    return null;
  }

  public static LinkedList<Point2D_F32> motionPath(CineastGrpc.MotionQueryContainer.MotionPath path) {
    LinkedList<Point2D_F32> list = new LinkedList<>();
    list.addAll(path.getPathList().stream().map(QueryContainerUtil::point).collect(Collectors.toList()));
    return list;
  }

  public static Point2D_F32 point(CineastGrpc.MotionQueryContainer.MotionPath.Point point) {
    return new Point2D_F32(point.getX(), point.getY());
  }

  public static SemanticMapQueryTermContainer semanticMapQueryContainer(CineastGrpc.SemanticMapQueryContainer container) {
    try {
      return new SemanticMapQueryTermContainer(new SemanticMap(
          ImageIO.read(new ByteArrayInputStream(container.getImage().toByteArray())),
          container.getConceptsMap())
      );
    } catch (IOException e) {
      LOGGER.error("Error in reading image from ImageQueryContainer: {}", LogHelper.getStackTrace(e));
    }
    return null;
  }

  public static TagQueryTermContainer tagQueryContainer(CineastGrpc.TagQueryContainer container) {
    return null; //TODO do we even still need that one?
  }

  public static TextQueryTermContainer textQueryContainer(CineastGrpc.TextQueryContainer container) {
    return new TextQueryTermContainer(container.getText());
  }


  public static AbstractQueryTermContainer queryTermContainer(CineastGrpc.QueryTerm term) {
    switch (term.getContainerCase()) {
      case AUDIOQUERYCONTAINER:
        return audioQueryContainer(term.getAudioQueryContainer());
      case BOOLEANQUERYCONTAINER:
        return booleanQueryContainer(term.getBooleanQueryContainer());
      case IDQUERYCONTAINER:
        return idQueryContainer(term.getIdQueryContainer());
      case IMAGEQUERYCONTAINER:
        return imageQueryContainer(term.getImageQueryContainer());
      case INSTANTQUERYCONTAINER:
        return instantQueryContainer(term.getInstantQueryContainer());
      case LOCATIONQUERYCONTAINER:
        return locationQueryContainer(term.getLocationQueryContainer());
      case MODELQUERYCONTAINER:
        return modelQueryContainer(term.getModelQueryContainer());
      case SEMANTICMAPQUERYCONTAINER:
        return semanticMapQueryContainer(term.getSemanticMapQueryContainer());
      case TAGQUERYCONTAINER:
        return tagQueryContainer(term.getTagQueryContainer());
      case TEXTQUERYCONTAINER:
        return textQueryContainer(term.getTextQueryContainer());
      case CONTAINER_NOT_SET:
        return null;
    }
    return null;
  }

  public static ReadableQueryConfig queryConfig(CineastGrpc.QueryConfig queryConfig) {
    List<Hints> hints = new ArrayList<Hints>(queryConfig.getHintsCount());
    for (String hint : queryConfig.getHintsList()) {
      if (hint == null) {
        continue;
      }

      Hints h = null;

      try {
        h = Hints.valueOf(hint);
      } catch (IllegalArgumentException e) {
        //ignore
      }
      if (h != null) {
        hints.add(h);
      }
    }

    QueryConfig config = new QueryConfig(queryConfig.getQueryId().getId(), hints);
    config.setMaxResults(queryConfig.getMaxResults());
    return config;
  }

  public static QueryTerm queryTerm(CineastGrpc.QueryTerm term) {
    return new QueryTerm(
        queryTermContainer(term),
        queryConfig(term.getConfig()),
        term.getWeight(),
        term.getCategoryList()
    );
  }

  public static QueryStage queryStage(CineastGrpc.QueryStage queryStage) {
    return new QueryStage(
        queryStage.getTermsList().stream().map(QueryContainerUtil::queryTerm).collect(Collectors.toList()),
        queryConfig(queryStage.getConfig())
    );
  }

  public static List<QueryStage> query(CineastGrpc.Query query) {
    return query.getStagesList().stream().map(QueryContainerUtil::queryStage).collect(Collectors.toList());
  }

  public static CineastGrpc.MediaSegmentId mediaSegmentId(String id) {
    return CineastGrpc.MediaSegmentId.newBuilder().setId(id).build();
  }

  public static CineastGrpc.MediaSegmentIdScore mediaSegmentIdScore(StringDoublePair pair) {
    return CineastGrpc.MediaSegmentIdScore.newBuilder().setId(mediaSegmentId(pair.key)).setScore(pair.value).build();
  }

  public static CineastGrpc.QueryId queryId(String id) {
    return CineastGrpc.QueryId.newBuilder().setId(id).build();
  }

  public static CineastGrpc.SimilarityQueryResult similarityQueryResult(String queryId, String category, List<StringDoublePair> pairs) {
    return CineastGrpc.SimilarityQueryResult.newBuilder().setQueryId(queryId(queryId)).setCategory(category).addAllResults(pairs.stream().map(QueryContainerUtil::mediaSegmentIdScore).collect(Collectors.toList())).build();
  }

  public static CineastGrpc.QueryResult queryResult(CineastGrpc.SimilarityQueryResult result) {
    return CineastGrpc.QueryResult.newBuilder().setSimilarityQueryResult(result).build();
  }

  public static CineastGrpc.QueryResult queryResult(CineastGrpc.MediaSegmentQueryResult result) {
    return CineastGrpc.QueryResult.newBuilder().setMediaSegmentQueryResult(result).build();
  }

  public static CineastGrpc.QueryResult queryResult(CineastGrpc.MediaObjectQueryResult result) {
    return CineastGrpc.QueryResult.newBuilder().setMediaObjectQueryResult(result).build();
  }

  public static CineastGrpc.QueryResult queryResult(CineastGrpc.MediaSegmentMetaDataQueryResult result) {
    return CineastGrpc.QueryResult.newBuilder().setMediaSegmentMetaDataQueryResult(result).build();
  }

  public static CineastGrpc.QueryResult queryResult(CineastGrpc.MediaObjectMetaDataQueryResult result) {
    return CineastGrpc.QueryResult.newBuilder().setMediaObjectMetaDataQueryResult(result).build();
  }

  public static CineastGrpc.MediaObjectMetaData mediaObjectMetaData(MediaObjectMetadataDescriptor descriptor) {
    return CineastGrpc.MediaObjectMetaData.newBuilder().setDomain(descriptor.getDomain()).setKey(descriptor.getKey()).setValue(descriptor.getValue()).build();
  }

  public static CineastGrpc.MediaSegmentMetaData mediaSegmentMetaData(MediaSegmentMetadataDescriptor descriptor) {
    return CineastGrpc.MediaSegmentMetaData.newBuilder().setDomain(descriptor.getDomain()).setKey(descriptor.getKey()).setValue(descriptor.getValue()).build();
  }

}
