package org.vitrivr.cineast.core.extraction.decode.video;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bytedeco.javacpp.*;
import org.vitrivr.cineast.core.data.raw.CachedDataFactory;
import org.vitrivr.cineast.core.data.raw.images.MultiImage;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.*;

public class FFMpegVideoEncoder {

    private static final byte[] endcode = { 0, 0, 1, (byte) 0xb7};

    private static final Logger LOGGER = LogManager.getLogger();

    private final OutputStream out;

    private avcodec.AVCodec codec;
    private avcodec.AVCodecContext c;
    private avutil.AVFrame outFrame, rgbFrame;
    private avcodec.AVPacket pkt;
    private swscale.SwsContext sws_ctx;
    private int frameCounter = 0, packetCounter = 0;

    public FFMpegVideoEncoder(int width, int height, OutputStream out){
        this.out = out;

        int fps = 25;

        String codecName = "libx264";

        codec = avcodec.avcodec_find_encoder_by_name(codecName);

        if (codec == null){
            LOGGER.error("Codec not found");
            return;
        }

        c = avcodec.avcodec_alloc_context3(codec);

        if (c == null){
            LOGGER.error("Could not allocate video codec context");
            return;
        }

        pkt = avcodec.av_packet_alloc();

        if (pkt == null){
            LOGGER.error("Could not allocate packet");
            return;
        }

        c.bit_rate(1_000_000); //TODO
        c.width(width);
        c.height(height);

        avutil.AVRational timeBase = new avutil.AVRational();
        timeBase.num(1);
        timeBase.den(fps);
        c.time_base(timeBase);

        avutil.AVRational frameRate = new avutil.AVRational();
        frameRate.num(fps);
        frameRate.den(1);
        c.framerate(frameRate);

        c.gop_size(10);
        c.max_b_frames(1);
        c.pix_fmt(avutil.AV_PIX_FMT_YUV420P);

        if (codec.id() == avcodec.AV_CODEC_ID_H264) {
            avutil.av_opt_set(c.priv_data(), "preset", "slow", 0);
        }

        int ret = avcodec.avcodec_open2(c, codec, (PointerPointer) null);
        if (ret < 0) {
            LOGGER.error("Could not open codec: " + ret);
            return;
        }

        outFrame = avutil.av_frame_alloc();
        if (outFrame == null) {
            LOGGER.error("Could not allocate frame");
            return;
        }

        outFrame.format(c.pix_fmt());
        outFrame.width(c.width());
        outFrame.height(c.height());

        ret = avutil.av_frame_get_buffer(outFrame, 32);
        if (ret < 0) {
            LOGGER.error("Could not allocate video frame data");
            return;
        }

        rgbFrame = avutil.av_frame_alloc();
        if (rgbFrame == null) {
            LOGGER.error("Could not allocate frame");
            return;
        }

        rgbFrame.format(avutil.AV_PIX_FMT_RGB24);
        rgbFrame.width(c.width());
        rgbFrame.height(c.height());

        ret = avutil.av_frame_get_buffer(rgbFrame, 32);
        if (ret < 0) {
            LOGGER.error("Could not allocate video frame data");
            return;
        }

        sws_ctx = swscale.sws_getContext(c.width(), c.height(), avutil.AV_PIX_FMT_RGB24, c.width(), c.height(), c.pix_fmt(), swscale.SWS_BILINEAR, null, null, (DoublePointer)null);

    }

    public void addFrame(MultiImage img){

        int ret = avutil.av_frame_make_writable(outFrame);
        if (ret < 0) {
            return;
        }

        ret = avutil.av_frame_make_writable(rgbFrame);
        if (ret < 0) {
            return;
        }

        int[] pixels = img.getColors();
        for(int i = 0; i < pixels.length; ++i) {
            rgbFrame.data(0).put(3 * i, (byte) (((pixels[i]) >> 16) & 0xff));
            rgbFrame.data(0).put(3 * i + 1, (byte) (((pixels[i]) >> 8) & 0xff));
            rgbFrame.data(0).put(3 * i + 2, (byte) ((pixels[i]) & 0xff));
        }

        swscale.sws_scale(sws_ctx, rgbFrame.data(), rgbFrame.linesize(), 0, outFrame.height(), outFrame.data(), outFrame.linesize());

        outFrame.pts(this.frameCounter++);
        encode(c, outFrame, pkt, out);
    }

    public void close() {
        encode(c, null, pkt, out);

        if (codec.id() == avcodec.AV_CODEC_ID_MPEG1VIDEO || codec.id() == avcodec.AV_CODEC_ID_MPEG2VIDEO) {
            try {
                out.write(endcode);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        avcodec.avcodec_free_context(c);
        avutil.av_frame_free(outFrame);
        avutil.av_frame_free(rgbFrame);
        avcodec.av_packet_free(pkt);

        try {
            out.flush();
            out.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public static void main(String[] args) throws IOException {

        OutputStream out = new FileOutputStream(new File("video.m4v"));

        BufferedImage testImg = ImageIO.read(new File("img.jpg"));

        MultiImage img = CachedDataFactory.DEFAULT_INSTANCE.newInMemoryMultiImage(testImg);

        FFMpegVideoEncoder encoder = new FFMpegVideoEncoder(img.getWidth(), img.getHeight(), out);

        for (int i = 0; i < 250; ++i){

            encoder.addFrame(img);

        }

        encoder.close();

    }

    private void encode(avcodec.AVCodecContext enc_ctx, avutil.AVFrame frame, avcodec.AVPacket pkt, OutputStream out) {

//        if (frame != null) {
//            System.out.println("Send frame " + frame.pts());
//        }

        int ret = avcodec.avcodec_send_frame(enc_ctx, frame);
        if (ret < 0) {
            LOGGER.error("Error sending a frame for encoding");
            return;
        }

        while (ret >= 0) {
            ret = avcodec.avcodec_receive_packet(enc_ctx, pkt);
            if (ret == avutil.AVERROR_EAGAIN()
                    ||
            ret == avutil.AVERROR_EOF()) {
                return;
            } else if (ret < 0) {
                LOGGER.error("Error during encoding");
            }
            //System.out.println("Write packet " + pkt.pts() + " size: " + pkt.size());

            pkt.pts(packetCounter++);
            byte[] arr = new byte[pkt.size()];
            pkt.data().position(0).get(arr);

            try {
                out.write(arr);
            } catch (IOException e) {
                e.printStackTrace();
            }
            avcodec.av_packet_unref(pkt);
        }
    }
}
