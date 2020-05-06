package org.vitrivr.cineast.core.features.exporter;

import georegression.struct.point.Point2D_F32;
import org.vitrivr.cineast.core.data.Pair;
import org.vitrivr.cineast.core.data.frames.VideoFrame;
import org.vitrivr.cineast.core.data.segments.SegmentContainer;
import org.vitrivr.cineast.core.db.PersistencyWriterSupplier;
import org.vitrivr.cineast.core.db.setup.EntityCreator;
import org.vitrivr.cineast.core.features.extractor.Extractor;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.function.Supplier;

public class MotionFrameExporter implements Extractor {
	private static final String PROPERTY_NAME_DESTINATION = "destination";

	private final File folder;

	/**
	 * Default constructor - no parameters.
	 */
	public MotionFrameExporter() {
		this(new HashMap<>());
	}

	/**
	 * Constructor with property HashMap that allows for passing of parameters.
	 *
	 * Supported parameters:
	 *
	 * <ol>
	 *      <li>destination: Path where motion frameimages should be stored.</li>
	 * </ol>
	 *
	 * @param properties HashMap containing named properties
	 */
	public MotionFrameExporter(HashMap<String, String> properties) {
		this.folder =  new File(properties.getOrDefault(PROPERTY_NAME_DESTINATION, "./motion_frames"));
	}


	@Override
	public void init(PersistencyWriterSupplier phandlerSupply, int batchSize) {
		if(!folder.exists()){
			folder.mkdirs();
		}
	}

	@Override
	public void processSegment(SegmentContainer shot) {
		List<Pair<Integer,LinkedList<Point2D_F32>>> paths = shot.getPaths();
		for(VideoFrame f : shot.getVideoFrames()){
			File file = new File(folder, String.format("%06d",f.getId()) + ".jpg");
			BufferedImage bimg = f.getImage().getBufferedImage();
			for(Pair<Integer, LinkedList<Point2D_F32>> pair : paths){
				draw(bimg, pair.second);
			}
			try {
				ImageIO.write(bimg, "jpg", file);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
	}

	private static Color[] colors = new Color[]{
		new Color(255, 0, 0),
		new Color(230, 30, 0),
		new Color(205, 55, 0),
		new Color(180, 80, 0),
		new Color(155, 105, 0),
		new Color(130, 130, 0),
		new Color(105, 155, 0),
		new Color(80, 180, 0),
		new Color(55, 205, 0),
		new Color(30, 230, 0),
		new Color(0, 255, 0),
		
	};

	//private static BasicStroke fgStroke = new BasicStroke(1f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND);
	
	public static void draw(BufferedImage img, LinkedList<Point2D_F32> path){
		int col = 0, width = img.getWidth(), height = img.getHeight();
		if(path.size() > 1){
			Graphics2D g = (Graphics2D) img.getGraphics();
			Iterator<Point2D_F32> iter = path.iterator();
			Point2D_F32 last = iter.next();
			//g.setStroke(fgStroke);
			while(iter.hasNext()){
				g.setColor(colors[col]);
				col = (col + 1) % colors.length;
				Point2D_F32 current = iter.next();
				g.drawLine((int)(last.x * width), (int)(last.y * height),(int)(current.x * width), (int)(current.y * height));
				last = current;
			}
		}
	}
	
	@Override
	public void finish() {}
	
	@Override
	public void initalizePersistentLayer(Supplier<EntityCreator> supply) {}

	@Override
	public void dropPersistentLayer(Supplier<EntityCreator> supply) {}
}
