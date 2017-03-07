package org.vitrivr.cineast.core.decode.video;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jcodec.api.JCodecException;
import org.jcodec.common.FileChannelWrapper;
import org.jcodec.common.NIOUtils;
import org.jcodec.common.model.Picture;
import org.vitrivr.cineast.core.data.frames.VideoFrame;
import org.vitrivr.cineast.core.data.MultiImageFactory;
import org.vitrivr.cineast.core.util.LogHelper;
import org.vitrivr.cineast.core.util.PictureUtil;

@Deprecated
public class JCodecVideoDecoder implements VideoDecoder {

	private static final Logger LOGGER = LogManager.getLogger();
	private FileChannelWrapper channel;
	private JcodecFrameGrab fg;
	
	public JCodecVideoDecoder(File file){
		String path = file.getAbsolutePath().toLowerCase();
		if(path.endsWith(".mp4") || path.endsWith(".mov")){
			try {
				this.channel = NIOUtils.readableFileChannel(file);
				this.fg = new JcodecFrameGrab(channel);
				LOGGER.info("created JCodecVideoDecoder for {}", file.getAbsolutePath());
			} catch (FileNotFoundException e) {
				LOGGER.fatal("error while loading video");
				LOGGER.fatal(LogHelper.getStackTrace(e));
			} catch (IOException e) {
				LOGGER.fatal("error while loading video");
				LOGGER.fatal(LogHelper.getStackTrace(e));
			} catch (JCodecException e) {
				LOGGER.fatal("error while loading video");
				LOGGER.fatal(LogHelper.getStackTrace(e));
			} catch (NullPointerException e) {
				LOGGER.fatal("error while loading video");
			}
		}else{
			LOGGER.fatal("Can only decode mp4 and mov containers");
		}
	}
	
	/* (non-Javadoc)
	 * @see cineast.core.decode.video.VideoDecoder#seekToFrame(int)
	 */
	@Override
	public void seekToFrame(int frameNumber){
		try {
			this.fg.seekToFramePrecise(frameNumber);
			LOGGER.debug("seeking to frame {}", frameNumber);
		} catch (IOException e) {
			LOGGER.warn("could not seek to frame {}", frameNumber);
			LOGGER.warn(LogHelper.getStackTrace(e));
		} catch (JCodecException e) {
			LOGGER.warn("could not seek to frame {}", frameNumber);
			LOGGER.warn(LogHelper.getStackTrace(e));
		}
	}
	
	/* (non-Javadoc)
	 * @see cineast.core.decode.video.VideoDecoder#getFrameNumber()
	 */
	@Override
	public int getFrameNumber(){
		return (int)this.fg.getCurrentFrameNum();
	}
	
	/* (non-Javadoc)
	 * @see cineast.core.decode.video.VideoDecoder#getFrame()
	 */
	@Override
	public VideoFrame getFrame(){
		Picture p = null;
		int width = 0, height = 0;
		int[] _return = null;
		try {
			p = this.fg.getNativeFrame();
			_return = PictureUtil.toColorArray(p);
			width = p.getWidth();
			height = p.getHeight();
		} catch (IOException e) {
			LOGGER.warn("error while reading frame");
			LOGGER.warn(LogHelper.getStackTrace(e));
		} catch (NullPointerException e) {
			LOGGER.info("end of video reached");
		}
		if(_return == null){
			return null;
		}
		return new VideoFrame((int)this.fg.getCurrentFrameNum(), MultiImageFactory.newMultiImage(width, height, _return));
	}
	
	/* (non-Javadoc)
	 * @see cineast.core.decode.video.VideoDecoder#close()
	 */
	@Override
	public void close(){
		this.fg = null;
		NIOUtils.closeQuietly(this.channel);
		this.channel = null;
		LOGGER.info("closed JCodecVideoDecoder");
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#finalize()
	 */
	@Override
	protected void finalize() throws Throwable {
		this.close();
		super.finalize();
	}

	@Override
	public int getTotalFrameCount() {
		return this.fg.getTotalFrames();
	}

	@Override
	public double getFPS() {
		return this.fg.getFPS();
	}

	@Override
	public int getOriginalWidth() {
		return fg.getMediaInfo().getDim().getWidth();
	}

	@Override
	public int getOriginalHeight() {
		return fg.getMediaInfo().getDim().getHeight();
	}

	@Override
	public int getWidth() {
		return this.getOriginalWidth();
	}

	@Override
	public int getHeight() {
		return this.getOriginalHeight();
	}
	
	
}