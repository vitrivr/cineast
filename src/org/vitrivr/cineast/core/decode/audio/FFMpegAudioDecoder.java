package org.vitrivr.cineast.core.decode.audio;

import static org.bytedeco.javacpp.avcodec.*;

import static org.bytedeco.javacpp.avformat.av_read_frame;
import static org.bytedeco.javacpp.avformat.av_register_all;
import static org.bytedeco.javacpp.avformat.avformat_close_input;
import static org.bytedeco.javacpp.avformat.avformat_find_stream_info;
import static org.bytedeco.javacpp.avformat.avformat_open_input;

import static org.bytedeco.javacpp.avutil.*;

import static org.bytedeco.javacpp.swresample.*;
import static org.libav.avutil.bridge.AVMediaType.AVMEDIA_TYPE_AUDIO;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayDeque;
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
import org.vitrivr.cineast.core.data.audio.AudioFrame;
import org.vitrivr.cineast.core.decode.general.Decoder;


@SuppressWarnings("deprecation") //some of the new API replacing the deprecated one appears to be not yet available which is why the deprecated one has to be used.
public class FFMpegAudioDecoder implements AudioDecoder {

    private static final Logger LOGGER = LogManager.getLogger();

    private static final Set<String> supportedFiles = new HashSet<>();

    static {
        supportedFiles.add("audio/mp4");
        supportedFiles.add("audio/aac");
        supportedFiles.add("audio/mpeg");
        supportedFiles.add("audio/ogg");
        supportedFiles.add("audio/wav");
    }

    /** */
    private static final String CONFIG_CHANNELS_PROPERTY = "channels";
    private static final int CONFIG_CHANNELS_DEFAULT = 1;

    /** */
    private static final String CONFIG_SAMPLERATE_PROPERTY = "samplerate";
    private static final int CONFIG_SAMPLERATE_DEFAULT = 44100;

    /** */
    private static final int TARGET_FORMAT = AV_SAMPLE_FMT_S16;

    /** */
    private static final int BYTES_PER_SAMPLE = av_get_bytes_per_sample(TARGET_FORMAT);

    private long processedSamples = 0;
    private int currentFrameNumber = 0;
    private long framecount = 0;

    private ArrayDeque<AudioFrame> frameQueue = new ArrayDeque<>();

    private AVFormatContext pFormatCtx = null;
    private int             audioStream;
    private AVCodecContext  pCodecCtx = null;

    private AVPacket        packet = new AVPacket();

    private AVFrame decodedFrame = null;
    private AVFrame resampledFrame = null;

    private int[] frameFinished = new int[1];

    private IntPointer out_linesize = new IntPointer();

    private SwrContext swr_ctx = null;

    private AtomicBoolean complete = new AtomicBoolean(false);

    private int samplerate = CONFIG_SAMPLERATE_DEFAULT;
    private int channels = CONFIG_CHANNELS_DEFAULT;

