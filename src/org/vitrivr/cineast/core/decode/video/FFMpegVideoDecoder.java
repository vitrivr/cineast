package org.vitrivr.cineast.core.decode.video;

import static org.bytedeco.javacpp.avcodec.*;
import static org.bytedeco.javacpp.avformat.av_find_best_stream;
import static org.bytedeco.javacpp.avformat.av_read_frame;
import static org.bytedeco.javacpp.avformat.av_register_all;
import static org.bytedeco.javacpp.avformat.avformat_alloc_context;
import static org.bytedeco.javacpp.avformat.avformat_close_input;
import static org.bytedeco.javacpp.avformat.avformat_find_stream_info;
import static org.bytedeco.javacpp.avformat.avformat_open_input;
import static org.bytedeco.javacpp.avutil.*;
import static org.bytedeco.javacpp.swresample.swr_alloc_set_opts;
import static org.bytedeco.javacpp.swresample.swr_convert;
import static org.bytedeco.javacpp.swresample.swr_free;
import static org.bytedeco.javacpp.swresample.swr_get_out_samples;
import static org.bytedeco.javacpp.swresample.swr_init;
import static org.bytedeco.javacpp.swscale.*;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bytedeco.javacpp.*;
import org.bytedeco.javacpp.avcodec.AVCodec;
import org.bytedeco.javacpp.avformat.AVFormatContext;
import org.bytedeco.javacpp.avutil.AVDictionary;
import org.bytedeco.javacpp.avutil.AVRational;
import org.vitrivr.cineast.core.config.DecoderConfig;
import org.vitrivr.cineast.core.data.MultiImageFactory;
import org.vitrivr.cineast.core.data.frames.AudioDescriptor;
import org.vitrivr.cineast.core.data.frames.AudioFrame;
import org.vitrivr.cineast.core.data.frames.VideoDescriptor;
import org.vitrivr.cineast.core.data.frames.VideoFrame;
import org.vitrivr.cineast.core.decode.general.Decoder;
import org.vitrivr.cineast.core.decode.subtitle.SubTitleDecoder;
import org.vitrivr.cineast.core.decode.subtitle.SubtitleItem;
import org.vitrivr.cineast.core.decode.subtitle.SubtitleDecoderFactory;
import org.vitrivr.cineast.core.util.LogHelper;

/**
 * @author rgasser
 * @version 1.0
 * @created 17.01.17
 */
public class FFMpegVideoDecoder implements Decoder<VideoFrame> {
    /** Configuration property name for the {@link FFMpegVideoDecoder}: max width of the converted video. */
    private static final String CONFIG_MAXWIDTH_PROPERTY = "maxFrameWidth";

    /** Configuration property name for the {@link FFMpegVideoDecoder}: max height of the converted video. */
    private static final String CONFIG_HEIGHT_PROPERTY = "maxFrameHeight";

    /** Configuration property name for the {@link FFMpegVideoDecoder}: number of channels of the converted audio. If <= 0, then no audio will be decoded. */
    private static final String CONFIG_CHANNELS_PROPERTY = "channels";

    /** Configuration property name for the {@link FFMpegVideoDecoder}: samplerate of the converted audio. */
    private static final String CONFIG_SAMPLERATE_PROPERTY = "samplerate";

    /** Configuration property name for the {@link FFMpegVideoDecoder}: Indicates whether subtitles should be decoded as well. */
    private static final String CONFIG_SUBTITLE_PROPERTY= "subtitles";

    /** Configuration property default for the FFMpegVideoDecoder: max width of the converted video. */
    private final static int CONFIG_MAXWIDTH_DEFAULT = 640;

    /** Configuration property default for the FFMpegVideoDecoder: max height of the converted video. */
    private final static int CONFIG_MAXHEIGHT_DEFAULT = 480;

    /** Configuration property default for the FFMpegVideoDecoder: number of channels of the converted audio. */
    private static final int CONFIG_CHANNELS_DEFAULT = 1;

    /** Configuration property default for the FFMpegVideoDecoder: samplerate of the converted audio */
    private static final int CONFIG_SAMPLERATE_DEFAULT = 44100;


