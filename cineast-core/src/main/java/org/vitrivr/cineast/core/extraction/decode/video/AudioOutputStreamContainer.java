package org.vitrivr.cineast.core.extraction.decode.video;

import static org.bytedeco.ffmpeg.global.avcodec.AV_CODEC_CAP_VARIABLE_FRAME_SIZE;
import static org.bytedeco.ffmpeg.global.avcodec.avcodec_free_context;
import static org.bytedeco.ffmpeg.global.avcodec.avcodec_parameters_from_context;
import static org.bytedeco.ffmpeg.global.avutil.AV_ROUND_UP;
import static org.bytedeco.ffmpeg.global.avutil.AV_SAMPLE_FMT_FLTP;
import static org.bytedeco.ffmpeg.global.avutil.AV_SAMPLE_FMT_S16;
import static org.bytedeco.ffmpeg.global.avutil.av_frame_free;
import static org.bytedeco.ffmpeg.global.avutil.av_frame_make_writable;
import static org.bytedeco.ffmpeg.global.avutil.av_opt_set_int;
import static org.bytedeco.ffmpeg.global.avutil.av_opt_set_sample_fmt;
import static org.bytedeco.ffmpeg.global.avutil.av_rescale_q;
import static org.bytedeco.ffmpeg.global.avutil.av_rescale_rnd;
import static org.bytedeco.ffmpeg.global.swresample.swr_alloc;
import static org.bytedeco.ffmpeg.global.swresample.swr_convert;
import static org.bytedeco.ffmpeg.global.swresample.swr_free;
import static org.bytedeco.ffmpeg.global.swresample.swr_get_delay;
import static org.bytedeco.ffmpeg.global.swresample.swr_init;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bytedeco.ffmpeg.avformat.AVFormatContext;
import org.bytedeco.ffmpeg.avutil.AVDictionary;
import org.bytedeco.ffmpeg.avutil.AVFrame;
import org.bytedeco.ffmpeg.avutil.AVRational;
import org.bytedeco.ffmpeg.global.avcodec;
import org.bytedeco.ffmpeg.global.avformat;
import org.bytedeco.ffmpeg.global.avutil;
import org.bytedeco.ffmpeg.swresample.SwrContext;
import org.vitrivr.cineast.core.data.frames.AudioFrame;

class AudioOutputStreamContainer extends AbstractAVStreamContainer {

  private static final Logger LOGGER = LogManager.getLogger();
  private final int channels = 1;
  long next_pts;
  AVFrame tmp_frame;
  private int samples_count;
  private AVFrame frame;
  private SwrContext swr_ctx;
  private AVRational rat = new AVRational();

