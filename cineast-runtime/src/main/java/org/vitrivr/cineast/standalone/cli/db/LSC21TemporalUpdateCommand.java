package org.vitrivr.cineast.standalone.cli.db;

import com.github.rvesse.airline.annotations.Command;
import com.github.rvesse.airline.annotations.Option;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.*;
import java.util.stream.Collectors;
import kotlin.Pair;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.vitrivr.cineast.core.data.entities.MediaSegmentDescriptor;
import org.vitrivr.cineast.core.db.DataSource;
import org.vitrivr.cineast.core.db.cottontaildb.CottontailWrapper;
import org.vitrivr.cineast.core.util.CineastConstants;
import org.vitrivr.cineast.standalone.config.Config;
import org.vitrivr.cineast.standalone.importer.lsc2020.LSCUtilities;
import org.vitrivr.cottontail.client.iterators.Tuple;
import org.vitrivr.cottontail.client.iterators.TupleIterator;
import org.vitrivr.cottontail.client.language.dml.Update;
import org.vitrivr.cottontail.client.language.dql.Query;
import org.vitrivr.cottontail.grpc.CottontailGrpc;
import org.vitrivr.cottontail.grpc.CottontailGrpc.ColumnName;
import org.vitrivr.cottontail.grpc.CottontailGrpc.Literal;
import org.vitrivr.cottontail.grpc.CottontailGrpc.Literal.Builder;
import org.vitrivr.cottontail.grpc.CottontailGrpc.UpdateMessage.UpdateElement;

@Command(name = "lsc21-time-update", description = "Updates media segments based on their ID")
public class LSC21TemporalUpdateCommand implements Runnable {

  private final static Logger LOGGER = LogManager.getLogger(LSC21TemporalUpdateCommand.class);

  private static final String ENTITY_NAME = "cineast.cineast_segment";

  @Option(name = {"-p",
      "--progress"}, title = "Progress", description = "Flag to indicate that some progress information is desired")
  private boolean progress = false;

  @Option(name = {"-R", "--reset-abs"}, title= "Reset Absolute TImes", description = "Flag to indicate that the startAbs and endAbs fields should be reset to 0.0")
  private boolean resetAbsTime = false;

  private static MediaSegmentDescriptor convert(Tuple segmentTuple) {
    final String oid = (String) segmentTuple.get(MediaSegmentDescriptor.OBJECT_ID_COL_NAME);
    final String sid = (String) segmentTuple.get(CineastConstants.SEGMENT_ID_COLUMN_QUALIFIER);
    final int number = (Integer) segmentTuple.get(MediaSegmentDescriptor.SEGMENT_NO_COL_NAME);
    final int start = (Integer) segmentTuple.get(MediaSegmentDescriptor.SEGMENT_START_COL_NAME);
    final int end = (Integer) segmentTuple.get(MediaSegmentDescriptor.SEGMENT_END_COL_NAME);
    final double startabs = (Double) segmentTuple.get(
        MediaSegmentDescriptor.SEGMENT_STARTABS_COL_NAME);
    final double endabs = (Double) segmentTuple.get(MediaSegmentDescriptor.SEGMENT_ENDABS_COL_NAME);
    MediaSegmentDescriptor segment = new MediaSegmentDescriptor(
        oid, sid, number, start, end, (float) startabs, (float) endabs, true
    );
    return segment;
  }

  private static List<UpdateElement> convert(MediaSegmentDescriptor segment) {
    return Arrays.stream(MediaSegmentDescriptor.FIELDNAMES).map(name -> UpdateElement.newBuilder()
        .setColumn(ColumnName.newBuilder().setName(name).build())
        .setValue(CottontailGrpc.Expression.newBuilder().setLiteral(forValue(segment, name)).build())
        .build()).collect(Collectors.toList());
  }


  private static Literal forValue(MediaSegmentDescriptor segment, String name) {
    final Builder builder = Literal.newBuilder();

    switch (name) {
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
        LOGGER.warn("Cannot parse column" + name + " for segment " + segment.toString());
    }

    return builder.build();
  }

