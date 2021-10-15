package org.vitrivr.cineast.core.extraction.decode.audio;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bytedeco.javacpp.*;
import org.bytedeco.javacpp.avcodec.AVCodec;
import org.bytedeco.javacpp.avcodec.AVCodecContext;
import org.bytedeco.javacpp.avcodec.AVPacket;
import org.bytedeco.javacpp.avformat.AVFormatContext;
import org.bytedeco.javacpp.avutil.AVDictionary;
import org.bytedeco.javacpp.avutil.AVFrame;
import org.bytedeco.javacpp.avutil.AVRational;
import org.bytedeco.javacpp.swresample.SwrContext;
import org.vitrivr.cineast.core.config.DecoderConfig;
import org.vitrivr.cineast.core.config.CacheConfig;
import org.vitrivr.cineast.core.data.frames.AudioDescriptor;
import org.vitrivr.cineast.core.data.frames.AudioFrame;
import org.vitrivr.cineast.core.extraction.decode.general.Decoder;
import org.vitrivr.cineast.core.util.LogHelper;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayDeque;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * A {@link Decoder} implementation that decodes audio using the ffmpeg library + the corresponding Java bindings.
 *
 */
public class FFMpegAudioDecoder implements AudioDecoder {

    private static final Logger LOGGER = LogManager.getLogger();

