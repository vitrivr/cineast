package org.vitrivr.cineast.core.extraction.decode.video;

import com.github.kokorin.jaffree.StreamType;
import com.github.kokorin.jaffree.ffmpeg.*;
import com.github.kokorin.jaffree.ffprobe.FFprobe;
import com.github.kokorin.jaffree.ffprobe.FFprobeResult;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.vitrivr.cineast.core.config.CacheConfig;
import org.vitrivr.cineast.core.config.DecoderConfig;
import org.vitrivr.cineast.core.data.frames.AudioDescriptor;
import org.vitrivr.cineast.core.data.frames.VideoDescriptor;
import org.vitrivr.cineast.core.data.frames.VideoFrame;
import org.vitrivr.cineast.core.data.raw.CachedDataFactory;
import org.vitrivr.cineast.core.data.raw.images.MultiImage;
import org.vitrivr.cineast.core.extraction.decode.general.Decoder;

import java.awt.image.BufferedImage;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Set;
import java.util.concurrent.LinkedBlockingQueue;

public class FFMpegProcessVideoDecoder implements Decoder<VideoFrame> {

    /**
     * Lists the mime types supported by the FFMpegVideoDecoder.
     * <p>
     * TODO: List may not be complete yet.
     */
    public static final Set<String> supportedFiles = Set.of("multimedia/mp4", "video/mp4", "video/avi", "video/mpeg", "video/quicktime", "video/webm");
    /**
     * Configuration property name for the {@link FFMpegVideoDecoder}: max width of the converted video.
     */
    private static final String CONFIG_MAXWIDTH_PROPERTY = "maxFrameWidth";
    /**
     * Configuration property name for the {@link FFMpegVideoDecoder}: max height of the converted video.
     */
    private static final String CONFIG_HEIGHT_PROPERTY = "maxFrameHeight";
    /**
     * Configuration property name for the {@link FFMpegVideoDecoder}: number of channels of the converted audio. If <= 0, then no audio will be decoded.
     */
    private static final String CONFIG_CHANNELS_PROPERTY = "channels";
    /**
     * Configuration property name for the {@link FFMpegVideoDecoder}: samplerate of the converted audio.
     */
    private static final String CONFIG_SAMPLERATE_PROPERTY = "samplerate";
    /**
     * Configuration property name for the {@link FFMpegVideoDecoder}: Indicates whether subtitles should be decoded as well.
     */
    private static final String CONFIG_SUBTITLE_PROPERTY = "subtitles";
    /**
     * Configuration property default for the FFMpegVideoDecoder: max width of the converted video.
     */
    private final static int CONFIG_MAXWIDTH_DEFAULT = 1920;
    /**
     * Configuration property default for the FFMpegVideoDecoder: max height of the converted video.
     */
    private final static int CONFIG_MAXHEIGHT_DEFAULT = 1080;
    /**
     * Configuration property default for the FFMpegVideoDecoder: number of channels of the converted audio.
     */
    private static final int CONFIG_CHANNELS_DEFAULT = 1;
    /**
     * Configuration property default for the FFMpegVideoDecoder: sample rate of the converted audio
     */
    private static final int CONFIG_SAMPLERATE_DEFAULT = 44100;

    private final Path ffmpegPath = Path.of("ffmpeg");

    private FFmpegResultFuture future = null;
    private int estimatedFrameCount = 0;

    private static final Logger LOGGER = LogManager.getLogger();
    private CachedDataFactory factory;

    private final LinkedBlockingQueue<VideoFrame> videoFrameQueue = new LinkedBlockingQueue<>(10);