    private static final int TARGET_FORMAT = AV_SAMPLE_FMT_S16;
    private static final int BYTES_PER_SAMPLE = av_get_bytes_per_sample(TARGET_FORMAT);

    private static final Logger LOGGER = LogManager.getLogger();
    
    /** Lists the mimetypes supported by the FFMpegVideoDecoder.
     *
     * TODO: List may not be complete yet. */
    public static final Set<String> supportedFiles;
    static {
        HashSet<String> tmp = new HashSet<>();
        tmp.add("multimedia/mp4"); /* They share the same suffix with video (.mp4). */
        tmp.add("video/mp4");
        tmp.add("video/avi");
        tmp.add("video/mpeg");
        tmp.add("video/quicktime");
        tmp.add("video/webm");
        supportedFiles = Collections.unmodifiableSet(tmp);
    }

    private byte[] bytes;
    private int[] pixels;

    /** Internal data structure used to hold decoded VideoFrames and the associated timestamp. */
    private ArrayDeque<VideoFrame> videoFrameQueue = new ArrayDeque<>();

    /** Internal data structure used to hold decoded AudioFrames and the associated timestamp. */
    private ArrayDeque<AudioFrame> audioFrameQueue = new ArrayDeque<>();

    private AVFormatContext pFormatCtx;

    private int             videoStream = -1;
    private int             audioStream = -1;
    private avcodec.AVCodecContext pCodecCtxVideo = null;
    private avcodec.AVCodecContext pCodecCtxAudio = null;

    /** Field for raw frame as returned by decoder (regardless of being audio or video). */
    private avutil.AVFrame pFrame = null;

    /** Field for RGB frame (decoded video frame). */
    private avutil.AVFrame pFrameRGB = null;

    /** Field for re-sampled audio-sample. */
    private avutil.AVFrame resampledFrame = null;

    private avcodec.AVPacket packet;
    private BytePointer buffer = null;

    private IntPointer out_linesize = new IntPointer();

    private swscale.SwsContext sws_ctx = null;
    private swresample.SwrContext swr_ctx = null;

    private VideoDescriptor videoDescriptor = null;
    private AudioDescriptor audioDescriptor = null;
    private SubTitleDecoder subtitles = null;

    /** Indicates that decoding of video-data is complete. */
    private final AtomicBoolean videoComplete = new AtomicBoolean(false);

    /** Indicates that decoding of video-data is complete. */
    private final AtomicBoolean audioComplete = new AtomicBoolean(false);

    /** Indicates the EOF has been reached during decoding. */
    private final AtomicBoolean eof = new AtomicBoolean(false);

    /**
     *
     * @param queue
     * @return
     */
    private boolean readFrame(boolean queue) {
        /* Tries to read a new packet from the stream. */
        int read_results = av_read_frame(this.pFormatCtx, this.packet);
        if (read_results < 0 && !(read_results == AVERROR_EOF)) {
            LOGGER.error("Error occurred while reading packet. FFMPEG av_read_frame() returned code {}.", read_results);
            av_packet_unref(this.packet);
            return false;
        } else if (read_results == AVERROR_EOF && this.eof.getAndSet(true)) {
            return false;
        }

        /* Two cases: Audio or video stream!. */
        if (packet.stream_index() == this.videoStream) {
            /* Send packet to decoder. If no packet was read, send null to flush the buffer. */
            int decode_results = avcodec_send_packet(this.pCodecCtxVideo, this.eof.get() ? null : this.packet);
            if (decode_results < 0) {
                LOGGER.error("Error occurred while decoding frames from packet. FFMPEG avcodec_send_packet() returned code {}.", decode_results);
                av_packet_unref(this.packet);
                return true;
            }

            /*
             * Because a packet can theoretically contain more than one frame; repeat decoding until no samples are
             * remaining in the packet.
             */
            while (avcodec_receive_frame(this.pCodecCtxVideo, this.pFrame) == 0) {
              /* If queue is true; enqueue frame. */
                if (queue) {
                    readVideo();
                }
            }
        } else if (packet.stream_index() == this.audioStream) {
            /* Send packet to decoder. If no packet was read, send null to flush the buffer. */
            int decode_results = avcodec_send_packet(this.pCodecCtxAudio, this.eof.get() ? null : this.packet);
            if (decode_results < 0) {
                LOGGER.error("Error occurred while decoding frames from packet. FFMPEG avcodec_send_packet() returned code {}.", decode_results);
                av_packet_unref(this.packet);
                return true;
            }

            /*
             * Because a packet can theoretically contain more than one frame; repeat decoding until no samples are remaining in the packet.
             */
            while (avcodec_receive_frame(this.pCodecCtxAudio, this.pFrame) == 0) {
              /* If queue is true; enqueue frame. */
                if (queue) {
                    if (this.swr_ctx != null) {
                        this.enqueueResampledAudio();
                    } else {
                        this.enqueueOriginalAudio();
                    }
                }
            }
        }

        /* Free the packet that was allocated by av_read_frame. */
        av_packet_unref(this.packet);

        return true;
    }