  @Override
  @SuppressWarnings("unchecked")
  public void run() {
    if (Config.sharedConfig().getDatabase().getSelector() != DataSource.COTTONTAIL) {
      System.out.println("Other DB than Cottontail DB not supported (yet). Aborting");
      return;
    }

    /* Preparation. */
    final CottontailWrapper cottontail = new CottontailWrapper(Config.sharedConfig().getDatabase().getHost(), Config.sharedConfig().getDatabase().getPort());
    long txId = cottontail.client.begin();
    final Query query = new Query(ENTITY_NAME).select("*", null).txId(txId);
    final TupleIterator ti = cottontail.client.query(query);
    final LinkedList<Update> updates = new LinkedList<>();
    int counter = 0;
    int totalCounter = 0;

    /* Prepare updates. */
    while (ti.hasNext()) {
      final Tuple t = ti.next();
      final MediaSegmentDescriptor segment = convert(t);
      try {
        final Optional<LocalDateTime> oldt = extractTimeInformation(segment.getSegmentId());
        if(!oldt.isPresent()){
          continue;
        }
        final LocalDateTime ldt = oldt.get();
        final long msOfTheDay = ldt.getHour() * 60*60*1000 + ldt.getMinute() *60*1000 + ldt.getSecond()*1000;
        final long sAbs = ldt.toInstant(ZoneOffset.UTC).toEpochMilli() / 1000L;
        final Update update = new Update(ENTITY_NAME).values(
                new Pair<>(MediaSegmentDescriptor.SEGMENT_START_COL_NAME, (double) msOfTheDay),
                new Pair<>(MediaSegmentDescriptor.SEGMENT_END_COL_NAME, (double) msOfTheDay+1),
                new Pair<>(MediaSegmentDescriptor.SEGMENT_STARTABS_COL_NAME,
                 resetAbsTime ? 0.0 : sAbs),
                new Pair<>(MediaSegmentDescriptor.SEGMENT_ENDABS_COL_NAME,
                  resetAbsTime ? 0.0 : (sAbs+1))
        ).where(new org.vitrivr.cottontail.client.language.extensions.Literal(CineastConstants.SEGMENT_ID_COLUMN_QUALIFIER, "=", segment.getSegmentId())).txId(txId);
        updates.add(update);
      } catch (Exception e) {
        LOGGER.warn("Could not update " + segment.getSegmentId() + " due to exception: " + e.getMessage());
      }
    }

    /* Execute updates. */
    Update update;
    while ((update = updates.poll()) != null) {
      cottontail.client.update(update);
      totalCounter++;
      if (counter++ > 99) {
        if (progress) {
          System.out.println("Updated " + totalCounter + " rows.");
        }
        counter = 0;
      }
    }

    cottontail.client.commit(txId);
    System.out.println("Done.");
  }

  static Optional<LocalDateTime> extractTimeInformation(String segmentId){
    String[] parts = segmentId.split("_");
    String date = "", time = "";
    if(parts.length == 4){
      // "Simple format, e.g. is_20160810_071157_000
      date = parts[1];
      time = parts[2];
    }else if(parts.length == 5){
      // "Extended" format, e.g. is_b00000003_21i6bq_20150305_061213e
      date = parts[3];
      time = parts[4].substring(0,6);
    }else{
      LOGGER.warn("Something went wrong in extracting time info from "+segmentId);
      return Optional.empty();
    }
    int year = Integer.parseInt(date.substring(0,4));
    int month = Integer.parseInt(date.substring(4,6));
    int day = Integer.parseInt(date.substring(6));
    int hours = Integer.parseInt(time.substring(0,2));
    int minutes = Integer.parseInt(time.substring(2,4));
    int seconds = Integer.parseInt(time.substring(4));
    return Optional.of(LocalDateTime.of(year, month, day, hours, minutes, seconds));
  }
}
