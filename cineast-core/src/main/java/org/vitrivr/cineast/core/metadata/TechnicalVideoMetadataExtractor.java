package org.vitrivr.cineast.core.metadata;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bytedeco.javacpp.*;
import org.vitrivr.cineast.core.data.entities.MediaObjectMetadataDescriptor;
import org.vitrivr.cineast.core.extraction.decode.video.FFMpegVideoDecoder;
import org.vitrivr.cineast.core.util.MimeTypeHelper;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import static org.bytedeco.javacpp.avcodec.AVCodec;
import static org.bytedeco.javacpp.avformat.AVFormatContext;
import static org.bytedeco.javacpp.avformat.AVStream;

public class TechnicalVideoMetadataExtractor implements MetadataExtractor {

  private static final Logger LOGGER = LogManager.getLogger();

  /**
   * Key used to to store video FPS values.
   */
  private static final String KEY_VIDEO_FPS = "fps";

  /**
   * Key used to to store video duration values (in ms).
   */
  private static final String KEY_VIDEO_DURATION = "duration";

  /**
   * Key used to to store video width values.
   */
  private static final String KEY_VIDEO_WIDTH = "width";

  /**
   * Key used to to store video height values.
   */
  private static final String KEY_VIDEO_HEIGHT = "height";

  /**
   * Extracts the technical video metadata from the specified path and returns a List of {@link MediaObjectMetadataDescriptor} objects (one for each metadata entry).
   *
   * @param objectId ID of the multimedia object for which metadata will be generated.
   * @param path Path to the file for which metadata should be extracted.
   * @return List of {@link MediaObjectMetadataDescriptor}s or an empty list, if extracting metadata fails.
   */
  @Override
  public List<MediaObjectMetadataDescriptor> extract(String objectId, Path path) {

    final ArrayList<MediaObjectMetadataDescriptor> metadata = new ArrayList<>();
    if (!Files.exists(path)) {
      LOGGER.warn("File does not exist, returning empty metadata");
      return metadata;
    }

    /* we assume that everythign which can be handled by the ffmpegvideodecoder can also be handled here. Without this safety-guard, extraction will crash with a core-dump */
    if (!FFMpegVideoDecoder.supportedFiles.contains(MimeTypeHelper.getContentType(path.toString()))) {
      LOGGER.warn("File is not a video, returning empty metadata");
      return metadata;
    }

    /* Initialize the AVFormatContext. */
    final AVFormatContext pFormatContext = avformat.avformat_alloc_context();

    /* */
    if (avformat.avformat_open_input(pFormatContext, path.toString(), null, null) != 0) {
      LOGGER.error("Error while accessing file {}. Failed to obtain technical video metadata.", path.toString());
      return metadata;
    }

    /* Retrieve stream information. */
    if (avformat.avformat_find_stream_info(pFormatContext, (PointerPointer<?>) null) < 0) {
      LOGGER.error("Error, Ccouldn't find stream information. Failed to obtain technical video metadata.");
      return metadata;
    }

    final AVCodec codec = avcodec.av_codec_iterate(new Pointer());
    final int videoStreamIdx = avformat.av_find_best_stream(pFormatContext, avutil.AVMEDIA_TYPE_VIDEO, -1, -1, codec, 0);
    final AVStream videoStream = pFormatContext.streams(videoStreamIdx);
    final avutil.AVRational timebase = videoStream.time_base();

    /* Allocate new codec-context for codec returned by av_find_best_stream(). */
    final avcodec.AVCodecContext videoCodecContext = avcodec.avcodec_alloc_context3(codec);
    avcodec.avcodec_parameters_to_context(videoCodecContext, videoStream.codecpar());

    /* Open the code context. */
    if (avcodec.avcodec_open2(videoCodecContext, codec, (avutil.AVDictionary) null) < 0) {
      LOGGER.error("Error, Could not open video codec.  Failed to obtain technical video metadata.");
      return metadata;
    }

    /* Extract and add the video metadata to the list. */
    metadata.add(new MediaObjectMetadataDescriptor(objectId, this.domain(), KEY_VIDEO_FPS, ((float) videoStream.avg_frame_rate().num() / (float) videoStream.avg_frame_rate().den()), false));
    metadata.add(new MediaObjectMetadataDescriptor(objectId, this.domain(), KEY_VIDEO_DURATION, Math.floorDiv(videoStream.duration() * timebase.num() * 1000, timebase.den()), false));
    metadata.add(new MediaObjectMetadataDescriptor(objectId, this.domain(), KEY_VIDEO_WIDTH, videoCodecContext.width(), false));
    metadata.add(new MediaObjectMetadataDescriptor(objectId, this.domain(), KEY_VIDEO_HEIGHT, videoCodecContext.height(), false));

    /* Closes all the resources. */
    avcodec.avcodec_free_context(videoCodecContext);
    avformat.avformat_close_input(pFormatContext);

    /* Return list of results. */
    return metadata;
  }

  @Override
  public String domain() {
    return "technical";
  }
}