    /**
     * Returns the timestamp in milliseconds of the currently active frame. That timestamp is based on
     * a best-effort calculation by the FFMPEG decoder.
     *
     * @param stream Number of the stream. Determines the time base used to calculate the timestamp;
     * @return Timestamp of the current frame.
     */
    private Long getFrameTimestamp(int stream) {
        AVRational timebase = this.pFormatCtx.streams(stream).time_base();
        return Math.floorDiv((this.pFrame.best_effort_timestamp() * timebase.num() * 1000), timebase.den());
    }

    /**
     * Reads the decoded audio frames copies them directly into the AudioFrame data-structure which is subsequently
     * enqueued.
     */
    private void enqueueOriginalAudio() {
        /* Allocate output buffer... */
        int buffersize = this.pFrame.nb_samples() * av_get_bytes_per_sample(this.pFrame.format()) * this.pFrame.channels();
        byte[] buffer = new byte[buffersize];
        this.pFrame.data(0).position(0).get(buffer);

        /* Prepare frame and associated timestamp and add it to output queue. */
        AudioFrame frame = new AudioFrame(this.pCodecCtxAudio.frame_number(), this.getFrameTimestamp(this.audioStream), buffer, this.audioDescriptor);
        this.audioFrameQueue.add(frame);
    }


    /**
     * Reads the decoded audio frames and re-samples them using the SWR-CTX. The re-sampled frames are then
     * copied into the AudioFrame data-structure, which is subsequently enqueued.
     */
    private void enqueueResampledAudio() {
         /* Convert decoded frame. Break if resampling fails.*/
        if (swr_convert(this.swr_ctx, null, 0, this.pFrame.data(), this.pFrame.nb_samples()) < 0) {
            LOGGER.error("Could not convert sample (FFMPEG swr_convert() failed).", this.getClass().getName());
            return;
        }

        /* Prepare ByteOutputStream to write resampled data to. */
        ByteArrayOutputStream stream = new ByteArrayOutputStream();

        /* Now drain buffer and write samples to queue. */
        while(true) {
            /* Estimate number of samples that were converted. */
            int out_samples = swr_get_out_samples(this.swr_ctx, 0);
            if (out_samples < BYTES_PER_SAMPLE * this.resampledFrame.channels()) {
              break;
            }

            /* Allocate output frame and read converted samples. If no sample was read -> break (draining completed). */
            av_samples_alloc(this.resampledFrame.data(), out_linesize, this.resampledFrame.channels(), out_samples, TARGET_FORMAT, 0);
            out_samples = swr_convert(this.swr_ctx, this.resampledFrame.data(), out_samples, null, 0);
            if (out_samples == 0) {
              break;
            }

             /* Allocate output buffer... */
            int buffersize = out_samples * BYTES_PER_SAMPLE * this.resampledFrame.channels();
            byte[] buffer = new byte[buffersize];
            this.resampledFrame.data(0).position(0).get(buffer);
            try {
                stream.write(buffer);
            } catch (IOException e) {
                LOGGER.error("Could not write re-sampled frame to ByteArrayOutputStream due to an exception ({}).", LogHelper.getStackTrace(e));
                break;
            }
        }

        /* Prepare frame and associated timestamp and add it to output queue. */
        AudioFrame frame = new AudioFrame(this.pCodecCtxAudio.frame_number(), this.getFrameTimestamp(this.audioStream), stream.toByteArray(), this.audioDescriptor);
        this.audioFrameQueue.add(frame);
    }

