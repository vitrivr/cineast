package org.vitrivr.cineast.core.decode.video;

import java.awt.Point;
import java.awt.image.BufferedImage;
import java.awt.image.DataBuffer;
import java.awt.image.DataBufferInt;
import java.awt.image.DirectColorModel;
import java.awt.image.Raster;
import java.awt.image.SampleModel;
import java.awt.image.SinglePixelPackedSampleModel;
import java.awt.image.WritableRaster;
import java.io.File;
import java.nio.ByteOrder;
import java.util.ArrayDeque;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bridj.Pointer;
import org.libav.DefaultMediaReader;
import org.libav.LibavException;
import org.libav.avcodec.FrameWrapperFactory;
import org.libav.avcodec.ICodecContextWrapper;
import org.libav.avcodec.IFrameWrapper;
import org.libav.avformat.IStreamWrapper;
import org.libav.avutil.bridge.PixelFormat;
import org.libav.data.IFrameConsumer;
import org.libav.swscale.ScaleContextWrapper;
import org.libav.swscale.bridge.SWScaleLibrary;
import org.libav.video.VideoFrameDecoder;
import org.vitrivr.cineast.core.config.Config;
import org.vitrivr.cineast.core.data.Frame;
import org.vitrivr.cineast.core.data.MultiImageFactory;
import org.vitrivr.cineast.core.util.LogHelper;

@Deprecated
public class JLibAVVideoDecoder implements VideoDecoder {

	private static final Logger LOGGER = LogManager.getLogger();
	
	private int videoStreamIndex = 0;
	private DefaultMediaReader mediaReader;
	private VideoFrameDecoder decoder;
	private JLibAVFrameConsumer frameConsumer;
	
	private int originalWidth, originalHeight, width, height, framecount;
	
	private float fps;
	
	private boolean hasMorePackets = true;
	
	public JLibAVVideoDecoder(File file){
		if(!file.exists()){
			LOGGER.error("File does not exist {}", file.getAbsoluteFile());
			return;
		}
		
		try {
			this.mediaReader = new DefaultMediaReader(file.getAbsolutePath());
			this.decoder = new VideoFrameDecoder(this.mediaReader.getVideoStream(videoStreamIndex));
		} catch (LibavException e) {
			LOGGER.error("Error while initialising JLibAVVideoDecoder: {}", LogHelper.getStackTrace(e));
		}
		
		this.mediaReader.addVideoPacketConsumer(videoStreamIndex, this.decoder);
		this.mediaReader.setVideoStreamBufferingEnabled(videoStreamIndex, true);
		
		 ICodecContextWrapper codecContext = decoder.getCodecContext();
		 this.originalWidth = codecContext.getWidth();
	     this.originalHeight = codecContext.getHeight();

	     if(this.originalWidth > 640 || this.originalHeight > 480){
	    	 float scaleDown = Math.min((float)640 / (float)this.originalWidth, (float)480 / (float)this.originalHeight);
	    	 this.width = Math.round(this.originalWidth * scaleDown);
	    	 this.height = Math.round(this.originalHeight * scaleDown);
	    	 LOGGER.debug("scaling input video down by a factor of {} from {}x{} to {}x{}", scaleDown, this.originalWidth, this.originalHeight, this.width, this.height);
	     }else{
	    	 this.width = this.originalWidth;
	    	 this.height = this.originalHeight;
	     }
	     
	     this.frameConsumer = new JLibAVFrameConsumer(this.originalWidth, this.originalHeight, this.width, this.height, codecContext.getPixelFormat());
	     this.decoder.addFrameConsumer(this.frameConsumer);
	     
	     IStreamWrapper stream = decoder.getStream();
	     this.fps = getFPS(stream);
	     this.framecount = (int) stream.getFrameCount();
	     
	    // MultiImageFactory.announceImageDimensions(this.width, this.height);
	     
	}
	
	@Override
	public void seekToFrame(int frameNumber) {
		this.frameConsumer.setSeek(true);
		while(frameNumber > frameConsumer.getFrameNumber()){
			if(getFrame() == null){
				break;
			}
		}
		this.frameConsumer.setSeek(false);
	}
	
	@Override
	public int getFrameNumber() {
		return this.frameConsumer.getFrameNumber();
	}

	@Override
	public Frame getFrame() {
		Frame _return = this.frameConsumer.getNextFrame();
		if(_return != null){
			return _return;
		}
		while(this.hasMorePackets && (_return = this.frameConsumer.getNextFrame()) == null){
			try {
				this.hasMorePackets = this.mediaReader.readNextPacket(this.videoStreamIndex);
			} catch (LibavException e) {
				this.hasMorePackets = false;
				LOGGER.error("Error while decoding video: {}", LogHelper.getStackTrace(e));
			}
		}
		return _return;
	}

	@Override
	public int getTotalFrameCount() {
		return this.framecount;
	}

	@Override
	public double getFPS() {
		return this.fps;
	}

	@Override
	public void close() {
		this.decoder.close();
		try {
			this.mediaReader.close();
		} catch (LibavException e) {
			LOGGER.warn("Error while closing mediaReader: {}", LogHelper.getStackTrace(e));
		}
		this.frameConsumer.close();
	}

	@Override
	public int getOriginalWidth() {
		return this.originalWidth;
	}

