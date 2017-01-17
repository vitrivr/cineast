package org.vitrivr.cineast.core.decode.video;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bytedeco.javacpp.*;
import org.vitrivr.cineast.core.config.Config;
import org.vitrivr.cineast.core.data.Frame;
import org.vitrivr.cineast.core.data.MultiImageFactory;
import org.vitrivr.cineast.core.decode.general.Decoder;

import java.nio.file.Path;
import java.util.ArrayDeque;
import java.util.Set;

import static org.bytedeco.javacpp.avcodec.*;
import static org.bytedeco.javacpp.avcodec.av_packet_unref;
import static org.bytedeco.javacpp.avcodec.avcodec_close;
import static org.bytedeco.javacpp.avformat.*;
import static org.bytedeco.javacpp.avformat.avformat_close_input;
import static org.bytedeco.javacpp.avutil.*;
import static org.bytedeco.javacpp.avutil.av_free;
import static org.bytedeco.javacpp.swscale.SWS_BILINEAR;
import static org.bytedeco.javacpp.swscale.sws_getContext;
import static org.bytedeco.javacpp.swscale.sws_scale;

/**
 * @author rgasser
 * @version 1.0
 * @created 17.01.17
 */
public class NFFMpegVideoDecoder implements Decoder<Frame> {


    private static final Logger LOGGER = LogManager.getLogger();

    private int width, height, originalWidth, originalHeight, currentFrameNumber = 0;
    private double fps;
    private long framecount;
    private byte[] bytes;
    private int[] pixels;

    private ArrayDeque<Frame> frameQueue = new ArrayDeque<>();

    private avformat.AVFormatContext pFormatCtx = new avformat.AVFormatContext(null);
    private int             videoStream;
    private avcodec.AVCodecContext pCodecCtx = null;
    private avcodec.AVCodec pCodec = null;
    private avutil.AVFrame pFrame = null;
    private avutil.AVFrame pFrameRGB = null;
    private avcodec.AVPacket packet = new avcodec.AVPacket();
    private int[]           frameFinished = new int[1];
    private BytePointer buffer = null;

    private avutil.AVDictionary optionsDict = null;
    private swscale.SwsContext sws_ctx = null;

    private boolean complete = false;

    private boolean readFrame(boolean queue) {
        boolean readFrame = false;
        while (!readFrame && av_read_frame(pFormatCtx, packet) >= 0) {
            // Is this a packet from the video stream?
            if (packet.stream_index() == videoStream) {
                // Decode video frame
                avcodec_decode_video2(pCodecCtx, pFrame, frameFinished, packet);

                // Did we get a video frame?
                if (frameFinished[0] != 0) {
                    if (queue) {
                        queueFrame();
                    }
                    readFrame = true;
                }
            }

            // Free the packet that was allocated by av_read_frame
            av_packet_unref(packet);
        }
        return readFrame;
    }

