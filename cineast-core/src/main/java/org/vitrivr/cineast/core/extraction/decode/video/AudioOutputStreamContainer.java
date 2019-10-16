package org.vitrivr.cineast.core.extraction.decode.video;

import org.bytedeco.javacpp.avcodec;
import org.bytedeco.javacpp.avformat;
import org.bytedeco.javacpp.avutil;
import org.bytedeco.javacpp.swresample;
import org.vitrivr.cineast.core.data.frames.AudioFrame;

import static org.bytedeco.javacpp.avcodec.*;
import static org.bytedeco.javacpp.avutil.*;
import static org.bytedeco.javacpp.swresample.*;

class AudioOutputStreamContainer {

    private avformat.AVStream st;
    private AVCodec codec;
    AVCodecContext enc;
    long next_pts;
    private int samples_count;
    private AVFrame frame;
    AVFrame tmp_frame;
    private swresample.SwrContext swr_ctx;
    private avformat.AVFormatContext oc;
    private AVRational rat = new AVRational();

    AudioOutputStreamContainer(avformat.AVFormatContext oc, int codec_id, AVDictionary opt) {
        initStream(oc, codec_id);
        open_audio(opt);
        this.oc = oc;

        rat.num(1);
        rat.den(enc.sample_rate());

    }

    private static AVFrame alloc_audio_frame(int sample_fmt, long channel_layout, int sample_rate, int nb_samples) {

        AVFrame frame = avutil.av_frame_alloc();

        if (frame == null) {
            System.err.println("Error allocating an audio frame");
            return null;
        }

        frame.format(sample_fmt);
        frame.channel_layout(channel_layout);
        frame.sample_rate(sample_rate);
        frame.nb_samples(nb_samples);

        if (nb_samples > 0) {
            int ret = avutil.av_frame_get_buffer(frame, 0);
            if (ret < 0) {
                System.err.println("Error allocating an audio buffer");
                return null;
            }
        }

        return frame;
    }

    void close() {
        if (enc != null && !enc.isNull()) {
            avcodec_free_context(enc);
        }
        if (frame != null && !frame.isNull()) {
            av_frame_free(frame);
        }
        if (tmp_frame != null && !tmp_frame.isNull()) {
            av_frame_free(tmp_frame);
        }
        if (swr_ctx != null && !swr_ctx.isNull()) {
            swr_free(swr_ctx);
        }
    }

    private void initStream(avformat.AVFormatContext oc, int codec_id) {

        codec = avcodec.avcodec_find_encoder(codec_id);
        if (codec == null) {
            System.err.println("Could not find codec for id " + codec_id);
            return;
        }

        st = avformat.avformat_new_stream(oc, null);
        if (st == null) {
            System.err.println("Could not allocate stream");
            return;
        }

        st.id(oc.nb_streams() - 1);
        enc = avcodec.avcodec_alloc_context3(codec);
        if (enc == null) {
            System.err.println("Could not alloc an encoding context");
            return;
        }

        if (codec.type() != avutil.AVMEDIA_TYPE_AUDIO) {
            System.err.println("Not an audio codec");
            return;
        }

        enc.sample_fmt(!codec.sample_fmts().isNull() ?
                codec.sample_fmts().get(0) : AV_SAMPLE_FMT_FLTP);
        enc.bit_rate(64000);
        enc.sample_rate(44100);
        if (!codec.supported_samplerates().isNull()) {
            enc.sample_rate(codec.supported_samplerates().get(0));
            for (int i = 0; i < codec.supported_samplerates().limit(); i++) {
                if (codec.supported_samplerates().get(i) == 44100) {
                    enc.sample_rate(44100);
                }
            }
        }
        enc.channels(avutil.av_get_channel_layout_nb_channels(enc.channel_layout()));
        enc.channel_layout(AV_CH_LAYOUT_STEREO);
        if (codec.channel_layouts() != null) {
            enc.channel_layout(codec.channel_layouts().get(0));
            for (int i = 0; i < codec.channel_layouts().limit(); i++) {
                if (codec.channel_layouts().get(i) == AV_CH_LAYOUT_STEREO) {
                    enc.channel_layout(AV_CH_LAYOUT_STEREO);
                }
            }
        }
        enc.channels(avutil.av_get_channel_layout_nb_channels(enc.channel_layout()));
        AVRational timeBase = new AVRational();
        timeBase.num(1);
        timeBase.den(enc.sample_rate());
        st.time_base(timeBase);


        if ((oc.oformat().flags() & avformat.AVFMT_GLOBALHEADER) != 0) {
            oc.oformat().flags(oc.oformat().flags() | avformat.AVFMT_GLOBALHEADER);
        }


    }