    /**
     * Reads the decoded video frames and re-sizes them. The re-sized video frame is then copied into a VideoFrame data
     * structure, which is subsequently enqueued.
     */
    private void readVideo() {
        // Convert the image from its native format to RGB
        sws_scale(this.sws_ctx, this.pFrame.data(), this.pFrame.linesize(), 0, this.pCodecCtxVideo.height(), this.pFrameRGB.data(), this.pFrameRGB.linesize());

        // Write pixel data
        BytePointer data = this.pFrameRGB.data(0);

        data.position(0).get(bytes);

        for (int i = 0; i < pixels.length; ++i) {
            int pos = 3 * i;
            int r = bytes[pos] & 0xff;
            int g = bytes[pos + 1] & 0xff;
            int b = bytes[pos + 2] & 0xff;

            pixels[i] = ((0xFF) << 24) | ((r & 0xFF) << 16) | ((g & 0xFF) << 8) | ((b & 0xFF) << 0);
        }

        /* Prepare frame and associated timestamp and add it to output queue. */
        VideoFrame videoFrame = new VideoFrame(this.pCodecCtxVideo.frame_number(), this.getFrameTimestamp(this.videoStream), MultiImageFactory.newMultiImage(this.videoDescriptor.getWidth(), this.videoDescriptor.getHeight(), pixels), this.videoDescriptor);
        this.videoFrameQueue.add(videoFrame);
    }

    /**
     * Closes the FFMpegVideoDecoder and frees all (non-native) resources associated with it.
     */
    @Override
    public void close() {
        if (this.pFormatCtx == null) {
            return;
        }

        /* Free the raw frame. */
        if (this.pFrame != null) {
            av_frame_free(this.pFrame);
            this.pFrame = null;
        }

        /* Free the frame holding re-sampled audio. */
        if (this.resampledFrame != null) {
            av_frame_free(this.resampledFrame);
            this.resampledFrame = null;
        }

         /* Free the frame holding re-sized video. */
        if (this.pFrameRGB != null) {
            av_frame_free(this.pFrameRGB);
            this.pFrameRGB = null;
        }

        /* Free the packet. */
        if (this.packet != null) {
            av_packet_free(this.packet);
            this.packet.close();
            this.packet = null;
        }

        /* Frees the SWR context. */
        if (this.swr_ctx != null) {
            swr_free(this.swr_ctx);
            this.swr_ctx = null;
        }

        /* Frees the SWS context. */
        if (this.sws_ctx != null) {
            sws_freeContext(this.sws_ctx);
            this.sws_ctx = null;
        }

        /* Closes the audio codec context. */
        if (this.pCodecCtxAudio != null) {
            avcodec_free_context(this.pCodecCtxAudio);
            this.pCodecCtxAudio = null;
        }

        /* Closes the audio codec context. */
        if (this.pCodecCtxVideo != null) {
            avcodec_free_context(this.pCodecCtxVideo);
            this.pCodecCtxVideo = null;
        }

        /* Frees the ByteBuffer used to resize images. */
        if (this.buffer != null) {
            av_free(this.buffer);
            this.buffer = null;
        }

        /* Close the audio file context. */
        avformat_close_input(this.pFormatCtx);
        this.pFormatCtx = null;
    }

