package org.vitrivr.cineast.core.decode.video;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bytedeco.javacpp.*;
import org.vitrivr.cineast.core.config.DecoderConfig;
import org.vitrivr.cineast.core.data.frames.VideoFrame;
import org.vitrivr.cineast.core.data.MultiImageFactory;
import org.vitrivr.cineast.core.data.Pair;
import org.vitrivr.cineast.core.data.frames.AudioFrame;
import org.vitrivr.cineast.core.decode.general.Decoder;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.bytedeco.javacpp.avcodec.*;
import static org.bytedeco.javacpp.avcodec.av_packet_unref;
import static org.bytedeco.javacpp.avcodec.avcodec_close;
import static org.bytedeco.javacpp.avformat.*;
import static org.bytedeco.javacpp.avformat.avformat_close_input;
import static org.bytedeco.javacpp.avutil.*;
import static org.bytedeco.javacpp.avutil.av_free;
import static org.bytedeco.javacpp.swresample.*;
import static org.bytedeco.javacpp.swresample.swr_convert;
import static org.bytedeco.javacpp.swresample.swr_get_out_samples;
import static org.bytedeco.javacpp.swscale.SWS_BILINEAR;
import static org.bytedeco.javacpp.swscale.sws_getContext;
import static org.bytedeco.javacpp.swscale.sws_scale;

import org.bytedeco.javacpp.avformat.AVFormatContext;
import org.vitrivr.cineast.core.util.LogHelper;

/**
 * @author rgasser
 * @version 1.0
 * @created 17.01.17
 */
@SuppressWarnings("deprecation") //some of the new API replacing the deprecated one appears to be not yet available which is why the deprecated one has to be used.
public class FFMpegVideoDecoder implements Decoder<VideoFrame> {


    /* Configuration property-names and defaults for the DefaultImageDecoder. */
    private static final String CONFIG_MAXWIDTH_PROPERTY = "maxFrameWidth";
    private static final String CONFIG_HEIGHT_PROPERTY = "maxFrameHeight";
    private final static int CONFIG_MAXWIDTH_DEFAULT = 640;
    private final static int CONFIG_MAXHEIGHT_DEFAULT = 480;
    private static final String CONFIG_CHANNELS_PROPERTY = "channels";
    private static final int CONFIG_CHANNELS_DEFAULT = 1;
    private static final String CONFIG_SAMPLERATE_PROPERTY = "samplerate";
    private static final int CONFIG_SAMPLERATE_DEFAULT = 44100;


    private static final int TARGET_FORMAT = AV_SAMPLE_FMT_S16;
    private static final int BYTES_PER_SAMPLE = av_get_bytes_per_sample(TARGET_FORMAT);

    private static final Logger LOGGER = LogManager.getLogger();
    
    /** Lists the mimetypes supported by the FFMpegAudioDecoder. Hint: List may not be complete yet. */
    private static final Set<String> supportedFiles;
    static {
        HashSet<String> tmp = new HashSet<>();
        tmp.add("video/mp4");
        tmp.add("video/mpeg");
        tmp.add("video/x-msvideo"); //avi
        tmp.add("video/x-matroska");
        tmp.add("video/webm");
        supportedFiles = Collections.unmodifiableSet(tmp);
    }

    private int width;
    private int height;

    private double fps;
    private byte[] bytes;
    private int[] pixels;

    private ArrayDeque<Pair<Long,VideoFrame>> videoFrameQueue = new ArrayDeque<>();
    private ArrayDeque<Pair<Long,AudioFrame>> audioFrameQueue = new ArrayDeque<>();

    private AVFormatContext pFormatCtx;
    private int             videoStream = -1;
    private int             audioStream = -1;
    private avcodec.AVCodecContext pCodecCtxVideo = null;
    private avcodec.AVCodecContext pCodecCtxAudio = null;

    private avcodec.AVCodec pCodecVideo = new AVCodec();
    private avcodec.AVCodec pCodecAudio = new AVCodec();

    /** Field for raw frame as returned by decoder (regardless of being audio or video). */
    private avutil.AVFrame pFrame = null;

    /** Field for RGB frame (decoded video frame). */
    private avutil.AVFrame pFrameRGB = null;

    /** Field for re-sampled audio-sample. */
    private avutil.AVFrame resampledFrame = null;

    private avcodec.AVPacket packet = new avcodec.AVPacket();
    private int[]           frameFinished = new int[1];
    private BytePointer buffer = null;

    private avutil.AVDictionary optionsDict = null;

    private IntPointer out_linesize = new IntPointer();

    private swscale.SwsContext sws_ctx = null;
    private swresample.SwrContext swr_ctx = null;

    /** Indicates that decoding of audio-data is complete. */
    private final AtomicBoolean audioComplete = new AtomicBoolean(false);