    private void queueFrame() {
        // Convert the image from its native format to RGB
        sws_scale(sws_ctx, pFrame.data(), pFrame.linesize(), 0, pCodecCtx.height(), pFrameRGB.data(),
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

        this.frameQueue.add(new Frame(++this.currentFrameNumber, MultiImageFactory.newMultiImage(this.width, this.height, pixels)));
    }

    @Override
    public void close() {
        if(pFormatCtx == null){
            return;
        }

        // Free the RGB image
        av_free(buffer);
        av_free(pFrameRGB);

        // Free the YUV frame
        av_free(pFrame);

        // Close the codec
        avcodec_close(pCodecCtx);

        // Close the video file
        avformat_close_input(pFormatCtx);
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
     * @return Current instance of the decoder.
     */
    @Override
    public Decoder<Frame> init(Path path) {

        // Register all formats and codecs
        av_register_all();

        // Open video file
        if (avformat_open_input(pFormatCtx, path.toString(), null, null) != 0) {
            LOGGER.error("Error while accessing file {}", path.toString());
            return null;
        }

        // Retrieve stream information
        if (avformat_find_stream_info(pFormatCtx, (PointerPointer<?>)null) < 0) {
            LOGGER.error("Error, Couldn't find stream information");
            return null;
        }

        // Find the first video stream
        videoStream = -1;
        for (int i = 0; i < pFormatCtx.nb_streams(); i++) {
            if (pFormatCtx.streams(i).codec().codec_type() == AVMEDIA_TYPE_VIDEO) {
                videoStream = i;
                break;
            }
        }
        if (videoStream == -1) {
            LOGGER.error("Error, Couldn't find a video stream");
            return null;
        }

        // Get a pointer to the codec context for the video stream
        pCodecCtx = pFormatCtx.streams(videoStream).codec();

        avutil.AVRational framerate = pFormatCtx.streams(videoStream).avg_frame_rate();
        this.fps = ((double)framerate.num()) / ((double)framerate.den());
        this.framecount = pFormatCtx.streams(videoStream).nb_frames();


        // Find the decoder for the video stream
        pCodec = avcodec_find_decoder(pCodecCtx.codec_id());
        if (pCodec == null) {
            LOGGER.error("Error, Unsupported codec!");
            return null;
        }
        // Open codec
        if (avcodec_open2(pCodecCtx, pCodec, optionsDict) < 0) {
            LOGGER.error("Error, Could not open codec");
            return null;
        }

        // Allocate video frame
        pFrame = av_frame_alloc();

        // Allocate an AVFrame structure
        pFrameRGB = av_frame_alloc();
        if(pFrameRGB == null) {
            LOGGER.error("Error, Could not allocate frame");
            return null;
        }

        this.originalWidth = pCodecCtx.width();
        this.originalHeight = pCodecCtx.height();

        if(this.originalWidth > Config.getDecoderConfig().getMaxFrameWidth() || this.originalHeight > Config.getDecoderConfig().getMaxFrameHeight()){
            float scaleDown = Math.min((float)Config.getDecoderConfig().getMaxFrameWidth() / (float)this.originalWidth, (float)Config.getDecoderConfig().getMaxFrameHeight() / (float)this.originalHeight);
            this.width = Math.round(this.originalWidth * scaleDown);
            this.height = Math.round(this.originalHeight * scaleDown);
            LOGGER.debug("scaling input video down by a factor of {} from {}x{} to {}x{}", scaleDown, this.originalWidth, this.originalHeight, this.width, this.height);
        }else{
            this.width = this.originalWidth;
            this.height = this.originalHeight;
        }

        bytes = new byte[width * height * 3];
        pixels = new int[width * height];

        // Determine required buffer size and allocate buffer
        int numBytes = av_image_get_buffer_size(AV_PIX_FMT_RGB24,  pCodecCtx.width(), pCodecCtx.height(), 1);
        buffer = new BytePointer(av_malloc(numBytes));

        sws_ctx = sws_getContext(pCodecCtx.width(), pCodecCtx.height(),
                pCodecCtx.pix_fmt(), this.width, this.height,
                AV_PIX_FMT_RGB24, SWS_BILINEAR, null, null, (DoublePointer)null);

        // Assign appropriate parts of buffer to image planes in pFrameRGB
        // Note that pFrameRGB is an AVFrame, but AVFrame is a superset
        // of AVPicture
        avpicture_fill(new avcodec.AVPicture(pFrameRGB), buffer, AV_PIX_FMT_RGB24,
                this.width, this.height);

        LOGGER.debug("FFMpegVideoDecoder successfully initialized");
        this.complete = false;

        return this;
    }

    /**
     * Fetches the next piece of content of type T and returns it. This method can be safely invoked until
     * complete() returns false. From which on this method will return null.
     *
     * @return Content of type T.
     */
    @Override
    public Frame getNext() {
        if(this.frameQueue.isEmpty() && !this.complete){
            this.complete = !this.readFrame(true);
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
     * Indicates whether or not the current decoder has more content to return or not.
     *
     * @return True if more content can be fetched, false otherwise.
     */
    @Override
    public boolean complete() {
        return this.complete;
    }

    /**
     * Returns a set of the mime/types of supported files.
     *
     * @return Set of the mime-type of file formats that are supported by the current Decoder instance.
     */
    @Override
    public Set<String> supportedFiles() {
        return null;
    }
}