    /**
     * Initializes the decoder with a file. This is a necessary step before content can be retrieved from
     * the decoder by means of the getNext() method.
     *
     * @param path Path to the file that should be decoded.
     * @param config DecoderConfiguration used by the decoder.
     * @return True if initialization was successful, false otherwise.
     */
    @Override
    public boolean init(Path path, DecoderConfig config) {
        if(!Files.exists(path)){
            LOGGER.error("File does not exist {}", path.toString());
            return false;
        }

        /* Initialize the AVFormatContext. */
        this.pFormatCtx = avformat_alloc_context();

        /* Register all formats and codecs. */
        av_register_all();

        /* Open video file. */
        if (avformat_open_input(this.pFormatCtx, path.toString(), null, null) != 0) {
            LOGGER.error("Error while accessing file {}", path.toString());
            return false;
        }

        /* Retrieve stream information. */
        if (avformat_find_stream_info(pFormatCtx, (PointerPointer<?>)null) < 0) {
            LOGGER.error("Error, Couldn't find stream information");
            return false;
        }

        /* Initialize the packet. */
        this.packet = av_packet_alloc();
        if (this.packet == null) {
            LOGGER.error("Could not allocate packet data structure for decoded data.");
            return false;
        }

        /* Allocate frame that holds decoded frame information. */
        this.pFrame = av_frame_alloc();
        if (this.pFrame == null) {
            LOGGER.error("Could not allocate frame data structure for decoded data.");
            return false;
        }

        /* Open subtitles file (if configured). */
        if (config.namedAsBoolean(CONFIG_SUBTITLE_PROPERTY, false)) {
            final Optional<SubTitleDecoder> subtitles = SubtitleDecoderFactory.subtitleForVideo(path);
            subtitles.ifPresent(subTitleDecoder -> this.subtitles = subTitleDecoder);
        }

        if (this.initVideo(config) && this.initAudio(config)) {
            LOGGER.debug("{} was initialized successfully.", this.getClass().getName());
            return true;
        } else {
            return false;
        }
    }

    /**
     * Initializes the video decoding part of FFMPEG.
     *
     * @param config The {@link DecoderConfig} used for configuring the {@link FFMpegVideoDecoder}.
     * @return True if vide decoder was initialized, false otherwise.
     */
    private boolean initVideo(DecoderConfig config) {
        /* Read decoder config (VIDEO). */
        int maxWidth = config.namedAsInt(CONFIG_MAXWIDTH_PROPERTY, CONFIG_MAXWIDTH_DEFAULT);
        int maxHeight = config.namedAsInt(CONFIG_HEIGHT_PROPERTY, CONFIG_MAXHEIGHT_DEFAULT);

        /* Find the best video stream. */
        final AVCodec codec = av_codec_next((AVCodec)null);
        this.videoStream = av_find_best_stream(this.pFormatCtx,AVMEDIA_TYPE_VIDEO,-1, -1, codec, 0);
        if (this.videoStream == -1) {
            LOGGER.error("Couldn't find a video stream.");
            return false;
        }

        /* Allocate new codec-context for codec returned by av_find_best_stream(). */
        this.pCodecCtxVideo = avcodec_alloc_context3(codec);
        avcodec_parameters_to_context(this.pCodecCtxVideo, this.pFormatCtx.streams(this.videoStream).codecpar());
        /* Open the code context. */
        if (avcodec_open2(this.pCodecCtxVideo, codec, (AVDictionary)null) < 0) {
            LOGGER.error("Error, Could not open video codec.");
            return false;
        }

        /* Allocate an AVFrame structure that will hold the resized video. */
        this.pFrameRGB = av_frame_alloc();
        if(pFrameRGB == null) {
            LOGGER.error("Error. Could not allocate frame for resized video.");
            return false;
        }

        int originalWidth = pCodecCtxVideo.width();
        int originalHeight = pCodecCtxVideo.height();
        int width = originalWidth;
        int height = originalHeight;

        if (originalWidth > maxWidth || originalHeight > maxHeight) {
            float scaleDown = Math.min((float) maxWidth / (float) originalWidth, (float) maxHeight / (float) originalHeight);
            width = Math.round(originalWidth * scaleDown);
            height = Math.round(originalHeight * scaleDown);
            LOGGER.debug("scaling input video down by a factor of {} from {}x{} to {}x{}", scaleDown, originalWidth, originalHeight, width, height);
        }

        bytes = new byte[width * height * 3];
        pixels = new int[width * height];

        /* Initialize data-structures used for resized image. */
        int numBytes = av_image_get_buffer_size(AV_PIX_FMT_RGB24,  pCodecCtxVideo.width(), pCodecCtxVideo.height(), 1);
        this.buffer = new BytePointer(av_malloc(numBytes));
        av_image_fill_arrays(this.pFrameRGB.data(), this.pFrameRGB.linesize(), this.buffer, AV_PIX_FMT_RGB24, width, height, 1);

        /* Initialize SWS Context. */
        this.sws_ctx = sws_getContext(this.pCodecCtxVideo.width(), this.pCodecCtxVideo.height(), this.pCodecCtxVideo.pix_fmt(), width, height, AV_PIX_FMT_RGB24, SWS_BILINEAR, null, null, (DoublePointer)null);

        /* Initialize VideoDescriptor. */
        AVRational timebase = this.pFormatCtx.streams(this.videoStream).time_base();
        long duration = (1000L * timebase.num() * this.pFormatCtx.streams(this.videoStream).duration()/timebase.den());
        AVRational framerate = this.pFormatCtx.streams(this.videoStream).avg_frame_rate();
        float fps = ((float) framerate.num()) / ((float)framerate.den());
        this.videoDescriptor = new VideoDescriptor(fps, duration, width, height);

        /* Return true (success). */
        return true;
    }