    /** Lists the mime-types supported by the FFMpegAudioDecoder.
     *
     * TODO: List may not be complete yet. */
    private static final Set<String> supportedFiles;
    static {
        HashSet<String> tmp = new HashSet<>();
        tmp.add("multimedia/mp4"); /* They share the same suffix with audio (.mp4). */
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
    private static final int TARGET_FORMAT = avutil.AV_SAMPLE_FMT_S16;
    private static final int BYTES_PER_SAMPLE = avutil.av_get_bytes_per_sample(TARGET_FORMAT);

    /** Internal data structure used to hold decoded AudioFrames. */
    private ArrayDeque<AudioFrame> frameQueue = new ArrayDeque<>();

    private AVFormatContext pFormatCtx = null;
    private AVCodecContext  pCodecCtx = null;
    private int             audioStream = -1;

    private AVPacket        packet = null;

    private AVFrame decodedFrame = null;
    private AVFrame resampledFrame = null;

    private IntPointer out_linesize = new IntPointer();

    private SwrContext swr_ctx = null;

    private AtomicBoolean complete = new AtomicBoolean(false);

    private AudioDescriptor descriptor = null;

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
        do {
            int read_results = avformat.av_read_frame(this.pFormatCtx, this.packet);
            if (read_results < 0 && !(read_results == avutil.AVERROR_EOF)) {
                LOGGER.error("Error occurred while reading packet. FFMPEG av_read_frame() returned code {}.", read_results);
                avcodec.av_packet_unref(this.packet);
                break;
            }

            if (this.packet.stream_index() == this.audioStream) {
                /* Send packet to decoder. If no packet was read, send null to flush the buffer. */
                int decode_results = avcodec.avcodec_send_packet(this.pCodecCtx, read_results == avutil.AVERROR_EOF ? null : this.packet);
                if (decode_results < 0) {
                    LOGGER.error("Error occurred while decoding frames from packet. FFMPEG avcodec_send_packet() returned code {}.", decode_results);
                    avcodec.av_packet_unref(this.packet);
                    break;
                }

                /* Because a packet can theoretically contain more than one frame; repeat decoding until no samples are
                 * remaining in the packet.
                 */
                while (avcodec.avcodec_receive_frame(this.pCodecCtx, this.decodedFrame) == 0) {
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
            avcodec.av_packet_unref(this.packet);
        } while(!readFrame);

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
        return Math.floorDiv((this.decodedFrame.best_effort_timestamp() * timebase.num() * 1000), timebase.den());
    }

    /**
     * Reads the decoded frame and copies it directly into a new AudioFrame data-structure.
     *
     * @param samples Number of samples returned by the decoder.
     */
    private void readOriginal(int samples) {
        /* Allocate output buffer... */
        int buffersize = samples * avutil.av_get_bytes_per_sample(this.decodedFrame.format()) * this.decodedFrame.channels();
        byte[] buffer = new byte[buffersize];
        this.decodedFrame.data(0).position(0).get(buffer);

        /* ... and add frame to queue. */
        this.frameQueue.add(new AudioFrame(this.getFrameNumber(), this.getFrameTimestamp(), buffer, this.descriptor));
    }


    /**
     * Reads the decoded frame and re.samples it using the SWR-CTX. The re-sampled frame is then
     * copied into a AudioFrame data-structure.
     *
     * @param samples Number of samples returned by the decoder.
     */
    private void readResampled(int samples) {
         /* Convert decoded frame. Break if resampling fails.*/
        if (swresample.swr_convert(this.swr_ctx, null, 0, this.decodedFrame.data(), samples) < 0) {
            LOGGER.error("Could not convert sample (FFMPEG swr_convert() failed).", this.getClass().getName());
            return;
        }

        /* Prepare ByteOutputStream to write resampled data to. */
        ByteArrayOutputStream stream = new ByteArrayOutputStream();

        /* Now drain buffer and write samples to queue. */
        while(true) {
            /* Estimate number of samples that were converted. */
            int out_samples = swresample.swr_get_out_samples(this.swr_ctx, 0);
            if (out_samples < BYTES_PER_SAMPLE * this.resampledFrame.channels()) {
              break;
            }

            /* Allocate output frame and read converted samples. If no sample was read -> break (draining completed). */
            avutil.av_samples_alloc(this.resampledFrame.data(), out_linesize, this.resampledFrame.channels(), out_samples, TARGET_FORMAT, 0);
            out_samples = swresample.swr_convert(this.swr_ctx, this.resampledFrame.data(), out_samples, null, 0);
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

            avutil.av_freep(this.resampledFrame.data());
        }

        /* ... and add frame to queue. */
        this.frameQueue.add(new AudioFrame(this.getFrameNumber(), this.getFrameTimestamp(), stream.toByteArray(), this.descriptor));
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
     * @param path Path to the file that should be decoded.
     * @param decoderConfig {@link DecoderConfig} used by this {@link Decoder}.
     * @param cacheConfig The {@link CacheConfig} used by this {@link Decoder}
     * @return True if initialization was successful, false otherwise.
     */
    @Override
    public boolean init(Path path, DecoderConfig decoderConfig, CacheConfig cacheConfig) {
        if(!Files.exists(path)){
            LOGGER.error("File does not exist {}", path.toString());
            return false;
        }

        /* Read decoder configuration. */
        int samplerate = decoderConfig.namedAsInt(CONFIG_SAMPLERATE_PROPERTY, CONFIG_SAMPLERATE_DEFAULT);
        int channels = decoderConfig.namedAsInt(CONFIG_CHANNELS_PROPERTY, CONFIG_CHANNELS_DEFAULT);
        long channellayout = avutil.av_get_default_channel_layout(channels);

        /* Initialize the AVFormatContext. */
        this.pFormatCtx = avformat.avformat_alloc_context();

        /* Open file (pure frames or video + frames). */
        if (avformat.avformat_open_input(this.pFormatCtx, path.toString(), null, null) != 0) {
            LOGGER.error("Error while accessing file {}.", path.toString());
            return false;
        }

        /* Retrieve stream information. */
        if (avformat.avformat_find_stream_info(this.pFormatCtx, (PointerPointer<?>)null) < 0) {
            LOGGER.error("Couldn't find stream information.");
            return false;
        }

        /* Find the best stream. */
        final AVCodec codec = avcodec.av_codec_iterate(new Pointer());
        this.audioStream = avformat.av_find_best_stream(this.pFormatCtx, avutil.AVMEDIA_TYPE_AUDIO,-1, -1, codec, 0);
        if (this.audioStream == -1) {
            LOGGER.error("Couldn't find a supported audio stream.");
            return false;
        }

        /* Allocate new codec-context. */
        this.pCodecCtx = avcodec.avcodec_alloc_context3(codec);
        avcodec.avcodec_parameters_to_context(this.pCodecCtx, this.pFormatCtx.streams(this.audioStream).codecpar());

        /* Initialize context with stream's codec settings. */
        this.pCodecCtx.sample_rate(this.pFormatCtx.streams(this.audioStream).codecpar().sample_rate());
        this.pCodecCtx.channels(this.pFormatCtx.streams(this.audioStream).codecpar().channels());
        this.pCodecCtx.channel_layout(this.pFormatCtx.streams(this.audioStream).codecpar().channel_layout());
        this.pCodecCtx.sample_fmt(this.pFormatCtx.streams(this.audioStream).codecpar().format());

        /* Open the code context. */
        if (avcodec.avcodec_open2(this.pCodecCtx, codec, (AVDictionary) null) < 0) {
            LOGGER.error("Could not open audio codec.");
            return false;
        }
        
        /* Allocate the re-sample context. */
        this.swr_ctx = swresample.swr_alloc_set_opts(null, channellayout, TARGET_FORMAT, samplerate, this.pCodecCtx.channel_layout(), this.pCodecCtx.sample_fmt(), this.pCodecCtx.sample_rate(), 0, null);
        if(swresample.swr_init(this.swr_ctx) < 0) {
            this.swr_ctx = null;
            LOGGER.warn("Could not open re-sample context - original format will be kept!");
        }

       /* Initialize the packet. */
        this.packet = avcodec.av_packet_alloc();
        if (this.packet == null) {
            LOGGER.error("Could not allocate packet data structure for decoded data.");
            return false;
        }

        /* Allocate frame that holds decoded frame information. */
        this.decodedFrame = avutil.av_frame_alloc();
        if (this.decodedFrame == null) {
            LOGGER.error("Could not allocate frame data structure for decoded data.");
            return false;
        }

        /* Initialize out-frame. */
        this.resampledFrame = avutil.av_frame_alloc();
        if (this.resampledFrame == null) {
            LOGGER.error("Could not allocate frame data structure for re-sampled data.");
            return false;
        }
        this.resampledFrame.channel_layout(channellayout);
        this.resampledFrame.sample_rate(samplerate);
        this.resampledFrame.channels(channels);
        this.resampledFrame.format(TARGET_FORMAT);


        /* Initialize the AudioDescriptor. */
        AVRational timebase = this.pFormatCtx.streams(this.audioStream).time_base();
        long duration = Math.floorDiv(1000L * timebase.num() * this.pFormatCtx.streams(this.audioStream).duration(), timebase.den());

        if (this.swr_ctx == null) {
            this.descriptor = new AudioDescriptor(this.pCodecCtx.sample_rate(), this.pCodecCtx.channels(), duration);
        } else {
            this.descriptor = new AudioDescriptor(this.resampledFrame.sample_rate(), this.resampledFrame.channels(), duration);
        }

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
    public AudioFrame getNext() {
        if(this.frameQueue.isEmpty()){
            boolean frame = readFrame(true);
            if (!frame) {
              this.complete.set(true);
            }
        }
        return this.frameQueue.poll();
    }

    /**
     * Returns the total number of content pieces T this decoder can return for a given file. May be
     * zero if the decoder cannot determine that number.
     *
     * @return Number of frames in the audio-stream (if known).
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

    /**
     * Closes the FFMpegAudioDecoder and frees all (non-native) resources associated with it.
     */
    @Override
    public void close() {
        if (this.pFormatCtx == null) {
          return;
        }

        /* Free the audio frames */
        if (this.decodedFrame != null) {
            avutil.av_frame_free(this.decodedFrame);
            this.decodedFrame = null;
        }

        if (this.resampledFrame != null) {
            avutil.av_frame_free(this.resampledFrame);
            this.resampledFrame = null;
        }

        /* Free the packet. */
        if (this.packet != null) {
            avcodec.av_packet_free(this.packet);
            this.packet = null;
        }

        /* Frees the SWR context. */
        if (this.swr_ctx != null) {
            swresample.swr_free(this.swr_ctx);
            this.swr_ctx = null;
        }

        /* Close the codec context. */
        if (this.pCodecCtx != null) {
            avcodec.avcodec_free_context(this.pCodecCtx);
            this.pCodecCtx = null;
        }

        /* Close the audio file context. */
        avformat.avformat_close_input(this.pFormatCtx);
        this.pFormatCtx = null;
    }

}