	@Override
	public int getOriginalHeight() {
		return this.originalHeight;
	}

	
	 /**
     * http://libav-users.943685.n4.nabble.com/Retrieving-Frames-Per-Second-FPS-td946533.html
     * @param stream
     * @return
     */
    private static float getFPS(IStreamWrapper stream){
    	if(
    			(stream.getTimeBase().getDenominator() != stream.getRFrameRate().getNumerator())
    			||
    			(stream.getTimeBase().getNumerator() != stream.getRFrameRate().getDenominator())
    		){
    		
    		return (float) stream.getRFrameRate().getNumerator() / (float) stream.getRFrameRate().getDenominator();
    		
    	}else{
    		return (float) stream.getTimeBase().getNumerator() / (float) stream.getTimeBase().getDenominator();
    	}
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

@Deprecated
class JLibAVFrameConsumer implements IFrameConsumer{

	private static final int MAX_THUMB_SIZE = 200;
	private boolean seek = false;
	private int frameNumber = 0;
	//private int width, height, pixelFormat;
	private ArrayDeque<Frame> frameQueue;
	
	private ScaleContextWrapper scaleContext, thumbScaleContext;
    private IFrameWrapper rgbFrame, rgbThumb;
    private Pointer<Byte> rgbFrameData, rgbThumbData;
    private BufferedImage img, thumb;
    private int[] imageData, thumbData;
    
    private int inHeight;
	
	JLibAVFrameConsumer(int inwidth, int inheight, int width, int height, int pixelFormat) {
		this.inHeight = inheight;
		
		this.frameQueue = new ArrayDeque<>();
		
		int dstPixelFormat = PixelFormat.PIX_FMT_BGRA;
        if (ByteOrder.BIG_ENDIAN.equals(ByteOrder.nativeOrder())){
        	dstPixelFormat = PixelFormat.PIX_FMT_ARGB;
        }
        float thumbScale = (float)MAX_THUMB_SIZE / (float)Math.max(inwidth, inheight);
        int thumbWith = inwidth, thumbHeight = inheight;
        if(thumbScale < 1){
        	thumbWith = Math.round(inwidth * thumbScale);
        	thumbHeight = Math.round(inheight * thumbScale);
        }
        try{
        	this.scaleContext = ScaleContextWrapper.createContext(inwidth, inheight, PixelFormat.PIX_FMT_YUV420P, width, height, dstPixelFormat, SWScaleLibrary.SWS_BICUBIC);
        	this.rgbFrame = FrameWrapperFactory.getInstance().allocPicture(dstPixelFormat, width, height);
        	this.rgbFrameData = this.rgbFrame.getData().get();
        	
        	this.thumbScaleContext = ScaleContextWrapper.createContext(inwidth, inheight, PixelFormat.PIX_FMT_YUV420P, thumbWith, thumbHeight, dstPixelFormat, SWScaleLibrary.SWS_BICUBIC);
        	this.rgbThumb = FrameWrapperFactory.getInstance().allocPicture(dstPixelFormat, thumbWith, thumbHeight);
        	this.rgbThumbData = this.rgbThumb.getData().get();
        } catch (LibavException ex) {
	    	  
        }
		 
		 this.imageData = new int[width * height];
		 DataBuffer db = new DataBufferInt(this.imageData, this.imageData.length);
		 int[] masks = new int[] { 0x00ff0000, 0x0000ff00, 0x000000ff };
		 SampleModel sm = new SinglePixelPackedSampleModel(DataBuffer.TYPE_INT, width, height, masks);
		 WritableRaster wr = Raster.createWritableRaster(sm, db, new Point());
		 this.img = new BufferedImage(new DirectColorModel(24, 0x00ff0000, 0x0000ff00, 0x000000ff), wr, false, null);
		 
		 this.thumbData = new int[thumbWith * thumbHeight];
		 db = new DataBufferInt(this.thumbData, this.thumbData.length);
		 sm = new SinglePixelPackedSampleModel(DataBuffer.TYPE_INT, thumbWith, thumbHeight, masks);
		 wr = Raster.createWritableRaster(sm, db, new Point());
		 this.thumb = new BufferedImage(new DirectColorModel(24, 0x00ff0000, 0x0000ff00, 0x000000ff), wr, false, null);
		 
	}
	
	int getFrameNumber(){
		return this.frameNumber;
	}
	
	void setSeek(boolean seek){
		this.seek = seek;
	}
	
	@Override
	public void processFrame(Object producer, IFrameWrapper frame) throws LibavException {
		if(seek){
			++this.frameNumber;
			return;
		}
		
		this.scaleContext.scale(frame, this.rgbFrame, 0, this.inHeight);
		this.rgbFrameData.getIntsAtOffset(0, this.imageData, 0, this.imageData.length);
		
		this.thumbScaleContext.scale(frame, rgbThumb, 0, this.thumbData.length);
		this.rgbThumbData.getIntsAtOffset(0, this.thumbData, 0, this.thumbData.length);
        
        this.frameQueue.add(new Frame(++this.frameNumber, MultiImageFactory.newMultiImage(MultiImageFactory.copyBufferedImg(this.img), MultiImageFactory.copyBufferedImg(this.thumb))));
        //this.frameQueue.add(new Frame(++this.frameNumber, MultiImageFactory.copyIntoMultiImage(img)));
		
	}
	
	
	Frame getNextFrame(){
		if(this.frameQueue.isEmpty()){
			return null;
		}
		return this.frameQueue.pop();
	}
	
	void close(){
		if (scaleContext != null)
            scaleContext.free();
        if (rgbFrame != null)
            rgbFrame.free();
        
        scaleContext = null;
	}
	
}