  AudioOutputStreamContainer(AVFormatContext oc, int codec_id, int sampleRate, int bitRate, AVDictionary opt) {
    super(oc, codec_id);

    long channellayout = avutil.av_get_default_channel_layout(channels);

    if (codec.type() != avutil.AVMEDIA_TYPE_AUDIO) {
      LOGGER.error("Not an audio codec");
      return;
    }

    c.sample_fmt(!codec.sample_fmts().isNull() ?
        codec.sample_fmts().get(0) : AV_SAMPLE_FMT_FLTP);
    c.bit_rate(bitRate);
    c.sample_rate(sampleRate);
    if (!codec.supported_samplerates().isNull()) {
      c.sample_rate(codec.supported_samplerates().get(0));
      int i = 0;
      int lastSampleRate;
      while ((lastSampleRate = codec.supported_samplerates().get(++i)) > 0) {
        if (lastSampleRate == sampleRate) {
          c.sample_rate(sampleRate);
          break;
        }
      }
    }

    c.channel_layout(channellayout);
    c.channels(avutil.av_get_channel_layout_nb_channels(c.channel_layout()));

    if (codec.channel_layouts() != null) {
      c.channel_layout(codec.channel_layouts().get(0));
      int i = 0;
      long lastChannelLayout;
      while ((lastChannelLayout = codec.channel_layouts().get(++i)) > 0) {
        if (lastChannelLayout == channellayout) {
          c.channel_layout(channellayout);
          break;
        }
      }
    }
    c.channels(avutil.av_get_channel_layout_nb_channels(c.channel_layout()));
    AVRational timeBase = new AVRational();
    timeBase.num(1);
    timeBase.den(c.sample_rate());
    st.time_base(timeBase);

    if ((oc.oformat().flags() & avformat.AVFMT_GLOBALHEADER) != 0) {
      oc.oformat().flags(oc.oformat().flags() | avformat.AVFMT_GLOBALHEADER);
    }

    int nb_samples;

    AVDictionary topt = new AVDictionary();

    avutil.av_dict_copy(topt, opt, 0);
    int ret = avcodec.avcodec_open2(c, codec, topt);
    avutil.av_dict_free(topt);

    if (ret < 0) {
      LOGGER.error("Could not open audio codec: {}", ret);
      return;
    }

    if ((c.codec().capabilities() & AV_CODEC_CAP_VARIABLE_FRAME_SIZE) != 0) {
      nb_samples = 10000;
    } else {
      nb_samples = c.frame_size();
    }

    frame = alloc_audio_frame(c.sample_fmt(), c.channel_layout(), c.sample_rate(), nb_samples);
    tmp_frame = alloc_audio_frame(AV_SAMPLE_FMT_S16, channellayout, sampleRate, nb_samples);

    /* copy the stream parameters to the muxer */
    ret = avcodec_parameters_from_context(st.codecpar(), c);
    if (ret < 0) {
      LOGGER.error("Could not copy the stream parameters");
      return;
    }

    /* create resampler context */
    swr_ctx = swr_alloc();
    if (swr_ctx == null) {
      LOGGER.error("Could not allocate resampler context");
      return;
    }

    /* set options */
    av_opt_set_int(swr_ctx, "in_channel_count", c.channels(), 0);
    av_opt_set_int(swr_ctx, "in_sample_rate", c.sample_rate(), 0); //this should use input rather than output sampling rate, but doing so crashes the application...
    av_opt_set_sample_fmt(swr_ctx, "in_sample_fmt", AV_SAMPLE_FMT_S16, 0);
    av_opt_set_int(swr_ctx, "out_channel_count", c.channels(), 0);
    av_opt_set_int(swr_ctx, "out_sample_rate", c.sample_rate(), 0);
    av_opt_set_sample_fmt(swr_ctx, "out_sample_fmt", c.sample_fmt(), 0);

    /* initialize the resampling context */
    if ((swr_init(swr_ctx)) < 0) {
      LOGGER.error("Failed to initialize the resampling context");
    }

  }

  private static AVFrame alloc_audio_frame(int sample_fmt, long channel_layout, int sample_rate, int nb_samples) {

    AVFrame frame = avutil.av_frame_alloc();

    if (frame == null) {
      LOGGER.error("Error allocating an audio frame");
      return null;
    }

    frame.format(sample_fmt);
    frame.channel_layout(channel_layout);
    frame.sample_rate(sample_rate);
    frame.nb_samples(nb_samples);

    if (nb_samples > 0) {
      int ret = avutil.av_frame_get_buffer(frame, 0);
      if (ret < 0) {
        LOGGER.error("Error allocating an audio buffer");
        return null;
      }
    }

    return frame;
  }

  void close() {
    if (c != null && !c.isNull()) {
      avcodec_free_context(c);
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


  void addFrame(AudioFrame av) {
    AVFrame inputFrame = tmp_frame;
    int ret;
    int dst_nb_samples;

    inputFrame.data().put(av.getData());

    inputFrame.pts(next_pts);
    next_pts += inputFrame.nb_samples();


    /* convert samples from native format to destination codec format, using the resampler */
    /* compute destination number of samples */
    dst_nb_samples = (int) av_rescale_rnd(swr_get_delay(swr_ctx, c.sample_rate()) + inputFrame.nb_samples(),
        c.sample_rate(), c.sample_rate(), AV_ROUND_UP);


    /* when we pass a frame to the encoder, it may keep a reference to it
     * internally;
     * make sure we do not overwrite it here
     */
    ret = av_frame_make_writable(frame);
    if (ret < 0) {
      return;
    }

    /* convert to destination format */
    ret = swr_convert(swr_ctx,
        frame.data(), dst_nb_samples,
        inputFrame.data(), inputFrame.nb_samples());
    if (ret < 0) {
      LOGGER.error("Error while converting");
      return;
    }

    rat.num(1);
    rat.den(c.sample_rate());

    frame.pts(av_rescale_q(samples_count, rat, c.time_base()));
    samples_count += dst_nb_samples;

    encode(c, frame, pkt);


  }


}
