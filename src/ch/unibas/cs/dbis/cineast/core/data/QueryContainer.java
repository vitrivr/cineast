package ch.unibas.cs.dbis.cineast.core.data;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import ch.unibas.cs.dbis.cineast.core.decode.subtitle.SubtitleItem;
import georegression.struct.point.Point2D_F32;

public class QueryContainer implements FrameContainer {

	private MultiImage img;
	private Frame frame;
	private ArrayList<SubtitleItem> subitem = new ArrayList<SubtitleItem>(1);
	private List<LinkedList<Point2D_F32>> paths = new LinkedList<LinkedList<Point2D_F32>>();
	private float relativeStart = 0, relativeEnd = 0;
	
	public QueryContainer(MultiImage img){
		this.img = img;
	}
	
	@Override
	public MultiImage getAvgImg() {
		return this.img;
	}

	@Override
	public MultiImage getMedianImg() {
		return this.img;
	}

	@Override
	public Frame getMostRepresentativeFrame() {
		if(this.frame == null){
			int id = (getStart() + getEnd()) /2; 
			this.frame = new Frame(id, this.img);
		}
		return this.frame;
	}

	@Override
	public int getStart() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public int getEnd() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public List<SubtitleItem> getSubtitleItems() {
		return this.subitem;
	}

	@Override
	public float getRelativeStart() {
		return relativeStart;
	}

	@Override
	public float getRelativeEnd() {
		return relativeEnd;
	}
	
	public void setRelativeStart(float relativeStart){
		this.relativeStart = relativeStart;
	}
	
	public void setRelativeEnd(float relativeEnd){
		this.relativeEnd = relativeEnd;
	}

	@Override
	public List<LinkedList<Point2D_F32>> getPaths() {
		return this.paths;
	}

	@Override
	public List<Frame> getFrames() {
		ArrayList<Frame> _return = new ArrayList<Frame>(1);
		_return.add(this.frame);
		return _return;
	}

	@Override
	public long getId() {
		return 0;
	}

	@Override
	public long getSuperId() {
		return 0;
	}
	
	public void addPath(LinkedList<Point2D_F32> path){
		this.paths.add(path);
	}

}