    /**
     *
     * @param queue
     * @return
     */
    private boolean readFrame(boolean queue) {
        boolean readFrame = false;

        /* Outer loop: Read packet (frame) from stream. */
        while (!readFrame && av_read_frame(pFormatCtx, packet) >= 0) {
            if (packet.stream_index() == this.audioStream) {
                /* Counter: Size of packet (frame). */
                int remaining = packet.size();

                /* Because a packet can theoretically contain more than one frame; repeat decoding until no samples are
                 * remaining in the packet.
                 */
                while (remaining > 0)
                    remaining -= avcodec_decode_audio4(pCodecCtx, this.decodedFrame, frameFinished, packet);

                    if (frameFinished[0] > 0) {

                        /* Increment frame number. */
                        this.currentFrameNumber += 1;

                        /* If queue is true; enqueue frame. */
                        if (queue) {
                            /* Determine number of in / out samples based on codec settings. */
                            int in_samples = this.decodedFrame.nb_samples();

                            /* Convert decoded frame. Break if resampling fails.*/
                            if (swr_convert(this.swr_ctx, null, 0, this.decodedFrame.data(), in_samples) < 0) {
                                LOGGER.error("Could not convert sample (FFMPEG swr_convert() failed).", this.getClass().getName());
                                break;
                            }

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

                                /* ... and add frame to queue. */
                                this.frameQueue.add(new AudioFrame(this.processedSamples, this.samplerate, this.channels, buffer));

                                /* Increment the number of processed samples. */
                                this.processedSamples += out_samples;
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

    @Override
    public void seekToFrame(int frameNumber) {
        while(this.currentFrameNumber < frameNumber){
            if(!readFrame(false)){
                break;
            }
        }
    }

    @Override
    public int getFrameNumber() {
        return this.currentFrameNumber;
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

        /* Read decoder configuration. */
        this.samplerate = config.namedAsInt(CONFIG_SAMPLERATE_PROPERTY, CONFIG_SAMPLERATE_DEFAULT);
        this.channels = config.namedAsInt(CONFIG_CHANNELS_PROPERTY, CONFIG_CHANNELS_DEFAULT);
        long channellayout = av_get_default_channel_layout(this.channels);

        /* Initialize the AVFormatContext. */
        this.pFormatCtx = new AVFormatContext(null);

        if(!Files.exists(path)){
            LOGGER.error("File does not exist {}", path.toString());
            return null;
        }

        // Register all formats and codecs
        av_register_all();

        /* Open file (pure audio or video + audio). */
        if (avformat_open_input(this.pFormatCtx, path.toString(), null, null) != 0) {
            LOGGER.error("Error while accessing file {}.", path.toString());
            return null;
        }

        // Retrieve stream information
        if (avformat_find_stream_info(this.pFormatCtx, (PointerPointer<?>)null) < 0) {
            LOGGER.error("Couldn't find stream information.");
            return null;
        }

        // Find the first audio stream
        this.audioStream = -1;
        for (int i = 0; i < this.pFormatCtx.nb_streams(); i++) {
            if (this.pFormatCtx.streams(i).codec().codec_type() == AVMEDIA_TYPE_AUDIO) {
                this.audioStream = i;
                break;
            }
        }
        if (audioStream == -1) {
            LOGGER.error("Couldn't find a audio stream in the provided file {}.", path.toString());
            return null;
        }

        this.framecount = this.pFormatCtx.streams(audioStream).nb_frames();

        /* Get a pointer to the codec context for the audio stream */
        this.pCodecCtx = this.pFormatCtx.streams(this.audioStream).codec();


        /* Find the decoder for the video stream */
        AVCodec pCodec = avcodec_find_decoder(this.pCodecCtx.codec_id());
        if (pCodec == null) {
            LOGGER.error("Unsupported codec for audio in file {}", path.toString());
            return null;
        }

        /* Open codec. */
        AVDictionary optionsDict = null;
        if (avcodec_open2(pCodecCtx, pCodec, optionsDict) < 0) {
            LOGGER.error("Unable to open codec.");
            return null;
        }

        /* Allocate the re-sample context. */
        this.swr_ctx = swr_alloc_set_opts(null, channellayout, TARGET_FORMAT, this.samplerate, this.pCodecCtx.channel_layout(), this.pCodecCtx.sample_fmt(), this.pCodecCtx.sample_rate(), 0, null);
        if(swr_init(this.swr_ctx) < 0) {
            LOGGER.error("Error, Could not open re-sample context.");
            return null;
        }

        /* Initialize decodedFrame. */
        this.decodedFrame = av_frame_alloc();

        /* Initialize out-frame. */
        this.resampledFrame = av_frame_alloc();
        this.resampledFrame.channel_layout(channellayout);
        this.resampledFrame.sample_rate(this.samplerate);
        this.resampledFrame.channels(this.channels);
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
     * Returns the total number of content pieces T this decoder can return
     * for a given file.
     *
     * @return
     */
    @Override
    public int count() {
        return (int) this.framecount;
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
        swr_free(this.swr_ctx);

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
