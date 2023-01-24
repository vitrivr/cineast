package org.vitrivr.cineast.core.extraction.decode.video;

import com.github.kokorin.jaffree.StreamType;
import com.github.kokorin.jaffree.ffmpeg.*;
import com.github.kokorin.jaffree.ffprobe.FFprobe;
import com.github.kokorin.jaffree.ffprobe.FFprobeResult;
import net.coobird.thumbnailator.Thumbnails;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.vitrivr.cineast.core.config.CacheConfig;
import org.vitrivr.cineast.core.config.DecoderConfig;
import org.vitrivr.cineast.core.data.frames.AudioDescriptor;
import org.vitrivr.cineast.core.data.frames.AudioFrame;
import org.vitrivr.cineast.core.data.frames.VideoDescriptor;
import org.vitrivr.cineast.core.data.frames.VideoFrame;
import org.vitrivr.cineast.core.data.raw.CachedDataFactory;
import org.vitrivr.cineast.core.data.raw.images.MultiImage;
import org.vitrivr.cineast.core.extraction.decode.general.Decoder;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Set;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicInteger;


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

    private final static int CONFIG_MAXWIDTH_DEFAULT = 1920;
    /**
     * Configuration property default for the FFMpegVideoDecoder: max height of the converted video.
     */
    private final static int CONFIG_MAXHEIGHT_DEFAULT = 1080;

    private final Path ffmpegPath = Path.of("ffmpeg");

    private FFmpegResultFuture future = null;
    private int estimatedFrameCount = 0;

    private static final Logger LOGGER = LogManager.getLogger();
    private CachedDataFactory factory;

    private final LinkedBlockingQueue<VideoFrame> videoFrameQueue = new LinkedBlockingQueue<>(10);
    private final LinkedBlockingQueue<AudioFrame> audioFrameQueue = new LinkedBlockingQueue<>(1000);

    @Override
    public boolean init(Path path, DecoderConfig decoderConfig, CacheConfig cacheConfig) {

        if (!Files.exists(path)) {
            LOGGER.error("File does not exist {}", path.toString());
            return false;
        }

        /* Initialize MultiImageFactory using the ImageCacheConfig. */
        if (cacheConfig == null) {
            LOGGER.error("You must provide a valid ImageCacheConfig when initializing the FFMpegProcessVideoDecoder.");
            return false;
        }
        this.factory = cacheConfig.sharedCachedDataFactory();

        //checking container and stream information

        FFprobeResult ffprobeResult = FFprobe.atPath(ffmpegPath).setInput(path).setShowStreams(true).execute();

        VideoDescriptor videoDescriptor = null;
        AudioDescriptor audioDescriptor = null;

        for (com.github.kokorin.jaffree.ffprobe.Stream stream : ffprobeResult.getStreams()) {
            if (stream.getCodecType() == StreamType.VIDEO) {
                videoDescriptor = new VideoDescriptor(stream.getAvgFrameRate().floatValue(), Math.round(stream.getDuration() * 1000d), stream.getWidth(), stream.getHeight());
                if (stream.getNbFrames() != null) {
                    this.estimatedFrameCount = stream.getNbFrames();
                }
                continue;
            }
            if (stream.getCodecType() == StreamType.AUDIO) {
                audioDescriptor = new AudioDescriptor(stream.getSampleRate().floatValue(), stream.getChannels(), Math.round(stream.getDuration() * 1000d)); //TODO stream id mismatch between ffprobe and ffmpeg, figure out how to deal with multiple streams
            }
        }

        if (videoDescriptor == null) {
            LOGGER.error("No video stream found in {}", path.toString());
            return false;
        }

        final float maxWidth = decoderConfig.namedAsInt(CONFIG_MAXWIDTH_PROPERTY, CONFIG_MAXWIDTH_DEFAULT);
        final float maxHeight = decoderConfig.namedAsInt(CONFIG_HEIGHT_PROPERTY, CONFIG_MAXHEIGHT_DEFAULT);

        VideoDescriptor finalVideoDescriptor = videoDescriptor;
        AudioDescriptor finalAudioDescriptor = audioDescriptor;
        future = FFmpeg.atPath(ffmpegPath)
                .addInput(UrlInput.fromPath(path))
                .addOutput(FrameOutput.withConsumer(
                        new FrameConsumer() {

                            final HashMap<Integer, Stream> streamHashMap = new HashMap<>();
                            int frameCounter = 0;

                            final HashMap<Integer, AtomicInteger> audioFrameIdCounter = new HashMap<>();

                            @Override
                            public void consumeStreams(List<Stream> streams) {
                                for (Stream stream : streams) {
                                    streamHashMap.put(stream.getId(), stream);
                                    if (stream.getType() == Stream.Type.AUDIO) {
                                        audioFrameIdCounter.put(stream.getId(), new AtomicInteger());
                                    }
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
                                            double scale = Math.min(bimg.getWidth() / maxWidth, bimg.getHeight() / maxHeight);
                                            try {
                                                bimg = Thumbnails.of(bimg).scale(scale).asBufferedImage();
                                            } catch (IOException e) {
                                                LOGGER.error("Could not scale frame", e);
                                            }
                                        }

                                        MultiImage image = factory.newMultiImage(bimg);
                                        VideoFrame videoFrame = new VideoFrame(frameCounter++, (1000 * frame.getPts()) / stream.getTimebase(), image, finalVideoDescriptor);

                                        try {
                                            videoFrameQueue.put(videoFrame);
                                        } catch (InterruptedException e) {
                                            LOGGER.error("Could not enqueue frame", e);
                                        }

                                    }
                                    case AUDIO -> {

                                        if (finalAudioDescriptor == null) {
                                            LOGGER.debug("received audio frame from unknown stream {}, ignoring", frame.getStreamId());
                                            return;
                                        }

                                        int[] samples = frame.getSamples();

                                        if (samples == null) {
                                            return;
                                        }

                                        AtomicInteger idCounter = audioFrameIdCounter.get(stream.getId());

                                        if (idCounter == null) {
                                            return;
                                        }

                                        byte[] reEncoded = new byte[samples.length * 2];

                                        for (int i = 0; i < samples.length; ++i) {

                                            short s = (short) (samples[i] / 65536);
                                            reEncoded[2*i] = ((byte) ((s) & 0xff));
                                            reEncoded[2*i + 1] = ((byte) ((s >> 8) & 0xff));

                                        }

                                        AudioFrame audioFrame = new AudioFrame(
                                                idCounter.getAndIncrement(),
                                                (1000 * frame.getPts()) / stream.getTimebase(),
                                                reEncoded,
                                                finalAudioDescriptor
                                        );

                                        try {
                                            audioFrameQueue.put(audioFrame);
                                        } catch (InterruptedException e) {
                                            LOGGER.error("Could not enqueue audio frame", e);
                                        }

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
            VideoFrame frame = videoFrameQueue.take();

            while (!decoderComplete()) {
                AudioFrame audioFrame = this.audioFrameQueue.peek();
                if (audioFrame == null) {
                    break;
                }
                if (audioFrame.getTimestamp() <= frame.getTimestamp()) {
                    frame.addAudioFrame(this.audioFrameQueue.poll());
                } else {
                    break;
                }
            }


            return frame;
        } catch (InterruptedException e) {
            return null;
        }

    }

    @Override
    public int count() {
        return estimatedFrameCount;
    }

    private boolean decoderComplete() {
        return this.future == null || this.future.isDone() || this.future.isCancelled();
    }

    @Override
    public boolean complete() {
        return this.videoFrameQueue.isEmpty() && decoderComplete();
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
