package org.vitrivr.cineast.core.extraction.decode.video;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bytedeco.javacpp.*;
import org.vitrivr.cineast.core.data.raw.images.MultiImage;

import static org.bytedeco.javacpp.avcodec.*;
import static org.bytedeco.javacpp.avutil.*;
import static org.bytedeco.javacpp.swscale.sws_freeContext;

class VideoOutputStreamContainer extends AbstractAVStreamContainer {

    private static final Logger LOGGER = LogManager.getLogger();
    int frameCounter = 0;
    private AVFrame rgbFrame, outFrame;
    private swscale.SwsContext sws_ctx;

    VideoOutputStreamContainer(int width, int height, int bitRate, int frameRate, avformat.AVFormatContext oc, int codec_id, AVDictionary opt) {
        super(oc, codec_id);

        if (codec.type() != avutil.AVMEDIA_TYPE_VIDEO) {
            LOGGER.error("Not a video codec");
            return;
        }


        c.bit_rate(bitRate);
        c.width(width);
        c.height(height);

        c.gop_size(10);
        c.max_b_frames(1);
        c.pix_fmt(avutil.AV_PIX_FMT_YUV420P);

        AVRational timeBase = new AVRational();
        timeBase.num(1);
        timeBase.den(frameRate);
        st.time_base(timeBase);
        c.time_base(st.time_base());

        if (c.codec_id() == avcodec.AV_CODEC_ID_MPEG2VIDEO) {
            c.max_b_frames(2);
        }
        if (c.codec_id() == avcodec.AV_CODEC_ID_MPEG1VIDEO) {
            c.mb_decision(2);
        }
        if (codec.id() == avcodec.AV_CODEC_ID_H264) {
            avutil.av_opt_set(c.priv_data(), "preset", "slow", 0);
        }


        if ((oc.oformat().flags() & avformat.AVFMT_GLOBALHEADER) != 0) {
            oc.oformat().flags(oc.oformat().flags() | avformat.AVFMT_GLOBALHEADER);
        }

        AVDictionary topt = new AVDictionary();

        av_dict_copy(topt, opt, 0);

        /* open the codec */
        int ret = avcodec_open2(c, codec, topt);
        av_dict_free(topt);
        if (ret < 0) {
            LOGGER.error("Could not open video codec: {}", ret);
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

        /* copy the stream parameters to the muxer */
        ret = avcodec_parameters_from_context(st.codecpar(), c);
        if (ret < 0) {
            LOGGER.error("Could not copy the stream parameters");
            return;
        }

        sws_ctx = swscale.sws_getContext(c.width(), c.height(), avutil.AV_PIX_FMT_RGB24, c.width(), c.height(), c.pix_fmt(), swscale.SWS_BILINEAR, null, null, (DoublePointer) null);

    }


    void addFrame(MultiImage img) {

        int ret = avutil.av_frame_make_writable(outFrame);
        if (ret < 0) {
            return;
        }

        ret = avutil.av_frame_make_writable(rgbFrame);
        if (ret < 0) {
            return;
        }

        int[] pixels = img.getColors();
        for (int i = 0; i < pixels.length; ++i) {
            rgbFrame.data(0).put(3 * i, (byte) (((pixels[i]) >> 16) & 0xff));
            rgbFrame.data(0).put(3 * i + 1, (byte) (((pixels[i]) >> 8) & 0xff));
            rgbFrame.data(0).put(3 * i + 2, (byte) ((pixels[i]) & 0xff));
        }

        swscale.sws_scale(sws_ctx, rgbFrame.data(), rgbFrame.linesize(), 0, outFrame.height(), outFrame.data(), outFrame.linesize());

        outFrame.pts(this.frameCounter++);
        encode(c, outFrame, pkt);
    }

    void close() {
        if (c != null && !c.isNull()) {
            avcodec_free_context(c);
        }
        if (rgbFrame != null && !rgbFrame.isNull()) {
            av_frame_free(rgbFrame);
        }
        if (outFrame != null && !outFrame.isNull()) {
            av_frame_free(outFrame);
        }
        if (sws_ctx != null && !sws_ctx.isNull()) {
            sws_freeContext(sws_ctx);
        }

    }

}