    /**
     * Initializes the audio decoding part of FFMPEG.
     *
     * @param config The {@link DecoderConfig} used for configuring the {@link FFMpegVideoDecoder}.
     * @return True if a) audio decoder was initialized, b) number of channels is smaller than zero (no audio) or c) audio is unavailable or unsupported, false if initialization failed due to technical reasons.
     */
    private boolean initAudio(DecoderConfig config) {
        /* Read decoder configuration. */
        int samplerate = config.namedAsInt(CONFIG_SAMPLERATE_PROPERTY, CONFIG_SAMPLERATE_DEFAULT);
        int channels = config.namedAsInt(CONFIG_CHANNELS_PROPERTY, CONFIG_CHANNELS_DEFAULT);
        long channellayout = av_get_default_channel_layout(channels);

        /* If number of channels is smaller or equal than zero; return true (no audio decoded). */
        if (channels <= 0) {
            LOGGER.info("Channel setting is smaller than zero. Continuing without audio!");
            this.audioComplete.set(true);
            return true;
        }

        /* Find the best frames stream. */
        final AVCodec codec = av_codec_next((AVCodec)null);
        this.audioStream = av_find_best_stream(this.pFormatCtx, AVMEDIA_TYPE_AUDIO,-1, -1, codec, 0);
        if (this.audioStream < 0) {
            LOGGER.warn("Couldn't find a supported audio stream. Continuing without audio!");
            this.audioComplete.set(true);
            return true;
        }

        /* Allocate new codec-context. */
        this.pCodecCtxAudio = avcodec_alloc_context3(codec);
        avcodec_parameters_to_context(this.pCodecCtxAudio, this.pFormatCtx.streams(this.audioStream).codecpar());

        /* Open the code context. */
        if (avcodec_open2(this.pCodecCtxAudio, codec, (AVDictionary)null) < 0) {
            LOGGER.error("Could not open audio codec. Continuing without audio!");
            this.audioComplete.set(true);
            return true;
        }

        /* Allocate the re-sample context. */
        this.swr_ctx = swr_alloc_set_opts(null, channellayout, TARGET_FORMAT, samplerate, this.pCodecCtxAudio.channel_layout(), this.pCodecCtxAudio.sample_fmt(), this.pCodecCtxAudio.sample_rate(), 0, null);
        if(swr_init(this.swr_ctx) < 0) {
            this.swr_ctx = null;
            LOGGER.warn("Could not open re-sample context - original format will be kept!");
        }

        /* Initialize decoded and resampled frame. */
        this.resampledFrame = av_frame_alloc();
        if (this.resampledFrame == null) {
            LOGGER.error("Could not allocate frame data structure for re-sampled data.");
            return false;
        }

        /* Initialize out-frame. */
        this.resampledFrame = av_frame_alloc();
        this.resampledFrame.channel_layout(channellayout);
        this.resampledFrame.sample_rate(samplerate);
        this.resampledFrame.channels(channels);
        this.resampledFrame.format(TARGET_FORMAT);

        /* Initialize the AudioDescriptor. */
        final AVRational timebase = this.pFormatCtx.streams(this.audioStream).time_base();
        final long duration = (1000L * timebase.num() * this.pFormatCtx.streams(this.audioStream).duration()/timebase.den());
        if (this.swr_ctx == null) {
            this.audioDescriptor = new AudioDescriptor(this.pCodecCtxAudio.sample_rate(), this.pCodecCtxAudio.channels(), duration);
        } else {
            this.audioDescriptor = new AudioDescriptor(this.resampledFrame.sample_rate(), this.resampledFrame.channels(), duration);
        }

        /* Completed initialization. */
        return true;
    }


