package org.vitrivr.cineast.standalone.importer.redhen;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.time.temporal.ChronoField;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Optional;
import java.util.function.Supplier;
import java.util.stream.Stream;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.vitrivr.cineast.core.data.entities.MediaSegmentDescriptor;
import org.vitrivr.cineast.core.data.entities.SimpleFulltextFeatureDescriptor;
import org.vitrivr.cineast.core.data.providers.primitive.PrimitiveTypeProvider;
import org.vitrivr.cineast.core.data.providers.primitive.StringTypeProvider;
import org.vitrivr.cineast.core.importer.Importer;

public class TextImporter implements Importer<Pair<String, String>> {

  static final DateTimeFormatter DATE_TIME_FORMATTER = (
      new DateTimeFormatterBuilder()
          .appendPattern("yyyyMMddHHmmss")
          .appendFraction(ChronoField.MILLI_OF_SECOND, 0, 3, true)
          .toFormatter()
  );
  private final Path fsPath;
  private final Supplier<Stream<String>> lines;
  private final Iterator<Pair<String, String>> iter;
  private final String fieldName;
  private List<MediaSegmentDescriptor> objectDescriptors;
  private ListIterator<MediaSegmentDescriptor> segments;
  private MediaSegmentDescriptor curSegment;

  public TextImporter(Path fsPath, String fieldName) {
    this.fsPath = fsPath;
    this.lines = () -> {
      try {
        return Files.lines(this.fsPath);
      } catch (IOException e) {
        throw new UncheckedIOException(e);
      }
    };
    this.iter = this.alignedStream().iterator();
    this.fieldName = fieldName;
  }

  public void setSegments(List<MediaSegmentDescriptor> segments) {
    this.segments = segments.listIterator();
    getNextSegment();
  }

  void getNextSegment() {
    if (this.segments.hasNext()) {
      this.curSegment = this.segments.next();
    } else {
      this.curSegment = null;
    }
  }

  private LocalDateTime parse(String bit) {
    return LocalDateTime.parse(bit, DATE_TIME_FORMATTER);
  }

  private float floatOfDuration(Duration duration) {
    return duration.getSeconds() + duration.getNano() / 1000000000f;
  }

  private Stream<Pair<String, String>> alignedStream() {
    return this.lines.get().flatMap(
        line -> {
          String[] bits = line.split("\\|");
          if (bits.length != 4 || !bits[2].equals(this.fieldName)) {
            return Stream.empty();
          }
          LocalDateTime startTime = parse(bits[0]);
          LocalDateTime endTime = parse(bits[1]);
          LocalDateTime videoStart = this.getHeaderInfo().get().start;
          Duration subStart = Duration.between(videoStart, startTime);
          Duration subEnd = Duration.between(videoStart, endTime);
          while (this.curSegment != null && this.curSegment.getEndabs() < floatOfDuration(
              subStart)) {
            getNextSegment();
          }
          /* We now know that curSegment.end >= subtitle.start
           * Seg: ?---|
           * Sub:    |--?
           */
          ArrayList<String> segmentIds = new ArrayList<>();
          int lookaheads = 0;
          while (this.curSegment != null && this.curSegment.getStartabs() < floatOfDuration(subEnd)) {
            segmentIds.add(this.curSegment.getSegmentId());
            if (this.segments.hasNext()) {
              this.curSegment = this.segments.next();
              lookaheads++;
            } else {
              break;
            }
          }
          for (int i = 0; i < lookaheads; i++) {
            this.segments.previous();
          }
          if (lookaheads > 0) {
            this.segments.previous();
            this.curSegment = this.segments.next();
          }
          return segmentIds.stream().map(segId -> new ImmutablePair<>(segId, bits[3]));
        }
    );
  }

  @Override
  public Pair<String, String> readNext() {
    if (this.iter.hasNext()) {
      return this.iter.next();
    }
    return null;
  }

  @Override
  public Map<String, PrimitiveTypeProvider> convert(Pair<String, String> data) {
    HashMap<String, PrimitiveTypeProvider> map = new HashMap<>(2);
    map.put(
      SimpleFulltextFeatureDescriptor.FIELDNAMES[0],
      new StringTypeProvider(data.getLeft())
    );
    map.put(
      SimpleFulltextFeatureDescriptor.FIELDNAMES[1],
      new StringTypeProvider(data.getRight())
    );
    return map;
  }

  public final class HeaderInfo {

    public String name;
    public LocalDateTime start;

    public HeaderInfo(String name, LocalDateTime start) {
      this.name = name;
      this.start = start;
    }
  }

  private boolean headerParsed = false;
  private Optional<HeaderInfo> headerInfo;

  public Optional<HeaderInfo> getHeaderInfo() {
    if (!headerParsed) {
      this.headerInfo = this.lines.get().limit(20).flatMap(s -> {
        String[] bits = s.split("\\|");
        if (bits.length == 3 && bits[0].equals("TOP")) {
          return Stream.of(new HeaderInfo(
              bits[2],
              LocalDateTime.parse(bits[1], DATE_TIME_FORMATTER)
          ));
        } else {
          return Stream.empty();
        }
      }).findFirst();
      headerParsed = true;
    }
    return this.headerInfo;
  }
}