    /** Indicates that decoding of video-data is complete. */
    private final AtomicBoolean videoComplete = new AtomicBoolean(false);

    private boolean readFrame(boolean queue) {
        boolean readFrame = false;
        while (!readFrame && av_read_frame(pFormatCtx, packet) >= 0) {
            // Is this a packet from the video stream?
            if (packet.stream_index() == this.videoStream) {
                // Decode video frame
                avcodec_decode_video2(this.pCodecCtxVideo, this.pFrame, this.frameFinished, this.packet);

                // Did we get a video frame?
                if (frameFinished[0] != 0) {
                    if (queue) {
                        queueFrame();
                    }
                    readFrame = true;
                }
            } else if (packet.stream_index() == this.audioStream) {
                /* Counter: Size of packet (frame). */
                int remaining = packet.size();

                /*
                 * Because a packet can theoretically contain more than one frame; repeat decoding until
                 * no samples are remaining in the packet.
                 */
                while (remaining > 0) {
                    int result = avcodec_decode_audio4(this.pCodecCtxAudio, this.pFrame, this.frameFinished, this.packet);
                    if (result < 0) {
                        LOGGER.error("Error occurred while decoding frames. FFMPEG avcodec_decode_audio4() returned code {}.", result);
                        break;
                    }
                    remaining -= result;
                }

                if (frameFinished[0] > 0) {
                    /* If queue is true; enqueue frame. */
                    if (queue) {
                        if (this.swr_ctx != null) {
                            this.readResampled(this.pFrame.nb_samples());
                        } else {
                            this.readOriginal(this.pFrame.nb_samples());
                        }
                    }
                    readFrame = true;
                }
            }

            /* Free the packet that was allocated by av_read_frame. */
            av_packet_unref(packet);
        }
        return readFrame;
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
        return (long)Math.floor((this.pFrame.best_effort_timestamp() * (float)timebase.num() * 1000)/(float)timebase.den());
    }

    /**
     * Reads the decoded frames copies them directly into the AudioFrame data-structure.
     *
     * @param samples Number of samples returned by the decoder.
     */
    private void readOriginal(int samples) {
        /* Allocate output buffer... */
        int buffersize = samples * av_get_bytes_per_sample(this.pFrame.format()) * this.pFrame.channels();
        byte[] buffer = new byte[buffersize];
        this.pFrame.data(0).position(0).get(buffer);

        /* Prepare frame and associated timestamp and add it to output queue. */
        Long timestamp = this.getFrameTimestamp(this.audioStream);
        AudioFrame frame = new AudioFrame(this.pCodecCtxAudio.frame_number(), timestamp, this.pFrame.sample_rate(), this.pFrame.channels(), buffer);
        this.audioFrameQueue.add(new Pair<>(timestamp, frame));
    }