    /**
     * Fetches the next piece of content of type T and returns it. This method can be safely invoked until
     * complete() returns false. From which on this method will return null.
     *
     * @return Content of type T.
     */
    @Override
    public VideoFrame getNext() {
        /* Read frames until a video-frame becomes available. */
        while (this.videoFrameQueue.isEmpty() && !this.eof.get()) {
            this.readFrame(true);
        }

        /* If frame-queue is empty and EOF has been reached, then video decoding was completed. */
        if (this.videoFrameQueue.isEmpty()) {
            this.videoComplete.set(true);
            return null;
        }

        /* Fetch that video frame. */
        final VideoFrame videoFrame = this.videoFrameQueue.poll();

        /*
         * Now if the audio stream is set, read AudioFrames until the timestamp of the next AudioFrame in the
         * queue becomes greater than the timestamp of the VideoFrame. All these AudioFrames go to the VideoFrame.
         */
        while (!this.audioComplete.get()) {
            while (this.audioFrameQueue.isEmpty() && !this.eof.get()) {
                this.readFrame(true);
            }

            if (this.audioFrameQueue.isEmpty()) {
                this.audioComplete.set(true);
                break;
            }

            AudioFrame audioFrame = this.audioFrameQueue.peek();
            if (audioFrame.getTimestamp() <= videoFrame.getTimestamp()) {
                videoFrame.addAudioFrame(this.audioFrameQueue.poll());
            } else {
                break;
            }
        }

        /*
         * Now, if there is a subtitle stream, try to append the corresponding subtitle.
         */
        if (this.subtitles != null) {
            while (true) {
                final SubtitleItem item = this.subtitles.getLast();
                if (videoFrame.getTimestamp() >= item.getStartTimestamp() && videoFrame.getTimestamp() <= item.getEndTimestamp()) {
                    videoFrame.addSubtitleItem(item);
                    break;
                } else if (videoFrame.getTimestamp() > item.getEndTimestamp()) {
                    if (!this.subtitles.increment()) break;
                } else {
                    break;
                }
            }
        }


        /* Return VideoFrame. */
        return videoFrame;
    }

    /**
     * Returns the total number of content pieces T this decoder can return for a given file.
     *
     * @return
     */
    @Override
    public int count() {
        return (int) this.pFormatCtx.streams(this.videoStream).nb_frames();
    }

    /**
     * Indicates whether or not the current decoder has more content to return or not.
     *
     * @return True if more content can be fetched, false otherwise.
     */
    @Override
    public boolean complete() {
        return this.videoComplete.get();
    }

    /**
     * Returns a set of the mime/types of supported files.
     *
     * @return Set of the mime-type of file formats that are supported by the current Decoder instance.
     */
    @Override
    public Set<String> supportedFiles() {
        return supportedFiles;
    }

    /**
     * Closes the {@link FFMpegVideoDecoder} if this hasn't happened yet.
     *
     * @throws Throwable On error
     */
    @Override
    protected void finalize() throws Throwable {
        this.close();
        super.finalize();
    }
}
