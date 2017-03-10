package org.vitrivr.cineast.core.decode.audio;

import static org.bytedeco.javacpp.avcodec.*;

import static org.bytedeco.javacpp.avformat.*;
import static org.bytedeco.javacpp.avutil.*;

import static org.bytedeco.javacpp.swresample.*;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayDeque;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bytedeco.javacpp.IntPointer;
import org.bytedeco.javacpp.PointerPointer;

import org.bytedeco.javacpp.avcodec.AVCodec;
import org.bytedeco.javacpp.avcodec.AVCodecContext;
import org.bytedeco.javacpp.avcodec.AVPacket;

import org.bytedeco.javacpp.avformat.AVFormatContext;

import org.bytedeco.javacpp.avutil.AVDictionary;
import org.bytedeco.javacpp.avutil.AVFrame;

import org.vitrivr.cineast.core.config.DecoderConfig;
import org.vitrivr.cineast.core.data.frames.AudioFrame;
import org.vitrivr.cineast.core.decode.general.Decoder;
import org.vitrivr.cineast.core.util.LogHelper;

@SuppressWarnings("deprecation") //some of the new API replacing the deprecated one appears to be not yet available which is why the deprecated one has to be used.
public class FFMpegAudioDecoder implements AudioDecoder {

    private static final Logger LOGGER = LogManager.getLogger();

    /** Lists the mimetypes supported by the FFMpegAudioDecoder. Hint: List may not be complete yet. */
    private static final Set<String> supportedFiles;
    static {
        HashSet<String> tmp = new HashSet<>();
        tmp.add("audio/mp4");
        tmp.add("audio/aac");
        tmp.add("audio/mpeg");
        tmp.add("audio/ogg");
        tmp.add("audio/wav");
        tmp.add("audio/flac");
        supportedFiles = Collections.unmodifiableSet(tmp);
    }

    /** Property name and default value for channel settings. */
    private static final String CONFIG_CHANNELS_PROPERTY = "channels";
    private static final int CONFIG_CHANNELS_DEFAULT = 2;

    /** Property name and default value for target samplerate settings. */
    private static final String CONFIG_SAMPLERATE_PROPERTY = "samplerate";
    private static final int CONFIG_SAMPLERATE_DEFAULT = 44100;

    /** Default value for output sample format (16bit signed PCM). */
    private static final int TARGET_FORMAT = AV_SAMPLE_FMT_S16;
    private static final int BYTES_PER_SAMPLE = av_get_bytes_per_sample(TARGET_FORMAT);

    private ArrayDeque<AudioFrame> frameQueue = new ArrayDeque<>();

    private AVFormatContext pFormatCtx = null;
    private AVCodecContext  pCodecCtx = null;
    private int             audioStream = -1;

    private AVPacket        packet = new AVPacket();

    private AVFrame decodedFrame = null;
    private AVFrame resampledFrame = null;

    private int[] frameFinished = new int[1];

    private IntPointer out_linesize = new IntPointer();

    private SwrContext swr_ctx = null;

    private AtomicBoolean complete = new AtomicBoolean(false);

