package org.vitrivr.cineast.api.grpc.util;

import georegression.struct.point.Point2D_F32;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.vitrivr.cineast.api.grpc.CineastGrpc;
import org.vitrivr.cineast.core.data.Location;
import org.vitrivr.cineast.core.data.SemanticMap;
import org.vitrivr.cineast.core.data.frames.AudioDescriptor;
import org.vitrivr.cineast.core.data.frames.AudioFrame;
import org.vitrivr.cineast.core.data.providers.primitive.*;
import org.vitrivr.cineast.core.data.query.containers.*;
import org.vitrivr.cineast.core.db.BooleanExpression;
import org.vitrivr.cineast.core.db.RelationalOperator;
import org.vitrivr.cineast.core.util.LogHelper;

import javax.imageio.ImageIO;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.LinkedList;
import java.util.stream.Collectors;

public class QueryContainerUtil {

    private static final Logger LOGGER = LogManager.getLogger();

    public static AudioQueryContainer audioQueryContainer(CineastGrpc.AudioQueryContainer container) {
        return new AudioQueryContainer(container.getAudioFramesList().stream().map(QueryContainerUtil::audioFrame).collect(Collectors.toList()));
    }

    public static AudioFrame audioFrame(CineastGrpc.AudioFrame frame) {
        AudioDescriptor descriptor = new AudioDescriptor(frame.getSamplingRate(), frame.getChannels(), frame.getDuration());
        return new AudioFrame(frame.getIdx(), frame.getTimestamp(), frame.getData().toByteArray(), descriptor);
    }

    public static BooleanQueryContainer booleanQueryContainer(CineastGrpc.BooleanQueryContainer container) {

        return new BooleanQueryContainer(
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
        switch (operator){

            case EQ: return RelationalOperator.EQ;
            case NEQ: return RelationalOperator.NEQ;
            case GEQ: return RelationalOperator.GEQ;
            case LEQ: return RelationalOperator.LEQ;
            case GREATER: return RelationalOperator.GREATER;
            case LESS: return RelationalOperator.LESS;
            case BETWEEN: return RelationalOperator.BETWEEN;
            case LIKE: return RelationalOperator.LIKE;
            case ILIKE: return RelationalOperator.ILIKE;
            case NLIKE: return RelationalOperator.NLIKE;
            case RLIKE: return RelationalOperator.RLIKE;
            case ISNULL: return RelationalOperator.ISNULL;
            case ISNOTNULL: return RelationalOperator.ISNOTNULL;
            case IN: return RelationalOperator.IN;
            case UNRECOGNIZED: return null;
        }

        return null;
    }

    public static PrimitiveTypeProvider primitiveTypeProvider(CineastGrpc.PrimitiveType type) {
        switch(type.getValueCase()) {
            case STRINGVALUE: return new StringTypeProvider(type.getStringValue());
            case INTVALUE: return new IntTypeProvider(type.getIntValue());
            case LONGVALUE: return new LongTypeProvider(type.getLongValue());
            case FLOATVALUE: return new FloatTypeProvider(type.getFloatValue());
            case DOUBLEVALUE: return new DoubleTypeProvider(type.getDoubleValue());
            case BOOLEANVALUE: return new BooleanTypeProvider(type.getBooleanValue());
            case VALUE_NOT_SET: return new NothingProvider();
        }
        return new NothingProvider();
    }

    public static IdQueryContainer idQueryContainer(CineastGrpc.IdQueryContainer container) {
        return new IdQueryContainer(container.getSegmentId().getId());
    }

    public static ImageQueryContainer imageQueryContainer(CineastGrpc.ImageQueryContainer container) {
        try {
            return new ImageQueryContainer(
            ImageIO.read(new ByteArrayInputStream(container.getImage().toByteArray()))
            );
        } catch (IOException e) {
            LOGGER.error("Error in reading image from ImageQueryContainer: {}", LogHelper.getStackTrace(e));
        }
        return null;
    }

    public static InstantQueryContainer instantQueryContainer(CineastGrpc.InstantQueryContainer container) {
        return new InstantQueryContainer(container.getInstant());
    }

    public static LocationQueryContainer locationQueryContainer(CineastGrpc.LocationQueryContainer container) {
        return new LocationQueryContainer(Location.of(container.getLongitude(), container.getLatitude()));
    }

    public static ModelQueryContainer modelQueryContainer(CineastGrpc.ModelQueryContainer container) {
        //TODO figure out better mesh representation
        return null;
    }

    public static MotionQueryContainer motionQueryContainer(CineastGrpc.MotionQueryContainer container) {
        MotionQueryContainer motionQueryContainer = new MotionQueryContainer();
        container.getBackgroundPathList().stream().forEach(path -> motionQueryContainer.addBgPath(motionPath(path)));
        container.getForegroundPathList().stream().forEach(path -> motionQueryContainer.addPath(motionPath(path)));
        return motionQueryContainer;
    }

    public static LinkedList<Point2D_F32> motionPath(CineastGrpc.MotionQueryContainer.MotionPath path) {
        LinkedList<Point2D_F32> list = new LinkedList<>();
        list.addAll(path.getPathList().stream().map(QueryContainerUtil::point).collect(Collectors.toList()));
        return list;
    }

    public static Point2D_F32 point(CineastGrpc.MotionQueryContainer.MotionPath.Point point) {
        return new Point2D_F32(point.getX(), point.getY());
    }

    public static SemanticMapQueryContainer semanticMapQueryContainer(CineastGrpc.SemanticMapQueryContainer container) {
        try {
            return new SemanticMapQueryContainer(new SemanticMap(
                    ImageIO.read(new ByteArrayInputStream(container.getImage().toByteArray())),
                    container.getConceptsMap())
            );
        } catch (IOException e) {
            LOGGER.error("Error in reading image from ImageQueryContainer: {}", LogHelper.getStackTrace(e));
        }
        return null;
    }

    public static TagQueryContainer tagQueryContainer(CineastGrpc.TagQueryContainer container) {
        return null; //TODO do we even still need that one?
    }

    public static TextQueryContainer textQueryContainer(CineastGrpc.TextQueryContainer container) {
        return new TextQueryContainer(container.getText());
    }



}