    /**
     * Reads the decoded frames and resamples them using the SWR-CTX. The resampled frames are then
     * read to the AudioFrame data-structure.
     *
     * @param samples Number of samples returned by the decoder.
     */
    private void readResampled(int samples) {
         /* Convert decoded frame. Break if resampling fails.*/
        if (swr_convert(this.swr_ctx, null, 0, this.pFrame.data(), samples) < 0) {
            LOGGER.error("Could not convert sample (FFMPEG swr_convert() failed).", this.getClass().getName());
            return;
        }

        /* Prepare ByteOutputStream to write resampled data to. */
        ByteArrayOutputStream stream = new ByteArrayOutputStream();

        /* Now drain buffer and write samples to queue. */
        while(true) {
            /* Estimate number of samples that were converted. */
            int out_samples = swr_get_out_samples(this.swr_ctx, 0);
            if (out_samples < BYTES_PER_SAMPLE * this.resampledFrame.channels()) break;

            /* Allocate output frame and read converted samples. If no sample was read -> break (draining completed). */
            av_samples_alloc(this.resampledFrame.data(), out_linesize, this.resampledFrame.channels(), out_samples, TARGET_FORMAT, 0);
            out_samples = swr_convert(this.swr_ctx, this.resampledFrame.data(), out_samples, null, 0);
            if (out_samples == 0) break;

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
        Long timestamp = this.getFrameTimestamp(this.audioStream);
        AudioFrame frame = new AudioFrame(this.pCodecCtxAudio.frame_number(), timestamp, this.resampledFrame.sample_rate(), this.resampledFrame.channels(), stream.toByteArray());
        this.audioFrameQueue.add(new Pair<>(timestamp, frame));
    }

    /**
     *
     * @return
     */
    private void queueFrame() {
        // Convert the image from its native format to RGB
        sws_scale(sws_ctx, pFrame.data(), pFrame.linesize(), 0, this.pCodecCtxVideo.height(), this.pFrameRGB.data(),
                pFrameRGB.linesize());

        // Write pixel data
        BytePointer data = pFrameRGB.data(0);

        data.position(0).get(bytes);

        for (int i = 0; i < pixels.length; ++i) {
            int pos = 3 * i;
            int r = bytes[pos] & 0xff;
            int g = bytes[pos + 1] & 0xff;
            int b = bytes[pos + 2] & 0xff;

            pixels[i] = ((0xFF) << 24) | ((r & 0xFF) << 16) | ((g & 0xFF) << 8) | ((b & 0xFF) << 0);
        }

        /* Prepare frame and associated timestamp and add it to output queue. */
        VideoFrame videoFrame = new VideoFrame(this.pCodecCtxVideo.frame_number(), MultiImageFactory.newMultiImage(this.width, this.height, pixels));
        Long timestamp = this.getFrameTimestamp(this.videoStream);
        this.videoFrameQueue.add(new Pair<>(timestamp, videoFrame));
    }

    @Override
    public void close() {
        if(pFormatCtx != null) return;

        // Free the RGB image
        av_free(this.buffer);
        av_free(this.pFrameRGB);

        // Free the raw frame
        av_free(this.pFrame);

        /* Free AudioFrames. */
        av_free(this.resampledFrame);

        /* Frees the SWR context. */
        if (this.swr_ctx != null) {
            swr_free(this.swr_ctx);
            this.swr_ctx = null;
        }

        /* Close the codecs. */
        if (this.pCodecCtxVideo != null) {
            avcodec_close(this.pCodecCtxVideo);
            this.pCodecCtxVideo = null;
        }
        if (this.pCodecCtxAudio != null) {
            avcodec_close(this.pCodecCtxAudio);
            this.pCodecCtxAudio = null;
        }

        /* Close the video file */
        avformat_close_input(this.pFormatCtx);
        this.pFormatCtx = null;
    }

    public int getWidth() {
        return this.width;
    }

    public int getHeight() {
        return this.height;
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
        /* Initialize the AVFormatContext. */
        this.pFormatCtx = new AVFormatContext(null);

        if(!Files.exists(path)){
            LOGGER.error("File does not exist {}", path.toString());
            return false;
        }

        // Register all formats and codecs
        av_register_all();

        // Open video file
        if (avformat_open_input(this.pFormatCtx, path.toString(), null, null) != 0) {
            LOGGER.error("Error while accessing file {}", path.toString());
            return false;
        }

        // Retrieve stream information
        if (avformat_find_stream_info(pFormatCtx, (PointerPointer<?>)null) < 0) {
            LOGGER.error("Error, Couldn't find stream information");
            return false;
        }


        if (this.initVideo(path, config) && this.initAudio(path, config)) {
            LOGGER.debug("FFMpegVideoDecoder successfully initialized");
            return true;
        } else {
            return false;
        }
    }

    /**
     *
     * @param path
     * @param config
     * @return
     */
    private boolean initVideo(Path path, DecoderConfig config) {
        /* Read decoder config (VIDEO). */
        int maxWidth = config.namedAsInt(CONFIG_MAXWIDTH_PROPERTY, CONFIG_MAXWIDTH_DEFAULT);
        int maxHeight = config.namedAsInt(CONFIG_HEIGHT_PROPERTY, CONFIG_MAXHEIGHT_DEFAULT);

        /* Find the best video stream. */
        this.videoStream = av_find_best_stream(this.pFormatCtx,AVMEDIA_TYPE_VIDEO,-1, -1, this.pCodecVideo, 0);
        if (this.videoStream == -1) {
            LOGGER.error("Couldn't find a video stream.");
            return false;
        }

        // Get a pointer to the codec context for the video stream
        this.pCodecCtxVideo = this.pFormatCtx.streams(videoStream).codec();


        avutil.AVRational framerate = this.pFormatCtx.streams(videoStream).avg_frame_rate();
        this.fps = ((double)framerate.num()) / ((double)framerate.den());

        // Open codec
        if (avcodec_open2(this.pCodecCtxVideo, this.pCodecVideo, optionsDict) < 0) {
            LOGGER.error("Error, Could not open video codec");
            return false;
        }

        // Allocate video frame
        pFrame = av_frame_alloc();

        // Allocate an AVFrame structure
        pFrameRGB = av_frame_alloc();
        if(pFrameRGB == null) {
            LOGGER.error("Error, Could not allocate frame");
            return false;
        }

        int originalWidth = pCodecCtxVideo.width();
        int originalHeight = pCodecCtxVideo.height();

        if (originalWidth > maxWidth || originalHeight > maxHeight) {
            float scaleDown = Math.min((float) maxWidth / (float) originalWidth, (float) maxHeight / (float) originalHeight);
            this.width = Math.round(originalWidth * scaleDown);
            this.height = Math.round(originalHeight * scaleDown);
            LOGGER.debug("scaling input video down by a factor of {} from {}x{} to {}x{}", scaleDown, originalWidth, originalHeight, this.width, this.height);
        } else{
            this.width = originalWidth;
            this.height = originalHeight;
        }

        bytes = new byte[width * height * 3];
        pixels = new int[width * height];

        // Determine required buffer size and allocate buffer
        int numBytes = av_image_get_buffer_size(AV_PIX_FMT_RGB24,  pCodecCtxVideo.width(), pCodecCtxVideo.height(), 1);
        buffer = new BytePointer(av_malloc(numBytes));

        this.sws_ctx = sws_getContext(pCodecCtxVideo.width(), pCodecCtxVideo.height(),
                pCodecCtxVideo.pix_fmt(), this.width, this.height,
                AV_PIX_FMT_RGB24, SWS_BILINEAR, null, null, (DoublePointer)null);

        // Assign appropriate parts of buffer to image planes in pFrameRGB
        // Note that pFrameRGB is an AVFrame, but AVFrame is a superset
        // of AVPicture
        avpicture_fill(new avcodec.AVPicture(pFrameRGB), buffer, AV_PIX_FMT_RGB24, this.width, this.height);
        return true;
    }


    /**
     *
     * @param path
     * @param config
     * @return
     */
    private boolean initAudio(Path path, DecoderConfig config) {
        /* Read decoder configuration. */
        int samplerate = config.namedAsInt(CONFIG_SAMPLERATE_PROPERTY, CONFIG_SAMPLERATE_DEFAULT);
        int channels = config.namedAsInt(CONFIG_CHANNELS_PROPERTY, CONFIG_CHANNELS_DEFAULT);
        long channellayout = av_get_default_channel_layout(channels);

        /* Find the best frames stream. */
        this.audioStream = av_find_best_stream(this.pFormatCtx, AVMEDIA_TYPE_AUDIO,-1, -1, this.pCodecAudio, 0);
        if (this.audioStream == -1) {
            LOGGER.warn("Couldn't find a supported audio stream. Continuing without audio!");
            this.audioComplete.set(true);
            return true;
        }

        /* Get a pointer to the codec context for the frames stream and open it. */
        this.pCodecCtxAudio = this.pFormatCtx.streams(this.audioStream).codec();
        if (avcodec_open2(this.pCodecCtxAudio, this.pCodecAudio, optionsDict) < 0) {
            LOGGER.error("Error, Could not open audio codec. Continuing without audio!");
            this.audioComplete.set(true);
            return true;
        }

        /* Allocate the re-sample context. */
        this.swr_ctx = swr_alloc_set_opts(null, channellayout, TARGET_FORMAT, samplerate, this.pCodecCtxAudio.channel_layout(), this.pCodecCtxAudio.sample_fmt(), this.pCodecCtxAudio.sample_rate(), 0, null);
        if(swr_init(this.swr_ctx) < 0) {
            this.swr_ctx = null;
            LOGGER.warn("Warning! Could not open re-sample context - original format will be kept!");
        }

        /* Initialize decoded and resampled frame. */
        this.resampledFrame = av_frame_alloc();

        /* Initialize out-frame. */
        this.resampledFrame = av_frame_alloc();
        this.resampledFrame.channel_layout(channellayout);
        this.resampledFrame.sample_rate(samplerate);
        this.resampledFrame.channels(channels);
        this.resampledFrame.format(TARGET_FORMAT);

        /* Completed initialization. */
        LOGGER.debug("{} was initialized successfully.", this.getClass().getName());
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
        while (this.videoFrameQueue.isEmpty() && !this.videoComplete.get()) {
            this.videoComplete.set(!this.readFrame(true));
        }

        /* Fetch that video frame. */
        Pair<Long, VideoFrame> frame = this.videoFrameQueue.poll();


        /* Now if the audio stream is set, read AudioFrames until the timestamp of the next AudioFrame in the
         * queue becomes greater than the timestamp of the VideoFrame. All these AudioFrames go to the VideoFrame.
         */
        while (this.audioStream > -1  && !this.audioComplete.get()) {
            if (this.audioFrameQueue.isEmpty()) {
                this.audioComplete.set(!this.readFrame(true));
            }

            Pair<Long,AudioFrame> audioFrame = this.audioFrameQueue.peek();
            if (audioFrame == null) continue;
            if (audioFrame.first <= frame.first) {
                frame.second.addAudioFrame(this.audioFrameQueue.poll().second);
            } else {
                break;
            }
        }

        /* Return VideoFrame. */
        return frame.second;
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
        return this.audioComplete.get() && this.videoComplete.get();
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
}