    /**
     * Reads the next packet from the stream containing 1:n frames. If queue is set to true,
     * the decoded frames are enqueued.
     *
     * @param queue If true, decoded frames are enqueued. Otherwise, they are discarded.
     * @return True if frame was read, false otherwise.
     */
    private boolean readFrame(boolean queue) {
        boolean readFrame = false;

        /* Outer loop: Read packet (frame) from stream. */
        while (!readFrame && av_read_frame(this.pFormatCtx, this.packet) >= 0) {
            if (this.packet.stream_index() == this.audioStream) {
                /* Counter: Size of packet (frame). */
                int remaining = this.packet.size();

                /* Because a packet can theoretically contain more than one frame; repeat decoding until no samples are
                 * remaining in the packet.
                 */
                while (remaining > 0) {
                    int result = avcodec_decode_audio4(this.pCodecCtx, this.decodedFrame, this.frameFinished, this.packet);
                    if (result < 0) {
                        LOGGER.error("Error occurred while decoding frames from packet. FFMPEG avcodec_decode_audio4() returned code {}.", result);
                        break;
                    }
                    remaining -= result;
                }

                if (this.frameFinished[0] > 0) {
                    /* If queue is true; enqueue frame. */
                    if (queue) {
                        if (this.swr_ctx != null) {
                            this.readResampled(this.decodedFrame.nb_samples());
                        } else {
                            this.readOriginal(this.decodedFrame.nb_samples());
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
     * @return Timestamp of the current frame.
     */
    private Long getFrameTimestamp() {
        AVRational timebase = this.pFormatCtx.streams(this.audioStream).time_base();
        return (long)Math.floor((this.decodedFrame.best_effort_timestamp() * (float)timebase.num() * 1000)/(float)timebase.den());
    }

    /**
     * Reads the decoded frame and copies it directly into a new AudioFrame data-structure.
     *
     * @param samples Number of samples returned by the decoder.
     */
    private void readOriginal(int samples) {
        /* Allocate output buffer... */
        int buffersize = samples * av_get_bytes_per_sample(this.decodedFrame.format()) * this.decodedFrame.channels();
        byte[] buffer = new byte[buffersize];
        this.decodedFrame.data(0).position(0).get(buffer);

        /* ... and add frame to queue. */
        this.frameQueue.add(new AudioFrame(this.getFrameNumber(), this.getFrameTimestamp(), this.decodedFrame.sample_rate(), this.decodedFrame.channels(), buffer));
    }


    /**
     * Reads the decoded frame and re.samples it using the SWR-CTX. The re-sampled frame is then
     * copied into a AudioFrame data-structure.
     *
     * @param samples Number of samples returned by the decoder.
     */
    private void readResampled(int samples) {
         /* Convert decoded frame. Break if resampling fails.*/
        if (swr_convert(this.swr_ctx, null, 0, this.decodedFrame.data(), samples) < 0) {
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
                LOGGER.error("Could not write resampled frame to ByteArrayOutputStream due to an exception ({}).", LogHelper.getStackTrace(e));
                break;
            }
        }

        /* ... and add frame to queue. */
        this.frameQueue.add(new AudioFrame(this.getFrameNumber(), this.getFrameTimestamp(), this.resampledFrame.sample_rate(), this.resampledFrame.channels(), stream.toByteArray()));
    }

    /**
     * Seeks to a specific frame, discarding all frames in between.
     *
     * @param frameNumber The frame number that is being sought.
     */
    @Override
    public void seekToFrame(int frameNumber) {
        while(this.pCodecCtx.frame_number() < frameNumber){
            if(!readFrame(false)){
                break;
            }
        }
    }

    /**
     * Returns the current frame number. This methods gets this information
     * directly from the decoder.
     *
     * @return Frame that is currently being processed.
     */
    @Override
    public int getFrameNumber() {
        return this.pCodecCtx.frame_number();
    }

    /**
     * Initializes the decoder with a file. This is a necessary step before content can be retrieved from
     * the decoder by means of the getNext() method.
     *
     * @param path   Path to the file that should be decoded.
     * @param config DecoderConfiguration used by the decoder.
     * @return Current instance of the decoder.
     */
    @Override
    public Decoder<AudioFrame> init(Path path, DecoderConfig config) {
        if(!Files.exists(path)){
            LOGGER.error("File does not exist {}", path.toString());
            return null;
        }

        /* Read decoder configuration. */
        int samplerate = config.namedAsInt(CONFIG_SAMPLERATE_PROPERTY, CONFIG_SAMPLERATE_DEFAULT);
        int channels = config.namedAsInt(CONFIG_CHANNELS_PROPERTY, CONFIG_CHANNELS_DEFAULT);
        long channellayout = av_get_default_channel_layout(channels);

        /* Initialize the AVFormatContext. */
        this.pFormatCtx = new AVFormatContext(null);

        // Register all formats and codecs
        av_register_all();

        /* Open file (pure frames or video + frames). */
        if (avformat_open_input(this.pFormatCtx, path.toString(), null, null) != 0) {
            LOGGER.error("Error while accessing file {}.", path.toString());
            return null;
        }

        // Retrieve stream information
        if (avformat_find_stream_info(this.pFormatCtx, (PointerPointer<?>)null) < 0) {
            LOGGER.error("Couldn't find stream information.");
            return null;
        }

        /* Find the best frames stream. */
        AVCodec codec = new AVCodec();
        this.audioStream = av_find_best_stream(this.pFormatCtx, AVMEDIA_TYPE_AUDIO,-1, -1, codec, 0);
        if (this.audioStream == -1) {
            LOGGER.error("Couldn't find a supported frames stream.");
            return null;
        }

        /* Get a pointer to the codec context for the frames stream and open it. */
        this.pCodecCtx = this.pFormatCtx.streams(this.audioStream).codec();
        if (avcodec_open2(this.pCodecCtx, codec, new AVDictionary()) < 0) {
            LOGGER.error("Error, Could not open frames codec");
            return null;
        }

        /* Allocate the re-sample context. */
        this.swr_ctx = swr_alloc_set_opts(null, channellayout, TARGET_FORMAT, samplerate, this.pCodecCtx.channel_layout(), this.pCodecCtx.sample_fmt(), this.pCodecCtx.sample_rate(), 0, null);
        if(swr_init(this.swr_ctx) < 0) {
            this.swr_ctx = null;
            LOGGER.warn("Warning! Could not open re-sample context - original format will be kept!");
        }

        /* Initialize decodedFrame. */
        this.decodedFrame = av_frame_alloc();

        /* Initialize out-frame. */
        this.resampledFrame = av_frame_alloc();
        this.resampledFrame.channel_layout(channellayout);
        this.resampledFrame.sample_rate(samplerate);
        this.resampledFrame.channels(channels);
        this.resampledFrame.format(TARGET_FORMAT);

        /* Completed initialization. */
        LOGGER.debug("{} was initialized successfully.", this.getClass().getName());
        return this;
    }

    /**
     * Fetches the next piece of content of type T and returns it. This method can be safely invoked until
     * complete() returns false. From which on this method will return null.
     *
     * @return Content of type T.
     */
    @Override
    public AudioFrame getNext() {
        if(this.frameQueue.isEmpty()){
            boolean frame = readFrame(true);
            if (!frame) this.complete.set(true);
        }
        return this.frameQueue.poll();
    }

    /**
     * Returns the total number of content pieces T this decoder can return for a given file. May be
     * zero if the decoder cannot determine that number.
     *
     * @return
     */
    @Override
    public int count() {
        return (int) this.pFormatCtx.streams(this.audioStream).nb_frames();
    }

    /**
     * Indicates whether or not the decoder has more content to return.
     *
     * @return True if more content can be retrieved, false otherwise.
     */
    @Override
    public boolean complete() {
        return this.complete.get();
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

    @Override
    public void close() {
        if (pFormatCtx == null) return;

        /* Free the YUV frame */
        av_free(this.decodedFrame);
        av_free(this.resampledFrame);

        /* Frees the SWR context. */
        if (this.swr_ctx != null) {
            swr_free(this.swr_ctx);
            this.swr_ctx = null;
        }

        /* Close the codec */
        avcodec_close(this.pCodecCtx);
        this.pCodecCtx = null;

        /* Close the video file */
        avformat_close_input(this.pFormatCtx);
        pFormatCtx = null;
    }

    /**
     * Closes the FFMpegAudioDecoder if this hasn't happened yet.
     *
     * @throws Throwable
     */
    @Override
    protected void finalize() throws Throwable {
        this.close();
        super.finalize();
    }
}