    @Override
    public boolean init(Path path, DecoderConfig decoderConfig, CacheConfig cacheConfig) {

        if (!Files.exists(path)) {
            LOGGER.error("File does not exist {}", path.toString());
            return false;
        }

        /* Initialize MultiImageFactory using the ImageCacheConfig. */
        if (cacheConfig == null) {
            LOGGER.error("You must provide a valid ImageCacheConfig when initializing the FFMpegVideoDecoder.");
            return false;
        }
        this.factory = cacheConfig.sharedCachedDataFactory();

        //checking container and stream information

        FFprobeResult ffprobeResult = FFprobe.atPath(ffmpegPath).setInput(path).setShowStreams(true).execute();

        VideoDescriptor videoDescriptor = null;
        final HashMap<Integer, AudioDescriptor> audioDescriptors = new HashMap<>();

        for (com.github.kokorin.jaffree.ffprobe.Stream stream: ffprobeResult.getStreams()) {
            if (stream.getCodecType() == StreamType.VIDEO) {
                videoDescriptor = new VideoDescriptor(stream.getAvgFrameRate().floatValue(), Math.round(stream.getDuration() * 1000d), stream.getWidth(), stream.getHeight());
                if (stream.getNbFrames() != null) {
                    this.estimatedFrameCount = stream.getNbFrames();
                }
                continue;
            }
            if (stream.getCodecType() == StreamType.AUDIO) {
                AudioDescriptor descriptor = new AudioDescriptor(stream.getSampleRate().floatValue(), stream.getChannels(), Math.round(stream.getDuration() * 1000d));
                audioDescriptors.put(stream.getIndex(), descriptor);
            }
        }

        if (videoDescriptor == null) {
            LOGGER.error("No video stream found in {}", path.toString());
            return false;
        }

        final int maxWidth = decoderConfig.namedAsInt(CONFIG_MAXWIDTH_PROPERTY, CONFIG_MAXWIDTH_DEFAULT);
        final int maxHeight = decoderConfig.namedAsInt(CONFIG_HEIGHT_PROPERTY, CONFIG_MAXHEIGHT_DEFAULT);

        VideoDescriptor finalVideoDescriptor = videoDescriptor;
        future = FFmpeg.atPath(ffmpegPath)
                .addInput(UrlInput.fromPath(path))
                .addOutput(FrameOutput.withConsumer(
                        new FrameConsumer() {

                            final HashMap<Integer, Stream> streamHashMap = new HashMap<>();
                            int frameCounter = 0;

                            @Override
                            public void consumeStreams(List<Stream> streams) {
                                for (Stream stream : streams) {
                                    streamHashMap.put(stream.getId(), stream);
                                }
                            }

                            @Override
                            public void consume(Frame frame) {

                                Stream stream = streamHashMap.get(frame.getStreamId());

                                if (stream == null) {
                                    //no information about the stream, ignore frame
                                    LOGGER.debug("received frame from unknown stream {}, ignoring", frame.getStreamId());
                                    return;
                                }

                                switch (stream.getType()) {

                                    case VIDEO -> {

                                        BufferedImage bimg = frame.getImage();

                                        if (bimg.getWidth() > maxWidth || bimg.getHeight() > maxHeight) {
                                            //TODO rescale
                                        }

                                        MultiImage image = factory.newMultiImage(bimg);
                                        VideoFrame videoFrame = new VideoFrame(frameCounter++, (1000 * frame.getPts()) / stream.getTimebase(), image, finalVideoDescriptor);

                                        try {
                                            videoFrameQueue.put(videoFrame);
                                        } catch (InterruptedException e) {
                                            LOGGER.error("Could not enqueue frame", e);
                                        }

                                        break;
                                    }
                                    case AUDIO -> {

                                        //TODO audio data conversion

                                        break;
                                    }
                                }


                            }
                        }
                ))
                .executeAsync();


        return true;
    }

    @Override
    public void close() {

        if (this.future != null) {
            this.future.graceStop();
            this.future = null;
        }

    }

    @Override
    public VideoFrame getNext() {
        if (this.complete()) {
            return null;
        }
        try {
            return videoFrameQueue.take();
        } catch (InterruptedException e) {
            return null;
        }

    }

    @Override
    public int count() {
        return estimatedFrameCount;
    }

    @Override
    public boolean complete() {
        return (this.future == null || this.future.isDone() || this.future.isCancelled()) && this.videoFrameQueue.isEmpty();
    }

    @Override
    public Set<String> supportedFiles() {
        return supportedFiles;
    }

    @Override
    public boolean canBeReused() {
        return false;
    }
}