    private void open_audio(AVDictionary opt_arg) {
        avcodec.AVCodecContext c = enc;
        int nb_samples;

        AVDictionary opt = new AVDictionary();

        avutil.av_dict_copy(opt, opt_arg, 0);
        int ret = avcodec.avcodec_open2(c, codec, opt);
        avutil.av_dict_free(opt);

        if (ret < 0) {
            System.err.println("Could not open audio codec: " + ret);
            return;
        }


        if ((c.codec().capabilities() & AV_CODEC_CAP_VARIABLE_FRAME_SIZE) != 0)
            nb_samples = 10000;
        else
            nb_samples = c.frame_size();

        frame = alloc_audio_frame(c.sample_fmt(), c.channel_layout(), c.sample_rate(), nb_samples);
        tmp_frame = alloc_audio_frame(AV_SAMPLE_FMT_S16, c.channel_layout(), c.sample_rate(), nb_samples);

        /* copy the stream parameters to the muxer */
        ret = avcodec_parameters_from_context(st.codecpar(), c);
        if (ret < 0) {
            System.err.println("Could not copy the stream parameters");
            return;
        }

        /* create resampler context */
        swr_ctx = swr_alloc();
        if (swr_ctx == null) {
            System.err.println("Could not allocate resampler context");
            return;
        }

        /* set options */
        av_opt_set_int(swr_ctx, "in_channel_count", c.channels(), 0);
        av_opt_set_int(swr_ctx, "in_sample_rate", c.sample_rate(), 0);
        av_opt_set_sample_fmt(swr_ctx, "in_sample_fmt", AV_SAMPLE_FMT_S16, 0);
        av_opt_set_int(swr_ctx, "out_channel_count", c.channels(), 0);
        av_opt_set_int(swr_ctx, "out_sample_rate", c.sample_rate(), 0);
        av_opt_set_sample_fmt(swr_ctx, "out_sample_fmt", c.sample_fmt(), 0);

        /* initialize the resampling context */
        if ((ret = swr_init(swr_ctx)) < 0) {
            System.err.println("Failed to initialize the resampling context");
        }
    }




    void write_audio_frame(AudioFrame av)
    {
        AVPacket pkt = new avcodec.AVPacket();
        AVFrame inputFrame = tmp_frame;
        int ret;
        int[] got_packet = new int[1];
        int dst_nb_samples;

        av_init_packet(pkt);

        inputFrame.data().put(av.getData());

        inputFrame.pts(next_pts);
        next_pts  += inputFrame.nb_samples();


        /* convert samples from native format to destination codec format, using the resampler */
        /* compute destination number of samples */
        dst_nb_samples = (int) av_rescale_rnd(swr_get_delay(swr_ctx, enc.sample_rate()) + inputFrame.nb_samples(),
                enc.sample_rate(), enc.sample_rate(), AV_ROUND_UP);


        /* when we pass a frame to the encoder, it may keep a reference to it
         * internally;
         * make sure we do not overwrite it here
         */
        ret = av_frame_make_writable(frame);
        if (ret < 0)
            return;

        /* convert to destination format */
        ret = swr_convert(swr_ctx,
                frame.data(), dst_nb_samples,
                inputFrame.data(), inputFrame.nb_samples());
        if (ret < 0) {
            System.err.println("Error while converting\n");
            return;
        }


        frame.pts(av_rescale_q(samples_count, rat, enc.time_base()));
        samples_count += dst_nb_samples;

        ret = avcodec_encode_audio2(enc, pkt, frame, got_packet);
        if (ret < 0) {
            System.err.println("Error encoding audio frame: ");
            return;
        }

        if (got_packet[0] != 0) {

            avcodec.av_packet_rescale_ts(pkt, enc.time_base(), st.time_base());
            pkt.stream_index(st.index());

            /* Write the compressed frame to the media file. */
            ret = avformat.av_interleaved_write_frame(oc, pkt);

            if (ret < 0) {
                System.err.println("Error while writing audio frame: ");
            }
        }

    }


}
