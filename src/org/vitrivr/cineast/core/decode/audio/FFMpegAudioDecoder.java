package org.vitrivr.cineast.core.decode.audio;

import static org.bytedeco.javacpp.avcodec.*;

import static org.bytedeco.javacpp.avformat.av_read_frame;
import static org.bytedeco.javacpp.avformat.av_register_all;
import static org.bytedeco.javacpp.avformat.avformat_close_input;
import static org.bytedeco.javacpp.avformat.avformat_find_stream_info;
import static org.bytedeco.javacpp.avformat.avformat_open_input;

import static org.bytedeco.javacpp.avutil.*;
import static org.bytedeco.javacpp.avutil.av_get_channel_layout_nb_channels;
import static org.bytedeco.javacpp.avutil.av_samples_get_buffer_size;

import static org.bytedeco.javacpp.swresample.*;
import static org.libav.avutil.bridge.AVMediaType.AVMEDIA_TYPE_AUDIO;

import java.io.File;
import java.util.ArrayDeque;

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

import org.vitrivr.cineast.core.data.audio.AudioFrame;





@SuppressWarnings("deprecation") //some of the new API replacing the deprecated one appears to be not yet available which is why the deprecated one has to be used.
public class FFMpegAudioDecoder implements AudioDecoder {

    private static final Logger LOGGER = LogManager.getLogger();

    /**
     *
     */
    private static final int target_channel_layout = AV_CH_LAYOUT_MONO;

    /**
     *
     */
    private static final int target_sample_rate = 44100;

    /**
     *
     */
    private static final int target_format = AV_SAMPLE_FMT_S16;

    /**
     *
     */
    private static final int bytes_per_sample = av_get_bytes_per_sample(target_format);


    private int currentFrameNumber = 0;
    private long framecount;

    private ArrayDeque<AudioFrame> frameQueue = new ArrayDeque<>();

    private AVFormatContext pFormatCtx = new AVFormatContext(null);
    private int             audioStream;
    private AVCodecContext  pCodecCtx = null;

    private AVPacket        packet = new AVPacket();

    private AVFrame decodedFrame = null;
    private AVFrame resampledFrame = null;

    private int[] frameFinished = new int[1];

    IntPointer out_linesize = new IntPointer();

    private byte[] outBuff = new byte[1];


    private SwrContext swr_ctx = null;

    public FFMpegAudioDecoder(File file){

        if(!file.exists()){
            LOGGER.error("File does not exist {}", file.getAbsolutePath());
            return;
        }

        // Register all formats and codecs
        av_register_all();

        /* Open file (pure audio or video + audio). */
        if (avformat_open_input(this.pFormatCtx, file.getAbsolutePath(), null, null) != 0) {
            LOGGER.error("Error while accessing file {}.", file.getAbsolutePath());
            return;
        }

        // Retrieve stream information
        if (avformat_find_stream_info(this.pFormatCtx, (PointerPointer<?>)null) < 0) {
            LOGGER.error("Couldn't find stream information.");
            return;
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
            LOGGER.error("Couldn't find a audio stream in the provided file {}.", file.getAbsolutePath());
            return;
        }

        /* Get a pointer to the codec context for the audio stream */
        this.pCodecCtx = this.pFormatCtx.streams(this.audioStream).codec();


        /* Find the decoder for the video stream */
        AVCodec pCodec = avcodec_find_decoder(this.pCodecCtx.codec_id());
        if (pCodec == null) {
            LOGGER.error("Unsupported codec for audio in file {}", file.getAbsolutePath());
            return;
        }

        /* Open codec. */
        AVDictionary optionsDict = null;
        if (avcodec_open2(pCodecCtx, pCodec, optionsDict) < 0) {
            LOGGER.error("Unable to open codec.");
            return;
        }

        /* Allocate the re-sample context. */
         this.swr_ctx = swr_alloc_set_opts(null, target_channel_layout, target_format, target_sample_rate, this.pCodecCtx.channel_layout(), this.pCodecCtx.sample_fmt(), this.pCodecCtx.sample_rate(), 0, null);
         if(swr_init(this.swr_ctx) < 0) {
             LOGGER.error("Error, Could not open re-sample context.");
             return;
         }

        /* Initialize decodedFrame. */
        this.decodedFrame = av_frame_alloc();

        /* Initialize out-frame. */
        this.resampledFrame = av_frame_alloc();
        this.resampledFrame.channel_layout(target_channel_layout);
        this.resampledFrame.sample_rate(target_sample_rate);
        this.resampledFrame.channels(av_get_channel_layout_nb_channels(target_channel_layout));
        this.resampledFrame.format(target_format);

        /* Completed initialization. */
        LOGGER.debug("{} was initialized successfully.", this.getClass().getName());
    }

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
                                if (out_samples < bytes_per_sample * this.resampledFrame.channels()) break; // see comments, thanks to @dajuric for fixing this

                                /* Allocate output frame and read converted samples. If no sample was read -> break (draining completed). */
                                av_samples_alloc(this.resampledFrame.data(), out_linesize, this.resampledFrame.channels(), out_samples, target_format, 1);
                                out_samples = swr_convert(this.swr_ctx, this.resampledFrame.data(), out_samples, null, 0);
                                if (out_samples == 0) break;

                                /* Allocate output buffer... */
                                int buffersize = out_samples *  bytes_per_sample;
                                if (this.outBuff.length < buffersize) this.outBuff = new byte[buffersize];
                                this.resampledFrame.data(0).position(0).get(outBuff);

                                /* ... and add frame to queue. */
                                this.frameQueue.add(new AudioFrame(this.currentFrameNumber, outBuff));
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

    @Override
    public AudioFrame getFrame() {
        if(this.frameQueue.isEmpty()){
            readFrame(true);
        }
        return this.frameQueue.poll();
    }

    @Override
    public int getTotalFrameCount() {
        return (int) this.framecount;
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




    @Override
    protected void finalize() throws Throwable {
        this.clone();
        super.finalize();
    }
}
