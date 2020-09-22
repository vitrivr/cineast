package org.vitrivr.cineast.core.extraction.decode.video;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bytedeco.javacpp.avcodec;
import org.bytedeco.javacpp.avformat;
import org.bytedeco.javacpp.avutil;

public abstract class AbstractAVStreamContainer {

    private static final Logger LOGGER = LogManager.getLogger();
    avcodec.AVPacket pkt;
    protected avformat.AVStream st;
    protected avcodec.AVCodec codec;
    avcodec.AVCodecContext c;
    private avformat.AVFormatContext oc;

    AbstractAVStreamContainer(avformat.AVFormatContext oc, int codec_id) {
        this.oc = oc;
        st = avformat.avformat_new_stream(oc, null);
        if (st == null) {
            LOGGER.error("Could not allocate stream");
            return;
        }
        st.id(oc.nb_streams() - 1);

        codec = avcodec.avcodec_find_encoder(codec_id);
        if (codec == null) {
            LOGGER.error("Could not find codec for id " + codec_id);
            return;
        }

        c = avcodec.avcodec_alloc_context3(codec);
        if (c == null) {
            LOGGER.error("Could not alloc an encoding context");
            return;
        }

        c.codec_id(codec_id);

        pkt = avcodec.av_packet_alloc();
    }


    void encode(avcodec.AVCodecContext enc_ctx, avutil.AVFrame frame, avcodec.AVPacket pkt) {

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

            avcodec.av_packet_rescale_ts(pkt, c.time_base(), st.time_base());
            pkt.stream_index(st.index());

            /* Write the compressed frame to the media file. */
            avformat.av_interleaved_write_frame(oc, pkt);

            avcodec.av_packet_unref(pkt);
        }
    }

}
