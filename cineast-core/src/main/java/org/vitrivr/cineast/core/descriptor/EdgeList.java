package org.vitrivr.cineast.core.descriptor;

import boofcv.alg.feature.detect.edge.CannyEdge;
import boofcv.alg.feature.detect.edge.EdgeContour;
import boofcv.factory.feature.detect.edge.FactoryEdgeDetectors;
import boofcv.io.image.ConvertBufferedImage;
import boofcv.struct.image.GrayS16;
import boofcv.struct.image.GrayU8;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.vitrivr.cineast.core.data.MultiImage;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

public class EdgeList {

	private EdgeList() {
	}

	private static final int CACHE_SIZE = 8;

	private static final Logger LOGGER = LogManager.getLogger();
	private static final float THRESHOLD_LOW = 0.1f, THRESHOLD_HIGH = 0.3f;

	public static List<EdgeContour> getEdgeList(MultiImage img){
		LOGGER.traceEntry();
		BufferedImage withBackground = new BufferedImage(img.getWidth(), img.getHeight(), BufferedImage.TYPE_INT_RGB);
		Graphics g = withBackground.getGraphics();
		g.setColor(Color.white);
		g.fillRect(0, 0, img.getWidth(), img.getHeight());
		g.drawImage(img.getBufferedImage(), 0, 0, null);
		GrayU8 gray = ConvertBufferedImage.convertFrom(withBackground, (GrayU8) null);
		CannyEdge<GrayU8, GrayS16> canny = getCanny();
		canny.process(gray, THRESHOLD_LOW, THRESHOLD_HIGH, null);
		List<EdgeContour> _return = canny.getContours();
		LOGGER.traceExit();
		return _return;
	}
	
	private static LoadingCache<Thread, CannyEdge<GrayU8, GrayS16>> cannies = CacheBuilder
			.newBuilder()
			.maximumSize(CACHE_SIZE)
			.expireAfterAccess(10, TimeUnit.MINUTES)
			.build(new CacheLoader<Thread, CannyEdge<GrayU8, GrayS16>>() {

				@Override
				public CannyEdge<GrayU8, GrayS16> load(Thread arg0) {
					return FactoryEdgeDetectors.canny(2, true, true,
							GrayU8.class, GrayS16.class);
				}
			});

	private static synchronized CannyEdge<GrayU8, GrayS16> getCanny() {
		Thread current = Thread.currentThread();
		try {
			return cannies.get(current);
		} catch (ExecutionException e) {
			return null; // NEVER HAPPENS
		}
	}

}
