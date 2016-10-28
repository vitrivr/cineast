package org.vitrivr.cineast.core.decode.video;

import static org.bytedeco.javacpp.avcodec.av_packet_unref;
import static org.bytedeco.javacpp.avcodec.avcodec_close;
import static org.bytedeco.javacpp.avcodec.avcodec_decode_video2;
import static org.bytedeco.javacpp.avcodec.avcodec_find_decoder;
import static org.bytedeco.javacpp.avcodec.avcodec_open2;
import static org.bytedeco.javacpp.avcodec.avpicture_fill;
import static org.bytedeco.javacpp.avformat.av_read_frame;
import static org.bytedeco.javacpp.avformat.av_register_all;
import static org.bytedeco.javacpp.avformat.avformat_close_input;
import static org.bytedeco.javacpp.avformat.avformat_find_stream_info;
import static org.bytedeco.javacpp.avformat.avformat_open_input;
import static org.bytedeco.javacpp.avutil.AVMEDIA_TYPE_VIDEO;
import static org.bytedeco.javacpp.avutil.AV_PIX_FMT_RGB24;
import static org.bytedeco.javacpp.avutil.av_frame_alloc;
import static org.bytedeco.javacpp.avutil.av_free;
import static org.bytedeco.javacpp.avutil.av_image_get_buffer_size;
import static org.bytedeco.javacpp.avutil.av_malloc;
import static org.bytedeco.javacpp.swscale.SWS_BILINEAR;
import static org.bytedeco.javacpp.swscale.sws_getContext;
import static org.bytedeco.javacpp.swscale.sws_scale;

import java.io.File;
import java.util.ArrayDeque;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bytedeco.javacpp.BytePointer;
import org.bytedeco.javacpp.DoublePointer;
import org.bytedeco.javacpp.PointerPointer;
import org.bytedeco.javacpp.avcodec.AVCodec;
import org.bytedeco.javacpp.avcodec.AVCodecContext;
import org.bytedeco.javacpp.avcodec.AVPacket;
import org.bytedeco.javacpp.avcodec.AVPicture;
import org.bytedeco.javacpp.avformat.AVFormatContext;
import org.bytedeco.javacpp.avutil.AVDictionary;
import org.bytedeco.javacpp.avutil.AVFrame;
import org.bytedeco.javacpp.avutil.AVRational;
import org.bytedeco.javacpp.swscale.SwsContext;
import org.vitrivr.cineast.core.config.Config;
import org.vitrivr.cineast.core.data.Frame;
import org.vitrivr.cineast.core.data.MultiImageFactory;

public class FFMpegVideoDecoder implements VideoDecoder {

  private static final Logger LOGGER = LogManager.getLogger();
  
  private int width, height, originalWidth, originalHeight, currentFrameNumber = 0;
  private double fps;
  private long framecount;
  private byte[] bytes;
  private int[] pixels;
  
  private ArrayDeque<Frame> frameQueue = new ArrayDeque<>();
  
  private AVFormatContext pFormatCtx = new AVFormatContext(null);
  private int             videoStream;
  private AVCodecContext  pCodecCtx = null;
  private AVCodec         pCodec = null;
  private AVFrame         pFrame = null;
  private AVFrame         pFrameRGB = null;
  private AVPacket        packet = new AVPacket();
  private int[]           frameFinished = new int[1];
  private BytePointer     buffer = null;

  private  AVDictionary    optionsDict = null;
  private SwsContext sws_ctx = null;
  
  public FFMpegVideoDecoder(File file){
    
    if(!file.exists()){
      LOGGER.error("File does not exist {}", file.getAbsolutePath());
      return;
    }
    
    // Register all formats and codecs
    av_register_all();

    // Open video file
    if (avformat_open_input(pFormatCtx, file.getAbsolutePath(), null, null) != 0) {
      LOGGER.error("Error while accessing file {}", file.getAbsolutePath());
      return;
    }

    // Retrieve stream information
    if (avformat_find_stream_info(pFormatCtx, (PointerPointer<?>)null) < 0) {
      LOGGER.error("Error, Couldn't find stream information");
      return;
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
      return;
    }
    
    // Get a pointer to the codec context for the video stream
    pCodecCtx = pFormatCtx.streams(videoStream).codec();

    AVRational framerate = pFormatCtx.streams(videoStream).avg_frame_rate();
    this.fps = ((double)framerate.num()) / ((double)framerate.den());
    this.framecount = pFormatCtx.streams(videoStream).nb_frames();
    
    
    // Find the decoder for the video stream
    pCodec = avcodec_find_decoder(pCodecCtx.codec_id());
    if (pCodec == null) {
      LOGGER.error("Error, Unsupported codec!");
      return;
    }
    // Open codec
    if (avcodec_open2(pCodecCtx, pCodec, optionsDict) < 0) {
      LOGGER.error("Error, Could not open codec");
      return;
    }

    // Allocate video frame
    pFrame = av_frame_alloc();

    // Allocate an AVFrame structure
    pFrameRGB = av_frame_alloc();
    if(pFrameRGB == null) {
      LOGGER.error("Error, Could not allocate frame");
      return;
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
    avpicture_fill(new AVPicture(pFrameRGB), buffer, AV_PIX_FMT_RGB24,
        this.width, this.height);
    
  }
  
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

    this.frameQueue.add(new Frame(++this.currentFrameNumber,
        MultiImageFactory.newMultiImage(this.width, this.height, pixels)));
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
  public Frame getFrame() {
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
  public double getFPS() {
    return this.fps;
  }

  @Override
  public void close() {
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

  @Override
  public int getOriginalWidth() {
    return this.originalWidth;
  }

  @Override
  public int getOriginalHeight() {
    return this.originalHeight;
  }

  @Override
  public int getWidth() {
    return this.width;
  }

  @Override
  public int getHeight() {
    return this.height;
  }

}
