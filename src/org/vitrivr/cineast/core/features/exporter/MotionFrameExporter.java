package org.vitrivr.cineast.core.features.exporter;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import javax.imageio.ImageIO;

import org.vitrivr.cineast.core.config.Config;
import org.vitrivr.cineast.core.data.Frame;
import org.vitrivr.cineast.core.data.Pair;
import org.vitrivr.cineast.core.data.SegmentContainer;
import org.vitrivr.cineast.core.db.PersistencyWriter;
import org.vitrivr.cineast.core.features.extractor.Extractor;

import georegression.struct.point.Point2D_F32;

public class MotionFrameExporter implements Extractor {

private static File folder = new File(Config.getExtractorConfig().getOutputLocation(), "motionframes");
	
	@Override
	public void init(PersistencyWriter<?> phandler) {
		phandler.close();
		if(!folder.exists()){
			folder.mkdirs();
		}
	}

	@Override
	public void processShot(SegmentContainer shot) {
		List<Pair<Integer,LinkedList<Point2D_F32>>> paths = shot.getPaths();
		for(Frame f : shot.getFrames()){
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

}
