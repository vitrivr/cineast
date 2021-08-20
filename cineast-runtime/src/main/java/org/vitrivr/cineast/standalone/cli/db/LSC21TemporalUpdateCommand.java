package org.vitrivr.cineast.standalone.cli.db;

import com.github.rvesse.airline.annotations.Command;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import javax.print.attribute.standard.Media;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.vitrivr.cineast.core.config.DatabaseConfig;
import org.vitrivr.cineast.core.config.DatabaseConfig.Selector;
import org.vitrivr.cineast.core.config.DatabaseConfig.Writer;
import org.vitrivr.cineast.core.data.entities.MediaSegmentDescriptor;
import org.vitrivr.cineast.core.db.cottontaildb.CottontailWrapper;
import org.vitrivr.cineast.core.util.CineastConstants;
import org.vitrivr.cineast.standalone.config.Config;
import org.vitrivr.cineast.standalone.importer.lsc2020.LSCUtilities;
import org.vitrivr.cottontail.client.TupleIterator;
import org.vitrivr.cottontail.client.TupleIterator.Tuple;
import org.vitrivr.cottontail.client.language.dml.Update;
import org.vitrivr.cottontail.client.language.dql.Query;
import org.vitrivr.cottontail.grpc.CottontailGrpc.ColumnName;
import org.vitrivr.cottontail.grpc.CottontailGrpc.EntityName;
import org.vitrivr.cottontail.grpc.CottontailGrpc.EntityNameOrBuilder;
import org.vitrivr.cottontail.grpc.CottontailGrpc.From;
import org.vitrivr.cottontail.grpc.CottontailGrpc.Literal;
import org.vitrivr.cottontail.grpc.CottontailGrpc.Literal.Builder;
import org.vitrivr.cottontail.grpc.CottontailGrpc.Projection;
import org.vitrivr.cottontail.grpc.CottontailGrpc.Projection.ProjectionElement;
import org.vitrivr.cottontail.grpc.CottontailGrpc.Projection.ProjectionOperation;
import org.vitrivr.cottontail.grpc.CottontailGrpc.Scan;
import org.vitrivr.cottontail.grpc.CottontailGrpc.TransactionId;
import org.vitrivr.cottontail.grpc.CottontailGrpc.UpdateMessage;
import org.vitrivr.cottontail.grpc.CottontailGrpc.UpdateMessage.UpdateElement;

@Command(name="lsc21-time-update", description = "Updates media segments based on their ID")
public class LSC21TemporalUpdateCommand implements Runnable{

  private final static Logger LOGGER = LogManager.getLogger(LSC21TemporalUpdateCommand.class);

  @Override
  public void run() {
    if(Config.sharedConfig().getDatabase().getSelector() != Selector.COTTONTAIL ||
        Config.sharedConfig().getDatabase().getWriter() != Writer.COTTONTAIL
    ){
      System.out.println("Other DB than Cottontail DB not supported (yet). Aborting");
      return;
    }
    final CottontailWrapper cottontail = new CottontailWrapper(Config.sharedConfig().getDatabase(), false);
    final long txId = cottontail.client.begin();
    final EntityName entityName = EntityName.newBuilder().setName("cineast.cineast_segement").build();
    final Query query = new Query("cineast.cineast_segment");
    query.select("*");
    final TupleIterator ti = cottontail.client.query(query, txId);
    final List<UpdateElement> updateElements = new ArrayList<>();
    while(ti.hasNext()){
      final Tuple t = ti.next();
      final MediaSegmentDescriptor segment = convert(t);
      final Optional<String> minuteIdOpt = LSCUtilities.filenameToMinuteId(segment.getSegmentId());
      if(!minuteIdOpt.isPresent()){
        LOGGER.warn("Could not update "+segment.getSegmentId());
       continue;
      }
      final LocalDateTime ldt = LSCUtilities.fromMinuteId(minuteIdOpt.get());
      final long msAbs = ldt.toInstant(ZoneOffset.UTC).toEpochMilli();
      final long msAbsNext = msAbs+1;
      final MediaSegmentDescriptor newSegment = new MediaSegmentDescriptor(
          segment.getSegmentId(),
          segment.getObjectId(),
          segment.getSequenceNumber(),
          (int)msAbs,
          (int)msAbsNext,
          (float)msAbs,
          (float)msAbsNext,
          segment.exists()
      );
      updateElements.addAll(convert(newSegment));
    }
    cottontail.client.update(UpdateMessage.newBuilder()
            .setFrom(From.newBuilder().setScan(Scan.newBuilder().setEntity(entityName).build()).build())
            .setTxId(TransactionId.newBuilder().setValue(txId).build())
            .addAllUpdates(updateElements)
        .build());
  }

  private static MediaSegmentDescriptor convert(Tuple segmentTuple){
    final String oid = (String) segmentTuple.get(MediaSegmentDescriptor.OBJECT_ID_COL_NAME);
    final String sid = (String) segmentTuple.get(CineastConstants.SEGMENT_ID_COLUMN_QUALIFIER);
    final int number = (Integer) segmentTuple.get(MediaSegmentDescriptor.SEGMENT_NO_COL_NAME);
    final int start = (Integer) segmentTuple.get(MediaSegmentDescriptor.SEGMENT_START_COL_NAME);
    final int end = (Integer) segmentTuple.get(MediaSegmentDescriptor.SEGMENT_END_COL_NAME);
    final double startabs = (Double) segmentTuple.get(MediaSegmentDescriptor.SEGMENT_STARTABS_COL_NAME);
    final double endabs = (Double) segmentTuple.get(MediaSegmentDescriptor.SEGMENT_ENDABS_COL_NAME);
    MediaSegmentDescriptor segment = new MediaSegmentDescriptor(
        sid, oid, number, start, end, (float)startabs, (float)endabs, true
    );
    return segment;
  }

  private static List<UpdateElement> convert(MediaSegmentDescriptor segment){
    return Arrays.stream(MediaSegmentDescriptor.FIELDNAMES).map(name -> UpdateElement.newBuilder()
        .setColumn(ColumnName.newBuilder().setName(name).build())
        .setValue(forValue(segment, name))
        .build()).collect(Collectors.toList());
  }

  private static Literal forValue(MediaSegmentDescriptor segment, String name){
    final Builder builder = Literal.newBuilder();

    switch(name){
      case CineastConstants.SEGMENT_ID_COLUMN_QUALIFIER:
        builder.setStringData(segment.getSegmentId());
        break;
      case MediaSegmentDescriptor.OBJECT_ID_COL_NAME:
        builder.setStringData(segment.getObjectId());
        break;
      case MediaSegmentDescriptor.SEGMENT_NO_COL_NAME:
        builder.setIntData(segment.getSequenceNumber());
        break;
      case MediaSegmentDescriptor.SEGMENT_START_COL_NAME:
        builder.setIntData(segment.getStart());
        break;
      case MediaSegmentDescriptor.SEGMENT_END_COL_NAME:
        builder.setIntData(segment.getEnd());
        break;
      case MediaSegmentDescriptor.SEGMENT_STARTABS_COL_NAME:
        builder.setFloatData(segment.getStartabs());
        break;
      case MediaSegmentDescriptor.SEGMENT_ENDABS_COL_NAME:
        builder.setFloatData(segment.getEndabs());
        break;
      default:
        LOGGER.warn("Cannot parse column"+name+" for segment "+segment.toString());
    }

    return builder.build();
  }
}
